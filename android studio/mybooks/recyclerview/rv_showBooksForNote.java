package com.example.mybooks.recyclerview;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mybooks.R;
import com.example.mybooks.home.showBookDetail;
import com.example.mybooks.model.AladinBook;
import com.example.mybooks.note.showNote;
import com.willy.ratingbar.BaseRatingBar;

import java.util.ArrayList;

public class rv_showBooksForNote extends RecyclerView.Adapter<rv_showBooksForNote.AladinnViewHolder> {
    /**
     * 노트 첫 화면에서, 노트가 있는 책을 보여주는 rv adapter
     */
    private final String TAG=this.getClass().getSimpleName();
    Context context;
    ArrayList<AladinBook> aladinBook; //arraylist안에 생성자로 만든 n개의 데이터가 들어있다.


    // 생성자
    public rv_showBooksForNote(Context context, ArrayList<AladinBook> aladinBook){
        this.context = context;
        this.aladinBook = aladinBook;
    }



    @NonNull
    @Override // 내가 만든 한 칸의 레이아웃을 메모리에 올려 객체화함
    public AladinnViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); //inflate시켜주는 도구
        View itemView = inflater.inflate(R.layout.rv_item_show_books_for_note, parent, false); //내가 만든 한 칸 뷰를 객체화함
        Log.e("홀더 context ", String.valueOf(context));

        return new AladinnViewHolder(itemView); //이제 re_sharing_room 레이아웃을 손댈 수 있게 됨
    }



    // 바인드뷰홀더 : 뷰홀더에 들어갈 데이터만 바꿔준다.
    // 데이터가 들어간 뷰를 매번 인플레이트하고 새로 만드는게 아니라, 뷰를 그대로 쓰고(holder) arraylist에 있던 데이터만 set해준다.
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull AladinnViewHolder holder, int position) {
        /**
         * 표지, 제목, 별점, 메모갯수, 날짜 (+isbn)
         */
        AladinBook aladin = aladinBook.get(position); // 배열에서 꺼내온 책 객체를 view에 삽입


        String date = aladin.getDate().substring(2,10);
        if (aladin.getRating() != 0) {
            holder.ratingBar.setRating(aladin.getRating()); // 별점
        } else {
            holder.ratingBar.setVisibility(View.GONE);
        }
        Glide.with(context).load(aladin.getCover()).into(holder.img_cover); // 책 표지
        holder.tv_title_note.setText(aladin.getTitle()); // 책 제목
        holder.countNote.setText("노트 "+aladin.getCountNote()); // 노트 갯수
        holder.date_note.setText(date); // 날짜 (23-01-11)
        holder.itemView.setOnClickListener(new View.OnClickListener() { // 전체 클릭되는건가??
            @Override
            public void onClick(View v) { Log.e(TAG,"itemView.setOnClickListener() ");


                // 화면 이동
                Intent intent = new Intent(context, showNote.class); // 화면 이동
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);

                // Library table에서 id값 받아와서 intent로 넘기기
                intent.putExtra("isbn", aladin.getIsbn());
                intent.putExtra("countNote", aladin.getCountNote());
                context.startActivity(intent);
                Log.e(TAG, "aladin.getIsbn()"+aladin.getIsbn());
                Log.e(TAG, "aladin.getCountNote()"+aladin.getCountNote());
                Log.e(TAG, "startActivity() \n"+TAG+" -> showNote.class ");

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
        TextView tv_title_note, countNote, date_note;
        BaseRatingBar ratingBar;

        //순서 1에서 뷰홀더를 리턴받았음 이제 해당 레이아웃에 손 댈 수 있게 되어서 find해주고 선언해서 사용하면 된다.
        public AladinnViewHolder(@NonNull View itemView) {
            super(itemView);

            // 책 검색
            img_cover = itemView.findViewById(R.id.img_cover_note);
            tv_title_note = itemView.findViewById(R.id.tv_title_note);
            ratingBar = itemView.findViewById(R.id.baseStar_note);
            countNote = itemView.findViewById(R.id.countNote);
            date_note = itemView.findViewById(R.id.date_note);

        }
    }


}