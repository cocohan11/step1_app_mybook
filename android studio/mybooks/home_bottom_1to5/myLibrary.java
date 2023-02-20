package com.example.mybooks.home_bottom_1to5;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mybooks.R;
import com.example.mybooks.databinding.ActivityMyLibraryBinding;
import com.example.mybooks.library.searchInMyLibrary;
import com.example.mybooks.model.AladinBook;
import com.example.mybooks.model.Response;
import com.example.mybooks.myPage.myPageEditActivity;
import com.example.mybooks.recyclerview.rv_searchBookByAladin;
import com.example.mybooks.retrofit.HttpRequest;
import com.example.mybooks.retrofit.RetrofitClient;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;

public class myLibrary extends AppCompatActivity implements rv_searchBookByAladin.OnLoadMoreListener {
    /**
     * 내 서재에서 담은 책 조회하기
     */
    private final String TAG=this.getClass().getSimpleName();
    private ActivityMyLibraryBinding binding;
    private rv_searchBookByAladin rvAdapter;
    private ArrayList<AladinBook> isbnArrayList = new ArrayList<>(); // 전체 데이터
    private ArrayList<AladinBook> isbnArrayListChange = new ArrayList<>(); // 추출해낸 데이터들
    private String clubBook;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyLibraryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        clubBook = getIntent().getStringExtra("clubBook");
        Log.e(TAG, "clubBook:"+clubBook);



/*
        // 책 검색 이벤트 (검색버튼 / 입력한 글자)
       binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                Toast.makeText(getApplicationContext(), "[검색버튼클릭] 검색어 = "+query, Toast.LENGTH_LONG).show();
                keyboardHide();
                setIsbnArrayListChange(clickSearch(query)); // 다른 arraylist에 대입하여
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }

        });


        // 서치뷰  닫기 버튼 X 모양 버튼 클릭시 이벤트
        binding.searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                Toast.makeText(getApplicationContext(), "닫기", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "닫기");
                binding.searchView.onActionViewCollapsed();
                return true;
            }
        });
*/


        // 책 검색 - 돋보기 이미지
        binding.btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(myLibrary.this, searchInMyLibrary.class);
                intent.putExtra("isbnArrayList", isbnArrayList);
                startActivity(intent);
                Log.e(TAG, "startActivity() \n"+TAG+" -> searchInMyLibrary.class ");

            }
        });


        // 읽은 상태별 보기
        // #1 전체
        binding.bookStateAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                recyclerview_setAdapter(binding.rv, isbnArrayList); // isbnArrayListChange -> isbnArrayList
                setUI_backgroundColor_selectOne(binding.bookStateAll);

            }
        });
        // #2 읽은 책
        binding.bookStateRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                clickSearch("read"); // bookState가 read인 것을 찾아낸다
                recyclerview_setAdapter(binding.rv, isbnArrayListChange); // isbnArrayListChange -> isbnArrayList
                setUI_backgroundColor_selectOne(binding.bookStateRead);

            }
        });
        // #3 읽고있는 책
        binding.bookStateReading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                clickSearch("reading"); // bookState가 read인 것을 찾아낸다
                recyclerview_setAdapter(binding.rv, isbnArrayListChange); // isbnArrayListChange -> isbnArrayList]
                setUI_backgroundColor_selectOne(binding.bookStateReading);

            }
        });
        // #4 읽고싶은 책
        binding.bookStateHopeToRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                clickSearch("hopeToRead"); // bookState가 read인 것을 찾아낸다
                recyclerview_setAdapter(binding.rv, isbnArrayListChange); // isbnArrayListChange -> isbnArrayList]
                setUI_backgroundColor_selectOne(binding.bookStateHopeToRead);

            }
        });


    } // ~onCreate()



    // rv dialog ok클릭시에 finish
    private void rvItem_click_returnBookInfo(rv_searchBookByAladin adapter) {
        adapter.setWhenClickListener(new rv_searchBookByAladin.OnItemsClickListener() {
            @Override
            public void onItemClickReturnBookInfo(String cover, String title, String isbn) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("cover",cover);
                resultIntent.putExtra("title",title);
                resultIntent.putExtra("isbn",isbn);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }


    private void clickSearch(String bookState) { Log.e(TAG, "bookState:"+bookState);

        isbnArrayListChange.clear(); // 초기화

        int index = 0;
        for (AladinBook aladinBook : isbnArrayList) {
            if (aladinBook.getBookState().equals(bookState)) {
                isbnArrayListChange.add(isbnArrayList.get(index));
            }
            index ++;
        }

        Log.e(TAG, "isbnArrayListChange : "+isbnArrayListChange);
    }




    @SuppressLint("UseCompatLoadingForDrawables")
    private void setUI_backgroundColor_selectOne(TextView tv) { // 선택한 하나만 색상주기

        binding.bookStateAll.setBackgroundResource(0);
        binding.bookStateRead.setBackgroundResource(0);
        binding.bookStateReading.setBackgroundResource(0);
        binding.bookStateHopeToRead.setBackgroundResource(0);

        tv.setBackgroundDrawable(getResources().getDrawable(R.drawable.stroke_top_orange));

    }


    private void setUI_backgroundColor() { // 전체빼고 전부 다 주황색상 없애기

        binding.bookStateAll.setBackgroundResource(R.drawable.stroke_top_orange);

        binding.bookStateRead.setBackgroundResource(0);
        binding.bookStateReading.setBackgroundResource(0);
        binding.bookStateHopeToRead.setBackgroundResource(0);

    }



    private void setMyBooks() { // 서버에서 내가 추가한 책들 데이터 받아와서 UI에 업데이트


        RetrofitClient retrofitClient = RetrofitClient.getInstance();
        HttpRequest httpRequest = retrofitClient.getRetrofitInterface();


        // 요청 메소드 이름 : getMyLibrary
        httpRequest.getMyLibrary(new homeActivity().shared_AutoLogin(getApplicationContext()), "all").enqueue(new Callback<ArrayList<AladinBook>>() { // 파라미터 3개보내면서 요청하면 결과로 응답/실패 // 비동기
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
//                Log.e(TAG, "isbnArrayList.get(0).getIsbn() : "+isbnArrayList.get(0).getIsbn()); // K842830844


                // set 리사이클러뷰 by arraylist
                //
                //
                // 우선 다 보여주기
                recyclerview_setAdapter(binding.rv, isbnArrayList); // (rv, 1권에 대한 arraylist)

            }

            @Override
            public void onFailure(Call<ArrayList<AladinBook>> call, Throwable t) {
                Log.e(TAG, "onFailure() "+t.getMessage());
            }
        });
    }


    //스크롤이 끝에 도달하였을 때 실행 내용
    @Override
    public void onLoadMore() {
        Log.e("MainActivity_", "onLoadMore");
//        rvAdapter.setProgressMore(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
            }
        }, 1000);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void recyclerview_setAdapter(RecyclerView rv, ArrayList arrayList) { Log.e(TAG, "recyclerview_setAdapter()");

        rvAdapter = new rv_searchBookByAladin(getApplicationContext(), arrayList, (rv_searchBookByAladin.OnLoadMoreListener) this, clubBook, myLibrary.this); // rv 어댑터 객체 생성
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(layoutManager2);
        rv.setAdapter(rvAdapter);
        rvAdapter.notifyDataSetChanged();


        rvItem_click_returnBookInfo(rvAdapter);

    }


    @Override
    protected void onResume() {
        super.onResume(); Log.e(TAG, "onResume");

        // 서버에서 내가 추가한 책들 데이터 받아와서 UI에 업데이트
        setMyBooks();
        setUI_backgroundColor();

    }


}