package com.example.mybooks.login;

import static com.example.mybooks.login.login_Activity.strCode;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mybooks.databinding.ActivityJoinBinding;
import com.example.mybooks.home_bottom_1to5.homeActivity;
import com.example.mybooks.retrofit.HttpRequest;
import com.example.mybooks.model.Response;
import com.example.mybooks.retrofit.RetrofitClient;

import java.util.Properties;
import java.util.regex.Pattern;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import retrofit2.Call;
import retrofit2.Callback;

public class join_Activity extends AppCompatActivity {
    /**
     * 회원가입 (이메일 인증)
     */
    private final String TAG=this.getClass().getSimpleName();
    private ActivityJoinBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityJoinBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // 초기 설정
        binding.btnCheckCode.setEnabled(false); // 인증확인 버튼 눌러도 반응X


        // 인증번호 보내기 버튼
        binding.btnSendCodeToEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { Log.e(TAG, "인증번호 보내기");

                register_match(binding.etJoinEmail.getText().toString()); // 중복된 이메일인지 확인

            }
        });


        // 인증 확인 버튼
        binding.btnCheckCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (strCode.equals(binding.etJoinEmailCheck.getText().toString())) { Log.e(TAG, "(인증번호 입력 성공) 시 버튼 색 비활성화");
                    viewChangeAfterCheckingStringCode(binding.etJoinEmail, binding.etJoinEmailCheck, binding.btnSendCodeToEmail, binding.btnCheckCode, getApplicationContext());
                } else {
                    Toast.makeText(getApplicationContext(), "인증번호를 다시 확인해 주세요.", Toast.LENGTH_SHORT).show();
                }

            }
        });


        // 회원가입 완료 버튼
        binding.btnJoinDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (patternPW(binding.etJoinPw.getText().toString())
                        && binding.etJoinPw.getText().toString().equals(binding.etJoinPwDoubleCheck.getText().toString())) {

                    Log.e(TAG, "(정규식, 비번재입력 일치 통과) 시 회원가입 완료");
                    clientRegister(binding.etJoinEmail.getText().toString(), binding.etJoinPw.getText().toString()); // retrofit (이멜, 비번)

                } else {
                    Toast.makeText(getApplicationContext(), "다시 확인해주세요.", Toast.LENGTH_SHORT).show();
                }

            }
        });




    } // ~onCreate()



    // 회원가입을 하기위해 retrofit 라이브러리를 사용
    private void clientRegister(String email, String pw) { Log.e(TAG, "clientRegister() / email : "+email+", pw : "+pw);

        RetrofitClient retrofitClient = RetrofitClient.getInstance(); // 요청 인터페이스, 빌드, 로그 한 데 모아둔 클래스
        HttpRequest httpRequest = retrofitClient.getRetrofitInterface(); // 에서 꺼내쓴다.


        // 요청 메소드 이름 : getRegister
        httpRequest.getRegister(email, pw).enqueue(new Callback<Response>() { // 파라미터 3개보내면서 요청하면 결과로 응답/실패 // 비동기
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {

                Response response1 = response.body();
                assert response1 != null;
                Log.e(TAG, "onResponse() / response1 : "+response1.getMessage());


                if (response1.isResponse()) { // insert 완료했다면

                    // 회원가입 성공
                    Intent intent = new Intent(join_Activity.this, homeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // 현재화면빼고 아래액티비티 지움
                    startActivity(intent);
                    Log.e(TAG, "startActivity() \n"+TAG+" -> homeActivity.class ");

                    Toast.makeText(getApplicationContext(), "로그인 성공", Toast.LENGTH_SHORT).show();

                    // 쉐어드에 자동로그인하도록 저장
                    shared_AutoLogin(email, pw, getApplicationContext());

                } else {
                    Toast.makeText(getApplicationContext(), response1.getMessage(), Toast.LENGTH_SHORT).show(); // '이미 가입된 이메일입니다.'
                }

            }

            @Override
            public void onFailure(Call<Response> call, Throwable t) {
                Log.e(TAG, "onFailure() "+t.getMessage());
            }
        });
    }


    public void shared_AutoLogin(String email, String pw, Context context) { Log.e(TAG, "shared_AutoLogin() email : "+email+", pw : "+pw);

        SharedPreferences auto = context.getSharedPreferences("autoLogin", Activity.MODE_PRIVATE); // error! context를 앞에 붙여줌
        SharedPreferences.Editor autoLoginEdit = auto.edit();

        autoLoginEdit.putString("userEmail", email); //db컬럼명과 동일하게 하자
        autoLoginEdit.putString("userPw", pw); //파라미터로 받은 pw

        autoLoginEdit.apply(); //실질 저장
    }



    public boolean patternPW(String PW) { //비밀번호 유효성 검사(정규식)
        Log.e(TAG, "patternPW() parameter (PW : "+PW+")");

        boolean patternPW;
        if(Pattern.matches("^(?=.*[a-zA-z])(?=.*[0-9]).{6,12}$", PW)) { //숫자1개이상, 문자1개이상, 6~12자리(입력자체를 막아두긴 했지만 정규식으로 한 번더 필터)
            patternPW = true;
        } else {
            patternPW = false;
        }

        return patternPW;
    }



    //인증확인 후 일치하면 변경되는 et, btn상태
    public void viewChangeAfterCheckingStringCode(EditText et_email, EditText et_emailCheck, Button btn_sendEmail, Button btn_emailCheck, Context context) { // (이메일 입력, 인증번호 입력, 인증번호보내기 버튼, 인증확인 버튼)

        et_email.setTextColor(Color.parseColor("#CCCFCC")); // 이메일 텍스트는 바꾸면 안 됨
        et_email.setFocusable(false); //이메일
        et_email.setClickable(false);
        et_email.setEnabled(false); //버튼 비활성화(클릭리스너 작동x)

        et_emailCheck.setText("인증 완료"); //입력한 코드대신 '인증완료'로 변경
        et_emailCheck.setTextColor(Color.parseColor("#CCCFCC")); //회색으로 변경(비활성화)
        et_emailCheck.setFocusable(false); //껌뻑껌뻑 포커스 비활성화
        et_emailCheck.setClickable(false); //클릭 비활성화
        et_emailCheck.setEnabled(false); //버튼 비활성화(클릭리스너 작동x)

        btn_sendEmail.setEnabled(false); //인증번호보내기 버튼
        btn_sendEmail.setText("전송\n완료");

        btn_emailCheck.setBackgroundColor(Color.parseColor("#FF9422")); // 주황색으로 변경
        btn_emailCheck.setText("인증\n완료"); //버튼 인증확인->인증완료
        btn_emailCheck.setEnabled(false);


        Toast.makeText(context, "인증완료", Toast.LENGTH_SHORT).show();

    }



    public void viewChangeAfterSendingEmail(Button btn_sendEmail, Button btn_EmailCheck, Context context) {

        btn_sendEmail.setText("다시\n보내기");
        btn_sendEmail.setBackgroundColor(Color.parseColor("#FF9422"));

        btn_EmailCheck.setEnabled(true); //이메일로 인증번호보내기 전까지는 인증확인버튼 비활성화 >> 보내고 바로 활성화

        Toast.makeText(context, "기입하신 이메일로 인증번호를 보냈습니다.", Toast.LENGTH_SHORT).show();

    }


    private void register_match(String email) { Log.e(TAG, "register_match() / email : "+email);

        RetrofitClient retrofitClient = RetrofitClient.getInstance(); // 요청 인터페이스, 빌드, 로그 한 데 모아둔 클래스
        HttpRequest httpRequest = retrofitClient.getRetrofitInterface(); // 에서 꺼내쓴다.


        // 요청 메소드 이름 : getRegister_match
        httpRequest.getRegister_match(email).enqueue(new Callback<Response>() { // 파라미터 3개보내면서 요청하면 결과로 응답/실패 // 비동기
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {


                Response response1 = response.body();
                assert response1 != null;
                Log.e(TAG, "onResponse() / response1 : "+response1.getMessage());


                if (response1.isResponse()) { // 중복없음. 진
                     sendEmailForGmail(binding.etJoinEmail.getText().toString(), createEmailCode(5), getApplicationContext(), binding.btnSendCodeToEmail, binding.btnCheckCode); // 입력한 이메일 내 Gmail로 보내기

                } else { // 중복됨. 진행X
                    Toast.makeText(getApplicationContext(), response1.getMessage(), Toast.LENGTH_SHORT).show(); // '이미 가입된 이메일입니다.'
                }

            }

            @Override
            public void onFailure(Call<Response> call, Throwable t) {
                Log.e(TAG, "onFailure() "+t.getMessage());
            }
        });
    }



    public void sendEmailForGmail(String receiverEmail, String stringCode, Context context, Button btn_sendEmail, Button btn_EmailCheck) { String TAG = "sendEmailForGmail()";
        // 회원가입 시 이메일 인증번호 보내기 (사용자가 입력한 이메일, 랜덤생성코드 5자리)
        Log.e(TAG, "sendEmailForGmail() parameter (receiverEmail : "+receiverEmail+", stringCode : "+stringCode+")");

        if (!receiverEmail.equals("")) {
            try {

                String senderEmail = "cocohan4919@gmail.com";
                String senderPw = "rzmffmkroxspoolh"; // 임시 키 받음 (+22.05부터 정책이 바껴서 2단계 인증안하면 안된다고 함. 구글 보안 설정이 없어짐)
                String host = "smtp.gmail.com";
//            receiverEmail = "rlagksdl96@naver.com"; // test용

                Properties properties = System.getProperties();
                properties.put("mail.smtp.host", host);
                properties.put("mail.smtp.port", "465");
                properties.put("mail.smtp.ssl.enable", "true");
                properties.put("mail.smtp.auth", "true");

                Session session = Session.getInstance(properties, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() { // 인증
                        return new PasswordAuthentication(senderEmail, senderPw);
                    }
                });

                MimeMessage mimeMessage = new MimeMessage(session);
                mimeMessage.addRecipients(Message.RecipientType.TO, String.valueOf(new InternetAddress(receiverEmail))); // 에러나면 여기 확인하기


                // 메일 작성
                mimeMessage.setSubject("[ myBook ]에서 보낸 인증번호입니다."); // 제목
                mimeMessage.setText("인증번호 : "+stringCode+
                        "\n 어플로 돌아가 인증번호를 입력해주세요."); // 내용


                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Transport.send(mimeMessage);
                        } catch (MessagingException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();


                // 뷰 변경
                viewChangeAfterSendingEmail(btn_sendEmail, btn_EmailCheck, context);


            } catch (MessagingException e) { e.printStackTrace(); }

        } else { // 이메일란이 빈 값일 경우
            Toast.makeText(context, "이메일을 다시 확인해 주세요.", Toast.LENGTH_SHORT).show();
        }


    }



    public String createEmailCode(int how_figure) { // 이메일 인증코드 생성 (자리수)
        Log.e(TAG, "createEmailCode() parameter (how_figure : "+how_figure+")");


        String[] str = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",
                "t", "u", "v", "w", "x", "y", "z", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        String newCode = new String();

        for (int x = 0; x < how_figure; x++) {
            int random = (int) (Math.random() * str.length);
            newCode += str[random];
        }

        return strCode = newCode;
    }




}