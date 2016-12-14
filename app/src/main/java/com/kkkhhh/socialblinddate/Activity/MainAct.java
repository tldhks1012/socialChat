package com.kkkhhh.socialblinddate.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.andexert.library.RippleView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.kkkhhh.socialblinddate.Etc.UserValue;
import com.kkkhhh.socialblinddate.Fragment.FirstMainFrg;
import com.kkkhhh.socialblinddate.Fragment.FiveMainFrg;
import com.kkkhhh.socialblinddate.Fragment.FourMainFrg;
import com.kkkhhh.socialblinddate.Fragment.SecondMainFrg;
import com.kkkhhh.socialblinddate.Fragment.ThirdMainFrg;
import com.kkkhhh.socialblinddate.Model.UserModel;
import com.kkkhhh.socialblinddate.R;

public class MainAct extends AppCompatActivity {
    private ImageView actionPublicList, actionMyList, actionMsg,actionLike, actionProfile;
    private FirebaseAuth mFireAuth = FirebaseAuth.getInstance();
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private String uID = mFireAuth.getCurrentUser().getUid();
    Fragment mFragment;
    private RippleView publicListRippleView,actionMyListRippleView,actionMsgRippleView,actionLikeRippleView,actionProfileRippleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkUser();

    }

    private void init() {

        actionPublicList = (ImageView) findViewById(R.id.list_public_img);
        actionMyList = (ImageView) findViewById(R.id.list_my_img);
        actionMsg = (ImageView) findViewById(R.id.msg_img);
        actionLike=(ImageView)findViewById(R.id.like_img);
        actionProfile = (ImageView) findViewById(R.id.profile_img);

        publicListRippleView=(RippleView)findViewById(R.id.list_public_img_ripple);
        publicListRippleView.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                mFragment = new FirstMainFrg();
                actionPublicList.setImageResource(R.drawable.ic_action_list_public_yellow);
                actionMyList.setImageResource(R.drawable.ic_action_list_my_white);
                actionMsg.setImageResource(R.drawable.ic_action_msg_white);
                actionLike.setImageResource(R.drawable.ic_action_like_pull_white);
                actionProfile.setImageResource(R.drawable.ic_action_profile_white);
                commitFragment();

            }
        });
        actionMyListRippleView=(RippleView)findViewById(R.id.list_my_img_ripple);
        actionMyListRippleView.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                mFragment = new SecondMainFrg();
                actionPublicList.setImageResource(R.drawable.ic_action_list_public_white);
                actionMyList.setImageResource(R.drawable.ic_action_list_my_yellow);
                actionMsg.setImageResource(R.drawable.ic_action_msg_white);
                actionLike.setImageResource(R.drawable.ic_action_like_pull_white);
                actionProfile.setImageResource(R.drawable.ic_action_profile_white);
                commitFragment();
            }
        });
        actionMsgRippleView=(RippleView)findViewById(R.id.msg_img_ripple);
        actionMsgRippleView.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                mFragment = new ThirdMainFrg();
                actionPublicList.setImageResource(R.drawable.ic_action_list_public_white);
                actionMyList.setImageResource(R.drawable.ic_action_list_my_white);
                actionMsg.setImageResource(R.drawable.ic_action_msg_yellow);
                actionLike.setImageResource(R.drawable.ic_action_like_pull_white);
                actionProfile.setImageResource(R.drawable.ic_action_profile_white);
                commitFragment();
            }
        });
        actionLikeRippleView=(RippleView)findViewById(R.id.like_img_ripple);
        actionLikeRippleView.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                mFragment = new ThirdMainFrg();
                mFragment = new FourMainFrg();
                actionPublicList.setImageResource(R.drawable.ic_action_list_public_white);
                actionMyList.setImageResource(R.drawable.ic_action_list_my_white);
                actionMsg.setImageResource(R.drawable.ic_action_msg_white);
                actionLike.setImageResource(R.drawable.ic_action_like_pull_yellow);
                actionProfile.setImageResource(R.drawable.ic_action_profile_white);
                commitFragment();
            }
        });
        actionProfileRippleView=(RippleView)findViewById(R.id.profile_img_ripple);
        actionProfileRippleView.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                mFragment = new FiveMainFrg();
                actionPublicList.setImageResource(R.drawable.ic_action_list_public_white);
                actionMyList.setImageResource(R.drawable.ic_action_list_my_white);
                actionMsg.setImageResource(R.drawable.ic_action_msg_white);
                actionLike.setImageResource(R.drawable.ic_action_like_pull_white);
                actionProfile.setImageResource(R.drawable.ic_action_profile_yellow);
                commitFragment();
            }
        });


        mFragment = new FirstMainFrg();
        FragmentManager fm = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        mFragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_place, mFragment);
        fragmentTransaction.commit();

    }
    private void checkUser() {

        if (mFireAuth.getCurrentUser() == null) {
            Intent intent = new Intent(MainAct.this, WelcomeAct.class);
            startActivity(intent);
            finish();
        } else {
            final SharedPreferences preferences = getSharedPreferences(UserValue.SHARED_NAME, MODE_PRIVATE);
            String userID = preferences.getString(UserValue.USER_ID, null);
            if (userID == null) {
                databaseReference.child("users").child(uID).child("_check").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int value = dataSnapshot.getValue(Integer.class);
                        if (value == 1) {
                            Intent intent = new Intent(MainAct.this, SignProfileAct.class);
                            startActivity(intent);
                            finish();
                        } else if (value == 2) {
                            _userValue();
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }else{
                init();
            }

        }


    }

    private void _userValue() {
        databaseReference.child("users").child(uID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    UserModel userModel = dataSnapshot.getValue(UserModel.class);
                    String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                    databaseReference.child("users").child(uID).child("_tokenValue").setValue(refreshedToken);
                    SharedPreferences preferences = getSharedPreferences(UserValue.SHARED_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(UserValue.USER_ID, userModel._uID);
                    editor.putString(UserValue.USER_NAME, userModel._uNickname);
                    editor.putString(UserValue.USER_AGE, userModel._uAge);
                    editor.putString(UserValue.USER_GENDER, userModel._uGender);
                    editor.putString(UserValue.USER_LOCAL, userModel._uLocal);
                    editor.putString(UserValue.USER_PROFILE_IMG, userModel._profileImage);
                    editor.putInt(UserValue.USER_COIN,userModel._uCoin);
                    editor.putString(UserValue.USER_TOKEN,refreshedToken);
                    editor.putString(UserValue.PROFILE_IMAGE_UPDATE_STAMP,userModel._updateStamp);
                    editor.commit();
                    init();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }




    private void commitFragment(){
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_place, mFragment);
        fragmentTransaction.commit();
    }


}

