package com.example.mybooks.home;

import static com.example.mybooks.home.saveBook.longToStringDate;
import static com.example.mybooks.retrofit.RetrofitAladin.ttbkey;
import static com.example.mybooks.recyclerview.rv_searchBookByAladin.isMoreLoading;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
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

import com.example.mybooks.databinding.ActivityBookSearchBinding;
import com.example.mybooks.model.AladinBook;
import com.example.mybooks.recyclerview.rv_term;
import com.example.mybooks.retrofit.HttpRequest;
import com.example.mybooks.retrofit.RetrofitAladin;
import com.example.mybooks.recyclerview.rv_searchBookByAladin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;

public class book_search extends AppCompatActivity implements rv_searchBookByAladin.OnLoadMoreListener {
    /**
     * 책 검색결과 rv
     */
    private final String TAG=this.getClass().getSimpleName();
    private ActivityBookSearchBinding binding;
    private rv_searchBookByAladin rvAdapter;
    private ArrayList<AladinBook> aladinBookArr = new ArrayList<>(); // 검색결과로 나온 책들
    private String inputQuery; // 검색한 text
    private int page = 2;
    private int crawlingPage = 1;
    private String clubBook; // !=null) 값있음 (알라딘/교보)
    private boolean isEmpty; // 크롤링 값이비었냐
    private Handler handler = new Handler(); // 크롤링


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookSearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        clubBook = getIntent().getStringExtra("clubBook");
        Log.e(TAG, "포커스+키보드 올라옴// clubBook:"+clubBook);


        /** set data, view.. **/
        binding.searchView.setIconified(false); // 자동으로 키보드 올리기
        recyclerview_setAdapter(binding.rv, aladinBookArr); // (rv, 1권에 대한 arraylist)




