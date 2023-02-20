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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.menu.MenuView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mybooks.R;
import com.example.mybooks.club.chatting;
import com.example.mybooks.home.showBookDetail;
import com.example.mybooks.model.ClientInfo;
import com.example.mybooks.model.Club;
import com.example.mybooks.model.Response;
import com.example.mybooks.retrofit.HttpRequest;
import com.example.mybooks.retrofit.RetrofitClient;
import com.example.mybooks.socket.myService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class rv_myClub extends RecyclerView.Adapter<rv_myClub.AladinnViewHolder> {
    /**
     * 나의 독서 모임
     */
    private final String TAG=this.getClass().getSimpleName();
    private Context context, activity;
    private ArrayList<Club> clubs;


    // 생성자
    public rv_myClub(Context context, Context activity, ArrayList<Club> clubs){
        this.context = context;
        this.activity = activity;
        this.clubs = clubs;
    }



    @NonNull
    @Override // 내가 만든 한 칸의 레이아웃을 메모리에 올려 객체화함
    public AladinnViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); //inflate시켜주는 도구
        View itemView = inflater.inflate(R.layout.rv_item_myclub, parent, false); //내가 만든 한 칸 뷰를 객체화함
        Log.e("홀더 context ", String.valueOf(context));


        return new AladinnViewHolder(itemView); //이제 re_sharing_room 레이아웃을 손댈 수 있게 됨
    }



    // 바인드뷰홀더 : 뷰홀더에 들어갈 데이터만 바꿔준다.
    // 데이터가 들어간 뷰를 매번 인플레이트하고 새로 만드는게 아니라, 뷰를 그대로 쓰고(holder) arraylist에 있던 데이터만 set해준다.
    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    @Override
    public void onBindViewHolder(@NonNull AladinnViewHolder holder, @SuppressLint("RecyclerView") int position) {

        Club club = clubs.get(position);


        Glide.with(context).load(club.getImageUrl()).into(holder.img_cover); // 책 표지
        String joinerNum = club.getTurnout()+"/"+club.getFixed_num();
        holder.tv_total.setText(joinerNum); // "1/4"
        holder.tv_title_club.setText(club.getName()); // 모임이름
        if (club.getIntroduction() != null) {
            holder.tv_chat_club.setText(club.getIntroduction()); // 메세지 미리보기(변수명 변경해야함)
        }


        // 화면 이동
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, chatting.class);
                intent.putExtra("id",club.getId());
                intent.putExtra("name",club.getName());
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                Log.e(TAG, "club.getId():"+club.getId());
                Log.e(TAG, "startActivity() \n"+TAG+" -> showBookDetail.class ");
            }
        });


        // 다이얼로그 - 모임 나가기
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) { Log.e(TAG, "onLongClick");


                dialog_leave(activity, position, club.getId());
                return false;
            }
        });


        //
        //
        //
        // 1. 마지막 채팅 삽입예정
        // 2. 안 읽은 메세지 갯수
        // 3. 메세지 시간

    }



    private void sendService(Context context, String id) { Log.e(TAG, "sendServiceForEntry()");
        Intent intent = new Intent(context, myService.class);
        ClientInfo client = new chatting().returnMyInfoToClient(context, id, "퇴장", "bye");
        Log.e(TAG, "sendServiceForEntry() client:"+client);


        intent.putExtra("clubNum", client.getClubNum());
        intent.putExtra("purpose", client.getPurpose());
        intent.putExtra("email", client.getEmail());
        intent.putExtra("name", client.getName());
        intent.putExtra("img", client.getImg());
        intent.putExtra("chat", client.getChat());
        context.startService(intent); // 서비스 시작되는 곳
    }



    private void dialog_leave(Context activity, int removeIndex, String id) {
        AlertDialog.Builder msgBuilder = new AlertDialog.Builder(activity)
                .setTitle("모임 나가기")
                .setMessage("정말 모임을 나가시겠습니까?")
                .setPositiveButton("나가기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.e(TAG, "나가기 클릭/ removeIndex:"+removeIndex);


                        leaveClub_retrofit_delete(context.getSharedPreferences("autoLogin", Activity.MODE_PRIVATE).getString("userEmail", null), id);
                        sendService(context, id);
                        clubs.remove(removeIndex);
                        notifyItemRemoved(removeIndex);
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



    // 퇴장 retrofit
    private void leaveClub_retrofit_delete(String email, String id) {
        RetrofitClient retrofitClient = RetrofitClient.getInstance(); // 요청 인터페이스, 빌드, 로그 한 데 모아둔 클래스
        HttpRequest httpRequest = retrofitClient.getRetrofitInterface(); // 에서 꺼내쓴다.
        // retrofit
        // id로 클럽 나가기 (delete)
        // para(id, email)
        // 요청 메소드 이름 : getLeaveClub
        httpRequest.getLeaveClub(email, id)
                .enqueue(new Callback<Response>() {
                    @Override
                    public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                        Response response1 = response.body();
                        assert response1 != null;
                        Log.e(TAG, "response1() "+response1);
                        if (response1.isResponse()) {


                            Toast.makeText(context, "퇴장했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<Response> call, Throwable t) {
                        Log.e(TAG, "onFailure() "+t.getMessage());
                    }
                });
    }


    @Override
    public int getItemCount() { // 삭제하니 에러남
        return clubs.size();
    }

    class AladinnViewHolder extends RecyclerView.ViewHolder {

        ImageView img_cover;
        TextView tv_total, tv_title_club, tv_readYet, tv_chat_club, time_club;

        //순서 1에서 뷰홀더를 리턴받았음 이제 해당 레이아웃에 손 댈 수 있게 되어서 find해주고 선언해서 사용하면 된다.
        public AladinnViewHolder(@NonNull View itemView) {
            super(itemView);

            img_cover = itemView.findViewById(R.id.img_cover_club);
            tv_title_club = itemView.findViewById(R.id.tv_title_club);
            tv_total = itemView.findViewById(R.id.tv_total_club);
            tv_readYet = itemView.findViewById(R.id.tv_readYet);
            tv_chat_club = itemView.findViewById(R.id.tv_chat_club);
            time_club = itemView.findViewById(R.id.time_club);
        }
    }
}