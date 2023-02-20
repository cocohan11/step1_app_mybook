package com.example.mybooks.recyclerview;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mybooks.R;
import com.example.mybooks.home.showBookDetail;
import com.example.mybooks.model.Note;
import com.example.mybooks.model.Response;
import com.example.mybooks.note.showNote;
import com.example.mybooks.note.writeNote;
import com.example.mybooks.retrofit.HttpRequest;
import com.example.mybooks.retrofit.RetrofitClient;
import com.willy.ratingbar.BaseRatingBar;

import java.util.ArrayList;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;

public class rv_showNotes extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    /**
     * 노트 첫 화면에서, 노트가 있는 책을 보여주는 rv adapter
     */
    private final String TAG=this.getClass().getSimpleName();
    Context context;
    Context activity;
    ArrayList<Note> noteArrayList; //arraylist안에 생성자로 만든 n개의 데이터가 들어있다.

    // 스크롤 페이징
    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;
    private OnLoadMoreListener onLoadMoreListener;
    static public boolean isMoreLoadingNote = false; // 로딩중인지 구별
    int firstVisibleItem, visibleItemCount, totalItemCount, lastVisibleItem; // 맨밑 알기위함
    private LinearLayoutManager mLinearLayoutManager; // 위의 int들을 알아내기 위함




    // 페이징
    // 인터페이스 book_search.class에 장착하기
    public interface OnLoadMoreListener{
        void onLoadMore();
    }



    // 생성자
    public rv_showNotes(Context context, ArrayList<Note> noteArrayList, Activity activity, OnLoadMoreListener onLoadMoreListener){
        this.context = context;
        this.noteArrayList = noteArrayList;
        this.activity = activity;
        this.onLoadMoreListener = onLoadMoreListener; // 페이징 인터페이스 리스너 전달
    }

    public void setLinearLayoutManager(LinearLayoutManager linearLayoutManager){
        this.mLinearLayoutManager=linearLayoutManager;
    }

    @Override
    public int getItemViewType(int position) {
        return noteArrayList.get(position) != null ? VIEW_ITEM : VIEW_PROG; // 책정보 뷰 or 프로그래스 바
    }


    @NonNull
    @Override // 내가 만든 한 칸의 레이아웃을 메모리에 올려 객체화함
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_ITEM) {
            // #1 노트 정보
            return new NoteViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_note, parent, false));
        } else {
            // #2 프로그래스 바
            return new ProgressViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_progress, parent, false));
        }
    }



    // 바인드뷰홀더 : 뷰홀더에 들어갈 데이터만 바꿔준다.
    // 데이터가 들어간 뷰를 매번 인플레이트하고 새로 만드는게 아니라, 뷰를 그대로 쓰고(holder) arraylist에 있던 데이터만 set해준다.
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        /**
         * 표지, 제목, 별점, 메모갯수, 날짜 (+isbn)
         */
        if (holder instanceof NoteViewHolder) {
            Note note = noteArrayList.get(position); // 배열에서 꺼내온 책 객체를 view에 삽입\
            Log.e(TAG, "note.getPage():"+note.getPage());
            Log.e(TAG, "note.isOpen():"+note.isOpen());
            String page = "p. "+String.valueOf(note.getPage()); // error!! int형 그대로 넣으면 에러남
            String date = note.getDate().substring(2,16);


            ((NoteViewHolder) holder).tv_note_page.setText(page);
            ((NoteViewHolder) holder).tv_note_content.setText(note.getContent()); // 글
            ((NoteViewHolder) holder).tv_note_date.setText(date);
            if (note.getImgUrl() != null) {
                ((NoteViewHolder) holder).img_note_picture.setVisibility(View.VISIBLE);
                Glide.with(context).load(note.getImgUrl()).into(((NoteViewHolder) holder).img_note_picture); // 책 표지
            } else {
                ((NoteViewHolder) holder).img_note_picture.setVisibility(View.GONE);
            }

            // 전체공개/비공개
            if (note.isOpen()) {
                ((NoteViewHolder) holder).tv_note_open.setText("전체공개");
            } else {
                ((NoteViewHolder) holder).tv_note_open.setText("비공개");
                ((NoteViewHolder) holder).tv_note_open.setBackgroundResource(R.drawable.round_square_gray_wrap); // 주황색 -> 회색
            }

            // 옵션 (수정/삭제/공유하기)
            ((NoteViewHolder) holder).img_note_options.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { Log.e(TAG, "옵션 OnClickListener/ note.id:"+note.getId());

                    btn_options_dialog(note);

                }
            });
        } else {
            Log.e(TAG,"onBindViewHolder() ProgressViewHolder 뷰가 보이는 순간 ");
        }

    }



    // 리사이클러뷰 맨 밑에 닿을 때
    public void setRecyclerView(RecyclerView mView){
        mView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                int itemTotalCount = recyclerView.getAdapter().getItemCount() - 1;
                if (lastVisibleItemPosition == itemTotalCount) {
                    Log.e(TAG, "last Position...");
                }
                Log.e(TAG,"getItemCount:"+getItemCount());


                totalItemCount = mLinearLayoutManager.getItemCount();
                visibleItemCount = recyclerView.getChildCount();
                firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition(); // 화면에 보이는
                lastVisibleItem = mLinearLayoutManager.findLastVisibleItemPosition();

                Log.e("total", totalItemCount + "");
                Log.e("visible", visibleItemCount + "");
                Log.e("first", firstVisibleItem + "");
                Log.e("last", lastVisibleItem + "");
                Log.e("last2", lastVisibleItemPosition + "");
                Log.e("isMoreLoadingNote if문 전", String.valueOf(isMoreLoadingNote));


                // 맨 밑에 닿는 곳
                if (!isMoreLoadingNote && totalItemCount <= (lastVisibleItem + 1) && totalItemCount > 9 && dy > 0) {
                    if (onLoadMoreListener != null) {
                        int b = totalItemCount%10; // 나머지로 10단위인지 알아내기
                        Log.e("**** b:", String.valueOf(b));
                        if (b==0 && totalItemCount!=0) { // 나머지가 0이어야 10단위임
                            Log.e("**** onLoadMore()", "");
                            onLoadMoreListener.onLoadMore(); // 인터페이스라서 book_search.class에서 구현함
                            isMoreLoadingNote = true;
                        } else {
                            Log.e("**** else", "isMoreLoadingNote:"+isMoreLoadingNote);
                        }
                    }
                }
            }


        });
    }


    @Override
    public int getItemCount() { // 삭제하니 에러남
        return noteArrayList.size();
    }


    public void btn_options_dialog(Note note) { Log.e(TAG, "note:"+note);

        // 비공개면 공유하기 선택버튼이 없음
        CharSequence[] items;
        if (note.isOpen()) {
            items = new CharSequence[]{"수정하기", "삭제하기", "공유하기"};
        } else {
            items = new CharSequence[]{"수정하기", "삭제하기"}; // 공유하기 없음
        }

        // 제목셋팅
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setTitle("옵션");
        alertDialogBuilder.setItems(items,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        // 프로그램을 종료한다
                        Toast.makeText(context, id + " 선택했습니다.", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();


                        switch (id) {
                            case 0: // 수정
                                activity.startActivity(
                                        new Intent(activity, writeNote.class)
                                                .putExtra("수정", "수정")
                                                .putExtra("id", note.getId())
                                                .putExtra("isbn", note.getIsbn())
                                                .putExtra("page", note.getPage())
                                                .putExtra("imgUrl", note.getImgUrl())
                                                .putExtra("content", note.getContent())
                                                .putExtra("open", note.isOpen())
                                                .putExtra("Date", note.getDate())
                                );
                                break;

                            case 1: // 삭제
                                // 재확인 - 다이얼로그
                                dialog_delete(note);
                                break;

                            case 2: // 공유하기
                                // 미구현
                                //
                                //

                        }
                    }
                });

        // 다이얼로그 생성
        AlertDialog alertDialog = alertDialogBuilder.create();

        // 다이얼로그 보여주기
        alertDialog.show();
    }



    // 설계 중
    private void DeleteMyNoteToDB(String email, int id, Note note) { Log.e(TAG, "DeleteMyNoteToDB()");

        RetrofitClient retrofitClient = RetrofitClient.getInstance(); // 요청 인터페이스, 빌드, 로그 한 데 모아둔 클래스
        HttpRequest httpRequest = retrofitClient.getRetrofitInterface(); // 에서 꺼내쓴다.



        // 요청 메소드 이름 : DeleteMyNoteToDB (이멜, 노트id)
        httpRequest.getDeleteNote(email, id).enqueue(new Callback<Response>() { // 파라미터 3개보내면서 요청하면 결과로 응답/실패 // 비동기
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                Response response1 = response.body();
                assert response1 != null;
                Log.e(TAG, "onResponse() / response1 : "+response1.getMessage());
                if (response1.isResponse()) { // Delete 완료했다면


                    // arr에서 해당 객체를 삭제함
                    noteArrayList.remove(note);
                    notifyDataSetChanged();
                    Toast.makeText(context, "삭제 완료", Toast.LENGTH_SHORT).show();


                } else {
                    Toast.makeText(context, "다시 확인해주세요", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Response> call, Throwable t) {
                Log.e(TAG, "onFailure() "+t.getMessage());
            }
        });

    }


    private void dialog_delete(Note note) {
        // 삭제 다이얼로그
        AlertDialog.Builder msgBuilder = new AlertDialog.Builder(activity)
                .setTitle("노트 삭제")
                .setMessage("해당 노트를 삭제합니다.")
                .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {


                        // 서버에 delete쿼리 날림
                        DeleteMyNoteToDB(
                                context.getSharedPreferences("autoLogin", Activity.MODE_PRIVATE).getString("userEmail", null)
                                ,note.getId()
                                ,note
                        );

                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(activity, "취소했습니다", Toast.LENGTH_SHORT).show();
                    }
                });
        AlertDialog msgDlg = msgBuilder.create();
        msgDlg.show();
    }


    // 뷰 홀더
    // #1 알라딘 책 정보
    class NoteViewHolder extends RecyclerView.ViewHolder {

        ImageView img_note_options, img_note_picture;
        TextView tv_note_page, tv_note_open, tv_note_content, tv_note_date;

        //순서 1에서 뷰홀더를 리턴받았음 이제 해당 레이아웃에 손 댈 수 있게 되어서 find해주고 선언해서 사용하면 된다.
        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);

            // 노트 (순서대로)
            tv_note_page = itemView.findViewById(R.id.tv_note_page);
            tv_note_open = itemView.findViewById(R.id.tv_note_open);
            img_note_options = itemView.findViewById(R.id.img_note_options);
            img_note_picture = itemView.findViewById(R.id.img_note_picture);
            tv_note_content = itemView.findViewById(R.id.tv_note_content);
            tv_note_date = itemView.findViewById(R.id.tv_note_date);

        }
    }



    // #2 프로그래스 바
    class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar pBar;
        public ProgressViewHolder(View v) {
            super(v);
            pBar = (ProgressBar) v.findViewById(R.id.pBar);
        }
    }





}