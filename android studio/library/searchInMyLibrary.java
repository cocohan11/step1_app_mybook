package com.example.mybooks.library;

import androidx.appcompat.app.AppCompatActivity;
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
import android.widget.Toast;

import com.example.mybooks.databinding.ActivityMyLibraryBinding;
import com.example.mybooks.databinding.ActivitySearchInMyLibraryBinding;
import com.example.mybooks.model.AladinBook;
import com.example.mybooks.recyclerview.rv_searchBookByAladin;

import java.util.ArrayList;

public class searchInMyLibrary extends AppCompatActivity implements rv_searchBookByAladin.OnLoadMoreListener {
    /**
     * 내 서재에서 검색하기
     */
    private final String TAG=this.getClass().getSimpleName();
    private ActivitySearchInMyLibraryBinding binding;
    private rv_searchBookByAladin rvAdapter;
    private ArrayList<AladinBook> isbnArrayList = new ArrayList<>(); // 전체 데이터
    private ArrayList<AladinBook> isbnArrayListChange = new ArrayList<>(); // 추출해낸 데이터들



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchInMyLibraryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        isbnArrayList = (ArrayList<AladinBook>)getIntent().getSerializableExtra("isbnArrayList"); // intent로 받아온 arraylist from myLibrary.class
        Log.e(TAG, "isbnArrayList :"+isbnArrayList);
//        recyclerview_setAdapter(binding.rv, list); // (rv, 1권에 대한 arraylist)


        // 책 검색 이벤트
        binding.btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(getApplicationContext(), "[검색버튼클릭] 검색어 = "+binding.etSearch.getText().toString(), Toast.LENGTH_LONG).show();
                setIsbnArrayListChange(clickSearch(binding.etSearch.getText().toString())); // 다른 arraylist에 대입하여
                keyboardHide();

            }
        });


        // 서치뷰 닫기 버튼 X 모양 버튼 클릭시 이벤트
        binding.btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(getApplicationContext(), "닫기", Toast.LENGTH_SHORT).show();
                isbnArrayListChange.clear();
                recyclerview_setAdapter(binding.rv, isbnArrayListChange); // isbnArrayListChange -> isbnArrayList
                binding.etSearch.setText("");

            }
        });

    } // ~onCreate()



    private void setIsbnArrayListChange(ArrayList<Integer> index) { // 검색 UI 업뎃

        isbnArrayListChange.clear(); // 초기화

        for (int i : index) {
            isbnArrayListChange.add(isbnArrayList.get(i));
            Log.e(TAG, "isbnArrayListChange :"+isbnArrayListChange);
        }

        recyclerview_setAdapter(binding.rv, isbnArrayListChange);
    }


    private void keyboardHide() { // 서재 목록을 가려서.
        View view = getCurrentFocus();

        if (view != null) {
            Log.e(TAG, "포커스+키보드 내림");
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private void recyclerview_setAdapter(RecyclerView rv, ArrayList arrayList) { Log.e(TAG, "recyclerview_setAdapter()");

        rvAdapter = new rv_searchBookByAladin(getApplicationContext(), arrayList, this, "ㄴ", searchInMyLibrary.this); // rv 어댑터 객체 생성
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(layoutManager2);
        rv.setAdapter(rvAdapter);
        rvAdapter.notifyDataSetChanged();
    }



    private ArrayList<Integer> clickSearch(String keyword) { Log.e(TAG, "keyword : "+keyword);

        int index = 0;
        ArrayList<Integer> integers = new ArrayList<>();


        for (AladinBook aladinBook : isbnArrayList) {
            if (aladinBook.getTitle().contains(keyword)) {
                integers.add(index);
            }
            index ++;
        }

        Log.e(TAG, "integers : "+integers);
        return integers;
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



}