package com.example.mybooks.login;

import static com.example.mybooks.login.login_Activity.strCode;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.mybooks.databinding.ActivityLoginForgetPwBinding;
import com.example.mybooks.home_bottom_1to5.homeActivity;
import com.example.mybooks.retrofit.HttpRequest;
import com.example.mybooks.model.Response;
import com.example.mybooks.retrofit.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;

public class loginForgetPwActivity extends AppCompatActivity {
    /**
     * 비밀번호 재설정 (이메일 인증)
     * 비로그인상태
     */
    private final String TAG=this.getClass().getSimpleName();
    private ActivityLoginForgetPwBinding binding;
    private join_Activity activity = new join_Activity(); // 회원가입 때 만든 기능 재활용


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = com.example.mybooks.databinding.ActivityLoginForgetPwBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // 회언가입처럼 다 입력하고
        // 버튼 비활성화시킨다음
        // 비번 재설정하기


        // 초기 설정
        binding.btnCheckCode.setEnabled(false); // 인증확인 버튼 눌러도 반응X


        // 비밀번호 분실 시 이메일로 인증하기
        binding.btnSendCodeToEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activity.sendEmailForGmail(binding.etForgetPwEmail.getText().toString(), activity.createEmailCode(5),
                        getApplicationContext(), binding.btnSendCodeToEmail, binding.btnCheckCode);

            }
        });


        // 인증 확인 버튼
        binding.btnCheckCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (strCode.equals(binding.etForgetPwEmailCheck.getText().toString())) { Log.e(TAG, "(인증번호 입력 성공) 시 버튼 색 비활성화");

                    activity.viewChangeAfterCheckingStringCode(binding.etForgetPwEmail, binding.etForgetPwEmailCheck, binding.btnSendCodeToEmail, binding.btnCheckCode, getApplicationContext());

                } else {
                    Toast.makeText(getApplicationContext(), "인증번호를 다시 확인해 주세요.", Toast.LENGTH_SHORT).show();
                }

            }
        });


        // 비밀번호 변경 버튼
        binding.btnForgetPwDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (activity.patternPW(binding.etForgetPwPw.getText().toString()) // (정규식, 비번재입력 일치 통과) 시 회원가입 완료
                        && binding.etForgetPwPw.getText().toString().equals(binding.etForgetPwPwDoubleCheck.getText().toString())) {

                    clientChangePw(binding.etForgetPwEmail.getText().toString(), binding.etForgetPwPw.getText().toString(), getApplicationContext(),
                            loginForgetPwActivity.this, homeActivity.class);
                    // retrofit (이멜, 비번, context, thisActivity, 이동할 액티비티)

                    Log.e(TAG, "etForgetPwPw : "+binding.etForgetPwPw.getText().toString()+"\n etForgetPwPwDoubleCheck:"+
                            binding.etForgetPwPwDoubleCheck.getText().toString());

                } else {
                    Toast.makeText(getApplicationContext(), "다시 확인해주세요.", Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

    // retrofit (이멜, 비번, context, thisActivity, 이동할 액티비티)
    public void clientChangePw(String email, String pw, Context context, Context thisActivity, Class moveToActivity) { String TAG = "clientChangePw";

        RetrofitClient retrofitClient = RetrofitClient.getInstance();
        HttpRequest httpRequest = retrofitClient.getRetrofitInterface();


        // 요청 메소드 이름 : getRegister
        httpRequest.getChangePw(email, pw).enqueue(new Callback<Response>() { // 파라미터 3개보내면서 요청하면 결과로 응답/실패 // 비동기
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {

                Response response1 = response.body();
                assert response1 != null;
                Log.e(TAG, "onResponse() / response1 : "+response1.getMessage());


                if (response1.isResponse()) { // insert 완료했다면

                    // 비밀번호 변경 성공
                    Intent intent = new Intent(thisActivity, moveToActivity);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // 현재화면빼고 아래액티비티 지움
                    context.startActivity(intent); // error! context붙여야 함
                    Log.e(TAG, "startActivity() \n"+TAG+" -> homeActivity.class ");

                    Toast.makeText(context, "비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show();

                    new join_Activity().shared_AutoLogin(email, pw, context); // 쉐어드에 자동로그인하도록 저장


                } else {
                    Toast.makeText(context, "변경에 실패하였습니다.", Toast.LENGTH_SHORT).show(); // '이미 가입된 이메일입니다.'
                }

            }

            @Override
            public void onFailure(Call<Response> call, Throwable t) {
                Log.e(TAG, "onFailure() "+t.getMessage());
            }
        });

    }




}