package com.example.mybooks.home_bottom_1to5;

import static com.example.mybooks.socket.myService.isChatShow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.mybooks.club.chatting;
import com.example.mybooks.club.createClub;
import com.example.mybooks.databinding.ActivityClubBinding;
import com.example.mybooks.model.ClientInfo;
import com.example.mybooks.model.Club;
import com.example.mybooks.model.Response;
import com.example.mybooks.recyclerview.rv_myClub;
import com.example.mybooks.retrofit.HttpRequest;
import com.example.mybooks.retrofit.RetrofitClient;
import com.example.mybooks.club.searchClub;

import java.util.ArrayList;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;

public class clubActivity extends AppCompatActivity {
    /**
     * '나의 독서 모임' 첫 화면
     */
    private ActivityClubBinding binding;
    private final String TAG=this.getClass().getSimpleName();
    private rv_myClub rvAdapter_club;
    private ArrayList<Club> clubs = new ArrayList<>(); // 내가 참여한 모임들 정보
    private ClientInfo client;
    private Handler handler = new Handler();
    private ResultReceiver resultReceiver = new ResultReceiver(handler){
        /**
         * 서비스에서 데이터를 받는 통로
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            Log.e(TAG, "a onReceiveResult() resultData:"+resultData);
            client = new chatting().returnClientForBundle((resultData));
            Log.e(TAG, "clubs.size() : "+clubs.size());
            Log.e(TAG, "client : "+client);


            // 받아오는게 확인되면
            // rv의 저 item을 편집하기
            findClubNumAndSetMsgPreview(client);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) { Log.e(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        binding = ActivityClubBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        /** data */
        new chatting().sendService(resultReceiver, getApplicationContext(), "목록", getIntent().getStringExtra("id"),getIntent().getStringExtra("chat")); // 입장
        Objects.requireNonNull(binding.rvMyClub.getItemAnimator()).setChangeDuration(0);



        /** 리스너 */
        btn_createClub(binding.btnCreateClub); // + 버튼
        bottomNavigationBarClickListener(clubActivity.this, binding.homeBottom1Home, binding.homeBottom2Note, binding.homeBottom3Library, binding.homeBottom5MyProfile); // 하단메뉴5개 이벤트
        btn_moveToSearchClubs(binding.btnSearchClubs); // 돋보기 버튼


    } // ~onCreate()


    private void findClubNumAndSetMsgPreview(ClientInfo client) {
        Log.e(TAG, "findClubNumAndSetMsgPreview() client:"+client);

//        for (전체) {
//            id가 54인 club을 찾으면
//                    미리보기란에 대입한다.
//        }
//        clubs.set()
        if (client.getPurpose().equals("생성")) {
            setMyClubs(getSharedPreferences("autoLogin", Activity.MODE_PRIVATE).getString("userEmail", null));
            Log.e(TAG, "찾았다 생성");
        }

        for (int i=0; i<clubs.size(); i++) {
            if (clubs.get(i).getId().equals(client.getClubNum())){
                Log.e(TAG, "찾았다 요놈");
                clubs.get(i).setIntroduction(client.getChat()); // 서비스에서 받아온 채팅보이기
                rvAdapter_club.notifyItemChanged(i);
                break;
            }
        }

    }



