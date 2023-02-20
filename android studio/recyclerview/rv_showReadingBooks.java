package com.example.mybooks.recyclerview;


import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import static com.example.mybooks.home.saveBook.longToStringDate;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mybooks.R;
import com.example.mybooks.home.showBookDetail;
import com.example.mybooks.model.AladinBook;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.willy.ratingbar.BaseRatingBar;

import java.util.ArrayList;

public class rv_showReadingBooks extends RecyclerView.Adapter<rv_showReadingBooks.AladinnViewHolder> {
    /**
     * 홈에서 읽는 중인 책 2개씩만 조회하는 rv adapter
     */
    private final String TAG=this.getClass().getSimpleName();
    Context context;
    ArrayList<AladinBook> aladinBook; //arraylist안에 생성자로 만든 n개의 데이터가 들어있다.


    // 생성자
    public rv_showReadingBooks(Context context, ArrayList<AladinBook> aladinBook){
        this.context = context;
        this.aladinBook = aladinBook;
    }




    @NonNull
    @Override // 내가 만든 한 칸의 레이아웃을 메모리에 올려 객체화함
    public AladinnViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); //inflate시켜주는 도구
        View itemView = inflater.inflate(R.layout.rv_show_reading_books, parent, false); //내가 만든 한 칸 뷰를 객체화함
        Log.e("홀더 context ", String.valueOf(context));

        return new AladinnViewHolder(itemView); //이제 re_sharing_room 레이아웃을 손댈 수 있게 됨
    }



    // 바인드뷰홀더 : 뷰홀더에 들어갈 데이터만 바꿔준다.
    // 데이터가 들어간 뷰를 매번 인플레이트하고 새로 만드는게 아니라, 뷰를 그대로 쓰고(holder) arraylist에 있던 데이터만 set해준다.
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull AladinnViewHolder holder, int position) {
        /**
         * 표지, 제목
         */
        AladinBook aladin = aladinBook.get(position); // 배열에서 꺼내온 책 객체를 view에 삽입


        Glide.with(context).load(aladin.getCover()).into(holder.img_cover); // 책 표지
        holder.tv_title.setText(aladin.getTitle()); // 책 제목
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { Log.e(TAG,"itemView.setOnClickListener() ");


                // 화면 이동
                Intent intent = new Intent(context, showBookDetail.class); // 화면 이동
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);

                intent.putExtra("title", aladin.getTitle());
                intent.putExtra("isbn", aladin.getIsbn());
                intent.putExtra("cover", aladin.getCover());
                intent.putExtra("author", aladin.getAuthor());
                intent.putExtra("description", aladin.getDescription());
                intent.putExtra("publisher", aladin.getPublisher());
                intent.putExtra("bookState", aladin.getBookState());
                intent.putExtra("rating", aladin.getRating());
                intent.putExtra("startDate", aladin.getStartDate());
                intent.putExtra("finishDate", aladin.getFinishDate());

                context.startActivity(intent);
                Log.e(TAG, "startActivity() \n"+TAG+" -> showBookDetail.class ");

            }
        });

    }


    @Override
    public int getItemCount() { // 삭제하니 에러남
        return aladinBook.size();
    }



    // 뷰 홀더
    // #1 알라딘 책 정보
    class AladinnViewHolder extends RecyclerView.ViewHolder {

        ImageView img_cover;
        TextView tv_title;

        //순서 1에서 뷰홀더를 리턴받았음 이제 해당 레이아웃에 손 댈 수 있게 되어서 find해주고 선언해서 사용하면 된다.
        public AladinnViewHolder(@NonNull View itemView) {
            super(itemView);

            // 책 검색
            img_cover = itemView.findViewById(R.id.img_cover_home);
            tv_title = itemView.findViewById(R.id.tv_title_home);

        }
    }


}