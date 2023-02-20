package com.example.mybooks.recyclerview;

import static com.example.mybooks.recyclerview.rv_showNotes.isMoreLoadingNote;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mybooks.R;
import com.example.mybooks.club.showClubDetail;
import com.example.mybooks.model.Club;
import com.example.mybooks.retrofit.HttpRequest;
import com.example.mybooks.retrofit.RetrofitClient;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;

public class rv_searchClubs extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    /**
     * 나의 독서 모임
     */
    private final String TAG=this.getClass().getSimpleName();
    private Context context, activity;
    private ArrayList<Club> clubs;


    // 스크롤 페이징
    private final int VIEW_ITEM = 1;
    private final int VIEW_NEXT = 0;
//    private int itemPosition;
    private OnLoadMoreListener2 OnLoadMoreListener2;
    private String purpose; // new인지 asc인지에 따라 다르게 db에서 데이터받음
    private int test = 0; // 몇 번 추가되는지 알기위함
    private int firstVisibleItem, visibleItemCount, totalItemCount, lastVisibleItem; // 맨옆 알기위함
    private LinearLayoutManager mLinearLayoutManager; // 위의 int들을 알아내기 위함



    // 생성자
    public rv_searchClubs(Context context, Context activity, ArrayList<Club> clubs, String purpose, OnLoadMoreListener2 onLoadMoreListener2){
        this.context = context;
        this.activity = activity;
        this.clubs = clubs;
        this.purpose = purpose;
        this.OnLoadMoreListener2 = onLoadMoreListener2; // 페이징 인터페이스 리스너 전달
    }

    // 페이징
    public void setLinearLayoutManager(LinearLayoutManager linearLayoutManager){
        this.mLinearLayoutManager=linearLayoutManager;
    }

    // 페이징
    // 인터페이스 book_search.class에 장착하기
    public interface OnLoadMoreListener2{
        void onLoadMore2(String purpose);
    }


    @Override
    public int getItemViewType(int position) {
        Log.e("getItemViewType()", "position:"+position);

        return clubs.get(position) != null ? VIEW_ITEM : VIEW_NEXT; // 책정보 뷰 or 프로그래스 바
    }



    @Override // 내가 만든 한 칸의 레이아웃을 메모리에 올려 객체화함
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.e("홀더 context ", String.valueOf(context));
        if (viewType == VIEW_ITEM) {
            Log.e("#1 모임 정보", "");
            return new AladinnViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_search_clubs, parent, false));
        } else {
            Log.e("#2 프로그래스 바", "");
            return new ProgressViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_progress_horiz, parent, false));
        }
        /*
            1. 한 칸의 뷰를 처음 만들어내는 곳(이후부턴 재사용)
            2. 의사코드)
            3. if 맨 뒤 포지션이면
            4. '> 버튼' 노출, arr에서 '>' 삭제, 배열로 받아온 item들 추가
            5. >> 바인드뷰홀더에서 구현예정
        */
    }



    // 바인드뷰홀더 : 뷰홀더에 들어갈 데이터만 바꿔준다.
    // 데이터가 들어간 뷰를 매번 인플레이트하고 새로 만드는게 아니라, 뷰를 그대로 쓰고(holder) arraylist에 있던 데이터만 set해준다.
    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        /**
         * 알라딘 아이템    or    next 버튼
         */
        if (holder instanceof AladinnViewHolder) {
            Club club = clubs.get(position);
            setItems_And_clickEvent(club, (AladinnViewHolder) holder);
        }


        //
        // 1. 마지막 채팅 삽입예정
        // 2. 안 읽은 메세지 갯수
        // 3. 메세지 시간

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

                Log.e("dx", dx + ""); // << -   + >>
                Log.e("dy", dy + ""); // 세로 0
                Log.e("total", totalItemCount + ""); // 5
                Log.e("visible", visibleItemCount + ""); // 3~4 보이는 갯수
                Log.e("first", firstVisibleItem + ""); // 0~1 완전히 가려버린 인덱스
                Log.e("last", lastVisibleItem + ""); // 2 지나가는 중인 인덱스. 시작할 때
                Log.e("last2", lastVisibleItemPosition + ""); // 1 완전히 다 지나간 마지막 인덱스
                Log.e("isMoreLoadingNote if문 전", String.valueOf(isMoreLoadingNote));
                Log.e("**** ", "0");


                // 맨 밑에 닿는 곳
                if (OnLoadMoreListener2 != null) {
                    Log.e("**** ", "1");
                    Log.e("**** ", "isMoreLoadingNote:"+isMoreLoadingNote);

                    if (!isMoreLoadingNote
                        && totalItemCount <= (lastVisibleItem + 1)
                        && totalItemCount >= 5
                        && dx > 0) {

                        Log.e("**** ", "2");
                        int b = totalItemCount%5; // 나머지로 10단위인지 알아내기
                        Log.e("**** b:", String.valueOf(b));
                        if (b==0 && totalItemCount!=0) { // 나머지가 0이어야 5단위임
                            Log.e("**** ", "3");
                            OnLoadMoreListener2.onLoadMore2(purpose); // 인터페이스라서 book_search.class에서 구현함
                            isMoreLoadingNote = true;
                        } else {
                            Log.e("**** else", "isMoreLoadingNote:"+isMoreLoadingNote);
                        }
                    }
                }
            }


        });
    }



    private void setItems_And_clickEvent(Club club, AladinnViewHolder holder) {
        Glide.with(context).load(club.getImageUrl()).into(holder.img_coverUrl_club); // 책 표지
        String joinerNum = club.getTurnout()+"/"+club.getFixed_num();
        holder.tv_count_club.setText(joinerNum); // "1/4"
        holder.tv_name_club.setText(club.getName()); // 모임이름
        holder.tv_intro_club.setText(club.getIntroduction());
        Log.e("club.getTheme():", club.getTheme());


        String[] themeIndexList = club.getTheme().split(",");
        Log.e("themeIndexList.length:", String.valueOf(themeIndexList.length));
        if (themeIndexList.length == 1) {
            int aa = Integer.parseInt(themeIndexList[0]);
            holder.tv_theme_club1.setText(club.getThemeList()[aa]); // 14개 중 인덱스13의 테마
        } else if (themeIndexList.length == 2) {
            int aa = Integer.parseInt(themeIndexList[1]);
            holder.tv_theme_club1.setText(club.getThemeList()[aa]); // 14개 중 인덱스13의 테마
        }


        // 클릭 -> 모임 상세조회
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, showClubDetail.class); // 이동
                intent.putExtra("club", club);
                activity.startActivity(intent);
                Log.e(TAG, "startActivity() \n"+TAG+" -> showClubDetail.class ");
            }
        });
    }

    @Override
    public int getItemCount() { // 삭제하니 에러남
        return clubs.size();
    }


    // #1 알라딘 책 정보
    class AladinnViewHolder extends RecyclerView.ViewHolder {

        ImageView img_coverUrl_club;
        TextView tv_ages_club, tv_count_club, tv_name_club, tv_intro_club, tv_theme_club1, tv_theme_club2;

        //순서 1에서 뷰홀더를 리턴받았음 이제 해당 레이아웃에 손 댈 수 있게 되어서 find해주고 선언해서 사용하면 된다.
        public AladinnViewHolder(@NonNull View itemView) {
            super(itemView);
            img_coverUrl_club = itemView.findViewById(R.id.img_coverUrl_club);
            tv_ages_club = itemView.findViewById(R.id.tv_ages_club);
            tv_count_club = itemView.findViewById(R.id.tv_count_club);
            tv_name_club = itemView.findViewById(R.id.tv_name_club);
            tv_intro_club = itemView.findViewById(R.id.tv_intro_club);
            tv_theme_club1 = itemView.findViewById(R.id.tv_theme_club1);
            tv_theme_club2 = itemView.findViewById(R.id.tv_theme_club2);
        }
    }


    // #2 프로그래스 바
    class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar pBar2;
        public ProgressViewHolder(View v) {
            super(v);
            pBar2 = (ProgressBar) v.findViewById(R.id.pBar2);
        }
    }

}