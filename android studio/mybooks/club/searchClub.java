package com.example.mybooks.club;

import static com.example.mybooks.recyclerview.rv_showNotes.isMoreLoadingNote;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mybooks.databinding.ActivitySearchClubBinding;
import com.example.mybooks.model.Club;
import com.example.mybooks.recyclerview.rv_searchClubs;
import com.example.mybooks.recyclerview.rv_showNotes;
import com.example.mybooks.retrofit.HttpRequest;
import com.example.mybooks.retrofit.RetrofitClient;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;

public class searchClub extends AppCompatActivity implements rv_searchClubs.OnLoadMoreListener2 {
    /**
     * 모임 검색
     */
    private ActivitySearchClubBinding binding;
    private final String TAG = this.getClass().getSimpleName();
    private ArrayList<Club> clubs = new ArrayList<>(); // 초기값
    private ArrayList<Club> clubs_new = new ArrayList<>(); // 신규 모임 정보
    private ArrayList<Club> clubs_asc = new ArrayList<>(); // 시작가까운 모임 정보
    private rv_searchClubs rvAdapter_club1, rvAdapter_club2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchClubBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        /** data */
        setMyClubs("new", binding.rvSearchClub, rvAdapter_club1); // 신규모임
        setMyClubs("asc", binding.rvSearchClub2, rvAdapter_club2); // 빠른시작모임


        /** event */


    }


    @Override
    public void onLoadMore2(String purpose) {
        if (purpose.equals("new")) {
            addLoading(rvAdapter_club1, purpose, clubs_new);
        } else if (purpose.equals("asc")) {
            addLoading(rvAdapter_club2, purpose, clubs_asc);
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private void addLoading(rv_searchClubs rvAdapter, String purpose, ArrayList<Club> arrClubs) {
        arrClubs.add(null);
        rvAdapter.notifyDataSetChanged(); // 여기 위치해야 1초의 갭이 생겨서 로딩이 보임
        Log.e("isMoreLoadingNote 22:", String.valueOf(isMoreLoadingNote));
        Log.e(TAG, "onLoadMore2() arrClubs size:"+arrClubs.size());


        new Handler().postDelayed(new Runnable() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void run() {
                Log.e(TAG, "onLoadMore.remove() 전 " + arrClubs.size());
                arrClubs.remove(null);
                rvAdapter.notifyItemRemoved(arrClubs.size());
                Log.e(TAG, "onLoadMore.remove() 후 " + arrClubs.size());


                /*********** 다음 페이지를 불러오는 부분 **********/
                Log.e(TAG, "arrClubs.size()" + arrClubs.size());
                addPagingItems(purpose, arrClubs.size(), rvAdapter, arrClubs);
                /************************************************/


                isMoreLoadingNote = false;
                Log.e("isMoreLoadingNote 44:", String.valueOf(isMoreLoadingNote));
            }
        }, 1000);
    }



    private void addPagingItems(String purpose, int startIndex, rv_searchClubs rvAdapter, ArrayList<Club> arrClubs) { // 서버에서 내가 추가한 책들 데이터 받아와서 UI에 업데이트
        RetrofitClient retrofitClient = RetrofitClient.getInstance();
        HttpRequest httpRequest = retrofitClient.getRetrofitInterface();
        Log.e(TAG, "시작인덱스 : "+startIndex);

        // 요청 메소드 이름 : getClubsPaging
        httpRequest.getClubsPaging(purpose, startIndex).enqueue(new Callback<ArrayList<Club>>() { // 파라미터 3개보내면서 요청하면 결과로 응답/실패 // 비동기
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<ArrayList<Club>> call, retrofit2.Response<ArrayList<Club>> response) {

                ArrayList<Club> temClubs = response.body();
                Log.e(TAG, "temClubs 갯수: "+temClubs.size());
                Log.e(TAG, "temClubs : "+temClubs);
                assert arrClubs != null;
                arrClubs.addAll(temClubs);
                rvAdapter.notifyDataSetChanged(); // 데이터 변경 후 갱신
                Log.e(TAG, "clubs: "+arrClubs);


            }
            @Override
            public void onFailure(Call<ArrayList<Club>> call, Throwable t) {
                Log.e(TAG, "onFailure() "+t.getMessage());
            }
        });
    }




    private void setMyClubs(String purpose, RecyclerView rv, rv_searchClubs rvAdapter) { // 서버에서 내가 추가한 책들 데이터 받아와서 UI에 업데이트
        RetrofitClient retrofitClient = RetrofitClient.getInstance();
        HttpRequest httpRequest = retrofitClient.getRetrofitInterface();
        // 요청 메소드 이름 : getClubsPaging
        httpRequest.getClubsPaging(purpose, 0).enqueue(new Callback<ArrayList<Club>>() { // 파라미터 3개보내면서 요청하면 결과로 응답/실패 // 비동기
            @Override
            public void onResponse(Call<ArrayList<Club>> call, retrofit2.Response<ArrayList<Club>> response) {
                clubs = response.body();
                assert clubs != null;
                Log.e(TAG, "clubs 갯수:"+clubs.size());


                if (purpose.equals("new")) {
                    clubs_new = clubs;
                    rvAdapter_club1 = recyclerview_setAdapter(rv, clubs_new, purpose, rvAdapter);
                } else if (purpose.equals("asc")) {
                    clubs_asc = clubs;
                    rvAdapter_club2 = recyclerview_setAdapter(rv, clubs_asc, purpose, rvAdapter);
                }
            }
            @Override
            public void onFailure(Call<ArrayList<Club>> call, Throwable t) {
                Log.e(TAG, "onFailure() " + t.getMessage());
            }
        });
    }


    // rv 장착
    @SuppressLint("NotifyDataSetChanged")
    private rv_searchClubs recyclerview_setAdapter(RecyclerView rv, ArrayList<Club> list, String purpose, rv_searchClubs rvAdapter) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        rv.setLayoutManager(layoutManager);
        rvAdapter = new rv_searchClubs(getApplicationContext(), searchClub.this, list, purpose, this); // rv 어댑터 객체 생성
        rvAdapter.setLinearLayoutManager(layoutManager);
        rvAdapter.setRecyclerView(rv);
        rv.setAdapter(rvAdapter);
        Log.e(TAG, "club size() " + clubs.size());

        return rvAdapter;
    }






}