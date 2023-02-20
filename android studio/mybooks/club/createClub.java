package com.example.mybooks.club;

import static com.example.mybooks.home.saveBook.longToStringDate;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mybooks.R;
import com.example.mybooks.databinding.ActivityCreateClubBinding;
import com.example.mybooks.home.book_search;
import com.example.mybooks.home.showBookDetail;
import com.example.mybooks.home_bottom_1to5.clubActivity;
import com.example.mybooks.home_bottom_1to5.myLibrary;
import com.example.mybooks.model.Note;
import com.example.mybooks.model.Response;
import com.example.mybooks.note.writeNote;
import com.example.mybooks.recyclerview.rv_showBooksForNote;
import com.example.mybooks.recyclerview.rv_term;
import com.example.mybooks.recyclerview.rv_theme;
import com.example.mybooks.retrofit.HttpRequest;
import com.example.mybooks.retrofit.RetrofitClient;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.volobot.stringchooser.StringChooser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.Field;

public class createClub extends AppCompatActivity {
    /**
     * 모임 생성하기
     */
    private ActivityCreateClubBinding binding;
    private final String TAG=this.getClass().getSimpleName();
    private rv_term rvAdapter_term; // 기간
    private rv_theme rvAdapter_theme; // 모임 주제 설정(최대 2개)
    private List<String> week = new ArrayList<String>(); // 기간
    private String str_selectedItemIndexes; // 모임 주제 설정(최대 2개까지만 선택된 인덱스가 들어감)
//    private List<Integer> selectedItemIndexes = new ArrayList<>(); // 모임 주제 설정(최대 2개까지만 선택된 인덱스가 들어감)
    private String[] theme; // 주제
    private MaterialDatePicker materialDatePicker; // 날짜선택 달력
    private @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // millisecond to date
    private Long long_startDate, long_finishDate; // 시작일
    private String selectedAges, isbn, cover, title; // 선호 연령대
    private int fixed_num;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateClubBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        /** set data, view.. **/
        addItemsToList(); // add rv data to week, theme
        recyclerview_setAdapter(binding.rvTerm, week, binding.rvTheme, theme); // 어댑터 장착
        materialDatePicker = buildDatePicker(materialDatePicker); // 달력 build
        setDateToDate(binding.tvStartDate, binding.tvFinishDate); // 날짜 초기 셋팅



