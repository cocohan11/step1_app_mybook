package com.example.mybooks.myPage;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.mybooks.databinding.ActivityMyPageChangePasswordBinding;
import com.example.mybooks.home_bottom_1to5.homeActivity;
import com.example.mybooks.home_bottom_1to5.myPageActivity;
import com.example.mybooks.login.join_Activity;
import com.example.mybooks.login.loginForgetPwActivity;

public class myPage_changePassword extends AppCompatActivity {
    /**
     * 비밀번호 변경
     * 로그인 상태
     */
    private final String TAG=this.getClass().getSimpleName();
    private ActivityMyPageChangePasswordBinding binding;
    private join_Activity activity = new join_Activity(); // 회원가입 때 만든 기능 재활용
    private loginForgetPwActivity activityPw = new loginForgetPwActivity(); // pra2


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyPageChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        binding.btnChangePwDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 정규식 통과 -> DB 업뎃
                SharedPreferences auto = getSharedPreferences("autoLogin", Activity.MODE_PRIVATE); // pra1 : 쉐어드에서 이멜가져옴


                // retrofit (이멜, 비번, context, thisActivity, 이동할 액티비티)
                if (activity.patternPW(binding.etChangePwPw.getText().toString()) // (정규식, 비번재입력 일치 통과) 시 회원가입 완료
                        && binding.etChangePwPw.getText().toString().equals(binding.etChangePwPwDoubleCheck.getText().toString())) {


                    activityPw.clientChangePw(auto.getString("userEmail", null), binding.etChangePwPw.getText().toString(),
                            getApplicationContext(),myPage_changePassword.this, myPageActivity.class);
                    // retrofit (이멜, 비번, context, thisActivity, 이동할 액티비티)

                    Log.e(TAG, "etForgetPwPw : "+binding.etChangePwPw.getText().toString()+"\n etForgetPwPwDoubleCheck:"+
                            binding.etChangePwPwDoubleCheck.getText().toString());

                } else {
                    Toast.makeText(getApplicationContext(), "다시 확인해주세요.", Toast.LENGTH_SHORT).show();
                }




            }
        });
    }
}