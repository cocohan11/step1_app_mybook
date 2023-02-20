package com.example.mybooks.home_bottom_1to5;

import static com.example.mybooks.socket.myService.isChatShow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.mybooks.databinding.ActivityMainBinding;
import com.example.mybooks.home.book_search;
import com.example.mybooks.home.map;
import com.example.mybooks.model.AladinBook;
import com.example.mybooks.recyclerview.rv_searchBookByAladin;
import com.example.mybooks.recyclerview.rv_showReadingBooks;
import com.example.mybooks.retrofit.HttpRequest;
import com.example.mybooks.retrofit.RetrofitClient;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;

public class homeActivity extends AppCompatActivity {
    /**
     * 홈...
     */
    private final String TAG=this.getClass().getSimpleName();
    private ActivityMainBinding binding;
    private long backKeyPressedTime = 0; //뒤로가기 2번 종료
    private rv_showReadingBooks rvAdapter;
    private ArrayList<AladinBook> isbnArrayList = new ArrayList<>(); // 전체 데이터


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        bottomNavigationBarClickListener(homeActivity.this, binding.homeBottom2Note, binding.homeBottom3Library, binding.homeBottom4Club, binding.homeBottom5MyProfile); // 하단메뉴5개 이벤트


        // 토스트 메세지
        Intent i = getIntent(); // 전부 "로그인 성공"임
        if (i.getStringExtra("toast") != null) {
            Toast.makeText(getApplicationContext(), i.getStringExtra("toast"), Toast.LENGTH_SHORT).show();
        }



        // 책 검색 클릭
        binding.tvSearchbook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(homeActivity.this, book_search.class); // 여기서 진짜 책 검색
                startActivity(intent);
                Log.e(TAG, "startActivity() \n"+TAG+" -> book_search.class ");

            }
        });


        // 지도로 쉽게 근처도서관 찾기
        binding.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(homeActivity.this, map.class); // 여기서 진짜 책 검색
                startActivity(intent);
                Log.e(TAG, "startActivity() \n"+TAG+" -> map.class ");

            }
        });





    } // ~onCreate()




    private void setMyBooks(String bookState) { // 서버에서 내가 추가한 책들 데이터 받아와서 UI에 업데이트


        RetrofitClient retrofitClient = RetrofitClient.getInstance();
        HttpRequest httpRequest = retrofitClient.getRetrofitInterface();


        // 요청 메소드 이름 : getMyLibrary
        httpRequest.getMyLibrary(new homeActivity().shared_AutoLogin(getApplicationContext()), bookState).enqueue(new Callback<ArrayList<AladinBook>>() { // 파라미터 3개보내면서 요청하면 결과로 응답/실패 // 비동기
            @Override
            public void onResponse(Call<ArrayList<AladinBook>> call, retrofit2.Response<ArrayList<AladinBook>> response) {

                isbnArrayList = response.body();
                assert isbnArrayList != null;
/*                {
                    "title": "2023 오형소 토탈 오엑스 - 전직렬 형사소송법 4개년 기출문제 진도별 총정리",
                    "isbn": "K362830940",
                    "author": "오제현 지음",
                    "cover": "https:\/\/image.aladin.co.kr\/product\/30697\/98\/coversum\/k362830940_1.jpg",
                    "bookState": "reading",
                    "startDate": "1671148800000",
                    "finishDate": null,
                    "rating": null
                }*/

                // only set 'the reading books' on rv
                recyclerview_setAdapter(binding.rvHome, isbnArrayList);

            }

            @Override
            public void onFailure(Call<ArrayList<AladinBook>> call, Throwable t) {
                Log.e(TAG, "onFailure() "+t.getMessage());
            }
        });
    }




    @SuppressLint("NotifyDataSetChanged")
    private void recyclerview_setAdapter(RecyclerView rv, ArrayList arrayList) { Log.e(TAG, "recyclerview_setAdapter()");

        rvAdapter = new rv_showReadingBooks(getApplicationContext(), arrayList); // rv 어댑터 객체 생성
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(layoutManager2);
        rv.setAdapter(rvAdapter);
        rvAdapter.notifyDataSetChanged();

    }


    // 메소드 한 번 만들어두고 이 패키지안에서 재사용하기
    protected void bottomNavigationBarClickListener(Context activity, LinearLayout menu2, LinearLayout menu3, LinearLayout menu4, LinearLayout menu5) {
        final String TAG=this.getClass().getSimpleName();
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
        // 모임 (메뉴4)
        menu4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(activity, clubActivity.class); // 이동
                startActivity(intent);
                Log.e(TAG, "startActivity() \n"+TAG+" -> clubActivity.class ");

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
    public void onBackPressed() {
//        super.onBackPressed(); //상속받지 말고 기존의 뒤로가기 버튼을 제거해줘야되는구나
        if (System.currentTimeMillis() > backKeyPressedTime + 1500) { //연타가 1.5초
            backKeyPressedTime = System.currentTimeMillis();
            Toast.makeText(getApplicationContext(), "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        // 1.5초 이내에 뒤로가기 버튼을 한번 더 클릭시 finish()(앱 종료)
        if (System.currentTimeMillis() <= backKeyPressedTime + 1500) {
            finish();
        }
    }



    // 쉐어드에 저장된 이멜 꺼내기
    public String shared_AutoLogin(Context context) {

        SharedPreferences auto = context.getSharedPreferences("autoLogin", Activity.MODE_PRIVATE); // error! context를 앞에 붙여줌
        return auto.getString("userEmail", null);
    }



    @Override
    protected void onResume() {
        super.onResume(); Log.e(TAG, "onResume");

        // 서버에서 내가 추가한 책들 데이터 받아와서 UI에 업데이트
        setMyBooks("reading"); // only reading books

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

