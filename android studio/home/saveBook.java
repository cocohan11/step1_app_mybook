package com.example.mybooks.home;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mybooks.R;
import com.example.mybooks.databinding.ActivitySaveBookBinding;
import com.example.mybooks.home_bottom_1to5.homeActivity;
import com.example.mybooks.home_bottom_1to5.myPageActivity;
import com.example.mybooks.retrofit.HttpRequest;
import com.example.mybooks.model.Response;
import com.example.mybooks.retrofit.RetrofitClient;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.willy.ratingbar.BaseRatingBar;

import java.util.ArrayList;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;

public class saveBook extends AppCompatActivity {
    /**
     * 서재에 책 추가하기 || 서재에 있는 책 수정삭제
     */
    private final String TAG=this.getClass().getSimpleName();
    private ActivitySaveBookBinding binding;
    private ArrayList<Long> dateList = new ArrayList<>(); // 독서기간) {시작일, 종료일}이 담긴 배열
    private @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // millisecond to date
    private String bookState; // (read/reading/hopeToRead) 중 1
    private float bookRating; // 5점만점, 0.5점씩+
    private MaterialDatePicker materialDatePicker1; // #1 읽은 책 (시작일, 종료일)
    private MaterialDatePicker materialDatePicker2; // #2 읽고 있는 책 (시작일)
    private CalendarConstraints.Builder calendarConstraintBuilder = new CalendarConstraints.Builder().setValidator(DateValidatorPointBackward.now()); // 캘린더 객체 // 미래날짜는 막기
    private long finishDay;
    private Intent i;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySaveBookBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        i = getIntent(); // i != null 이면 서재에서 옴 아니면 책 추가
        setItems(i); // 삽입 (제목, 표지)

        Log.e(TAG, "onCreate() isbn :"+i.getStringExtra("isbn"));



