package com.example.mybooks.recyclerview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mybooks.R;
import com.example.mybooks.model.ClientInfo;

import java.util.ArrayList;

public class rv_chatting extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    /**
     * item : 채팅 1개
     * 타인, 내 채팅 구분하여 보이기
     */
    private final String TAG=this.getClass().getSimpleName();
    Context context;
    ArrayList<ClientInfo> clientList; //arraylist안에 생성자로 만든 n개의 데이터가 들어있다.
    private final int CHAT_MINE = 1;
    private final int CHAT_OTHER = 0;
    private int thisPosition = 0;

    // 생성자
    public rv_chatting(Context context, ArrayList<ClientInfo> client){
        this.context = context;
        this.clientList = client;
    }


    public int getPosition() {
        return thisPosition; // 여기서 리턴받아야 onCreateViewHolder()에서 구분이 가능함
    }


    @Override
    public int getItemViewType(int position) {
        Log.e("getItemViewType()", "position:"+position);
        thisPosition = position;
        if (clientList.get(position).getEmail().equals(context.getSharedPreferences("autoLogin", Activity.MODE_PRIVATE).getString("userEmail", null))){
            clientList.get(position).setViewType(CHAT_MINE); // 클라이언트 객체의 이멜이 나와 같으면 viewType을 1로 바꾼다(==주황으로바꿈)
        }
        return clientList.get(position).getViewType(); // 여기서 리턴받아야 onCreateViewHolder()에서 구분이 가능함
    }

    @NonNull
    @Override // 내가 만든 한 칸의 레이아웃을 메모리에 올려 객체화함
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.e("onCreateViewHolder()", "viewType:"+viewType);
        if (viewType == CHAT_MINE) {
            // #1 내 채팅(주황색)
            return new mineClientViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_chat_mine, parent, false));
        } else {
            // #2 타인 채인(회색)
            return new otherClientViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_chat_other, parent, false));
        }
    }



    // 바인드뷰홀더 : 뷰홀더에 들어갈 데이터만 바꿔준다.
    // 데이터가 들어간 뷰를 매번 인플레이트하고 새로 만드는게 아니라, 뷰를 그대로 쓰고(holder) arraylist에 있던 데이터만 set해준다.
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) { Log.e("clientList size:", String.valueOf(clientList.size()));
        /**
         * 표지, 제목
         */
        ClientInfo client = clientList.get(position); // 배열에서 꺼내온 책 객체를 view에 삽입
//        Log.e("onBindViewHolder() client:", String.valueOf(client));


        if (holder instanceof mineClientViewHolder) {
            Glide.with(context).load(client.getImg()).circleCrop().into(((mineClientViewHolder) holder).img_chat_mine); // 프사
            ((mineClientViewHolder) holder).tv_chat_name_mine.setText(client.getName()); // 닉네임
            ((mineClientViewHolder) holder).tv_chat_msg_mine.setText(client.getChat()); // 메세지
        } else {
            Glide.with(context).load(client.getImg()).circleCrop().into(((otherClientViewHolder) holder).img_chat); // 프사
            ((otherClientViewHolder) holder).tv_chat_name.setText(client.getName()); // 닉네임
            ((otherClientViewHolder) holder).tv_chat_msg.setText(client.getChat()); // 메세지
        }



    }



    @Override
    public int getItemCount() { // 삭제하니 에러남
        return clientList.size();
    }


    // #1 내 채팅 (주황)
    class mineClientViewHolder extends RecyclerView.ViewHolder {
        ImageView img_chat_mine;
        TextView tv_chat_name_mine, tv_chat_msg_mine;
        //순서 1에서 뷰홀더를 리턴받았음 이제 해당 레이아웃에 손 댈 수 있게 되어서 find해주고 선언해서 사용하면 된다.
        public mineClientViewHolder(@NonNull View itemView) {
            super(itemView);
            img_chat_mine = itemView.findViewById(R.id.img_chat_mine);
            tv_chat_name_mine = itemView.findViewById(R.id.tv_chat_name_mine);
            tv_chat_msg_mine = itemView.findViewById(R.id.tv_chat_msg_mine);
        }
    }
    // #2 타인 채팅 (회색)
    class otherClientViewHolder extends RecyclerView.ViewHolder {
        ImageView img_chat;
        TextView tv_chat_name, tv_chat_msg;
        //순서 1에서 뷰홀더를 리턴받았음 이제 해당 레이아웃에 손 댈 수 있게 되어서 find해주고 선언해서 사용하면 된다.
        public otherClientViewHolder(@NonNull View itemView) {
            super(itemView);
            img_chat = itemView.findViewById(R.id.img_chat);
            tv_chat_name = itemView.findViewById(R.id.tv_chat_name);
            tv_chat_msg = itemView.findViewById(R.id.tv_chat_msg);
        }
    }


}