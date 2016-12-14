package com.kkkhhh.socialblinddate.Activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.andexert.library.RippleView;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kkkhhh.socialblinddate.Etc.CustomBitmapPool;
import com.kkkhhh.socialblinddate.Etc.UserValue;
import com.kkkhhh.socialblinddate.Etc.Util;
import com.kkkhhh.socialblinddate.R;
import com.rey.material.widget.ProgressView;
import com.soundcloud.android.crop.Crop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

//////////// 회원가입 유저 프로필 입력 Act //////////////////

public class SignProfileAct extends AppCompatActivity implements View.OnClickListener {
    private Button dialogLocalBtn, dialogGenderBtn, profileNextBtn;
    private String[] itemsLocal = {"서울", "부산", "대구", "대전", "울산", "광주", "인천", "세종", "경기", "경남", "경북", "전남", "전북", "강원", "제주", "충북", "충남"};
    private String[] itemsGender = {"남자", "여자"};
    private FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = mFirebaseDatabase.getReference().getRoot();
    private DatabaseReference signProfileRef;
    private EditText nicknameEdit, ageEdit;
    private String nicknameStr, ageStr, genderStr, localStr;
    private ProgressDialog progressDialog;
    private String userID;
    private RippleView profileImgRippleView;
    private ImageView profileImg;
    private String imagePath, imgByteValue;
    private FirebaseAuth mFireAuth = FirebaseAuth.getInstance();
    private static int PICK_ALBUM = 0;
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    private static long ONE_MEGABYTE = 1024 * 1024;
    private ProgressView progressView;
    private String changeString;
    private ScrollView signProfileLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_profile);


        init();
        changeInit();
    }


    private void changeInit() {

        changeString=getIntent().getStringExtra(Util.CHANGE_PROFILE);

        if (changeString != null) {
            progressView.setVisibility(View.VISIBLE);
            signProfileLayout.setVisibility(View.INVISIBLE);
            SharedPreferences preferences = getSharedPreferences(UserValue.SHARED_NAME, MODE_PRIVATE);
            nicknameStr = preferences.getString(UserValue.USER_NAME, null);
            ageStr = preferences.getString(UserValue.USER_AGE, null);
            genderStr = preferences.getString(UserValue.USER_GENDER, null);
            localStr = preferences.getString(UserValue.USER_LOCAL, null);
            nicknameEdit.setText(nicknameStr);
            nicknameEdit.setEnabled(false);
            ageEdit.setText(ageStr);
            dialogLocalBtn.setText(localStr);
            dialogGenderBtn.setText(genderStr);
            dialogGenderBtn.setOnClickListener(null);

            storageRef.child(preferences.getString(UserValue.USER_PROFILE_IMG, null)).getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {


                    Glide.with(getApplicationContext()).load(bytes).centerCrop().
                            crossFade(1000).bitmapTransform(new CropCircleTransformation(new CustomBitmapPool())).into(profileImg);

                    String getByteString = Base64.encodeToString(bytes, 0);
                    imgByteValue = getByteString;
                    progressView.setVisibility(View.GONE);
                    signProfileLayout.setVisibility(View.VISIBLE);
                }
            });

        }
    }

    ///초기 내용 설정
    private void init() {
        nicknameEdit = (EditText) findViewById(R.id.sign_nickname);
        ageEdit = (EditText) findViewById(R.id.sign_age);

        dialogLocalBtn = (Button) findViewById(R.id.sign_local);
        dialogGenderBtn = (Button) findViewById(R.id.sign_gender);
        profileNextBtn = (Button) findViewById(R.id.sign_profile_next);

        dialogGenderBtn.setOnClickListener(this);
        dialogLocalBtn.setOnClickListener(this);
        profileNextBtn.setOnClickListener(this);

        progressDialog = new ProgressDialog(this);

        profileImgRippleView = (RippleView) findViewById(R.id.profile_img_ripple_view);
        profileImg = (ImageView) findViewById(R.id.profile_img);
        userID = mFireAuth.getCurrentUser().getUid();

        signProfileRef = databaseReference.child("users").child(userID);

        progressView=(ProgressView)findViewById(R.id.progressview);

        signProfileLayout=(ScrollView)findViewById(R.id.sign_profile);
        setProfileImage();


    }

    ///다음버튼 눌렸을 시 - 값을 받아서 체크 후 전송
    private void profileNext() {
        nicknameStr = nicknameEdit.getText().toString().trim();
        ageStr = ageEdit.getText().toString().trim();
        genderStr = dialogGenderBtn.getText().toString().trim();
        localStr = dialogLocalBtn.getText().toString().trim();

        if (TextUtils.isEmpty(nicknameStr)) {
            Toast.makeText(getApplicationContext(), "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show();

        } else if (TextUtils.isEmpty(ageStr)) {
            Toast.makeText(getApplicationContext(), "나이를 입력해주세요.", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(genderStr)) {
            Toast.makeText(getApplicationContext(), "성별을 선택해주세요", Toast.LENGTH_SHORT).show();

        } else if (TextUtils.isEmpty(localStr)) {
            Toast.makeText(getApplicationContext(), "지역을 선택해주세요.", Toast.LENGTH_SHORT).show();
        } else {
            ///전송
            writeUserSecond(nicknameStr, ageStr, localStr, genderStr);
        }


    }

    ///showDialog [성별, 지역]
    private void showDialog(final String[] item, String title, final Button btn) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SignProfileAct.this);


        builder.setTitle(title);
        builder.setPositiveButton("닫기", null);

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

    ///Firebase User 모델 전송 메소드
    private void writeUserSecond(String nickname, String age, String local, String gender) {
        //progressDialog 보여준다
        progressDialog.setMessage("정보를 저장중입니다.");
        progressDialog.show();
        byte[] file = Base64.decode(imgByteValue, 0);
        StorageReference img1_Ref = storageRef.child("userProfile").child(userID).child("profileImg");
        img1_Ref.putBytes(file);
        imagePath = img1_Ref.getPath();
        ///버튼클릭 리스너
        //Database userProfile 에 셋팅
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat CurDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String stampTime = CurDateFormat.format(date);
        SharedPreferences preferences = getSharedPreferences(UserValue.SHARED_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(UserValue.PROFILE_IMAGE_UPDATE_STAMP,null);
        editor.commit();
        signProfileRef.child("_updateStamp").setValue(stampTime);
        signProfileRef.child("_check").setValue(2);
        signProfileRef.child("_uNickname").setValue(nickname);
        signProfileRef.child("_uAge").setValue(age);
        signProfileRef.child("_uLocal").setValue(local);
        signProfileRef.child("_profileImage").setValue(imagePath);
        signProfileRef.child("_uGender").setValue(gender, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    //전송 오류가 나면 뜨는 메세지
                    System.out.println("Data could not be saved " + databaseError.getMessage());
                    Toast.makeText(SignProfileAct.this, "정보가 저장 되질 않습니다.", Toast.LENGTH_SHORT).show();
                    progressDialog.cancel();
                } else {
                    //전송이 완성 했을 경우
                    if (changeString != null) {
                        finish();
                    } else {
                        Intent intent = new Intent(SignProfileAct.this, MainAct.class);
                        startActivity(intent);
                        progressDialog.cancel();
                        finish();
                    }
                }
            }
        });
    }

    private void setProfileImage() {
        profileImgRippleView.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                doTakeAlbumAction();
            }
        });
    }

    //앨범을 가기 위한 Intent 값
    private void doTakeAlbumAction() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, Crop.REQUEST_PICK);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) {
            beginCrop(data.getData());
        } else if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, data);
        }
    }

    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(this);
    }


    private void handleCrop(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (data != null) {

                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap orgImage = null;
                    try {
                        orgImage = MediaStore.Images.Media.getBitmap(getContentResolver(), Crop.getOutput(data));
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        orgImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] dataByte = baos.toByteArray();
                        // 5. Glide 라이브러리를 이용하여 이미지뷰에 삽입
                        Glide.with(this).
                                load(dataByte)
                                .centerCrop()
                                .bitmapTransform(new CropCircleTransformation(new CustomBitmapPool()))
                                .into(profileImg);
                        String getByteString = Base64.encodeToString(dataByte, 0);
                        imgByteValue = getByteString;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (resultCode == Crop.RESULT_ERROR) {
                    Toast.makeText(this, Crop.getError(data).getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.sign_local: {
                showDialog(itemsLocal, "지역 선택", dialogLocalBtn);
                break;
            }
            case R.id.sign_gender: {
                showDialog(itemsGender, "성별 선택", dialogGenderBtn);
                break;
            }
            case R.id.sign_profile_next: {
                profileNext();
                break;
            }
            default:
        }
    }
}
