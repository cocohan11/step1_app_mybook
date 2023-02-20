package com.example.mybooks.note;

import static com.example.mybooks.recyclerview.rv_showNotes.isMoreLoadingNote;
import static com.example.mybooks.retrofit.RetrofitAladin.ttbkey;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.mybooks.R;
import com.example.mybooks.databinding.ActivityMainBinding;
import com.example.mybooks.databinding.ActivityShowNoteBinding;
import com.example.mybooks.databinding.ActivityWriteNoteBinding;
import com.example.mybooks.home.showBookDetail;
import com.example.mybooks.home_bottom_1to5.myNoteActivity;
import com.example.mybooks.model.AladinBook;
import com.example.mybooks.model.Note;
import com.example.mybooks.model.Note;
import com.example.mybooks.model.Response;
import com.example.mybooks.recyclerview.MultiImageAdapter;
import com.example.mybooks.recyclerview.rv_searchBookByAladin;
import com.example.mybooks.recyclerview.rv_showNotes;
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

public class showNote extends AppCompatActivity implements rv_showNotes.OnLoadMoreListener {
    /**
     * 한 책에 대한 노트들 보기
     */
    private ActivityShowNoteBinding binding;
    private final String TAG=this.getClass().getSimpleName();
    private ArrayList<Note> noteArrayList = new ArrayList<>(); // 이 책에 대한 사용자의 노트들
    private rv_showNotes rvAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShowNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Log.e(TAG, "onCreate() noteArrayList:"+noteArrayList);