        /** 리스너 **/
        rvItem_click_returnBookInfo(rvAdapter); // ok(선택완료) 클릭시 이벤트 -> finish()
        btn_click_searchView(); // 책 이름 검색 (알라딘/교보)


    } // ~onCreate()



    private void crawling_yes(String word, int page) {
        new Thread() {
            @Override
            public void run() {
                try {
                    String URL_yes = "http://www.yes24.com/product/search?query="+word+"&page="+page;
                    Log.e("URL_yes :", URL_yes);


                    //크롤링 할 구문
                    Document doc = Jsoup.connect(URL_yes).get();	//URL 웹사이트에 있는 html 코드를 다 끌어오기
                    Elements block  = doc.select("#goodsListWrap");
                    Elements container_img  = block.select(".img_bdr");
                    Elements container_name  = block.select(".info_name");
                    Elements container_author  = block.select(".info_auth");
                    Elements container_sub  = block.select(".info_name");
                    /*
                        1. 크롤링할 url주소 (검색어, 페이징 넘버)
                        2. Jsoup 라이브러리로 html코드를 다 끌어온다. 쿼리스트링을 사용해야되니 get method를 사용한다.
                        3. select 문법: id 인경우 #, class인 경우 .으로 태그 영역을 선택한다.\
                        4. Elements: 여러개가 담기는 경우 이 타입, 1개면 Element 타입이다.
                        5. block으로 가져온 태그 덩어리의 하위 태그에서 img, name, author, subtitle을 추출한다.
                        6. 4가지 다 class로 특정지었다.
                    */
                    Log.e("block :", String.valueOf(block.size())); // 1
                    Log.e("container_img :", String.valueOf(container_img.size())); // 24
                    Log.e("container_name :", String.valueOf(container_name.size())); // 24
                    Log.e("container_author :", String.valueOf(container_author.size())); // 24
                    Log.e("container_sub :", String.valueOf(container_sub.size())); // 24
                    // 주석의 숫자만큼 출력되어야 한다.



                    for(int i = 0; i < container_img.size(); i++) { // 24개
                        try {
                            String imgUrl = String.valueOf(container_img.get(i).tagName("img").child(0).attr("data-original"));
                            String name = container_name.get(i).select(".gd_name").text();
                            String author  = container_author.tagName("a").get(i).text();
                            String subTitle  = container_sub.get(i).child(2).tagName("span").text();
//                            String subTitle  = container_sub.select("span:last-child").text(); // 인덱스(2)가 없다면 빈 문자열을 넣는 식으로 에러해결
                            if (subTitle.contains("새창이동") || subTitle.contains("]")) {
                                subTitle = "-";
                            }


                            aladinBookArr.add(new AladinBook(imgUrl, name, author, subTitle));


                        } catch (Exception e) { Log.e("e :", String.valueOf(e)); }
                    }
                    /*
                        1. <img>태그와 나머지 4개의 태그들도 size가 24개일 것이다. 안 그러면 위와 같은 에러 뜬다.
                        2. container_img.get(i): 특정지은 태그들이 24개이기 때문에 i++하면서 추출한다.
                        3. tagName("img"): 하위 태그들 중 <img>태그를 특정짓는다.
                        4. child(0): 하위 태그 중 첫번째 태그를 특정짓는다.
                        5. attr("data-original"): 이 태그의 저 속성의 value를 추출한다. String을 리턴한다.
                        6. text(): 태그로 감싸진 내용을 String으로 리턴한다.
                        7. yes24의 경우 child(2)를 하면 다른걸 긁어올 때가 있다. 그래서 if문으로 잘 못 추출하는 경우에 대한 예외처리했다.
                        8. rv에 들어갈 arr에 방금 생성한 객체를 추가하고 24개를 반복해서 보여준다.
                    */




                    isEmpty = container_name.isEmpty();
                    Log.e("Tag", "isNull? : " + isEmpty);
                    if(!isEmpty) {
                        handler.post(new Runnable() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void run() {


                                rvAdapter.notifyDataSetChanged();


                            }
                        });
                    }
                } catch (IOException e) { e.printStackTrace(); }
            }
        }.start();
        /*
            1. 4개 중 아무값이나 상관없음. 어차피 값은 다 있거나 다 없거나니까.
            2. == 값이 있다면
            3. 핸들러를 통해 original thread에 순차적으로 접근한다. 뷰에 접근할 때는 핸들러를 사용해야 함
            4. 어댑터의 데이터가 변경되었음을 알려줘서 화면을 갱신한다.
            5. 크롤링을 할 때는 네트워크 에러가 뜰 수 있으니, thread로 작업단위를 분리해준다.
        */

    }





    private void crawling_kyobo(String word, int page) {
        new Thread() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void run() {
                try {
                    String URL_kyobo = "https://search.kyobobook.co.kr/search?keyword="+word+"&gbCode=TOT&target=total&page="+page;
                    Log.e("URL_kyobo :", URL_kyobo);


                    //크롤링 할 구문
                    Document doc = Jsoup.connect(URL_kyobo).get();	//URL 웹사이트에 있는 html 코드를 다 끌어오기
                    Elements block  = doc.select("#shopData_list");
                    Elements container_img  = block.select(".result_checkbox");
                    Elements container_name  = block.select(".prod_info");
                    Elements container_author  = block.select(".auto_overflow_inner .rep");
                    Elements container_sub  = block.select(".prod_desc_info");


                    Log.e("block :", String.valueOf(block.size())); // 1
                    Log.e("container_img :", String.valueOf(container_img.size())); // 20
                    Log.e("container_name :", String.valueOf(container_name.size())); // 20
                    Log.e("container_author :", String.valueOf(container_author.size())); // 20
                    Log.e("container_sub :", String.valueOf(container_sub.size())); // 20



                    for(int i = 0; i < container_img.size(); i++) { // 20개
                        try {
                            String imgUrl = "https://contents.kyobobook.co.kr/pdt/"+container_img.get(i).attr("data-bid")+".jpg"; // 9791167913098
                            String name  = container_name.get(i).select("a:last-child").text();
//                            String name  = container_name.get(i).child(2).text(); // 인덱스가 1일 때, 2일 때가 있어서 가변하는 문제때문에 에러발생 >> 맨마지막 태그를 가리킴
                            String author  = container_author.get(i).text();
                            String subTitle  = container_sub.get(i).text();


                            aladinBookArr.add(new AladinBook(imgUrl, name, author, subTitle));
                            Log.e("i", String.valueOf(i));
                        } catch (IndexOutOfBoundsException e) { Log.e("e :", String.valueOf(e)); }
                    }


                    isEmpty = container_name.isEmpty();
                    Log.e("Tag", "isNull? : " + isEmpty);
                    if(!isEmpty) {
                        handler.post(new Runnable() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void run() {


                                rvAdapter.notifyDataSetChanged();


                            }
                        });
                    }
                } catch (IOException e) { e.printStackTrace(); }
            }
        }.start();
    }



    // 책 검색 이벤트
    private void btn_click_searchView() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                inputQuery = query; // 전역변수에 대입
                Toast.makeText(getApplicationContext(), "[검색버튼클릭] 검색어 = "+query, Toast.LENGTH_LONG).show();
                aladinBookArr.clear();


                if (clubBook != null) {
                    if (clubBook.equals("교보문고에서고르기")) { // then.. crawling
                        crawling_kyobo(inputQuery, 1);

                    } else if(clubBook.equals("yes24에서고르기")) {
                        crawling_yes(inputQuery, 1);
                    }
                } else {
                    clickSearch(inputQuery, 1, true); // 알라딘 api // (text, 1)
                }


                keyboardHide();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });

        /*
            1. setOnQueryTextListener():검색창 이벤트 리스너
            2. onQueryTextSubmit() query -> 파라미터 : 검색한 텍스트
            3. inputQuery: 이벤트 없어도 사용해야되서 전역변수에 대입
            4. aladinBookArr.clear(): 누적되면 안 되서
            5. 교보문고에서고르기 : 교보문고, yes24, 알라딘마다 알고리즘이 조금씩 달라서 각 메소드를 만듦
            6. crawling_kyobo(): 1페이지부터 시작. Jsoup 크롤링 라이브러리 사용
            7. crawling_yes(): 마찬가지. 둘은 같은 라이브러리지만, html이 달라서 따로 구현함
            8. clickSearch(): API
            9. keyboardHide(): 키보드가 화면을 가려서 내리기. 알아서 안 내려감.
            10. onQueryTextChange(): 값이 ""빈문자열일 때로 이벤트주면 편함. 지원님이 알려준 방법
        */
    }



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



    // 페이징 - 10개 다 보여주고 스크롤 내리면 10개 더 보여줌
    @Override
    public void onLoadMore() {


        Log.e(TAG, "6666 onLoadMore add(null) 전~"+aladinBookArr.size());
        aladinBookArr.add(null);
        rvAdapter.notifyItemInserted(aladinBookArr.size() - 1);
        Log.e(TAG, "6666 onLoadMore add(null) 후~"+aladinBookArr.size());


        new Handler().postDelayed(new Runnable() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void run() {


                Log.e(TAG, "6666 onLoadMore.remove() 전 "+aladinBookArr.size());
                aladinBookArr.remove(null);
                rvAdapter.notifyItemRemoved(aladinBookArr.size());
                Log.e(TAG, "6666 onLoadMore.remove() 후 "+aladinBookArr.size());


                /*********** 다음 페이지를 불러오는 부분 **********/
                if (clubBook != null) {
                    if (clubBook.equals("교보문고에서고르기")) { // then.. crawling
                        crawlingPage++; // 1부터 시작임
                        crawling_kyobo(inputQuery, crawlingPage);

                    } else if(clubBook.equals("yes24에서고르기")) {
                        crawlingPage++;
                        crawling_yes(inputQuery, crawlingPage);
                    }

                } else {
                    Log.e(TAG, "6666 page"+page);
                    clickSearch(inputQuery, page, false);
                    page++; // 2 -> 3page
                }

                /************************************************/

//                rvAdapter.setMoreLoading(false); // 로딩 해제
                rvAdapter.notifyDataSetChanged(); // 데이터 변경 후 갱신
                isMoreLoading = false;

            }
        }, 1000);

    }



    // 키보드 내려라
    private void keyboardHide() { // 책 목록을 가려서.
        View view = getCurrentFocus();

        if (view != null) {
            Log.e(TAG, "포커스+키보드 내림");
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }



    // 알라딘 책 검색 api를 통해 책 목록을 받아옴
    private void clickSearch(String keyword, int page, boolean listClear) { Log.e(TAG, "clickSearch() keyword : "+keyword);

        RetrofitAladin retrofitAladin = RetrofitAladin.getInstance(); // 요청 인터페이스, 빌드, 로그 한 데 모아둔 클래스
        HttpRequest httpRequest_aladin = retrofitAladin.getRetrofitInterface(); // 에서 꺼내쓴다.


        // 요청 메소드 이름 : getSearchBook
        httpRequest_aladin.getSearchBook(ttbkey, keyword, page).enqueue(new Callback<String>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<String> call, retrofit2.Response<String> response) {


                if (listClear) { // 이벤트면 clear, 페이징이면 add라서 조건줌
                    aladinBookArr.clear(); // 다른 키워드 입력할 때 arr에 add되는 문제때문에.
                }

                Log.e(TAG, "onResponse: " + response.body());
                try {

                    // xml to json
                    assert response.body() != null;
                    JSONObject json = XML.toJSONObject(response.body()); // 태그형태를 {"" : ""} 이런 json형태로 바꿔줌
                    Log.e(TAG, "json: " + json);
                    // 규칙! object 뒤에는 str, array뒤에는 idx


                    // JSONArray (반복문 돌리기 위함 - i 필요)
                    JSONArray jsonArray = new JSONArray(); // JSONObject 여도 되지않을까?
                    jsonArray.put(json.getJSONObject("object").getJSONArray("item"));
                    Log.e(TAG, "\n\n\n json.getJSONObject(\"object\").getJSONArray(\"item\") : " + json.getJSONObject("object").getJSONArray("item"));
                    Log.e(TAG, "\n\n\n jsonArray_title : " + json.getJSONObject("object").getJSONArray("item")); // 1개임
                    Log.e(TAG, "\n\n\n jsonArray_title : " + json.getJSONObject("object").getJSONArray("item").length()); // 기본 10건 (페이징하면 더 가져오기)
                    Log.e(TAG, "\n\n\n jsonArray_title : " + json.getJSONObject("object").getJSONArray("item").getJSONObject(0)); // 책 1권에 대한 정보
                    Log.e(TAG, "\n\n\n jsonArray_title : " + json.getJSONObject("object").getJSONArray("item").getJSONObject(1).getString("title")); // 책 1권에 대한 제목



                    JSONArray jsonArr = json.getJSONObject("object").getJSONArray("item"); // 길어서 대입
                    Log.e(TAG, "jsonArr.length " + jsonArr.length());



                    for (int i=0; i<jsonArr.length(); i++) { // 기본 10개로 설정되어있음

                        // 상세 설명
                        String description = jsonArr.getJSONObject(i).getString("description"); // 필요한 부분만 추출출
                        int index = description.indexOf("<br/>");
                        Log.e(TAG, "index: " + index);
                        description = description.substring(index+5); // <br/> 제거

                        // Arraylist<aladinBook> 타입으로 만들기
                        AladinBook aladinBook = new AladinBook(
                                jsonArr.getJSONObject(i).getString("isbn"),
                                jsonArr.getJSONObject(i).getString("title"),
                                jsonArr.getJSONObject(i).getString("link"),
                                jsonArr.getJSONObject(i).getString("author"),
                                jsonArr.getJSONObject(i).getString("pubDate"),
                                description,
                                jsonArr.getJSONObject(i).getString("cover"),
                                jsonArr.getJSONObject(i).getString("publisher")
                        );

                        aladinBookArr.add(aladinBook);
                    } // ~for()
                    rvAdapter.notifyDataSetChanged(); // 데이터 변경 후 갱신



                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.e(TAG, "onResponse() 끝");
                Log.e(TAG, "onResponse() size:"+aladinBookArr.size());
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
                // error! E/book_search: onFailure: Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 1 path $
                // >> solve! new GsonBuilder() 생성성
            }
       });


    }




    @SuppressLint("NotifyDataSetChanged")
    public void recyclerview_setAdapter(RecyclerView rv, ArrayList arrayList) {

        LinearLayoutManager layoutManager2 = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(layoutManager2);
        rvAdapter = new rv_searchBookByAladin(getApplicationContext(), arrayList, this, clubBook, book_search.this); // rv 어댑터 객체 생성
        rvAdapter.setLinearLayoutManager(layoutManager2);
        rvAdapter.setRecyclerView(rv);
        rv.setAdapter(rvAdapter);
    }


    @Override
    protected void onResume() { Log.e(TAG, "onResume()");
        super.onResume();
    }

    @Override
    protected void onStart() { Log.e(TAG, "onStart()");
        super.onStart();
    }

    @Override
    protected void onRestart() { Log.e(TAG, "onRestart()");
        super.onRestart();
        keyboardHide(); // item 클릭 후 뒤로가기했을 때, keyboard가 없어야 자연스러워서 여기 위치
        // 왜 안 없어지지?
    }
}