package com.example.mybooks.recyclerview;


import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import static com.example.mybooks.home.saveBook.longToStringDate;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
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
import com.example.mybooks.model.AladinBook;
import com.example.mybooks.model.Note;
import com.willy.ratingbar.BaseRatingBar;

import java.util.ArrayList;

public class rv_searchBookByAladin extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    /**
     * 책 정보 미리보기 rv adapter
     * 책 검색, 서재 조회
     */
    private final String TAG=this.getClass().getSimpleName();
    Context context, activity;
    ArrayList<AladinBook> aladinBook; //arraylist안에 생성자로 만든 n개의 데이터가 들어있다.
    private @SuppressLint("SimpleDateFormat")
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // millisecond to date
    private rv_searchBookByAladin.OnItemsClickListener listener;

    // 스크롤 페이징
    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;
    private OnLoadMoreListener onLoadMoreListener;
    static public boolean isMoreLoading = false; // 로딩중인지 구별
    private String clubBook; // 클럽생성시 들어오는 곳
    int firstVisibleItem, visibleItemCount, totalItemCount, lastVisibleItem; // 맨밑 알기위함
    private int visibleThreshold = 1;
    private LinearLayoutManager mLinearLayoutManager; // 위의 int들을 알아내기 위함


    // 인터페이스 book_search.class에 장착하기
    public interface OnLoadMoreListener{ 
        void onLoadMore();
    }


    public interface OnItemsClickListener{
        void onItemClickReturnBookInfo(String cover, String title, String isbn);
    }
    public void setWhenClickListener(rv_searchBookByAladin.OnItemsClickListener listener){
        this.listener = listener;
    }

    // 생성자
    public rv_searchBookByAladin(Context context, ArrayList<AladinBook> aladinBook, OnLoadMoreListener onLoadMoreListener, String clubBook, Activity activity){ //다른 예시에서는 arr를 파라미터로 넣던데...음... 메인가서 보자 뭘 넣는지
        this.context = context;
        this.aladinBook = aladinBook;
        this.onLoadMoreListener = onLoadMoreListener;
        this.clubBook = clubBook;
        this.activity = activity;
    }

    public void setLinearLayoutManager(LinearLayoutManager linearLayoutManager){
        this.mLinearLayoutManager=linearLayoutManager;
    }

    @Override
    public int getItemViewType(int position) {
        return aladinBook.get(position) != null ? VIEW_ITEM : VIEW_PROG; // 책정보 뷰 or 프로그래스 바
    }


    // 내가 만든 한 칸의 레이아웃을 메모리에 올려 객체화함
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.e("홀더 context ", String.valueOf(context));

        if (viewType == VIEW_ITEM) {
            // #1 알라딘 책 정보
            return new AladinnViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_searchbook_by_aladin, parent, false));
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


        if (holder instanceof AladinnViewHolder) {
            // AladinnViewHolder가 아니면 프로그래스 바가 나옴
            // 뷰 홀더 2개임


            Log.e(TAG, "6666 onBindViewHolder() ");
            Log.e(TAG, "aladinBook.size() : "+aladinBook.size());
            AladinBook aladin = aladinBook.get(position); //arr의 인덱스에 해당하는 객체를 가져와서

            Log.e(TAG, "aladin.getCover() : "+aladin.getCover());
            Glide.with(context).load(aladin.getCover()).into(((AladinnViewHolder) holder).img_cover); // 책 표지
            ((AladinnViewHolder) holder).tv_title.setText(aladin.getTitle()); // 제목
            ((AladinnViewHolder) holder).tv_author.setText(aladin.getAuthor()); // 작가
            ((AladinnViewHolder) holder).tv_description.setText(aladin.getDescription()); // 설명


            // 서재
            if (aladin.getBookState() != null) {
                ((AladinnViewHolder) holder).tv_description.setVisibility(View.GONE);
                ((AladinnViewHolder) holder).linear_Date.setVisibility(View.VISIBLE);

                if (aladin.getBookState().equals("read")) { // 읽은 책
                    ((AladinnViewHolder) holder).tv_start.setVisibility(View.VISIBLE);
                    ((AladinnViewHolder) holder).tv_startDate.setVisibility(View.VISIBLE);
                    ((AladinnViewHolder) holder).tv_finish.setVisibility(View.VISIBLE);
                    ((AladinnViewHolder) holder).tv_finishDate.setVisibility(View.VISIBLE);
                    ((AladinnViewHolder) holder).baseStar.setVisibility(View.VISIBLE);
                    ((AladinnViewHolder) holder).baseStar.setRating(aladin.getRating());
                    Log.e(TAG, "aladin.getStartDate(): "+aladin.getStartDate());
                    ((AladinnViewHolder) holder).tv_startDate.setText(longToStringDate(sdf, aladin.getStartDate())); // 시작일
                    ((AladinnViewHolder) holder).tv_finishDate.setText(longToStringDate(sdf, aladin.getFinishDate())); // 종료일


                } else if (aladin.getBookState().equals("reading")) { // 읽고 있는 책
                    ((AladinnViewHolder) holder).tv_start.setVisibility(View.VISIBLE);
                    ((AladinnViewHolder) holder).tv_startDate.setVisibility(View.VISIBLE);
                    ((AladinnViewHolder) holder).tv_finish.setVisibility(View.GONE);
                    ((AladinnViewHolder) holder).tv_finishDate.setVisibility(View.GONE);
                    ((AladinnViewHolder) holder).baseStar.setVisibility(View.GONE);
                    ((AladinnViewHolder) holder).tv_startDate.setText(longToStringDate(sdf, aladin.getStartDate())); // 시작일

                } else { // 읽고 싶은 책
                    ((AladinnViewHolder) holder).tv_start.setVisibility(View.GONE);
                    ((AladinnViewHolder) holder).tv_startDate.setVisibility(View.GONE);
                    ((AladinnViewHolder) holder).tv_finish.setVisibility(View.GONE);
                    ((AladinnViewHolder) holder).tv_finishDate.setVisibility(View.GONE);
                    ((AladinnViewHolder) holder).baseStar.setVisibility(View.GONE);
                }

            // 책 검색
            } else
             {
                ((AladinnViewHolder) holder).tv_description.setVisibility(View.VISIBLE);
                ((AladinnViewHolder) holder).linear_Date.setVisibility(View.GONE);
                ((AladinnViewHolder) holder).baseStar.setVisibility(View.GONE);
            }



            // 모임 > 모임생성 > 모임책 선택
//            Log.e("clubBook ", clubBook);
            ((AladinnViewHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clubBook!=null) {
                        dialog_select(aladin.getCover(), aladin.getTitle(), aladin.getIsbn());


                    } else {
                        Log.e(TAG,"itemView.setOnClickListener()");
                        Intent intent = new Intent(context, showBookDetail.class); // 화면 이동
                        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);

                        intent.putExtra("title", aladin.getTitle());
                        intent.putExtra("isbn", aladin.getIsbn());
                        intent.putExtra("cover", aladin.getCover());
                        intent.putExtra("author", aladin.getAuthor());
                        intent.putExtra("description", aladin.getDescription());
                        intent.putExtra("publisher", aladin.getPublisher());


                        // 서재에서 들어가는경우 상세정보는 상품조회 API를 통해 set해준다
                        if (aladin.getBookState() != null) {
                            intent.putExtra("isbn", aladin.getIsbn()); // 상품조회 API에 필요한 파라미터
                            intent.putExtra("bookState", aladin.getBookState());
                            intent.putExtra("rating", aladin.getRating());
                            intent.putExtra("startDate", aladin.getStartDate());
                            intent.putExtra("finishDate", aladin.getFinishDate());
                            Log.e(TAG, "startDate~~ : "+aladin.getStartDate());
                            Log.e(TAG, "rating~~ : "+aladin.getRating());
                        }

                        context.startActivity(intent);
                        Log.e(TAG, "startActivity() \n"+TAG+" -> showBookDetail.class ");
                    }
                }
            });
        } else {
            Log.e(TAG,"onBindViewHolder() ProgressViewHolder 뷰가 보이는 순간 ");
        }

    }

    private void dialog_select(String cover, String title, String isbn) {
        AlertDialog.Builder msgBuilder = new AlertDialog.Builder(activity)
                .setTitle("읽을 책 선택")
                .setMessage("해당 책을 선택합니다.")
                .setPositiveButton("선택", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if(listener != null){
                            listener.onItemClickReturnBookInfo(cover, title, isbn);
                        }
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

    @Override
    public int getItemCount() { // 삭제하니 에러남
        return aladinBook.size();
    }


    public void setMoreLoading(boolean isMoreLoading) {
        this.isMoreLoading=isMoreLoading;
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


                totalItemCount = mLinearLayoutManager.getItemCount();
                visibleItemCount = recyclerView.getChildCount();
                firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition(); // 화면에 보이는
                lastVisibleItem = mLinearLayoutManager.findLastVisibleItemPosition();

                Log.e("total", totalItemCount + "");
                Log.e("visible", visibleItemCount + "");

                Log.e("first", firstVisibleItem + "");
                Log.e("last", lastVisibleItem + "");
                Log.e("last2", lastVisibleItemPosition + "");
                Log.e("isMoreLoading if문 전", String.valueOf(isMoreLoading));


//                if (!isMoreLoading && (totalItemCount - visibleItemCount)<= (firstVisibleItem + visibleThreshold)) { // error! 두 번씩 함수 호출되는 문제
//
//                    isMoreLoading = true;
//                    onLoadMoreListener.onLoadMore(); // 프로그래스 바 나오면서 list 추가됨
//                    Log.e("****맨 밑 if문1", String.valueOf(isMoreLoading));
//
//                }


                // 맨 밑에 닿는 곳
                if (!isMoreLoading && totalItemCount <= (lastVisibleItem + 1) && totalItemCount > 9 && dy > 0) {
                    if (onLoadMoreListener != null) {
                        Log.e("****맨 밑 22", String.valueOf(isMoreLoading));
                        onLoadMoreListener.onLoadMore(); // 인터페이스라서 book_search.class에서 구현함
                        isMoreLoading = true;
                    }
                }



                Log.e("isMoreLoading if문 후", String.valueOf(isMoreLoading));
            }


        });
    }


    // 뷰 홀더
    // #1 알라딘 책 정보
    class AladinnViewHolder extends RecyclerView.ViewHolder {

        ImageView img_cover;
        TextView tv_title, tv_author, tv_description, tv_start, tv_startDate, tv_finish, tv_finishDate;
        LinearLayout linear_Date;
        BaseRatingBar baseStar;

        //순서 1에서 뷰홀더를 리턴받았음 이제 해당 레이아웃에 손 댈 수 있게 되어서 find해주고 선언해서 사용하면 된다.
        public AladinnViewHolder(@NonNull View itemView) {
            super(itemView);

            // 책 검색
            img_cover = itemView.findViewById(R.id.img_cover);
            tv_title = itemView.findViewById(R.id.tv_title);
            tv_author = itemView.findViewById(R.id.tv_author);
            tv_description = itemView.findViewById(R.id.tv_description);

            // 서재
            linear_Date = itemView.findViewById(R.id.linear_Date);
            tv_start = itemView.findViewById(R.id.tv_start);
            tv_startDate = itemView.findViewById(R.id.tv_startDate);
            tv_finish = itemView.findViewById(R.id.tv_finish);
            tv_finishDate = itemView.findViewById(R.id.tv_finishDate);
            baseStar = itemView.findViewById(R.id.baseStar);
        }
    }


    // #2 프로그래스 바
    static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar pBar;
        public ProgressViewHolder(View v) {
            super(v);
            pBar = (ProgressBar) v.findViewById(R.id.pBar);
        }
    }
    
}