        /** 리스너 **/
        et_touchListener_returnScrollTrue(binding.etIntroduction); // '모임소개' 스크롤 속성주기
        layout_click_getStartDate(binding.linearDate, binding.tvStartDate, binding.tvFinishDate, 1); // 달력에서 시작일 선택
        rvItem_click_changeFinishDate(rvAdapter_term, binding.tvFinishDate); // 기간(rv item) click시에 종료일 변경
        showNumberPicker(binding.tvTurnout); // 다이얼로그(멤버수)
        showStringPicker(binding.tvAges); // 다이얼로그(선호 연령대)
        rvItem_click_theme(rvAdapter_theme); // 모임 주제 설정(최대2개) - 선택한 index
        dialog_whereSelectBook(binding.tvChooseBook, binding.linearBook); // 다이얼로그(읽을 책)
        btn_click_createBookClub(binding.btnCreateBookClub); // 최종생성버튼



    } // ~onCreate()






    private void btn_click_createBookClub(String master_email
            , String isbn, String name, String introduction, String imageUrl, String bookTitle, int turnout, int fixed_num,
                                            String ages, String theme, int term, Long open_date, Long start_date, Long finish_date) {
        Log.e(TAG, "btn_click_createBookClub()" +
                "\nmaster_email : "+master_email+
                "\nisbn : "+isbn+
                "\nname : "+name+
                "\nintroduction : "+introduction+
                "\nimageUrl : "+imageUrl+
                "\nturnout : "+turnout+
                "\nfixed_num : "+fixed_num+
                "\nages : "+ages+
                "\ntheme : "+theme+
                "\nterm : "+term+
                "\nopen_date : "+longToStringDate(sdf, open_date)+
                "\nstart_date : "+longToStringDate(sdf, start_date)+
                "\nfinish_date : "+longToStringDate(sdf, finish_date)
        );


        if (master_email!=null && !name.equals("") && imageUrl!=null && theme!=null) {
            RetrofitClient retrofitClient = RetrofitClient.getInstance(); // 요청 인터페이스, 빌드, 로그 한 데 모아둔 클래스
            HttpRequest httpRequest = retrofitClient.getRetrofitInterface(); // 에서 꺼내쓴다.


            // 요청 메소드 이름 : getCreateClub
            httpRequest.getCreateClub(master_email, isbn, name, introduction, imageUrl, bookTitle, turnout, fixed_num, ages, theme, term, open_date, start_date, finish_date)
                    .enqueue(new Callback<Response>() {
                        @Override
                        public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                            Response response1 = response.body();

                            assert response1 != null;
                            Log.e(TAG, "response1() "+response1);


                            if (response1.isResponse()) {
                                Intent intent = new Intent(createClub.this, clubActivity.class);
                                intent.putExtra("purpose", "생성");
                                intent.putExtra("id", response1.getMessage());
//                                intent.putExtra("name", name);
//                                intent.putExtra("turnout", turnout);
//                                intent.putExtra("fixed_num", fixed_num);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // [ABCDE]가 있고, E에서 C를 열면 상위 DE제거
                                Log.e(TAG, "startActivity() \n"+TAG+" -> clubActivity.class ");
                                startActivity(intent);
                                Toast.makeText(getApplicationContext(), "모임을 생성했습니다.", Toast.LENGTH_SHORT).show();
//                                finish();
                            }
                        }
                        @Override
                        public void onFailure(Call<Response> call, Throwable t) {
                            Log.e(TAG, "onFailure() "+t.getMessage());
                        }
                    });
        } else {
            Toast.makeText(getApplicationContext(), "필수 정보를 입력해주세요", Toast.LENGTH_SHORT).show();
        }
    }



    // 최하단 최종 생성 버튼
    private void btn_click_createBookClub(Button btn) {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder msgBuilder = new AlertDialog.Builder(createClub.this)
                        .setTitle("모임 생성")
                        .setMessage("모임을 생성하시겠습니까?")
                        .setPositiveButton("생성", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {


                                btn_click_createBookClub(
                                        getSharedPreferences("autoLogin", Activity.MODE_PRIVATE).getString("userEmail", null)
                                        , isbn, binding.etClubName.getText().toString(), binding.etIntroduction.getText().toString(), cover, title, 1, fixed_num
                                        , selectedAges, str_selectedItemIndexes, rvAdapter_term.getIndex()+1, MaterialDatePicker.todayInUtcMilliseconds(), long_startDate, long_finishDate
                                );
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(getApplicationContext(), "취소했습니다.", Toast.LENGTH_SHORT).show();
                            }
                        });
                AlertDialog msgDlg = msgBuilder.create();
                msgDlg.show();
            }
        });
        /*
            1. Button 뷰를 파라미터로 받는다. onCreate()에서 보기좋게 만들기 위해서다.
            2. v : 클릭한 버튼
            3.
        */
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==0){
            if (resultCode==RESULT_OK) {
                isbn = data.getStringExtra("isbn");
                cover = data.getStringExtra("cover");
                title = data.getStringExtra("title");

                // tv숨기고 LinearLayout 보이기(img, tv)
                binding.tvChooseBook.setVisibility(View.GONE);
                binding.linearBook.setVisibility(View.VISIBLE);
                Glide.with(getApplicationContext()).load(data.getStringExtra("cover")).into(binding.imgChooseBook); // 책 표지
                binding.tvBookTitle.setText(data.getStringExtra("title"));
            }
        }
        /*
            1. startActivityForResult()에서 보냈던 코드와 일치하게 받는다면
            2. 결과를 정상적으로 받았다면
            3. 처음 tv 숨기고
            4. linear 보이게하기
            5. linear안의 img view에 책 표지 삽입
            6. linear안의 tv에 책 제목 삽입
        */
    }



    // 읽을 책 선택 다이얼로그
    private void dialog_whereSelectBook(TextView tv, LinearLayout linear) {
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_show();
            }
        });
        linear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_show();
            }
        });
        /*
            1. 읽을 책을 골라주세요. 두 뷰(처음 tv, 선택하고 난 뒤의 linear) 모두 실행은 같다.
            2. 처음 클릭하면 다이얼로그가 뜬다. 선택하면 finish()되면서 tv가 숨겨진다.
            3. 이미 책을 선택한 상태에서 재선택한다.
        */
    }


    private void dialog_show() {
        CharSequence[] items = new CharSequence[]{"서재에서 고르기", "알라딘에서 고르기", "교보문고에서 고르기", "yes24에서고르기"};
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(createClub.this);
        alertDialogBuilder.setTitle("읽을 책을 선택하는 곳");
        alertDialogBuilder.setItems(items,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();


                        switch (id) {
                            case 0: // 서재
                                startActivityForResult(
                                        new Intent(createClub.this, myLibrary.class).putExtra("clubBook", "서재에서고르기")
                                        , 0
                                );
                                break;
                            case 1: // 알라딘에서 검색
                                startActivityForResult(
                                        new Intent(createClub.this, book_search.class).putExtra("clubBook", "알라딘에서고르기")
                                        , 0
                                );
                                break;
                            case 2: // 교보문고에서 검색
                                startActivityForResult(
                                        new Intent(createClub.this, book_search.class).putExtra("clubBook", "교보문고에서고르기")
                                        , 0
                                );
                                break;
                            case 3: // 교보문고에서 검색
                                startActivityForResult(
                                        new Intent(createClub.this, book_search.class).putExtra("clubBook", "yes24에서고르기")
                                        , 0
                                );
                                break;
                        }
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        /*
            1. 다이얼로그 창 보기
            2. 다이얼로그 제목. 서술어는 없게
            3. 아이템 하나를 클릭했을 때 이벤트 (다이얼로그 창, 보기 인덱스)
            4. 인덱스 0을 선택했다면
            5. 알라딘 책 검색 액티비티로 이동한다. extra로 선별하여 이벤트를 달리 줬다.
            6. 서재 액티비티로 이동한다. extra로 선별하여 이벤트를 달리 줬다.
            7. 다이얼로그 생성
            8. 다이얼로그 보여주기
        */
    }


    // 모임 주제 설정 - 선택한 index (최대 2개)
    private void rvItem_click_theme(rv_theme adapter) {
        adapter.setWhenClickListener(new rv_theme.OnItemsClickListener() {
            @Override
            public void onItemClickReturnIndex(List<Integer> list) {  Log.e(TAG, "list:"+list);
                str_selectedItemIndexes = ""; // 초기화
                for (Integer item : list) {
                    str_selectedItemIndexes += item.toString() + ",";
                }
                Log.e(TAG, "str_selectedItemIndexes:"+str_selectedItemIndexes);
            }
        });
        /*
            1. 어댑터로부터 리스너를 불러온다.
            2. 아이템 한 개를 클릭했을 때 인덱스리스트를 가져오는 인터페이스를 구현해놨다.
            3. list : [1, 13]
            4. 전역변수에 선언해서 마지막에 방 생성할 때 retrofit으로 서버에 보내자 (배열말고 그냥 String으로 보내는게 편할 듯)
        */
    }


    private void showStringPicker(TextView tv_String) {
        selectedAges = "무관"; // 초기셋팅
        tv_String.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog stringDialog = new Dialog(createClub.this);
                stringDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                stringDialog.setContentView(R.layout.dialog_string);
                Button okBtn = (Button) stringDialog.findViewById(R.id.string_btn_ok);
                Button cancelBtn = (Button) stringDialog.findViewById(R.id.string_btn_cancel);
                StringChooser stringChooser = stringDialog.findViewById(R.id.stringChooser);
                List<String> strings = new ArrayList<>();
                strings.add("무관");
                strings.add("성인");
                strings.add("청소년");
                strings.add("어린이");
                stringChooser.setStrings(strings);
                stringChooser.setStringChooserCallback(new StringChooser.StringChooserCallback() {
                    @Override
                    public void onStringPickerValueChange(String s, int position) {
                        Log.e(TAG, "s:"+s);
                        selectedAges = s;
                    }
                });
                okBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tv_String.setText(selectedAges+" >  ");
                        stringDialog.dismiss();
                    }
                });
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        stringDialog.dismiss();
                    }
                });
                stringDialog.show();
            }
        });
        /*
            1. numPicker와 마찬가지로 작동한다.
            2. 따로 만든 이유는 numberPicker가 문자열은 안되기 때문이다.
            3. StringChooser라는 라이브러리를 사용했다.
            4. 4개로 정해져있어서 배열을 쓰면 되겠지만 우선 익숙하게 만들었다.
            5. set date to view (StringChooser)
            6. setStringChooserCallback() : 다이얼로그로 선택되면 그 string값과 index를 콜백받는 함수
            7. setOnClickListener() onClick() : ok를 누르면 기존뷰에 text가 삽입된다.
            8. 그 밑 : x버튼을 누르면 다이얼로그가 꺼진다.
        */
    }




    // 멤버 수 다이얼로그
    private void showNumberPicker(TextView tv_num) {
        fixed_num = 4; // 초기셋팅
        tv_num.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog numDialog = new Dialog(createClub.this);
                numDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                numDialog.setContentView(R.layout.dialog_number);
                Button okBtn = (Button) numDialog.findViewById(R.id.btn_ok);
                Button cancelBtn = (Button) numDialog.findViewById(R.id.btn_cancel);
                final NumberPicker np = (NumberPicker) numDialog.findViewById(R.id.numPicker);

                np.setMinValue(1); // 여기부터 주석 1번
                np.setMaxValue(20);
                np.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
                np.setWrapSelectorWheel(false);
                np.setValue(4); // 키면 4부터 보임
                okBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fixed_num = np.getValue();
                        tv_num.setText(String.valueOf(np.getValue())+"명 >  ");
                        numDialog.dismiss();
                    }
                });
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        numDialog.dismiss();
                    }
                });
                numDialog.show();
            }
        });
        /*
            0. 다이얼로그 객체를 생성하고 그 객체에 대한 view와 picker를 구현한다.
            1. 숫자만 도로록 내려가는 넘버피커의 속성이다. (문자X)
            2. 최소, 최대 숫자(인덱스 아님)
            3. 포커스 블록 안 하면, 키보드 나타난다.
            4. 범위를 초과하면 다시 1로 돌아갈건지 10에서 멈출건지
            5. 키자마자 보이는 숫자
            6. 확인 누르면 set tv // "명 >  "붙여주기
            7. 취소하면 그냥 꺼지기
            8. 꼭 show()해줘야 나타남
        */
    }



    private void setDateToDate(TextView tv_start, TextView tv_finish) {
        long_startDate = MaterialDatePicker.todayInUtcMilliseconds();
        long_finishDate = long_startDate+(60L *60*24*1000*7);

        tv_start.setText(longToStringDate(sdf, long_startDate)); // 처음은 오늘날짜로 셋팅
        tv_finish.setText(longToStringDate(sdf, long_finishDate));
        /*
            1. 달력처음키면 오늘날짜로 설정해놨기 때문에 view와 동기화시켜준다.
            2. 초기 종료일은 오늘날짜로부터 1주 뒤의 날짜이다.
            3. long_startDate, long_finishDate이 여기서 처음 대입된다.
        */
    }



    // rv item click시에 다른 뷰 (text)변경하기
    private void rvItem_click_changeFinishDate(rv_term adapter, TextView tv_finish) {
        adapter.setWhenClickListener(new rv_term.OnItemsClickListener() {
            @Override
            public void onItemClickReturnIndex(int index) { Log.e(TAG, "index:"+index);

                long_finishDate = long_startDate+(60L *60*24*1000*7*(index+1)); // 에러나서 cast to Long 클릭하니 '60L으로' 변경됨
                tv_finish.setText(longToStringDate(sdf, long_finishDate));
            }
        });
        /*
            1. 클릭 리스너를 어댑터에서 꺼내온다.
            2. 어댑터에 인터페이스를 구현해놨고 이를 통해 activity.this에서 rv item index를 리턴받는다.
            3. index : 0~7 -> +1을하여 1주, 2주..의 시간을 곱한다.
            4. long to String ('2023-01-20') and set view
        */
    }



    // 달력 build
    private MaterialDatePicker buildDatePicker(MaterialDatePicker datePicker) {
        CalendarConstraints.Builder calendarConstraintBuilder = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now()); // Set min date (과거날짜는 막기)
        datePicker = MaterialDatePicker.Builder.datePicker()
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds()) // (당일)
                .setCalendarConstraints(calendarConstraintBuilder.build())
                .build();
        return datePicker;
        /*
            1. 안드로이드 제공 캘린더 클래스에서 객체를 생성한다.
            2. Validator : 검증자, 검사자 // 지정된 시점부터 날짜를 활성화 시킨다.
            3. 날짜를 선택하여 리턴해주는 객체를 생성한다.
            4. 날짜선정 : 오늘(milliseconds)
            5. Constraints : 제약조건 // 위에서 생성한 안드캘린더객체를 장착한다.
        */
    }



    // 달력에서 시작일 선택하기
    private void layout_click_getStartDate(LinearLayout layout, TextView tv_start, TextView tv_finish, int selectWeek) {
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                materialDatePicker.show(getSupportFragmentManager(), "datePicker");
                materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
                    @Override
                    public void onPositiveButtonClick(Object selection) {
                        long_startDate = Long.parseLong(String.valueOf(selection));
                        long_finishDate = long_startDate+(60*60*24*1000*7*selectWeek);
                        Log.e(TAG, "\n\nlong_startDate: " + long_startDate+"/\nlong_finishDate:"+long_finishDate);
                        Log.e(TAG, "Object -> String -> Long -> Date -> String:"+longToStringDate(sdf, long_startDate));
                        Log.e(TAG, "Object -> String -> Long -> Date -> String:"+longToStringDate(sdf, long_finishDate));


                        tv_start.setText(longToStringDate(sdf, long_startDate)); // long -> Date -> String
                        tv_finish.setText(longToStringDate(sdf, long_finishDate));
                    }
                });
            }
        });
        /*
            1. 다이얼로그형태의 날짜선택 다이얼로그창을 띄운다.
            2. 날짜선택버튼 리스너안의 실행코드는 다음과 같다.
            3. selection : '1674604800000' // Long타입이 아니라 Object타입이다.
            4. 이벤트가 있다면 전역변수로 날짜들을 선언해놔야 편하다.
            5. selectWeek : 1주선택하면 1, 2주면 2이다. 그만큼 뒤의 날짜가 선택된다.
        */
    }




    // rv의 리스트들에 값 추가
    private void addItemsToList() {
        for (int i=1; i<=8; i++) {
            String term = String.valueOf(i)+"주 동안";
            Log.e("for()", term);
            week.add(term); // 1~8주 동안
        }
        /*
            <기간>
            1. i가 다음조건에서 반복한다.(8번)
            2. "1주 동안"...이 로그로 찍힌다.
            3. 전역에 선언하고 생성해놨지만 데이터가 없었던 list에 값을 추가한다.
        */

        theme = new String[] {"IT/컴퓨터", "경영/경제", "교육/공부", "기획/마케팅", "요리/여행", "종료/봉사", "자연/환경", "정치/사회", "문화/예술", "인문/과학", "자기계발/취미", "고전문학", "주식/투자", "시/소설"};
        Log.e("theme:", String.valueOf(theme));
    }



    // rv 장착
    @SuppressLint("NotifyDataSetChanged")
    private void recyclerview_setAdapter(RecyclerView rv, List<String> list, RecyclerView rv2, String[] theme) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        rvAdapter_term = new rv_term(getApplicationContext(), list); // rv 어댑터 객체 생성
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(rvAdapter_term);
        /*
            <기간>
            1. 가로정렬 형태로 보일 것이라는 객체를 생성한다.
            2. 비어있던 rv어댑터의 객체를 생성하면서 list와 context를 어댑터에 넘겨준다. (넘겨받은 데이터로 어댑터내에서 동작)
            3. rv(뷰)에 위에서 정해둔 가로정렬레이아웃 매니저 객체를 장착한다.
            4. rv(뷰)에 어댑터를 장착한다.
        */

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4);
        rvAdapter_theme = new rv_theme(getApplicationContext(), theme); // rv 어댑터 객체 생성
        rv2.setLayoutManager(gridLayoutManager);
        rv2.setAdapter(rvAdapter_theme);
        /*
            <모임 주제 설정>
            1. 가로 4칸짜리 그리드레이아웃매니저 객체를 생성
            2. 어댑터에 배열을 넘긴다. (넘겨받은 데이터로 어댑터내에서 동작)
            3. rv(뷰)에 위에서 정해둔 그리드레이아웃 매니저 객체를 장착한다.
            4. rv(뷰)에 어댑터를 장착한다.
        */
    }



    // editText의 scroll 속성 주기
    @SuppressLint("ClickableViewAccessibility")
    private void et_touchListener_returnScrollTrue(EditText et) {
        et.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) { Log.e(TAG, "\nv.getParent():"+v.getParent());
                if (v.getId() ==R.id.et_introduction) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction()){
                        case MotionEvent.ACTION_UP:
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                }
                return false;
            }
        });
        /*
            1. editText에 터치 이벤트가 일어났을 때
            2. onTouch()메소드에 실행 내용을 뒤엎는다.
            3. pram) v : 터치된 뷰 / event : 터치에 대한 정보(ACTION_DOWN..)
            4. 만약 터치한 뷰의 id값과 지정한 뷰의 id값이 일치한다면 (이 뷰를 터치했다면)
            5. 터치한 뷰의 부모한테 touch event를 빼앗기지 않게 한다. (엄빠 방해하지마)
            6. 만약 터치에 대한 정보값이
            7. 손가락 1개를 화면에서 떨어트렸을 때 발생되는 Event값이라면
            8. 터치한 뷰의 부모의 touch event 방해금지가 풀린다.
        */
        /**
         문제!!
            ScrollView안에서의 editText의 scroll 속성적용이 되지 않았다.

         해결!!
             editText의 터치 리스너를 통해,
             et바깥이면 스크롤이 false를 리턴받고 et안을 클릭할 경우 스크롤이 true를 리턴받는다.
         */
    }


    // 정말 뒤로갈건가요?
    @Override
    public void onBackPressed() { Log.e(TAG, "onBackPressed");
        if (!binding.etClubName.getText().toString().equals("") || !binding.etIntroduction.getText().toString().equals("")) {
            new writeNote().dialog_reallyLeave(getApplicationContext(), createClub.this);
        } else {
            super.onBackPressed(); // 입력된 텍스트나 사진이 없으면 바로 뒤로가기
        }
    }


    @Override
    protected void onResume() { Log.e(TAG, "onResume()");
        super.onResume();
    }
}