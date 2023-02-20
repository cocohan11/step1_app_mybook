package com.example.mybooks.home_bottom_1to5;

import static com.example.mybooks.retrofit.RetrofitAladin.ttbkey;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.mybooks.R;
import com.example.mybooks.databinding.ActivityMainBinding;
import com.example.mybooks.databinding.ActivityMyNoteBinding;
import com.example.mybooks.home.book_search;
import com.example.mybooks.model.AladinBook;
import com.example.mybooks.note.showNote;
import com.example.mybooks.recyclerview.rv_searchBookByAladin;
import com.example.mybooks.recyclerview.rv_showBooksForNote;
import com.example.mybooks.retrofit.HttpRequest;
import com.example.mybooks.retrofit.RetrofitAladin;
import com.example.mybooks.retrofit.RetrofitClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import java.util.ArrayList;
import java.util.Comparator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class myNoteActivity extends AppCompatActivity {
    /**
     * 노트 첫 화면
     */
    private final String TAG=this.getClass().getSimpleName();
    private ActivityMyNoteBinding binding;
    private rv_showBooksForNote rvAdapter;
    private ArrayList<AladinBook> noteBookArrayList = new ArrayList<>(); // 검색결과로 나온 책들
    private int radioIndex=0;
    private int radioIndexDone=0;
    private AlertDialog.Builder alertDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        alertDialog = new AlertDialog.Builder(myNoteActivity.this);




        // 노트 정렬 (최신/오래된/별점 낮은/별점 높은/노트 많은/노트 적은)
        binding.linearSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RadioClick();

            }
        });


    } //~onCreate()


    public void RadioClick() {
        final String[] words = new String[] {"최신순", "오래된순", "별점 낮은순", "별점 높은순", "노트 적은순", "노트 많은순"};
        alertDialog.setTitle("선택")
                    .setSingleChoiceItems(words, radioIndexDone, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {


                            radioIndex = selectRadio(words[which]);
                            Toast.makeText(getApplicationContext(), "words : " + words[which], Toast.LENGTH_SHORT).show();


                        }})
                    .setNeutralButton("closed",null)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // 어댑터 갱신
                            // 인덱스 넘기기
                            radioIndexDone = radioIndex; // 선택중변수와 선택완료변수 다르게 생성
                            setArraylistLineUp(radioIndexDone); // 변수변경
                            recyclerview_setAdapter(binding.rv, noteBookArrayList); // 뷰변경
                            Toast.makeText(getApplicationContext(), "조회", Toast.LENGTH_SHORT).show();

                        }
                    })
                    .show();
    }


    public int selectRadio(String words) {
        int index = 0;
        if (words.equals("최신순")) {
            index=0;
        } else if (words.equals("오래된순")) {
            index=1;
        } else if (words.equals("별점 낮은순")) {
            index=2;
        } else if (words.equals("별점 높은순")) {
            index=3;
        } else if (words.equals("노트 적은순")) {
            index=4;
        } else if (words.equals("노트 많은순")) {
            index=5;
        }
        Log.e(TAG, "index:"+index);
        return index;
    }


    public void setArraylistLineUp(int index) { Log.e(TAG, "index:"+index);
        switch (index) {
            case 0:
                noteBookArrayList.sort(Comparator.comparing(AladinBook::getDate).reversed());
                binding.tvSort.setText("최신순");
                break;

            case 1:
                noteBookArrayList.sort(Comparator.comparing(AladinBook::getDate));
                binding.tvSort.setText("오래된순");
                break;

            case 2:
                noteBookArrayList.sort(Comparator.comparing(AladinBook::getRating));
                binding.tvSort.setText("별점낮은순");
                break;

            case 3:
                noteBookArrayList.sort(Comparator.comparing(AladinBook::getRating).reversed());
                binding.tvSort.setText("별점높은순");
                break;

            case 4:
                noteBookArrayList.sort(Comparator.comparing(AladinBook::getCountNote));
                binding.tvSort.setText("노트적은순");
                break;

            case 5:
                noteBookArrayList.sort(Comparator.comparing(AladinBook::getCountNote).reversed());
                binding.tvSort.setText("노트많은순");
                break;
        }
        Log.e(TAG, "noteBookArrayList:"+noteBookArrayList);
    }


    private void setRvItems(String email, RecyclerView rv) { Log.e(TAG, "email : "+email);

        RetrofitClient retrofitClient = RetrofitClient.getInstance(); // 요청 인터페이스, 빌드, 로그 한 데 모아둔 클래스
        HttpRequest httpRequest = retrofitClient.getRetrofitInterface(); // 에서 꺼내쓴다.



        // 요청 메소드 이름 : getBooksForNote
        httpRequest.getBooksForNote(email).enqueue(new Callback<ArrayList<AladinBook>>() { // 파라미터 3개보내면서 요청하면 결과로 응답/실패 // 비동기
            @Override
            public void onResponse(Call<ArrayList<AladinBook>> call, retrofit2.Response<ArrayList<AladinBook>> response) {


                noteBookArrayList = response.body();
                assert noteBookArrayList != null;
                /*
                {
                    "title": "언어의 온도 (170만부 기념 에디션)",
                    "cover": "https:\/\/image.aladin.co.kr\/product\/25260\/11\/coversum\/k322633102_1.jpg",
                    "date": "0000-00-00 00:00:00",
                    "countNote": "5",
                    "rating": "5"
                },
                }*/

                recyclerview_setAdapter(rv, noteBookArrayList);

            }

            @Override
            public void onFailure(Call<ArrayList<AladinBook>> call, Throwable t) {
                Log.e(TAG, "onFailure() "+t.getMessage());
            }
        });


    }


    @SuppressLint("NotifyDataSetChanged")
    public void recyclerview_setAdapter(RecyclerView rv, ArrayList arrayList) {

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        rvAdapter = new rv_showBooksForNote(getApplicationContext(), arrayList); // rv 어댑터 객체 생성
        rv.setLayoutManager(gridLayoutManager);
        rv.setAdapter(rvAdapter);
        rvAdapter.notifyDataSetChanged();

    }


    @Override
    protected void onResume() { Log.e(TAG, "onResume()");
        super.onResume();
        setRvItems(
                getSharedPreferences("autoLogin", Activity.MODE_PRIVATE).getString("userEmail", null)
                ,binding.rv   // 서버에서 가져오기
        );
    }

    @Override
    protected void onStart() { Log.e(TAG, "onStart()");
        super.onStart();
    }

    @Override
    protected void onRestart() { Log.e(TAG, "onRestart()");
        super.onRestart();
    }


}