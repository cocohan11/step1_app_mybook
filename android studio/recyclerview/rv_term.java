package com.example.mybooks.recyclerview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybooks.R;

import java.util.List;

public class rv_term extends RecyclerView.Adapter<rv_term.AladinnViewHolder> {
    /**
     * 가로로 "1주 동안" ~ "8주 동안" 선택하기
     */
    private final String TAG=this.getClass().getSimpleName();
    private Context context;
    private int row_index = 0; // "1주 동안"이 디폴트로 선택
    private List<String> week;
    private OnItemsClickListener listener;


    // 생성자
    public rv_term(Context context, List<String> week){
        this.context = context;
        this.week = week;
    }


    public interface OnItemsClickListener{
        void onItemClickReturnIndex(int index);
    }
    public void setWhenClickListener(OnItemsClickListener listener){
        this.listener = listener;
    }

    @NonNull
    @Override // 내가 만든 한 칸의 레이아웃을 메모리에 올려 객체화함
    public AladinnViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); //inflate시켜주는 도구
        View itemView = inflater.inflate(R.layout.rv_item_term, parent, false); //내가 만든 한 칸 뷰를 객체화함
        Log.e("홀더 context ", String.valueOf(context));


        return new AladinnViewHolder(itemView); //이제 re_sharing_room 레이아웃을 손댈 수 있게 됨
    }



    // 바인드뷰홀더 : 뷰홀더에 들어갈 데이터만 바꿔준다.
    // 데이터가 들어간 뷰를 매번 인플레이트하고 새로 만드는게 아니라, 뷰를 그대로 쓰고(holder) arraylist에 있던 데이터만 set해준다.
    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    @Override
    public void onBindViewHolder(@NonNull AladinnViewHolder holder, @SuppressLint("RecyclerView") int position) {

        String thisWeek = week.get(position);
        holder.tv_term.setText(thisWeek);
        holder.tv_term.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { Log.e("onClick()", "row_index:"+row_index+"\nposition:"+position);
                row_index=position;
                notifyDataSetChanged();

                if(listener != null){
                    listener.onItemClickReturnIndex(position);
                }
            }
        });

        if(row_index==position){
            Log.e("onBindViewHolder()", "row_index==position");
//            holder.tv_term.setBackgroundColor(Color.parseColor("#567845"));
            holder.tv_term.setBackgroundDrawable(context.getDrawable(R.drawable.round_square_orange_wrap));
        }
        else
        {
            Log.e("onBindViewHolder()", "else");
//            holder.tv_term.setBackgroundColor(Color.parseColor("#ffffff"));
            holder.tv_term.setBackgroundDrawable(context.getDrawable(R.drawable.round_square_gray_wrap));
        }

    }

    @Override
    public int getItemCount() { // 삭제하니 에러남
        return week.size();
    }

    public int getIndex() { // 삭제하니 에러남
        return row_index;
    }

    class AladinnViewHolder extends RecyclerView.ViewHolder {

        TextView tv_term;

        //순서 1에서 뷰홀더를 리턴받았음 이제 해당 레이아웃에 손 댈 수 있게 되어서 find해주고 선언해서 사용하면 된다.
        public AladinnViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_term = itemView.findViewById(R.id.tv_term);
        }
    }
}