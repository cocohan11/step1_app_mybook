package com.example.mybooks.login;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.mybooks.club.chatting;
import com.example.mybooks.databinding.ActivityLoginBinding;
import com.example.mybooks.home_bottom_1to5.homeActivity;
import com.example.mybooks.model.ClientInfo;
import com.example.mybooks.retrofit.HttpRequest;
import com.example.mybooks.model.Response;
import com.example.mybooks.retrofit.RetrofitClient;
import com.example.mybooks.socket.myService;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.util.exception.KakaoException;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import retrofit2.Call;
import retrofit2.Callback;

public class login_Activity extends AppCompatActivity {
    /**
     * 로그인 (네이티브, 카카오)
     */
    private final String TAG=this.getClass().getSimpleName();
    private ActivityLoginBinding binding; // findviewbyid 대신 binding하면 객체만으로 바로 id접근 가능
    public static String strCode;
    private ISessionCallback mSessionCallback;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "-> login_Activity.class");
        startMyService(); // 위치 여기로하니까 에러 안남

//        Log.e(TAG, "keyhash : "+getKeyHash());
        // 스플래시 넣기
        //
        //



        // 자동로그인
        if (shared_isAutoLogin()) { // 자동로그인 기록이 있다면 바로 home으로 간다
            Intent intent = new Intent(login_Activity.this, homeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 현재화면빼고 아래액티비티 지움
            intent.putExtra("toast", "로그인 성공");
            startActivity(intent);
            Log.e(TAG, "startActivity() \n"+TAG+" -> homeActivity.class ");




        // 일반로그인
        } else {
            setBinding_clickEvents();
        }



        //카카오 로그인 기능 시작
        mSessionCallback = new ISessionCallback() {
            @Override
            public void onSessionOpened() { // 로그인 요청
                kakaoSessionCallback();
            }
            @Override
            public void onSessionOpenFailed(KakaoException exception) {
                Toast.makeText(login_Activity.this, "세션 열기에 실패하였습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            }
        };
        Log.e(TAG, "mSessionCallback : "+mSessionCallback);
        Log.e(TAG, "Session.getCurrentSession() : "+Session.getCurrentSession());
        Session.getCurrentSession().addCallback(mSessionCallback);
        Session.getCurrentSession().checkAndImplicitOpen(); //세션 유지
        Log.e(TAG, "Session.getCurrentSession().isOpened : "+Session.getCurrentSession().isOpened()); // 세션 사용중인가


    } // ~onCreate()



    private void kakaoSessionCallback() {
        UserManagement.getInstance().me(new MeV2ResponseCallback() {
            @Override
            public void onFailure(ErrorResult errorResult) { // 로그인 요청했는데 실패했을 때
                Toast.makeText(login_Activity.this, "로그인 도중에 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                Toast.makeText(login_Activity.this, "세션이 닫혔습니다.. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(MeV2Response result) { // 로그인 성공

                Intent intent = new Intent(login_Activity.this, homeActivity.class);
                intent.putExtra("toast", "로그인 성공");
                startActivity(intent);
//                        finish(); // 에러나는지 확인/ 서비스 시작위치때문에 finish막음

                // 쉐어드에 저장 (이멜, 프사)
                shared_AutoLogin_kakao(result.getKakaoAccount().getEmail(), result.getKakaoAccount().getProfile().getProfileImageUrl(), result.getKakaoAccount().getProfile().getNickname());

                // DB에 유저정보 없으면 추가하기 (이멜, 프사)
                kakao_loginOrRegister(result.getKakaoAccount().getEmail(), result.getKakaoAccount().getProfile().getProfileImageUrl());


            }
        });
    }


    private void setBinding_clickEvents() {
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot()); // layout자체를 set해주는게 아니라 binding객체를 set해줌
        binding.tvLoginForgetPw.setPaintFlags( binding.tvUnderlineJoin.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG); // 밑줄(xml상으로 하면 번거로움)
        binding.tvUnderlineJoin.setPaintFlags( binding.tvUnderlineJoin.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);


        // 카카오 로그인
        binding.btnKakaoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { Log.e(TAG, "실제버튼 강제 클릭하기 performClick()");
                binding.btnKakaoSdk.performClick(); // 못생겨서 숨겨둠
            }
        });
        // 일반로그인하기
        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                clientLogin(binding.etLoginEmail.getText().toString(), binding.etLoginPw.getText().toString()); // http 확인

            }
        });
        // 비번잊으셨나요?
        binding.tvLoginForgetPw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(login_Activity.this, loginForgetPwActivity.class);
                startActivity(intent);
                Log.e(TAG, "startActivity() \n"+TAG+" -> loginForgetPwActivity.class ");

            }
        });
        // 회원가입하러 가기
        binding.tvUnderlineJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(login_Activity.this, join_Activity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // 현재화면빼고 아래액티비티 지움
                startActivity(intent);
                Log.e(TAG, "startActivity() \n"+TAG+" -> join_Activity.class ");

            }
        });
    }




    public static boolean isServiceRunning(Context context) {
        Log.e("isServiceRunning()", "서비스가 실행중인가");
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo rsi : am.getRunningServices(Integer.MAX_VALUE)) {
            if (myService.class.getName().equals(rsi.service.getClassName())) //[서비스이름]에 본인 것을 넣는다.
                return true;
        }
        return false;
    }


    /**
     * 서비스 시작
     */
    private void startMyService() {
        Log.e(TAG, "startBindService() isServiceRunning false : " + isServiceRunning(getApplicationContext()));
        if (!isServiceRunning(getApplicationContext())) {
            Log.e(TAG, "Trying to connect to service");
            Intent intent = new Intent(getApplicationContext(), myService.class);
            intent.putExtra("purpose", "시작"); // toService
            intent.putExtra("email", getSharedPreferences("autoLogin", Activity.MODE_PRIVATE).getString("userEmail", null));
//            intent.putExtra("purpose", "시작"); // toService
            startService(intent); // 서비스 시작되는 곳
        }
    }



    private void kakao_loginOrRegister(String email, String imgUrl) { Log.e(TAG, "register_match() / email : "+email+"/imgUrl:"+imgUrl);

        RetrofitClient retrofitClient = RetrofitClient.getInstance(); // 요청 인터페이스, 빌드, 로그 한 데 모아둔 클래스
        HttpRequest httpRequest = retrofitClient.getRetrofitInterface(); // 에서 꺼내쓴다.


        // 요청 메소드 이름 : getRegister_match
        httpRequest.getKakaoLogin(email, imgUrl).enqueue(new Callback<Response>() { // 파라미터 3개보내면서 요청하면 결과로 응답/실패 // 비동기
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {


                Response response1 = response.body();
                assert response1 != null;
                Log.e(TAG, "onResponse() / response1 : "+response1.getMessage());

//                Toast.makeText(getApplicationContext(), response1.getMessage(), Toast.LENGTH_SHORT).show(); // 로그인/회원가입

            }

            @Override
            public void onFailure(Call<Response> call, Throwable t) {
                Log.e(TAG, "onFailure() "+t.getMessage());
            }
        });
    }


    private void shared_AutoLogin_kakao(String email, String ImgUrl, String name) { Log.e(TAG, "shared_AutoLogin() email : "+email+", ImgUrl : "+ImgUrl);

        SharedPreferences auto = getSharedPreferences("autoLogin", Activity.MODE_PRIVATE); // error! context를 앞에 붙여줌
        SharedPreferences.Editor autoLoginEdit = auto.edit();

        autoLoginEdit.putString("userEmail", email);
        autoLoginEdit.putString("userImg", ImgUrl);
        autoLoginEdit.putString("userName", name);
        autoLoginEdit.putBoolean("kakao", true);

        autoLoginEdit.apply(); //실질 저장
    }


    public String getKeyHash(){ // 키해시 얻기
        try{
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            if(packageInfo == null) return null;
            for(Signature signature: packageInfo.signatures){
                try{
                    MessageDigest md = MessageDigest.getInstance("SHA");
                    md.update(signature.toByteArray());
                    return android.util.Base64.encodeToString(md.digest(), Base64.NO_WRAP);
                }catch (NoSuchAlgorithmException e){
                    Log.w("getKeyHash", "Unable to get MessageDigest. signature="+signature, e);
                }
            }
        }catch(PackageManager.NameNotFoundException e){
            Log.w("getPackageInfo", "Unable to getPackageInfo");
        }
        return null;
    }



    // 로그인을 하기위해 retrofit 라이브러리를 사용
    private void clientLogin(String email, String pw) { Log.e(TAG, "clientRegister() / email : "+email+", pw : "+pw);

        if (!email.equals("") && !pw.equals("")) {
            RetrofitClient retrofitClient = RetrofitClient.getInstance(); // 요청 인터페이스, 빌드, 로그 한 데 모아둔 클래스
            HttpRequest httpRequest = retrofitClient.getRetrofitInterface(); // 에서 꺼내쓴다.


            // 요청 메소드 이름 : clientLogin
            httpRequest.getLogin(email, pw).enqueue(new Callback<Response>() { // 파라미터 3개보내면서 요청하면 결과로 응답/실패 // 비동기
                @Override
                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {

                    Response response1 = response.body();
                    assert response1 != null;
                    Log.e(TAG, "onResponse() / response1 : "+response1.getMessage());


                    if (response1.isResponse()) { // UPDATE 완료했다면

                        Intent intent = new Intent(login_Activity.this, homeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // 현재화면빼고 아래액티비티 지움
                        intent.putExtra("toast", "로그인 성공 ");
                        startActivity(intent);
                        Log.e(TAG, "startActivity() \n"+TAG+" -> homeActivity.class ");

//                        Toast.makeText(getApplicationContext(), "로그인 성공", Toast.LENGTH_SHORT).show();

                        join_Activity joinActivity = new join_Activity(); // 다른 클래스 메소드 가져쓰려고
                        joinActivity.shared_AutoLogin(email, pw, getApplicationContext()); // 쉐어드에 자동로그인하도록 저장


                    } else {
                        Toast.makeText(getApplicationContext(), "다시 확인해주세요", Toast.LENGTH_SHORT).show(); // '이미 가입된 이메일입니다.'
                    }

                }

                @Override
                public void onFailure(Call<Response> call, Throwable t) {
                    Log.e(TAG, "onFailure() "+t.getMessage());
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "빈 값을 채워주세요", Toast.LENGTH_SHORT).show(); // '이미 가입된 이메일입니다.'
        }

    }




    private boolean shared_isAutoLogin() { // 자동로그인 참이냐

        boolean didAutoLogin = false;
        SharedPreferences auto = getSharedPreferences("autoLogin", Activity.MODE_PRIVATE); //Activity.MODE_PRIVATE : 해당데이터는 해당 앱에서만 사용가능

        Log.e(TAG, "auto.getString(\"userEmail\", null) : "+auto.getString("userEmail", null));
        if (auto.getString("userEmail", null) != null) didAutoLogin = true; // 쉐어드에 이멜기록이 있다면 자동로그인 고고

        return didAutoLogin;
    }

    private boolean shared_isAutoLogin_kakao() { // 카카오 자동로그인인가

        SharedPreferences auto = getSharedPreferences("autoLogin", Activity.MODE_PRIVATE); //Activity.MODE_PRIVATE : 해당데이터는 해당 앱에서만 사용가능

        Log.e(TAG, "auto.getBoolean(\"kakao\", false) : "+auto.getBoolean("kakao", false));
        return auto.getBoolean("kakao", false); // 쉐어드에 이멜기록이 있다면 자동로그인 고고
    }


    @Override
    protected void onResume() { Log.e(TAG, "onResume()");
        super.onResume();
    }

    @Override
    protected void onStart() { Log.e(TAG, "onStart()");
        super.onStart();
    }

    @Override
    protected void onRestart() { Log.e(TAG, "onRestart()");
        super.onRestart();
    }

    @Override
    protected void onPause() { Log.e(TAG, "onPause()");
        super.onPause();
    }

    @Override
    protected void onStop() { Log.e(TAG, "onStop()");
        super.onStop();
    }

    @Override
    public void onDestroy(){ Log.e(TAG, "onDestroy() ");
        super.onDestroy();
    }
}