package com.example.mybooks.myPage;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.mybooks.login.login_Activity;
import com.example.mybooks.retrofit.HttpRequest;
import com.example.mybooks.model.Response;
import com.example.mybooks.retrofit.RetrofitClient;
import com.kakao.auth.Session;
import com.kakao.network.ApiErrorCode;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.UnLinkResponseCallback;

import retrofit2.Call;
import retrofit2.Callback;

public class myPageSettingActivity extends AppCompatActivity {
    /**
     * 설정 (비번변경, 로그아웃, 회원탈퇴)
     */
    private final String TAG=this.getClass().getSimpleName();
    private com.example.mybooks.databinding.ActivityMyPageSettingBinding binding;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = com.example.mybooks.databinding.ActivityMyPageSettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // 쉐어드, DB 수정
        shared_setProfile(); // 닉넴, 이메일


        // 비밀번호 변경 (보임/안 보임)
        showButtonAndClickListener(); // 카카오/네이티브 유저




        // 로그아웃 버튼
        binding.btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                logout_sharedAndGoToLoginActivity(); // 쉐어드만 삭제 (네/아니오)

            }
        });



        //회원탈퇴 버튼
        binding.btnDeleteUser.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                dropOut_sharedAndGoToLoginActivity(); // 쉐어드, DB 삭제 (네/아니오)

            }
        });


    } // ~onCreate()


    private void showButtonAndClickListener() { // 버튼이 보이거나 안 보이거나

        // 카카오 유저
        if (Session.getCurrentSession().isOpened()) {
            binding.btnChangePw.setVisibility(View.GONE); // 카카오는 비번을 못 바꿈

        // 네이티브 유저
        } else {
            binding.btnChangePw.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // 이동
                    Intent intent = new Intent(myPageSettingActivity.this, myPage_changePassword.class); // 화면 이동
                    startActivity(intent);
                    Log.e(TAG, "startActivity() \n"+TAG+" -> myPage_changePassword.class ");

                }
            });
        }
    }


    private void shared_setProfile() {

        SharedPreferences auto = getSharedPreferences("autoLogin", Activity.MODE_PRIVATE); //Activity.MODE_PRIVATE : 해당데이터는 해당 앱에서만 사용가능
        Log.e(TAG, "\nuserEmail : "+auto.getString("userEmail", null)+
                "\n/userImg:"+auto.getString("userImg", null)+
                "\n/userImg:"+auto.getString("userName", null)+
                "\n/kakao:"+auto.getBoolean("kakao", false));


        // 닉네임
        if (auto.getString("userName", null) != null) {
            binding.tvUserName.setText(auto.getString("userName", null));

        } else { // 카톡로그인 아닌경우 처음엔 프사가 없음. 가라로 넣기
            binding.tvUserName.setText("독서하는 개미"); // 닉네임을 지어야 함
        }

        // 이메일
        if (auto.getString("userEmail", null) != null) {
            binding.tvUserEmail.setText(auto.getString("userEmail", null));

        } else {
            binding.tvUserEmail.setText("this is email..");
        }

    }


    private void dropOut_sharedAndGoToLoginActivity() { // 카카오는 연결을 끊는다.

        new AlertDialog.Builder(myPageSettingActivity.this) //탈퇴 의사를 묻는 팝업창 생성
                .setMessage("정말 탈퇴하시겠습니까?\n모든 데이터가 사라집니다.") //팝업창 메세지
                .setPositiveButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { //"네" 버튼 클릭 시 -> 회원탈퇴 수행


                        // 회원탈퇴 - 카카오
                        if (Session.getCurrentSession().isOpened()){
                            // 카카오 연결 끊기
                            UserManagement.getInstance().requestUnlink(new UnLinkResponseCallback() {
                                @Override
                                public void onFailure(ErrorResult errorResult) { //회원탈퇴 실패 시
                                    int result = errorResult.getErrorCode(); //에러코드 받음

                                    if(result == ApiErrorCode.CLIENT_ERROR_CODE) { //클라이언트 에러인 경우 -> 네트워크 오류
                                        Toast.makeText(getApplicationContext(), "네트워크 연결이 불안정합니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                                    } else { //클라이언트 에러가 아닌 경우 -> 기타 오류
                                        Toast.makeText(getApplicationContext(), "회원탈퇴에 실패했습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onSessionClosed(ErrorResult errorResult) { //처리 도중 세션이 닫힌 경우
                                    Toast.makeText(getApplicationContext(), "로그인 세션이 닫혔습니다. 다시 로그인해 주세요.", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(myPageSettingActivity.this, login_Activity.class);
                                    startActivity(intent);
                                    finish();
                                }

                                @Override
                                public void onNotSignedUp() { //가입된 적이 없는 계정에서 탈퇴를 요구하는 경우
                                    Toast.makeText(getApplicationContext(), "가입되지 않은 계정입니다. 다시 로그인해 주세요.", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(myPageSettingActivity.this, login_Activity.class);
                                    startActivity(intent);
                                    finish();
                                }

                                @Override
                                public void onSuccess(Long result) { //회원탈퇴에 성공한 경우

                                    // DB 삭제
                                    deleteUser(shared_myEmail());

                                    // 쉐어드 삭제
                                    shared_AutoLogin_delete();

                                    Toast.makeText(getApplicationContext(), "회원탈퇴 완료", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(myPageSettingActivity.this, login_Activity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });


                            // 회원탈퇴 - 네이티브 유저
                        } else {
                            // DB 삭제
                            deleteUser(shared_myEmail()); // 쉐어드에서 꺼낸 이멜로 회원탈퇴요청 서버로 보내기

                            // 쉐어드 삭제
                            shared_AutoLogin_delete();
                        }


                        dialog.dismiss(); //팝업창 제거
                    }
                })
                .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { //"아니요" 버튼 클릭 시 -> 팝업창 제거
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext(), "취소", Toast.LENGTH_SHORT).show();
                    }
                }).show();

    }




    // 회원탈퇴를 위해 retrofit 라이브러리를 사용
    private void deleteUser(String email) { Log.e(TAG, "deleteUser() / email : "+email);

        RetrofitClient retrofitClient = RetrofitClient.getInstance(); // 요청 인터페이스, 빌드, 로그 한 데 모아둔 클래스
        HttpRequest httpRequest = retrofitClient.getRetrofitInterface(); // 에서 꺼내쓴다.


        // 요청 메소드 이름 : clientLogin
        httpRequest.getDeleteUser(email).enqueue(new Callback<Response>() { // 파라미터 3개보내면서 요청하면 결과로 응답/실패 // 비동기
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {

                Response response1 = response.body();
                assert response1 != null;
                Log.e(TAG, "onResponse() / response1 : "+response1.getMessage());


                if (response1.isResponse()) { // DELETE 완료했다면

                    // 회원탈퇴 완료
                    Intent intent = new Intent(myPageSettingActivity.this, login_Activity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // 현재화면빼고 아래액티비티 지움
                    startActivity(intent);
                    Log.e(TAG, "startActivity() \n"+TAG+" -> login_Activity.class ");

                    Toast.makeText(getApplicationContext(), "회원탈퇴 완료", Toast.LENGTH_SHORT).show();


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



    private String shared_myEmail() {

        SharedPreferences auto = getSharedPreferences("autoLogin", Activity.MODE_PRIVATE); //Activity.MODE_PRIVATE : 해당데이터는 해당 앱에서만 사용가능
        return auto.getString("userEmail", null);
    }
    
    
    private void logout_sharedAndGoToLoginActivity() { // 쉐어드만 삭제

            new AlertDialog.Builder(myPageSettingActivity.this) //탈퇴 의사를 묻는 팝업창 생성
                    .setMessage("정말 로그아웃 하시겠습니까?") //팝업창 메세지
                    .setPositiveButton("네", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) { //"네" 버튼 클릭 시 -> 회원탈퇴 수행

                            if (Session.getCurrentSession().isOpened()) {
                                UserManagement.getInstance().requestLogout(new LogoutResponseCallback() {
                                    @Override
                                    public void onCompleteLogout() {

                                        Log.e(TAG, "카카오 로그아웃 onCompleteLogout() ");

                                        shared_AutoLogin_delete(); // 쉐어드 - 자동로그인 내용 삭제

                                        Intent intent = new Intent(myPageSettingActivity.this, login_Activity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // 현재화면빼고 아래액티비티 지움
                                        startActivity(intent);
                                    }
                                });

                            } else {

                                shared_AutoLogin_delete(); // 쉐어드 - 자동로그인 내용 삭제

                                // 화면 이동
                                Intent intent = new Intent(myPageSettingActivity.this, login_Activity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // 현재화면빼고 아래액티비티 지움
                                startActivity(intent);
                                Log.e(TAG, "startActivity() \n"+TAG+" -> login_Activity.class ");

                                Toast.makeText(getApplicationContext(), "로그아웃", Toast.LENGTH_SHORT).show();
                            }


                            dialog.dismiss(); //팝업창 제거
                        }
                    })
                    .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) { //"아니요" 버튼 클릭 시 -> 팝업창 제거
                            dialog.dismiss();
                            Toast.makeText(getApplicationContext(), "취소", Toast.LENGTH_SHORT).show();

                        }
                    }).show();
    }



    private void shared_AutoLogin_delete() { Log.e(TAG, "shared_AutoLogin_delete()");

        SharedPreferences auto = getSharedPreferences("autoLogin", Activity.MODE_PRIVATE); // error! context를 앞에 붙여줌
        SharedPreferences.Editor autoLoginEdit = auto.edit();

        Log.e(TAG, "쉐어드 삭제 : "+auto.getAll());
        autoLoginEdit.clear(); // 파일은 그대로 내용만 삭제됨
        autoLoginEdit.apply(); // 실질 저장
        Log.e(TAG, "쉐어드 삭제 : "+auto.getAll());
    }


}