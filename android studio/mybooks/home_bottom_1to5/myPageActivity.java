package com.example.mybooks.home_bottom_1to5;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.mybooks.R;
import com.example.mybooks.databinding.ActivityMyPageBinding;
import com.example.mybooks.myPage.myPageEditActivity;
import com.example.mybooks.myPage.myPageSettingActivity;
import com.example.mybooks.retrofit.HttpRequest;
import com.example.mybooks.model.Response;
import com.example.mybooks.retrofit.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;

public class myPageActivity extends AppCompatActivity {
    /**
     * 내 정보보기 (프로필, 설정 등)
     */
    private final String TAG=this.getClass().getSimpleName();
    private ActivityMyPageBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        // 프로필 수정완료 버튼
        binding.btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(myPageActivity.this, myPageEditActivity.class);
                startActivity(intent);
                Log.e(TAG, "startActivity() \n"+TAG+" -> myPageEditActivity.class ");

            }
        });


        // 설정 버튼
        binding.imgMyPageSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(myPageActivity.this, myPageSettingActivity.class);
                startActivity(intent);
                Log.e(TAG, "startActivity() \n"+TAG+" -> myPageSettingActivity.class ");

            }
        });


        // 홈 (메뉴1)
        binding.myPageBottom1Home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(myPageActivity.this, homeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // 현재화면빼고 아래액티비티 지움
                startActivity(intent);
                Log.e(TAG, "startActivity() \n"+TAG+" -> homeActivity.class ");

            }
        });


    }



    private void shared_setProfile() {

        SharedPreferences auto = getSharedPreferences("autoLogin", Activity.MODE_PRIVATE); //Activity.MODE_PRIVATE : 해당데이터는 해당 앱에서만 사용가능
        Log.e(TAG, "\nuserEmail : "+auto.getString("userEmail", null)+
                "\n/userImg:"+auto.getString("userImg", null)+
                "\n/userImg:"+auto.getString("userName", null)+
                "\n/kakao:"+auto.getBoolean("kakao", false));

        SharedPreferences.Editor autoLoginEdit = auto.edit();


        // 서버에서 닉넴, 프사, 책권수(+@) 가져오기
        RetrofitClient retrofitClient = RetrofitClient.getInstance();
        HttpRequest httpRequest = retrofitClient.getRetrofitInterface();


        // 요청 메소드 이름 : getProfile
        httpRequest.getProfile(auto.getString("userEmail", null)).enqueue(new Callback<Response>() { // 파라미터 3개보내면서 요청하면 결과로 응답/실패 // 비동기
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {

                Response response1 = response.body();
                assert response1 != null;
                Log.e(TAG, "onResponse() / response1 : "+response1.getMessage());


                if (response1.isResponse()) { // return 닉넴, 프사


                    // 닉넴
                    // 닉넴 없으면 쉐어드/ 쉐어드도없으면 / 가라로넣기
                    if (response1.getUserName() != null) { // 닉넴 있으면 삽입하기
                        binding.tvMyPageUserName.setText(response1.getUserName());

                        // 쉐어드에 업뎃
                        autoLoginEdit.putString("userName", response1.getUserName());


                    } else if (auto.getString("userName", null) != null){
                        binding.tvMyPageUserName.setText(auto.getString("userName", null));

                    } else {
                        binding.tvMyPageUserName.setText("독서하는 개미"); // 닉네임을 지어야 함
                    }


                    // 프사
                    if (response1.getUserImg() != null) { // 닉넴 있으면 삽입하기
                        Glide.with(getApplicationContext()).load(response1.getUserImg()).circleCrop().into(binding.imgMyPageMainImg); // 원형
                        autoLoginEdit.putString("userImg", response1.getUserImg());

                    } else if (auto.getString("userImg", null) != null){
                        Glide.with(getApplicationContext()).load(auto.getString("userImg", null)).circleCrop().into(binding.imgMyPageMainImg); // 원형

                    } else {
                        Glide.with(getApplicationContext()).load(R.drawable.backbag).circleCrop().into(binding.imgMyPageMainImg); // 가라
                    }


                    // 책 권수
                    binding.tvCountMyBook.setText(response1.getCountMyBook());

                }
                autoLoginEdit.apply(); //실질 저장


            }

            @Override
            public void onFailure(Call<Response> call, Throwable t) {
                Log.e(TAG, "onFailure() "+t.getMessage());
            }
        });




        // 로그아웃하니 쉐어드에 저장된 사용자 정보가 지워짐
        // 서버에서 가져오기

    }





    @Override
    protected void onResume() { Log.e(TAG, "onResume()");
        super.onResume();


        // 사용자 정보 삽입
        // onCreate()에 있다가 여기로 변경
        shared_setProfile(); // 닉넴, 프사

    }
}