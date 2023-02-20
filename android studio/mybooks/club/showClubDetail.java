package com.example.mybooks.club;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.example.mybooks.home.saveBook.longToStringDate;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mybooks.R;
import com.example.mybooks.databinding.ActivityShowClubDetailBinding;
import com.example.mybooks.home.saveBook;
import com.example.mybooks.home.showBookDetail;
import com.example.mybooks.model.Club;
import com.example.mybooks.model.Response;
import com.example.mybooks.retrofit.HttpRequest;
import com.example.mybooks.retrofit.RetrofitClient;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;

public class showClubDetail extends AppCompatActivity {
    /**
     * 모임 상세조회
     */
    private ActivityShowClubDetailBinding binding;
    private final String TAG=this.getClass().getSimpleName();
    SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd"); // millisecond to date


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShowClubDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        /** data */
        Club club = (Club) getIntent().getSerializableExtra("club");
        setDataOfTheClub(club); // set data



        /** 리스너 */
        btnClick_join(binding.btnJoinClub, getSharedPreferences("autoLogin", Activity.MODE_PRIVATE).getString("userEmail", null)
                , club.getId()
                , club.getName()
        );

    }


    private void btnClick_join(Button btn, String email, String id, String name) {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { Log.e(TAG, "가입하기 클릭");
                RetrofitClient retrofitClient = RetrofitClient.getInstance(); // 요청 인터페이스, 빌드, 로그 한 데 모아둔 클래스
                HttpRequest httpRequest = retrofitClient.getRetrofitInterface(); // 에서 꺼내쓴다.
                // retrofit
                // id로 클럽 가입 (insert)
                // para(id, email)
                // 요청 메소드 이름 : getJoinClub
                httpRequest.getJoinClub(email, id)
                        .enqueue(new Callback<Response>() {
                            @Override
                            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                                Response response1 = response.body();
                                assert response1 != null;
                                Log.e(TAG, "response1() "+response1);
                                if (response1.isResponse()) {


                                    Toast.makeText(getApplicationContext(), "가입되었습니다.", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(showClubDetail.this, chatting.class);
                                    intent.putExtra("purpose", "가입");
                                    intent.putExtra("id", id);
                                    intent.putExtra("name", name);
//                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); // 현재화면빼고 아래액티비티 지움
//                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // 현재화면빼고 아래액티비티 지움
//                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // [ABCDE]가 있고, E에서 C를 열면 상위 DE제거
                                    Log.e(TAG, "startActivity() \n"+TAG+" -> showBookDetail.class ");
                                    startActivity(intent);
                                }
                            }
                            @Override
                            public void onFailure(Call<Response> call, Throwable t) {
                                Log.e(TAG, "onFailure() "+t.getMessage());
                            }
                        });
            }
        });
    }


    // rv_searchClubs -> this
    // rv item --(intent)--> this
    private void setDataOfTheClub(Club club) {
        if (club !=null) {
            Log.e(TAG, "club:"+club);
            String[] themeIndexList = club.getTheme().split(",");
            Log.e("themeIndexList.length:", String.valueOf(themeIndexList.length));
            if (themeIndexList.length == 1) {
                int a1 = Integer.parseInt(themeIndexList[0]);
                binding.tvTheme1.setText(club.getThemeList()[a1]);
            } else if (themeIndexList.length == 2) {
                int a1 = Integer.parseInt(themeIndexList[0]);
                int a2 = Integer.parseInt(themeIndexList[1]);
                binding.tvTheme1.setText(club.getThemeList()[a1]);
                binding.tvTheme2.setText(club.getThemeList()[a2]);
            }
            String date = longToStringDate(sdf, club.getStart_date())+" ~ "+longToStringDate(sdf, club.getFinish_date());
            String turnout = club.getTurnout()+" / "+club.getFixed_num();
            Log.e("date:", date);
            Log.e("turnout:", turnout);



            Glide.with(getApplicationContext()).load(club.getImageUrl()).into(binding.imgCover); // 책 표지-바탕
            Glide.with(getApplicationContext()).load(club.getImageUrl()).into(binding.imgCoverClub); // 책 표지-읽을책
            Glide.with(getApplicationContext()).load(club.getMaster_img()).circleCrop().into(binding.imgMasterImg); // 모임장
            binding.tvName.setText(club.getName());
            binding.tvTitleClub.setText(club.getBookTitle());
            binding.tvDateTermClub.setText(date);
            binding.tvTurnoutClub.setText(turnout);
            binding.tvIntroClub.setText(club.getIntroduction());
            binding.tvMasterName.setText(club.getMaster_name());
        }
    }




}