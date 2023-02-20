package com.example.mybooks.myPage;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.loader.content.CursorLoader;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mybooks.R;
import com.example.mybooks.databinding.ActivityMyPageEditBinding;
import com.example.mybooks.retrofit.HttpRequest;
import com.example.mybooks.model.Response;
import com.example.mybooks.retrofit.RetrofitClient;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class myPageEditActivity extends AppCompatActivity {
    /**
     * 프로필 수정 (닉넴, 프사)
     */
    private final String TAG=this.getClass().getSimpleName();
    private ActivityMyPageEditBinding binding;
    public static final int GET_GALLERY_IMG = 1111; // intent 결과를 구분하기위한 아무값
    private MultipartBody.Part body;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyPageEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // 사용자 정보 삽입
        shared_setProfile(); // 닉넴, 프사



        // 프로필 사진 고르기
        binding.imgMyPageEditMainImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { Log.e(TAG, "프로필 사진 고르기 클릭");


                requestPermission(); // 갤러리 열기


            }
        });


        // 수정완료 버튼
        binding.btnModifyDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { Log.e(TAG, "수정완료 버튼 클릭");


                // 쉐어드, DB 수정 (이멜, 닉넴, 바디)
                updateProfile(getSharedPreferences("autoLogin", Activity.MODE_PRIVATE).getString("userEmail", null),
                        binding.etMyPageEditUserName.getText().toString(), body);


            }
        });



    } // ~onCreate()


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "onActivityResult() GET_GALLERY_IMG:"+GET_GALLERY_IMG+"/requestCode:"+requestCode+"/resultCode:"+resultCode+"/data:"+data);

        if(requestCode == GET_GALLERY_IMG && resultCode == RESULT_OK) { // (요청코드가 200인가, 호출성공인가)


            // 프사 뷰에 적용
            Glide.with(getApplicationContext()).load(data.getData()).circleCrop().into(binding.imgMyPageEditMainImg); // 원형
/*
            load : 선택 이미지 정보(사진을)
            override : 이미지 가로,세로 크기(필수x)
            into : 화면에 보여줄 이미지뷰 객체(어디뷰에 넣을거냐)
*/

            // retrofit 으로 보내기 위한 이미지 파일 가공
            Uri uri = data.getData(); // fileUri
            String path = getRealPathFromUri(uri, getApplicationContext()); // filePath
            File file = new File(path);


            Log.e(TAG, "\n\n uri: "+data.getData()
                    +"\n path: "+path
                    +"\n file: "+file);

            // body
            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            body = MultipartBody.Part.createFormData("uploaded_file", file.getName(), requestFile);
            Log.e(TAG, "requestFile : "+requestFile); // okhttp3.RequestBody$Companion$asRequestBody$1@a53cac3
            Log.e(TAG, "body : "+body);  // okhttp3.MultipartBody$Part@346ef40

        } else {

        }
    }



    //파일의 Uri로 경로를 찾아주는 함수(복붙)
    public String getRealPathFromUri(Uri uri, Context context) { // 절대 경로

        /*
        문제 상황
        uri: content://com.google.android.apps.photos.contentprovider/-1/1/content%3A%2F%2Fmedia%2Fexternal%2Fimages%2Fmedia%2F32/ORIGINAL/NONE/image%2Fjpeg/1203547196
        path: /-1/1/content://media/external/images/media/32/ORIGINAL/NONE/image/jpeg/1203547196
        file: /-1/1/content:/media/external/images/media/32/ORIGINAL/NONE/image/jpeg/1203547196

        원하는 결과
        path: /storage/emulated/0/DCIM/Camera/20220523_173047.jpg

        결과
        uri: content://com.google.android.apps.photos.contentprovider/-1/1/content%3A%2F%2Fmedia%2Fexternal%2Fimages%2Fmedia%2F35/ORIGINAL/NONE/image%2Fjpeg/1606470623
        path: /storage/emulated/0/Download/summer.jpg
        file: /storage/emulated/0/Download/summer.jpg

        주의
        storage>emulated>0>Download 에 넣어야 됨
        sdcard / 구글포토와 연동된 사진은 에러남. 처리를 안 했음
        */

        String[] projection = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(context, uri, projection, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();
        int column = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column);
        cursor.close();
        return result;
    }


    // 로그인을 하기위해 retrofit 라이브러리를 사용
    private void updateProfile(String email, String name, MultipartBody.Part body) { Log.e(TAG, "updateProfile() / email : "+email+", name : "+name+", body:"+body);

        RetrofitClient retrofitClient = RetrofitClient.getInstance(); // 요청 인터페이스, 빌드, 로그 한 데 모아둔 클래스
        HttpRequest httpRequest = retrofitClient.getRetrofitInterface(); // 에서 꺼내쓴다.



        // 사진 변경 안 함
        if (body == null) {

            // 요청 메소드 이름 : clientLogin
            httpRequest.getChangeProfile(email, name).enqueue(new Callback<Response>() { // 파라미터 3개보내면서 요청하면 결과로 응답/실패 // 비동기
                @Override
                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {

                    Response response1 = response.body();
                    assert response1 != null;
                    Log.e(TAG, "onResponse() / response1.getMessage() : "+response1.getMessage());


                    if (response1.isResponse()) { // UPDATE 완료했다면

                        shared_AutoLogin(email, null,name); // 쉐어드 변경 저장
                        Toast.makeText(getApplicationContext(), "프로필 수정완료", Toast.LENGTH_SHORT).show();
                        finish();

                    } else {
                        Toast.makeText(getApplicationContext(), "다시 확인해주세요", Toast.LENGTH_SHORT).show(); // '이미 가입된 이메일입니다.'
                    }

                }

                @Override
                public void onFailure(Call<Response> call, Throwable t) {
                    Log.e(TAG, "onFailure() "+t.getMessage());
                }
            });


        // 사진 변경함
        } else {

            // 요청 메소드 이름 : getUploadProfileImg (이멜, 닉넴, body)
            httpRequest.getUploadProfileImg(getSharedPreferences("autoLogin", Activity.MODE_PRIVATE).getString("userEmail", null), name, body).enqueue(new Callback<Response>() { // 파라미터 3개보내면서 요청하면 결과로 응답/실패 // 비동기
                @Override
                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {

                    Response response1 = response.body();
                    assert response1 != null;
                    Log.e(TAG, "onResponse() / response1 : "+response1.getMessage()); // ImgUrl


                    if (response1.isResponse()) { // UPDATE 완료했다면

                        Toast.makeText(getApplicationContext(), "프로필 수정완료", Toast.LENGTH_SHORT).show();
                        shared_AutoLogin(email, response1.getMessage(), name); // 쉐어드 변경 저장
                        finish();

                    } else {
                        Toast.makeText(getApplicationContext(), "다시 확인해주세요", Toast.LENGTH_SHORT).show(); // '이미 가입된 이메일입니다.'
                    }

                }

                @Override
                public void onFailure(Call<Response> call, Throwable t) {
                    Log.e(TAG, "onFailure() "+t.getMessage());
                }
            });
        }


    }




    // 프사변경 - 닉넴만/ 닉넴+사진
    private void shared_AutoLogin(String email, String ImgUrl, String name) { Log.e(TAG, "shared_AutoLogin() email : "+email+", ImgUrl : "+ImgUrl);

        SharedPreferences auto = getSharedPreferences("autoLogin", Activity.MODE_PRIVATE); // error! context를 앞에 붙여줌
        SharedPreferences.Editor autoLoginEdit = auto.edit();

        autoLoginEdit.putString("userEmail", email);
        autoLoginEdit.putString("userName", name);
        if (ImgUrl != null) autoLoginEdit.putString("userImg", ImgUrl); // 프사변경 - 닉넴+사진 변경인 경우

        Log.e(TAG, "\nuserEmail : "+auto.getString("userEmail", null)+
                "\n/userImg:"+auto.getString("userImg", null)+
                "\n/userImg:"+auto.getString("userName", null)+
                "\n/kakao:"+auto.getBoolean("kakao", false));

        autoLoginEdit.apply(); //실질 저장
    }



    private void shared_setProfile() { // 쉐어드로부터 불러와 뷰에 셋팅

        SharedPreferences auto = getSharedPreferences("autoLogin", Activity.MODE_PRIVATE); //Activity.MODE_PRIVATE : 해당데이터는 해당 앱에서만 사용가능
        Log.e(TAG, "\nuserEmail : "+auto.getString("userEmail", null)+
                "\n/userImg:"+auto.getString("userImg", null)+
                "\n/userImg:"+auto.getString("userName", null)+
                "\n/kakao:"+auto.getBoolean("kakao", false));


        // 닉네임
        if (auto.getString("userName", null) != null) {
            binding.etMyPageEditUserName.setText(auto.getString("userName", null));

        } else { // 카톡로그인 아닌경우 처음엔 프사가 없음. 가라로 넣기
            binding.etMyPageEditUserName.setText("독서하는 개미"); // 닉네임을 지어야 함
        }

        // 프사
        if (auto.getString("userImg", null) != null) {
            Glide.with(getApplicationContext()).load(auto.getString("userImg", null)).circleCrop().into(binding.imgMyPageEditMainImg); // 원형

        } else {
            Glide.with(getApplicationContext()).load(R.drawable.backbag).circleCrop().into(binding.imgMyPageEditMainImg); // 가라
        }

    }



    // # Permission 권한 요청
    private void requestPermission() { Log.e(TAG, "requestPermission()");
        // 마시멜로우 버전 이후라면 권한을 요청해라
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // 승인
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(getApplicationContext(), "허용된상태", Toast.LENGTH_SHORT).show();
                startActivityForResult( // (intent, 요청코드)
                        new Intent().setType("image/*").setAction(Intent.ACTION_PICK) // 한 장
                        , GET_GALLERY_IMG);

                // 거부
            } else {

                Toast.makeText(getApplicationContext(), "거부된상태", Toast.LENGTH_SHORT).show();
                String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE};
                ActivityCompat.requestPermissions(this, permission, 1); // 권한요청
            }
        }
    }


    // 갤러리 접근권한 결과
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                Log.e(TAG, "case1 입장");
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(getApplicationContext(), "거부->승인", Toast.LENGTH_SHORT).show();
                    startActivityForResult( // (intent, 요청코드)
                            new Intent().setType("image/*").setAction(Intent.ACTION_PICK) // 한 장
                            , GET_GALLERY_IMG);

                } else {
                    Toast.makeText(getApplicationContext(), "거부->거부", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }




}