    private void btn_moveToSearchClubs(ImageView img) {
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(clubActivity.this, searchClub.class));
                Log.e(TAG, "startActivity() \n"+TAG+" -> searchClub.class ");
            }
        });
    }



    private void setMyClubs(String email) { // 서버에서 내가 추가한 책들 데이터 받아와서 UI에 업데이트
        RetrofitClient retrofitClient = RetrofitClient.getInstance();
        HttpRequest httpRequest = retrofitClient.getRetrofitInterface();
        // 요청 메소드 이름 : getMyClubs
        httpRequest.getMyClubs(email).enqueue(new Callback<ArrayList<Club>>() { // 파라미터 3개보내면서 요청하면 결과로 응답/실패 // 비동기
            @Override
            public void onResponse(Call<ArrayList<Club>> call, retrofit2.Response<ArrayList<Club>> response) {
                assert response.body() != null;


                clubs = response.body();
                Log.e(TAG, "setMyClubs() clubs: "+clubs);
                assert clubs != null;
                recyclerview_setAdapter(binding.rvMyClub, clubs);
            }
            @Override
            public void onFailure(Call<ArrayList<Club>> call, Throwable t) {
                Log.e(TAG, "onFailure() "+t.getMessage());
            }
        });
    }


    // rv 장착
    @SuppressLint("NotifyDataSetChanged")
    private void recyclerview_setAdapter(RecyclerView rv, ArrayList<Club> list) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        rvAdapter_club = new rv_myClub(getApplicationContext(), clubActivity.this, list); // rv 어댑터 객체 생성
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(rvAdapter_club);
    }




    private void btn_createClub(ImageView img) {
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clubs.size() < 3) {
                    startActivity(new Intent(clubActivity.this, createClub.class));
                    Log.e(TAG, "startActivity() \n"+TAG+" -> createClub.class ");
                } else {
                    Toast.makeText(getApplicationContext(), "최대 3개까지 모임에 참여할 수 있습니다.", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }



    // 메소드 한 번 만들어두고 이 패키지안에서 재사용하기
    protected void bottomNavigationBarClickListener(Context activity, LinearLayout menu1, LinearLayout menu2, LinearLayout menu3, LinearLayout menu5) {
        final String TAG=this.getClass().getSimpleName();
        // 내 노트 (메뉴2)
        menu1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(activity, homeActivity.class); // 이동
                startActivity(intent);
                Log.e(TAG, "startActivity() \n"+TAG+" -> myNoteActivity.class ");

            }
        });
        // 내 노트 (메뉴2)
        menu2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(activity, myNoteActivity.class); // 이동
                startActivity(intent);
                Log.e(TAG, "startActivity() \n"+TAG+" -> myNoteActivity.class ");

            }
        });
        // 내 서재 (메뉴3)
        menu3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(activity, myLibrary.class); // 이동
                startActivity(intent);
                Log.e(TAG, "startActivity() \n"+TAG+" -> myLibrary.class ");

            }
        });
        // 내 정보 (메뉴5)
        menu5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(activity, myPageActivity.class); // 이동
                startActivity(intent);
                Log.e(TAG, "startActivity() \n"+TAG+" -> myPageActivity.class ");

            }
        });

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
    protected void onResume() { Log.e(TAG, "onResume()");
        super.onResume();
        if (getIntent().getStringExtra("purpose") != null) {
            if (getIntent().getStringExtra("purpose").equals("생성")) {
                new chatting().sendService(resultReceiver, getApplicationContext(), "생성", getIntent().getStringExtra("id"),getIntent().getStringExtra("chat")); // 입장
                Log.e(TAG, "onResume() 생성");

            }else {
                new chatting().sendService(resultReceiver, getApplicationContext(), "목록", getIntent().getStringExtra("id"),getIntent().getStringExtra("chat")); // 입장
                Objects.requireNonNull(binding.rvMyClub.getItemAnimator()).setChangeDuration(0);
                Log.e(TAG, "onResume() 목록");
            }
        }


        setMyClubs(getSharedPreferences("autoLogin", Activity.MODE_PRIVATE).getString("userEmail", null)); // finish()로 돌아올 것을 염두하기
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                    isChatShow = true; // 위치 : 다른 액티비티가 파괴되기도 전에 true해버리면 의미가 없어서 시간차를 둠
                    Log.e(TAG, "isChatShow:"+isChatShow);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    @Override
    protected void onPause() { Log.e(TAG, "onPause()");
        super.onPause();
    }
    @Override
    protected void onStop() { Log.e(TAG, "onStop()");
        super.onStop();
        isChatShow = false;
        Log.e(TAG, "isChatShow:"+isChatShow);
    }
    @Override
    public void onDestroy(){ Log.e(TAG, "onDestroy() ");
        super.onDestroy();
    }
}