        // 어떤 책인가요?
        // #1 읽은 책
        binding.btnReadAlready.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                읽은책클릭_색변경();
            }
        });

        // #2 읽고 있는 책
        binding.btnReading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                읽고있는책클릭_색변경();
            }
        });

        // #3 읽고 싶은 책
        binding.btnLoveToRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                읽고싶은책클릭_색변경();
            }
        });




        // 독서 기간 선택 (달력)
        binding.readingDateRange.setOnClickListener(new View.OnClickListener() { // 시작, 종료일 선택
            @Override
            public void onClick(View v) { // 상태에 따라 다르게 보이기


                // #1 읽은 책
                if (bookState.equals("read")) { Log.e(TAG, "read");
                    materialDatePicker1.show(getSupportFragmentManager(), "dateRangePicker");
                    materialDatePicker1.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
                        @Override
                        public void onPositiveButtonClick(Object selection) { // 저장 버튼
                            Log.e(TAG, "onPositiveButtonClick() selection: " + selection);
//                            Log.e(TAG, "onPositiveButtonClick() materialDatePicker.getHeaderText() : " + materialDatePicker1.getHeaderText()); >> [12월 17일] 처럼 년도가 안 나오는 문제 발생
//                            <형태 변형 순서>
//                            string
//                            분리
//                            long
//                            date
//                            string
                            milliToDate(String.valueOf(selection));  // 시작일, 종료일 UI 업뎃

                        }
                    });


                // #2 읽고 있는 책
                } else if (bookState.equals("reading")) { Log.e(TAG, "reading");
                    materialDatePicker2.show(getSupportFragmentManager(), "datePicker");
                    materialDatePicker2.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
                        @Override
                        public void onPositiveButtonClick(Object selection) {
                            Log.e(TAG, "2 onPositiveButtonClick() selection: " + selection);

                            milliToDate2(String.valueOf(selection), i);  // 시작일, 종료일 UI 업뎃

                        }
                    });

                // 에러
                } else {
                    Log.e(TAG, "독서 기간 선택 (달력): 에러 ");
                }

            }
        });



        // 평점을 남겨주세요! (별점)
        binding.base.setOnRatingChangeListener(new BaseRatingBar.OnRatingChangeListener() {
            @Override
            public void onRatingChange(BaseRatingBar ratingBar, float rating, boolean fromUser) {
                Log.e(TAG, "별점 : " + rating + "점"); // 5점만점에 0.5점씩
                bookRating = rating;
            }
        });



        // 저장하기
        // (타이틀, 커버, 책 상태, 독서 기간(시작, 종료), 평점)
        binding.btnSaveBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { Log.e(TAG, "저장 클릭)"); Log.e(TAG, "button:"+i.getStringExtra("button"));

                // 종료일이 비어있는 경우 방지
                if (bookState.equals("read")) {
                    finishDay = dateList.get(1);
                } else { finishDay = 0; }
                Log.e(TAG, "finishDay : " + finishDay);


                if (i.getStringExtra("button").equals("edit")) { // 서재
                    Log.e(TAG, "수정완료 버튼 클릭) 서재)");
                    editThisBookToDB(
                            new homeActivity().shared_AutoLogin(getApplicationContext()), i.getStringExtra("isbn"),
                            bookState, dateList.get(0), finishDay, bookRating);

                } else if (i.getStringExtra("button").equals("add")) { // 새로 저장
                    Log.e(TAG, "새로 저장 클릭)");
                    saveThisBookToDB(
                            new homeActivity().shared_AutoLogin(getApplicationContext()), i.getStringExtra("title"),
                            i.getStringExtra("isbn"), i.getStringExtra("author"), i.getStringExtra("cover"),
                            bookState, dateList.get(0), finishDay, bookRating);
                }




            }
        });

    } // ~onCreate()


    // 처음 들어왔을 때 기본 셋팅값
    private void setBaseData() {
        dateList.add(MaterialDatePicker.thisMonthInUtcMilliseconds()); // 독서 시작일
        dateList.add(MaterialDatePicker.todayInUtcMilliseconds()); // 독서 종료일
        binding.tvStartDate.setText(longToStringDate(sdf, MaterialDatePicker.thisMonthInUtcMilliseconds()));
        binding.tvFinishDate.setText(longToStringDate(sdf, MaterialDatePicker.todayInUtcMilliseconds()));
        bookState = "read";
        bookRating = 3;
    }


    // DB에 책 저장 (서재에 담김)
    private void saveThisBookToDB(String email, String title, String isbn, String author, String cover, String bookState, Long startDate, Long finishDate, float rating) {
        Log.e(TAG, "saveThisBookToDB()" +
                        "\nemail : "+email+
                        "\ntitle : "+title+
                        "\nisbn : "+isbn+
                        "\nauthor : "+author+
                        "\ncover : "+cover+
                        "\nbookState : "+bookState+
                        "\nstartDate : "+startDate+
                        "\n(startDate : "+longToStringDate(sdf, startDate)+
                        "\nfinishDate : "+finishDate+
                        "\nrating : "+rating
                );


        RetrofitClient retrofitClient = RetrofitClient.getInstance(); // 요청 인터페이스, 빌드, 로그 한 데 모아둔 클래스
        HttpRequest httpRequest = retrofitClient.getRetrofitInterface(); // 에서 꺼내쓴다.


        // 요청 메소드 이름 : getSaveBook
        httpRequest.getSaveBook(email, title, isbn, author, cover, bookState, startDate, finishDate, rating).enqueue(new Callback<Response>() {
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {

                Response response1 = response.body();
                assert response1 != null;


                if (response1.isResponse()) {

                    // 서재에 책 담기 완료
                    finish();
                    Toast.makeText(getApplicationContext(), "서재에 추가되었습니다", Toast.LENGTH_SHORT).show();

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


    private void editThisBookToDB(String email, String isbn, String bookState, Long startDate, Long finishDate, float rating) {
        Log.e(TAG, "editThisBookToDB()" +
                "\nemail : "+email+
                "\nisbn : "+isbn+
                "\nbookState : "+bookState+
                "\n(startDate : "+longToStringDate(sdf, startDate)+
                "\n(finishDate : "+longToStringDate(sdf, finishDate)+
                "\nrating : "+rating
        );


        // DB에 올리기
        RetrofitClient retrofitClient = RetrofitClient.getInstance(); // 요청 인터페이스, 빌드, 로그 한 데 모아둔 클래스
        HttpRequest httpRequest = retrofitClient.getRetrofitInterface(); // 에서 꺼내쓴다.


        // 요청 메소드 이름 : getSaveBook
        httpRequest.getEditBook(email, isbn, bookState, startDate, finishDate, rating).enqueue(new Callback<Response>() {
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {

                Response response1 = response.body();
                assert response1 != null;

                if (response1.isResponse()) { // 서재에 책 담기 완료
//                    finish(); // 되돌아갔을 때 업데이트가 안 되서 startActivity() 사용


                    Intent intent = new Intent(saveBook.this, showBookDetail.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // [ABCDE]가 있고, E에서 C를 열면 상위 DE제거

                    // showBookDetail에 전달할 데이터
                    intent.putExtra("title",i.getStringExtra("title"));
                    intent.putExtra("isbn",i.getStringExtra("isbn"));
                    intent.putExtra("cover",i.getStringExtra("cover"));
                    intent.putExtra("author",i.getStringExtra("author"));
                    intent.putExtra("startDate", startDate);
                    intent.putExtra("finishDate", finishDate);
                    intent.putExtra("bookState", bookState);
                    intent.putExtra("rating", rating);

                    startActivity(intent);
                    Toast.makeText(getApplicationContext(), "저장되었습니다", Toast.LENGTH_SHORT).show();

                    Log.e(TAG, "startActivity() \n"+TAG+" -> homeActivity.class ");

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

    // 최종적으로 원하는 날짜값
    static public String longToStringDate(SimpleDateFormat sdf, Long milli) {

        Date timeInDate = new Date(milli); // 1671408000000
        return sdf.format(timeInDate); // 2022-12-01
    }


    // Object -> string -> long -> arraylist<Long>
    private void milliToDate(String dateInput) { Log.e(TAG, "dateInput :"+dateInput); // Pair{1669852800000 1671408000000}

        dateList.clear(); // 인덱스를 0,1로 고정한 코드를 작성해서 clear해줘야 쌓이지 않음


        // Pair{1669852800000 1671408000000} to 1669852800000 1671408000000
        int beginIndex = dateInput.indexOf("{");
        int endIndex  = dateInput.indexOf("}");
        String cut = dateInput.substring(beginIndex+1, endIndex);
        Log.e(TAG, "stringToArraylist_LongType() cut : "+cut);


        // 배열만들기
        String[] Input = cut.split(" ");

        for (String each : Input) {
            dateList.add(Long.parseLong(each)); // string to long
            Log.e(TAG, "독서시간333 dateList :"+ dateList);
        }


        Log.e(TAG, "달력1의 종료일 < 달력2의 시작일");
        Log.e(TAG, "dateList.get(0) 시작일:"+longToStringDate(sdf, dateList.get(0)));
        Log.e(TAG, "dateList.get(1) 종료일:"+longToStringDate(sdf, dateList.get(1)));

        // set UI
        binding.tvStartDate.setText(longToStringDate(sdf, dateList.get(0)));
        binding.tvFinishDate.setText(longToStringDate(sdf, dateList.get(1)));

        // 읽고 있는 책 달력도 변경
        // #2 읽고 있는 책 (시작일)
        materialDatePicker2 = MaterialDatePicker.Builder.datePicker()
                .setSelection(dateList.get(0)) // (당일)
                .setCalendarConstraints(calendarConstraintBuilder.build())
                .build();

    }


    private void milliToDate2 (String dateInput, Intent i) { Log.e(TAG, "milliToDate2() dateInput : "+dateInput);
        /*
         * 주의) 달력1, 달력2의 시작일과 종료일 날짜 어긋나지 않게 변경하기
         */
        dateList.clear(); // 인덱스를 0,1로 고정한 코드를 작성해서 clear해줘야 쌓이지 않음
        dateList.add(Long.parseLong(dateInput)); // 시작일
        dateList.add(Long.parseLong(dateInput)); // 종료일
        Log.e(TAG, "dateList.get(0) 시작일:"+longToStringDate(sdf, dateList.get(0)));
        Log.e(TAG, "dateList.get(1) 종료일:"+longToStringDate(sdf, dateList.get(1)));


        // 달력1 시작일, 종료일이 선택일로 변경
        materialDatePicker1 = MaterialDatePicker.Builder.dateRangePicker()
                .setSelection(Pair.create(dateList.get(0), dateList.get(1))) // 날짜 삽입(시작일, 종료일)
                .setCalendarConstraints(calendarConstraintBuilder.build())
                .build();
        materialDatePicker2 = MaterialDatePicker.Builder.datePicker()
                .setSelection(dateList.get(0)) // (선택일)
                .setCalendarConstraints(calendarConstraintBuilder.build())
                .build();


        // set UI
        binding.tvFinishDate.setText(longToStringDate(sdf, dateList.get(0)));
        binding.tvStartDate.setText(longToStringDate(sdf, dateList.get(1)));
        Log.e(TAG, "독서시간22 dateList :"+ dateList);
    }


    private void setItems(Intent i) {

        /*******************
         * set data to view
         ******************/

        if (i != null) {

            // set
            binding.tvBookTitle2.setText(i.getStringExtra("title"));
            Glide.with(getApplicationContext()).load(i.getStringExtra("cover")).into(binding.imgBookCover2); // 책 표지


            // 서재 - 수정이라면
            if (i.getStringExtra("button").equals("edit")) {

                Log.e(TAG, "서재)수정 // title :"+ i.getStringExtra("title"));
                Log.e(TAG, "startDate :"+ i.getLongExtra("startDate", 0));
                Log.e(TAG, "finishDate :"+ i.getLongExtra("finishDate", 0));
                Log.e(TAG, "bookState :"+ i.getStringExtra("bookState"));


                if (i.getStringExtra("bookState") != null) { // 어떤 책인가요? 버튼 선택
                    if (i.getStringExtra("bookState").equals("read")) { // 읽은 책
                        읽은책클릭_색변경();
                    } else if (i.getStringExtra("bookState").equals("reading")){
                        읽고있는책클릭_색변경();
                    } else {
                        읽고싶은책클릭_색변경();
                    }
                }


                // 독서시간 - 읽은책 달력 설정
                if (i.getLongExtra("finishDate", 0) != 0) { // 시작일ㅇ 종료일ㅇ
                    Log.e(TAG, "시작일ㅇ 종료일ㅇ");

                    // #1 읽은 책
                    materialDatePicker1 = MaterialDatePicker.Builder.dateRangePicker()
                            .setSelection(Pair.create(i.getLongExtra("startDate", 0), i.getLongExtra("finishDate", 0))) // 날짜 삽입(시작일, 종료일)
                            .setCalendarConstraints(calendarConstraintBuilder.build())
                            .build();

                    // 독서시간 - String날짜
                    Log.e(TAG, "Pair.create startDate :"+ i.getLongExtra("startDate", 0));
                    binding.tvStartDate.setText(longToStringDate(sdf, i.getLongExtra("startDate", 0))); // 2022-12-05
                    binding.tvFinishDate.setText(longToStringDate(sdf, i.getLongExtra("finishDate", 0)));


                } else if (i.getLongExtra("startDate", 0) == 0) {

                    setBaseData(); // UI, 데이터 set
                    Log.e(TAG, "등록되니 날짜가없다면");


                    // #1 읽은 책 (시작일, 종료일)
                    materialDatePicker1 = MaterialDatePicker.Builder.dateRangePicker()
                            .setSelection(Pair.create(MaterialDatePicker.thisMonthInUtcMilliseconds(), MaterialDatePicker.todayInUtcMilliseconds())) // (선택된 상태인 시작일, 종료일)
                            .setCalendarConstraints(calendarConstraintBuilder.build())
                            .build();


                    // #2 읽고 있는 책 (시작일)
                    materialDatePicker2 = MaterialDatePicker.Builder.datePicker()
                            .setSelection(MaterialDatePicker.todayInUtcMilliseconds()) // (당일)
                            .setCalendarConstraints(calendarConstraintBuilder.build())
                            .build();




                } else { // 시작일ㅇ 종료일x
                    Log.e(TAG, "시작일ㅇ 종료일x");

                    // #1 읽은 책
                    materialDatePicker1 = MaterialDatePicker.Builder.dateRangePicker()
                            .setSelection(Pair.create(i.getLongExtra("startDate", 0), i.getLongExtra("startDate", 0))) // 날짜 삽입(시작일, 종료일)
                            .setCalendarConstraints(calendarConstraintBuilder.build())
                            .build();

                    binding.tvStartDate.setText(longToStringDate(sdf, i.getLongExtra("startDate", 0)));
                    binding.tvFinishDate.setText(longToStringDate(sdf, i.getLongExtra("startDate", 0))); // 종료일 == 시작일
                }


                // #2 읽고 있는 책 (시작일)
                materialDatePicker2 = MaterialDatePicker.Builder.datePicker()
                        .setSelection(i.getLongExtra("startDate", 0)) // (당일)
                        .setCalendarConstraints(calendarConstraintBuilder.build())
                        .build();

                // 독서시간 - arraylist
                dateList.add(i.getLongExtra("startDate", 0)); // 독서 시작일
                dateList.add(i.getLongExtra("finishDate", 0)); // 독서 종료일
                Log.e(TAG, "..독서시간 dateList :"+ dateList);


                // 별점
                binding.base.setRating(i.getFloatExtra("rating",0));




            // 처음 데이터 셋팅이라면
            } else if (i.getStringExtra("button").equals("add")) {

                setBaseData(); // UI, 데이터 set
                Log.e(TAG, "처음 데이터 셋팅이라면");


                // #1 읽은 책 (시작일, 종료일)
                materialDatePicker1 = MaterialDatePicker.Builder.dateRangePicker()
                        .setSelection(Pair.create(MaterialDatePicker.thisMonthInUtcMilliseconds(), MaterialDatePicker.todayInUtcMilliseconds())) // (선택된 상태인 시작일, 종료일)
                        .setCalendarConstraints(calendarConstraintBuilder.build())
                        .build();


                // #2 읽고 있는 책 (시작일)
                materialDatePicker2 = MaterialDatePicker.Builder.datePicker()
                        .setSelection(MaterialDatePicker.todayInUtcMilliseconds()) // (당일)
                        .setCalendarConstraints(calendarConstraintBuilder.build())
                        .build();

            }
        }
    }



        private void 읽은책클릭_색변경() { Log.e(TAG, "읽은 책");

        // 색상 변경
        binding.btnReadAlready.setBackgroundResource(R.drawable.btn_round_orange); // 오렌지!
        binding.btnReading.setBackgroundResource(R.drawable.text_round_gray);
        binding.btnLoveToRead.setBackgroundResource(R.drawable.text_round_gray);

        // 보임/숨김 변경
        binding.tvSubTitleDate.setVisibility(View.VISIBLE); // 독서기간, 기간둘다, (종료일), 평점
        binding.readingDateRange.setVisibility(View.VISIBLE);
        binding.finishDate.setVisibility(View.VISIBLE);
        binding.bookScore.setVisibility(View.VISIBLE);

        // 스크롤 맨 아래
        binding.scrollView.postDelayed(new Runnable() { // << 이렇게 해야 작동됨 (스택오버플로우에서 봄)
            @Override
            public void run() {
                binding.scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        },100);

        // 어떤 책인가요? 3개 중 1선택
        bookState = "read";

    }

    private void 읽고있는책클릭_색변경() { Log.e(TAG, "읽고있는 책");
        binding.btnReadAlready.setBackgroundResource(R.drawable.text_round_gray);
        binding.btnReading.setBackgroundResource(R.drawable.btn_round_orange); // 오렌지!
        binding.btnLoveToRead.setBackgroundResource(R.drawable.text_round_gray);

        binding.finishDate.setVisibility(View.GONE);
        binding.bookScore.setVisibility(View.GONE);

        binding.tvSubTitleDate.setVisibility(View.VISIBLE);
        binding.readingDateRange.setVisibility(View.VISIBLE);

        binding.scrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        },100);


        // #1 읽은 책 (시작일, 종료일)
        materialDatePicker1 = MaterialDatePicker.Builder.dateRangePicker()
                .setSelection(Pair.create(MaterialDatePicker.thisMonthInUtcMilliseconds(), MaterialDatePicker.todayInUtcMilliseconds())) // (선택된 상태인 시작일, 종료일)
                .setCalendarConstraints(calendarConstraintBuilder.build())
                .build();


        // #2 읽고 있는 책 (시작일)
        materialDatePicker2 = MaterialDatePicker.Builder.datePicker()
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds()) // (당일)
                .setCalendarConstraints(calendarConstraintBuilder.build())
                .build();

        binding.tvStartDate.setText(longToStringDate(sdf, MaterialDatePicker.todayInUtcMilliseconds()));

        bookState = "reading";
        bookRating = 0;
    }

    private void 읽고싶은책클릭_색변경() { Log.e(TAG, "읽고싶은 책");
        binding.btnReadAlready.setBackgroundResource(R.drawable.text_round_gray);
        binding.btnReading.setBackgroundResource(R.drawable.text_round_gray);
        binding.btnLoveToRead.setBackgroundResource(R.drawable.btn_round_orange); // 오렌지!

        binding.tvSubTitleDate.setVisibility(View.GONE);
        binding.readingDateRange.setVisibility(View.GONE);
        binding.bookScore.setVisibility(View.GONE);

        binding.scrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        },100);

        bookState = "hopeToRead";
        bookRating = 0;
    }

}