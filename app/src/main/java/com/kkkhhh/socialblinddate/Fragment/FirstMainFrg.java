package com.kkkhhh.socialblinddate.Fragment;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andexert.library.RippleView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kkkhhh.socialblinddate.Activity.FilterLocalActivity;
import com.kkkhhh.socialblinddate.Adapter.PostAdapter;
import com.kkkhhh.socialblinddate.Etc.EndlessRecyclerOnScrollListener;
import com.kkkhhh.socialblinddate.Etc.UserValue;
import com.kkkhhh.socialblinddate.Model.Post;
import com.kkkhhh.socialblinddate.R;
import com.melnykov.fab.FloatingActionButton;
import com.rey.material.app.BottomSheetDialog;
import com.rey.material.widget.ProgressView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class FirstMainFrg extends Fragment {

    private FloatingActionButton fab;
    private RecyclerView recyclerView;
    private LinearLayoutManager mManager;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private PostAdapter mAdapter;
    private List<Post> postList;
    private ProgressView progressView;
    private RequestManager mGlideRequestManager;
    private AlertDialog filterDialog;

    private int lastPosition = 10;
    private int index = 0;
    private static int current_page = 1;
    private String[] itemsLocal = {"서울", "부산", "대구", "대전", "울산", "광주", "인천", "세종", "경기", "경남", "경북", "전남", "전북", "강원", "제주", "충북", "충남"};
    private String genderCheck;
    private TextView noPost, coin;


    public FirstMainFrg() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_first_main, container, false);

        //UI 초기 설정 값
        _init(rootView);

        return rootView;
    }

    private void _init(View rootView) {
        //플로팅액션버튼
        fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        //필터 이미지 버튼

        //progressView
        progressView = (ProgressView) rootView.findViewById(R.id.progressview);
        mGlideRequestManager = Glide.with(this);
        postList = new ArrayList<Post>();
        recyclerView = (RecyclerView) rootView.findViewById(R.id.rv_recycler_view);
        recyclerView.setHasFixedSize(true);


        noPost = (TextView) rootView.findViewById(R.id.no_post);
        coin = (TextView) rootView.findViewById(R.id.frg_first_coin);


        databaseReference = FirebaseDatabase.getInstance().getReference().child("posts");

    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mManager = new LinearLayoutManager(getActivity());
        mManager.setStackFromEnd(false);
        mManager.setReverseLayout(false);
        recyclerView.setLayoutManager(mManager);


        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(UserValue.SHARED_NAME, Context.MODE_PRIVATE);
        int coinValue = sharedPreferences.getInt(UserValue.USER_COIN, 0);

        coin.setText("Coin: " + coinValue);

        _initDataBaseReference(databaseReference, current_page);

        fab.attachToRecyclerView(recyclerView);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog();
            }
        });
    }


    private void alertDialog() {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        genderCheck = null;
        //dialogView 레이아웃 참조
        final View dialogView = inflater.inflate(R.layout.dialog_filter_list, null);
        //필터링 지역 버튼 참조
        final Button filter_local = (Button) dialogView.findViewById(R.id.filter_dialog_local);
        RippleView rippleView=(RippleView)dialogView.findViewById(R.id.filter_dialog_local_ripple);

        //필터링 성별 버튼 참조
        final FrameLayout manBtn = (FrameLayout) dialogView.findViewById(R.id.filter_dialog_man_btn);
        final FrameLayout womanBtn = (FrameLayout) dialogView.findViewById(R.id.filter_dialog_woman_btn);



        final TextView manText = (TextView) dialogView.findViewById(R.id.filter_dialog_man_txt);
        final TextView womanText = (TextView) dialogView.findViewById(R.id.filter_dialog_woman_txt);
        //필터링 업로드 됐을때 버튼 참조
        final Button filter_upload_btn = (Button) dialogView.findViewById(R.id.filter_upload_btn);

        rippleView.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {

            @Override
            public void onComplete(RippleView rippleView) {
                Log.d("Sample", "Ripple completed");
                showDialog(itemsLocal, "지역선택", filter_local);
            }

        });
        manBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manTextChange(womanText, manText);
            }
        });

        womanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                womanTextChange(womanText, manText);
            }
        });







        filter_upload_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //필터링 지역 버튼 값 String 값으로 받음
                String localStr = filter_local.getText().toString();


                //genderStr,localStr(성별, 지역) 값이 없을 경우 토스트 쇼
                if (TextUtils.isEmpty(localStr)) {
                    Toast.makeText(getActivity(), "지역을 선택해주세요", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(genderCheck)) {
                    Toast.makeText(getActivity(), "성별을 선택해주세요", Toast.LENGTH_SHORT).show();
                } else {
                    if (genderCheck.equals("남자")) {
                        Intent intent = new Intent(getActivity(), FilterLocalActivity.class);
                        intent.putExtra("local", localStr);
                        intent.putExtra("gender", "남자");
                        startActivity(intent);
                    } else if (genderCheck.equals("여자")) {
                        Intent intent = new Intent(getActivity(), FilterLocalActivity.class);
                        intent.putExtra("local", localStr);
                        intent.putExtra("gender", "여자");
                        startActivity(intent);
                    }
                }
            }

        });

        BottomSheetDialog mDialog = new BottomSheetDialog(getActivity());
        mDialog
                .contentView(dialogView)
                .heightParam(ViewGroup.LayoutParams.WRAP_CONTENT)
                .inDuration(500)
                .cancelable(true)
                .show();
    }



    private void manTextChange(TextView womanText, TextView manText) {
        genderCheck = "남자";
        womanText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        womanText.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimaryLittle));

        manText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        manText.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimary));

    }

    private void womanTextChange(TextView womanText, TextView manText) {
        genderCheck = "여자";
        womanText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        womanText.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimary));

        manText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        manText.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimaryLittle));

    }

    //리스트 다이아로그
    private void showDialog(final String[] item, String title, final Button btn) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //타이틀 값 설정
        builder.setTitle(title);
        builder.setPositiveButton("닫기", null);
        //값이 입력되면 버튼으로 값 전달
        builder.setItems(item, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedText = Arrays.asList(item).get(which);
                btn.setText(selectedText);
                dialog.cancel();

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //초기 레퍼런스 설정
    private void _initDataBaseReference(final DatabaseReference dbRef, final int current_page) {


        dbRef.orderByChild("stump").limitToFirst(lastPosition).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {

                    noPost.setVisibility(View.VISIBLE);

                    progressView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.INVISIBLE);

                } else {

                    noPost.setVisibility(View.GONE);
                    //초기에 리스트를 초기화
                    postList.clear();

                    //for문을 돌려 리스트 값만큼 추가
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        Post postModel = postSnapshot.getValue(Post.class);

                        postList.add(postModel);

                    }
                    //PostAdapter 참조
                    mAdapter = new PostAdapter(postList, getActivity(), mGlideRequestManager,progressView,recyclerView);

                    //RecycleView 어댑터 세팅
                    recyclerView.setAdapter(mAdapter);


                    //index 값
                    index = postList.size() - 1;

                    recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(mManager) {
                        @Override
                        public void onLoadMore(int currentPage) {
                            progressView.setVisibility(View.VISIBLE);
                            loadPaging(dbRef, current_page);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadPaging(DatabaseReference dbRef, int current_page) {


        dbRef.orderByChild("stump").startAt(postList.get(index).stump).limitToFirst(lastPosition).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //for문을 돌려 리스트 값만큼 추가
                postList.remove(index);
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Post postModel = postSnapshot.getValue(Post.class);

                    postList.add(postModel);

                }


                mAdapter.notifyDataSetChanged();
                index = postList.size() - 1;


                //리스트뷰 애니메이션 효과
                progressView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}


