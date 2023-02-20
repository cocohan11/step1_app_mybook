package com.example.mybooks.recyclerview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybooks.R;

import java.util.ArrayList;
import java.util.List;

public class rv_theme extends RecyclerView.Adapter<rv_theme.AladinnViewHolder> {
    /**
     * 가로로 "1주 동안" ~ "8주 동안" 선택하기
     */
    private final String TAG=this.getClass().getSimpleName();
    private Context context;
    private List<Integer> selectedItemIndexes = new ArrayList<>(); // 최대 2개까지만 선택된 인덱스가 들어감
    private int row_index = 99; // 인덱스값x //'모임 주제 설정(최대2개)'가 선택되어있지 않도록 시작
    private String[] theme;
    private OnItemsClickListener listener;


    // 생성자
    public rv_theme(Context context, String[] theme){
        this.context = context;
        this.theme = theme;
    }


    public interface OnItemsClickListener{
        void onItemClickReturnIndex(List<Integer> list);
    }
    public void setWhenClickListener(OnItemsClickListener listener){
        this.listener = listener;
    }

    @NonNull
    @Override // 내가 만든 한 칸의 레이아웃을 메모리에 올려 객체화함
    public AladinnViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); //inflate시켜주는 도구
        View itemView = inflater.inflate(R.layout.rv_item_theme, parent, false); //내가 만든 한 칸 뷰를 객체화함
        Log.e("홀더 context ", String.valueOf(context));


        return new AladinnViewHolder(itemView); //이제 re_sharing_room 레이아웃을 손댈 수 있게 됨
    }



    // 바인드뷰홀더 : 뷰홀더에 들어갈 데이터만 바꿔준다.
    // 데이터가 들어간 뷰를 매번 인플레이트하고 새로 만드는게 아니라, 뷰를 그대로 쓰고(holder) arraylist에 있던 데이터만 set해준다.
    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    @Override
    public void onBindViewHolder(@NonNull AladinnViewHolder holder, @SuppressLint("RecyclerView") int position) {

        String thisTheme = theme[position];
        holder.tv_theme.setBackgroundDrawable(context.getDrawable(R.drawable.round_square_gray_wrap));
        holder.tv_theme.setText(thisTheme);
        holder.tv_theme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { Log.e("onClick()", "\nposition:"+position);

                if (selectedItemIndexes.size() == 0) {
                    selectedItemIndexes.add(position);

                // 최대 2개 선택한 경우
                } else if (selectedItemIndexes.size() == 1 && selectedItemIndexes.get(0) != position) {
                    selectedItemIndexes.add(position);
                } else if (selectedItemIndexes.get(0) == position) { // 이미주황색인걸
                    selectedItemIndexes.remove(0); // 삭제하고 회색으로 만들기
                } else if (selectedItemIndexes.get(1) == position) {
                    selectedItemIndexes.remove(1);
                } else if (selectedItemIndexes.size() == 2 && selectedItemIndexes.get(0) != position && selectedItemIndexes.get(1) != position) {
                    Toast.makeText(context, "2개까지만 선택 가능합니다.", Toast.LENGTH_SHORT).show();
                }
                Log.e("onClick()", "selectedItemIndexes:"+selectedItemIndexes);
                notifyDataSetChanged();
                if(listener != null){
                    listener.onItemClickReturnIndex(selectedItemIndexes);
                }
            }
        });


        if (selectedItemIndexes.size() == 1) {
            Log.e("size()", "1");
            Log.e("onClick() 뒤에", "selectedItemIndexes.get(0):"+selectedItemIndexes.get(0));
            Log.e("onClick() 뒤에", "position:"+position);
            if(selectedItemIndexes.get(0)==position) {
                Log.e("선택값과 데이터가 같다면 뷰를 주황으로 변경한다", "true");
                holder.tv_theme.setBackgroundDrawable(context.getDrawable(R.drawable.round_square_orange_wrap));
            }
        }
        if (selectedItemIndexes.size() == 2) {
            Log.e("size()", "2");
            Log.e("onClick() 뒤에", "selectedItemIndexes.get(0):"+selectedItemIndexes.get(0));
            Log.e("onClick() 뒤에", "selectedItemIndexes.get(1):"+selectedItemIndexes.get(1));
            Log.e("onClick() 뒤에", "position:"+position);
            if(selectedItemIndexes.get(0)==position) {
                holder.tv_theme.setBackgroundDrawable(context.getDrawable(R.drawable.round_square_orange_wrap));
                Log.e("00 선택값과 데이터가 같다면 뷰를 주황으로 변경한다", "true");
            }
            if(selectedItemIndexes.get(1)==position) {
                holder.tv_theme.setBackgroundDrawable(context.getDrawable(R.drawable.round_square_orange_wrap));
                Log.e("11 선택값과 데이터가 같다면 뷰를 주황으로 변경한다", "true");
            }
        }
    }

    @Override
    public int getItemCount() { // 삭제하니 에러남
        return theme.length;
    }

    public int getIndex() { // 삭제하니 에러남
        return row_index;
    }

    class AladinnViewHolder extends RecyclerView.ViewHolder {

        TextView tv_theme;

        //순서 1에서 뷰홀더를 리턴받았음 이제 해당 레이아웃에 손 댈 수 있게 되어서 find해주고 선언해서 사용하면 된다.
        public AladinnViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_theme = itemView.findViewById(R.id.tv_theme);
        }
    }
}