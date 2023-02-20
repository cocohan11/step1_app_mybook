package com.example.mybooks.home;

import static com.example.mybooks.retrofit.RetrofitAladin.ttbkey;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mybooks.databinding.ActivityShowBookDetailBinding;
import com.example.mybooks.home_bottom_1to5.homeActivity;
import com.example.mybooks.login.join_Activity;
import com.example.mybooks.model.AladinBook;
import com.example.mybooks.model.Response;
import com.example.mybooks.note.writeNote;
import com.example.mybooks.retrofit.HttpRequest;
import com.example.mybooks.retrofit.RetrofitAladin;
import com.example.mybooks.retrofit.RetrofitClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import retrofit2.Call;
import retrofit2.Callback;

public class showBookDetail extends AppCompatActivity {
    /**
     * 책 상세보기
     */
    private final String TAG=this.getClass().getSimpleName();
    private ActivityShowBookDetailBinding binding;
    private Intent i;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShowBookDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        // 책 추가 버튼 클릭
        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (i != null) {
                    Intent intent = new Intent(showBookDetail.this, saveBook.class); // 서재에 넣기위해 책 추가하러 가기
                    intent.putExtra("title",i.getStringExtra("title"));
                    intent.putExtra("isbn",i.getStringExtra("isbn"));
                    intent.putExtra("cover",i.getStringExtra("cover"));
                    intent.putExtra("author",i.getStringExtra("author"));

                    intent.putExtra("button","add");
                    startActivity(intent);

                    Log.e(TAG, "title :"+ i.getStringExtra("title"));
                    Log.e(TAG, "isbn :"+ i.getStringExtra("isbn"));
                    Log.e(TAG, "cover :"+ i.getStringExtra("cover"));
                    Log.e(TAG, "startActivity() \n"+TAG+" -> saveBook.class ");
                }
            }
        });


        // 책 수정 버튼 클릭
        binding.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (i != null) {
                    Intent intent = new Intent(showBookDetail.this, saveBook.class); // 서재에 넣기위해 책 추가하러 가기
                    intent.putExtra("title",i.getStringExtra("title"));
                    intent.putExtra("isbn",i.getStringExtra("isbn"));
                    intent.putExtra("cover",i.getStringExtra("cover"));
                    intent.putExtra("author",i.getStringExtra("author"));

                    // ++ 기간, 평점, 읽은 상태도 넘기기기
                    intent.putExtra("startDate", i.getLongExtra("startDate", 0));
                    intent.putExtra("finishDate", i.getLongExtra("finishDate", 0));
                    intent.putExtra("bookState",i.getStringExtra("bookState"));
                    intent.putExtra("rating",i.getFloatExtra("rating", 0)); // 212396421(o) / 2022-12-12(x)

                    intent.putExtra("button","edit");
                    startActivity(intent);

                    Log.e(TAG, "title :"+ i.getStringExtra("title"));
                    Log.e(TAG, "isbn :"+ i.getStringExtra("isbn"));
                    Log.e(TAG, "cover :"+ i.getStringExtra("cover"));
                    Log.e(TAG, "startDate :"+ i.getLongExtra("startDate", 0));
                    Log.e(TAG, "startActivity() \n"+TAG+" -> saveBook.class ");
                }
            }
        });


        // 책 삭제 버튼 클릭
        binding.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { Log.e(TAG, "삭제 클릭");

                dialog_delete(new homeActivity().shared_AutoLogin(getApplicationContext()), i.getStringExtra("isbn")); // 삭제 누름 -> retrofit으로 삭제 요청 -> 해당 책 서재에서 삭제

            }
        });


        // 노트 작성하기
        binding.floating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { Log.e(TAG, "노트 작성하기 클릭");

                startActivity(new Intent(showBookDetail.this, writeNote.class).putExtra("isbn", i.getStringExtra("isbn")));
                Log.e(TAG, "startActivity() \n"+TAG+" -> writeNote.class ");

            }
        });



    } // ~onCreate()




    private void dialog_delete(String email, String isbn) {

        Log.e(TAG, "\nemail :"+email+
                        "\nisbn :"+ isbn);


        // 삭제 다이얼로그
        AlertDialog.Builder msgBuilder = new AlertDialog.Builder(showBookDetail.this)
                .setTitle("책 삭제")
                .setMessage("서재에서 책을 삭제합니다.")
                .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {


                        RetrofitClient retrofitClient = RetrofitClient.getInstance(); // 요청 인터페이스, 빌드, 로그 한 데 모아둔 클래스
                        HttpRequest httpRequest = retrofitClient.getRetrofitInterface(); // 에서 꺼내쓴다.


                        // 요청 메소드 이름 : getSaveBook
                        httpRequest.getDeleteBook(email, isbn).enqueue(new Callback<Response>() {
                            @Override
                            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {

                                Response response1 = response.body();
                                assert response1 != null;

                                if (response1.isResponse()) {

                                    // 서재에서 책 삭제 완료
                                    finish();
                                    Toast.makeText(showBookDetail.this, "서재에서 삭제되었습니다", Toast.LENGTH_SHORT).show();

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
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Toast.makeText(showBookDetail.this, "안 끔", Toast.LENGTH_SHORT).show();

                    }
                });
        AlertDialog msgDlg = msgBuilder.create();
        msgDlg.show();

    }

    private void setItems(Intent i) {

        if (i != null) {
            binding.tvBookTitle.setText(i.getStringExtra("title"));
            binding.tvBookAuthor.setText(i.getStringExtra("author"));
            Glide.with(getApplicationContext()).load(i.getStringExtra("cover")).into(binding.imgBookCover); // 책 표지
            binding.tvBookDescription.setText(i.getStringExtra("description"));
            binding.tvBookPublisher.setText(i.getStringExtra("publisher"));


            // 서재에 책이 담겨있다면
            if (i.getStringExtra("bookState") != null ) {
                binding.btnSave.setVisibility(View.GONE);
                binding.tvBookState.setVisibility(View.VISIBLE); // 읽는중인지 보이게하기
                binding.btnEdit.setVisibility(View.VISIBLE);
                binding.btnDelete.setVisibility(View.VISIBLE);
                binding.frameFloatingBtn.setVisibility(View.VISIBLE); // 노트 작성하기 버튼 보이기

                // set 책읽는중인지 상태
                if (i.getStringExtra("bookState").equals("read")) {
                    binding.tvBookState.setText("읽은 책");

                } else if (i.getStringExtra("bookState").equals("reading")) {
                    binding.tvBookState.setText("읽고 있는 책");

                } else if (i.getStringExtra("bookState").equals("hopeToRead")) {
                    binding.tvBookState.setText("읽고 싶은 책");
                }

            }

            // 별점
            if (i.getFloatExtra("rating", 0) != 0) {

                binding.baseStar.setVisibility(View.VISIBLE); // 읽는중인지 보이게하기
                Log.e(TAG, "rating~~ 2: "+i.getFloatExtra("rating",0));
                binding.baseStar.setRating(i.getFloatExtra("rating",0));
            }
        }

        assert i != null;
        if (i.getStringExtra("isbn") != null) { // 책소개, 출판사.. 등까지는 db에 저장 안 하니까 retrofit으로 불러오기

            Log.e(TAG, "isbn:"+i.getStringExtra("isbn")); // 8999728005
            showDetail(i.getStringExtra("isbn")); // retrofit

        }
    }




    private void showDetail(String isbn) { Log.e(TAG, "showDetail() isbn : "+isbn);

        RetrofitAladin retrofitAladin = RetrofitAladin.getInstance(); // 요청 인터페이스, 빌드, 로그 한 데 모아둔 클래스
        HttpRequest httpRequest_aladin = retrofitAladin.getRetrofitInterface(); // 에서 꺼내쓴다.


        // 요청 메소드 이름 : getSearchBook
        httpRequest_aladin.getSearchBook(ttbkey, isbn, 1).enqueue(new Callback<String>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<String> call, retrofit2.Response<String> response) {

                Log.e(TAG, "onResponse: " + response.body());
                try {

                    JSONObject json = XML.toJSONObject(response.body()); // 태그형태를 {"" : ""} 이런 json형태로 바꿔줌
                    Log.e(TAG, "json: " + json.toString());
                    Log.e(TAG, "7777 json.getJSONObject(\"object\"): " + json.getJSONObject("object"));
                    Log.e(TAG, "7777 " + json.getJSONObject("object").getJSONObject("item").get("description"));

                    // 상세 설명
                    String description = String.valueOf(json.getJSONObject("object").getJSONObject("item").get("description")); // 필요한 부분만 추출출
                    String publisher = String.valueOf(json.getJSONObject("object").getJSONObject("item").get("publisher")); // 필요한 부분만 추출출

                    int index = description.indexOf("<br/>");
                    Log.e(TAG, "7777 publisher: " + publisher);
                    Log.e(TAG, "7777 index: " + index);
                    description = description.substring(index+5); // <br/> 제거
                    Log.e(TAG, "7777 description: " + description);


                    // 서재 책 (책 소개, 출판사)
                    if (description.equals("")) {
                        binding.tvBookDescription.setText("-");
                    } else {
                        binding.tvBookDescription.setText(description);
                    }
                    binding.tvBookPublisher.setText(publisher); // api로 불러와서 UI에 set함


                    // 별점
                    if (i.getFloatExtra("rating",0) != 0) {
                        Log.e(TAG, "rating~~99 VISIBLE: "+i.getFloatExtra("rating",0));
                        binding.baseStar.setVisibility(View.VISIBLE); // 읽는중인지 보이게하기
                        binding.baseStar.setRating(i.getFloatExtra("rating",0));
                    } else {
                        Log.e(TAG, "rating~~99 GONE");
                        binding.baseStar.setVisibility(View.GONE); // 읽는중인지 보이게하기
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });


    }



    @Override
    protected void onResume() {
        super.onResume(); Log.e(TAG, "onResume");

        i = getIntent();
        setItems(i); // 삽입 (제목, 표지, 지은이, 출판사)

    }

}