        // 플로팅 액션버튼 +
        binding.floatingActionButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(showNote.this, writeNote.class).putExtra("isbn", getIntent().getStringExtra("isbn")));
                Log.e(TAG, "startActivity() \n"+TAG+" -> writeNote.class ");

            }
        });



        // 정렬 (최신순/오래된순/페이지순/페이지역순)
        binding.linearSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog_sort();

            }
        });


    } //~onCreate()



    // 정렬 다이얼로그
    public void dialog_sort() { Log.e(TAG, "정렬 다이얼로그()");
        final CharSequence[] items = { "최신순", "오래된순", "페이지순", "페이지역순"};
        // 제목셋팅
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(showNote.this);
        alertDialogBuilder.setTitle("옵션");
        alertDialogBuilder.setItems(items,
                new DialogInterface.OnClickListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    public void onClick(DialogInterface dialog, int id) {

                        // 프로그램을 종료한다
                        setArraylistLineUp(id);
                        Toast.makeText(getApplicationContext(), id + " 선택했습니다.", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        rvAdapter.notifyDataSetChanged();
                    }
                });

        // 다이얼로그 생성
        AlertDialog alertDialog = alertDialogBuilder.create();

        // 다이얼로그 보여주기
        alertDialog.show();
    }


    // (최신순/오래된순/페이지순/페이지역순)
    public void setArraylistLineUp(int index) { Log.e(TAG, "index:"+index);
        switch (index) {
            case 0:
                noteArrayList.sort(Comparator.comparing(Note::getDate).reversed());
                binding.tvSort.setText("최신순");
                break;

            case 1:
                noteArrayList.sort(Comparator.comparing(Note::getDate));
                binding.tvSort.setText("오래된순");
                break;

            case 2:
                noteArrayList.sort(Comparator.comparing(Note::getPage));
                binding.tvSort.setText("페이지순");
                break;

            case 3:
                noteArrayList.sort(Comparator.comparing(Note::getPage).reversed());
                binding.tvSort.setText("페이지역순");
                break;
        }
        Log.e(TAG, "noteArrayList:"+noteArrayList);
    }

    

    // retrofit - getNotes about this book
    private void setRvItems(String email, String isbn, int nextIndex) {
        Log.e(TAG, "isbn : "+isbn);
        Log.e(TAG, "nextIndex:"+nextIndex);

        RetrofitClient retrofitClient = RetrofitClient.getInstance(); // 요청 인터페이스, 빌드, 로그 한 데 모아둔 클래스
        HttpRequest httpRequest = retrofitClient.getRetrofitInterface(); // 에서 꺼내쓴다.



        // 요청 메소드 이름 : getBooksForNote
        httpRequest.getNotesAboutTheBook(email, isbn, nextIndex).enqueue(new Callback<ArrayList<Note>>() { // 파라미터 3개보내면서 요청하면 결과로 응답/실패 // 비동기
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<ArrayList<Note>> call, retrofit2.Response<ArrayList<Note>> response) {
                ArrayList<Note> list = response.body(); // 잠깐 담겼다가 NoteArraylist에 배분해줌
                assert list != null;
                /*{
                    "page": "1",
                    "imgUrl": null,
                    "content": "ㅊㅌㅊ",
                    "date": "2023-01-09 21:38:21",
                    "open": "1"
                }}*/
                Log.e(TAG, "11 noteArrayList size:"+noteArrayList.size());
                Log.e(TAG, "11 noteArrayList:"+noteArrayList);
//                noteArrayList.clear();
                noteArrayList.addAll(list);
                Log.e(TAG, "22 noteArrayList size:"+noteArrayList.size());
                Log.e(TAG, "22 onCreate() noteArrayList:"+noteArrayList);
                rvAdapter.notifyDataSetChanged();

            }

            @Override
            public void onFailure(Call<ArrayList<Note>> call, Throwable t) {
                Log.e(TAG, "onFailure() "+t.getMessage());
            }
        });


    }



    // 페이징
    // 10개/ 스크롤 내리면 프로그레스 바가 보임
    @Override
    public void onLoadMore() {

        noteArrayList.add(null);
        rvAdapter.notifyItemInserted(noteArrayList.size() - 1);
        Log.e(TAG, "6666 onLoadMore add(null) 후~"+noteArrayList.size());


        new Handler().postDelayed(new Runnable() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void run() {


                Log.e(TAG, "onLoadMore.remove() 전 "+noteArrayList.size());
                noteArrayList.remove(null);
                rvAdapter.notifyItemRemoved(noteArrayList.size());
                Log.e(TAG, "onLoadMore.remove() 후 "+noteArrayList.size());


                /*********** 다음 페이지를 불러오는 부분 **********/

                Log.e(TAG, "noteArrayList.size()"+noteArrayList.size());
                setRvItems(
                        getSharedPreferences("autoLogin", Activity.MODE_PRIVATE).getString("userEmail", null)
                        , getIntent().getStringExtra("isbn")
                        , noteArrayList.size() // 2번째부터 10일꺼임
                );

                /************************************************/

//                rvAdapter.setMoreLoading(false); // 로딩 해제
                rvAdapter.notifyDataSetChanged(); // 데이터 변경 후 갱신
                isMoreLoadingNote = false;

            }
        }, 1000);
    }


    @SuppressLint("NotifyDataSetChanged")
    public void recyclerview_setAdapter(RecyclerView rv, ArrayList arrayList) {

        LinearLayoutManager layoutManager2 = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(layoutManager2);
        rvAdapter = new rv_showNotes(getApplicationContext(), arrayList, showNote.this, this); // rv 어댑터 객체 생성
        rvAdapter.setLinearLayoutManager(layoutManager2);
        rvAdapter.setRecyclerView(rv);
        rv.setAdapter(rvAdapter);
        rvAdapter.notifyDataSetChanged();

    }


    @SuppressLint("SetTextI18n")
    private void setIntentItem(Intent i) { Log.e(TAG, "countNote : " + i.getIntExtra("countNote", 0));
        binding.tvCount.setText("작성한 노트 ("+i.getIntExtra("countNote", 0)+"개)");
    }


    @Override
    protected void onResume() {
        super.onResume(); Log.e(TAG, "onResume");

        setIntentItem(getIntent());
        recyclerview_setAdapter(binding.rvShowNote, noteArrayList);
        setRvItems(
                getSharedPreferences("autoLogin", Activity.MODE_PRIVATE).getString("userEmail", null)
                , getIntent().getStringExtra("isbn")
                , 0 // 처음에는 0~10불러오기
        );
    }

}