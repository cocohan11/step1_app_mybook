package com.example.mybooks.club;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mybooks.databinding.ActivityChattingBinding;
import com.example.mybooks.home_bottom_1to5.clubActivity;
import com.example.mybooks.model.ClientInfo;
import com.example.mybooks.model.Club;
import com.example.mybooks.model.Response;
import com.example.mybooks.note.writeNote;
import com.example.mybooks.recyclerview.rv_chatting;
import com.example.mybooks.recyclerview.rv_myClub;
import com.example.mybooks.recyclerview.rv_searchClubs;
import com.example.mybooks.recyclerview.rv_term;
import com.example.mybooks.recyclerview.rv_theme;
import com.example.mybooks.retrofit.HttpRequest;
import com.example.mybooks.retrofit.RetrofitClient;
import com.example.mybooks.socket.myService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import static com.example.mybooks.socket.myService.isChatShow;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.security.auth.callback.CallbackHandler;

import retrofit2.Call;
import retrofit2.Callback;

public class chatting extends AppCompatActivity {
    /**
     * ????????? ??????
     */
    private ActivityChattingBinding binding;
    private final String TAG = this.getClass().getSimpleName();
    private rv_chatting rvAdapter_chat; // ???????????????
    private ArrayList<ClientInfo> chatList = new ArrayList<>();
    private ClientInfo client;
    private Handler handler = new Handler();
    private ResultReceiver resultReceiver = new ResultReceiver(handler){
        /**
         * ??????????????? ???????????? ?????? ??????
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            Log.e(TAG, "onReceiveResult() resultData:"+resultData);
            client = returnClientForBundle((resultData));
            Log.e(TAG, "onReceiveResult() client : "+client);


            addChatToList(rvAdapter_chat, chatList, client); // ?????? ??????!!
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) { Log.e(TAG, "onCreate() ");
        super.onCreate(savedInstanceState);
        binding = ActivityChattingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        /** set data, view.. **/
        setClubName(binding.tvNameClub);
        sendServiceFor??????or??????(); // adapter ??????????????? set???????????? ?????? ??????


        /** ????????? **/
        btn_click_sendMsg(binding.btnChatSend, binding.etChat); // ?????? ?????????
        paging_getChattingMore(binding.rvChat);


    } // ~onCreate()



    private void paging_getChattingMore(RecyclerView rv) {
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);


                // ????????? ??????
                if (newState == RecyclerView.SCROLL_STATE_SETTLING
                        && rvAdapter_chat.getPosition() == chatList.size()-1 // ?????? 1????????? x
                        && chatList.size() > 13
                        && chatList.size() % 10 == 0) { // ??? ????????? ???????????? ???
                    Log.e(TAG, "?????????/????????? ??????, ?????????0 ????????????");


                    // ?????????????????? ?????? ????????????
                    getChatting(getIntent().getStringExtra("id"), chatList.size());
                }
            }
        });
    }


    // ????????? ??? db?????? ?????? ?????? ????????????
    private void getChatting(String id, int index) { // ???????????? ?????? ????????? ?????? ????????? ???????????? UI??? ????????????
        Log.e(TAG, "getChatting() id : "+id); // 54
        RetrofitClient retrofitClient = RetrofitClient.getInstance();
        HttpRequest httpRequest = retrofitClient.getRetrofitInterface();
        // ?????? ????????? ?????? : getChatting
        httpRequest.getChatting(
                getSharedPreferences("autoLogin", Activity.MODE_PRIVATE).getString("userEmail", null)
                , id
                , index
        ).enqueue(new Callback<ArrayList<ClientInfo>>() { // ???????????? 3??????????????? ???????????? ????????? ??????/?????? // ?????????
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<ArrayList<ClientInfo>> call, retrofit2.Response<ArrayList<ClientInfo>> response) {


                if (index == 0) { // ???????????????
                    chatList = response.body();
                    recyclerview_setAdapter(binding.rvChat, chatList);
                    Log.e(TAG, "<20");

                } else {
                    ArrayList<ClientInfo> arr = response.body();
                    chatList.addAll(arr);
                    rvAdapter_chat.notifyDataSetChanged(); // ????????? ?????? ??? ??????
                    Log.e(TAG, "20~");
                }
                Log.e(TAG, "chatList ??????: "+chatList.size());
            }
            @Override
            public void onFailure(Call<ArrayList<ClientInfo>> call, Throwable t) {
                Log.e(TAG, "onFailure() "+t.getMessage());
                recyclerview_setAdapter(binding.rvChat, chatList);
            }
        });
    }



    private void setClubName(TextView tv) {
        if (getIntent().getStringExtra("name") != null) {
            Log.e(TAG, "club name:"+getIntent().getStringExtra("name"));
            tv.setText(getIntent().getStringExtra("name"));
        }
    }



    private void sendServiceFor??????or??????() { Log.e(TAG, "sendServiceFor??????or??????()");
        /*
            ?????? : resultReceiver??? ?????????, ????????? ??????
            ?????? : resultReceiver??? ?????????, ?????? ?????? ????????????
         */


        if (getIntent().getStringExtra("purpose") != null ) {
            // showClubDetail -> this
            sendService(resultReceiver, getApplicationContext(), getIntent().getStringExtra("purpose"), getIntent().getStringExtra("id"),"ee"); // ??????
            recyclerview_setAdapter(binding.rvChat, chatList);
            Log.e(TAG, ":??????");


        } else {
            // rv_myClub -> this
            // notification -> this
            getChatting(getIntent().getStringExtra("id"), 0);
            sendService(resultReceiver, getApplicationContext(),"??????", getIntent().getStringExtra("id"),getIntent().getStringExtra("chat")); // ??????
            Log.e(TAG, ":??????");
        }
    }




    public void sendService(ResultReceiver resultReceiver, Context context, String purpose, String id, String chat) { Log.e(TAG, "sendServiceForEntry()");
        Intent intent = new Intent(context, myService.class);
        ClientInfo client = new chatting().returnMyInfoToClient(context, id, purpose, chat);
        Log.e(TAG, "sendServiceForEntry() client:"+client);


        intent.putExtra("clubNum", client.getClubNum());
        intent.putExtra("purpose", purpose);
        intent.putExtra("email", client.getEmail());
        intent.putExtra("name", client.getName());
        intent.putExtra("img", client.getImg());
        intent.putExtra("chat", client.getChat());
        intent.putExtra("RECEIVER", resultReceiver); //(2)
        context.startService(intent); // ????????? ???????????? ???
    }






    private void btn_click_sendMsg(Button btn, EditText et) {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String chat = binding.etChat.getText().toString();
                Log.e(TAG, "click chat:" + chat);
                et.setText(""); // ????????? ?????????


                if (chat.equals("exit")) { // ~~~~ ?????? ??????
                    Log.e(TAG, "???????????? ???????????????.");
                } else {
                    if (getIntent().getStringExtra("id") != null) {
                        sendService(resultReceiver, getApplicationContext(), "??????", getIntent().getStringExtra("id"), chat);
                    }
                }
            }
        });
    }



    public ClientInfo returnMyInfoToClient(Context context, String id, String purpose, String chat) {
        Log.e("returnMyInfoToClient()", "chat:"+chat);
        return new ClientInfo(
                null
                , id
                , purpose
                , context.getSharedPreferences("autoLogin", Activity.MODE_PRIVATE).getString("userEmail", null)
                , context.getSharedPreferences("autoLogin", Activity.MODE_PRIVATE).getString("userName", null)
                , context.getSharedPreferences("autoLogin", Activity.MODE_PRIVATE).getString("userImg", null)
                , chat
                , 0
        );
    }



    public ClientInfo returnClientForBundle(Bundle resultData) {
        Log.e(TAG, "returnClientForBundle() resultData:"+resultData);
        return new ClientInfo(null
                , resultData.getString("clubNum")
                , resultData.getString("purpose")
                , resultData.getString("email")
                , resultData.getString("name")
                , resultData.getString("img")
                , resultData.getString("chat")
                , 0
        );
    }



    private void addChatToList(rv_chatting rvAdapter, ArrayList<ClientInfo> list, ClientInfo client) {
        handler.post(new Runnable() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void run() {
//                list.add(client); // ui??? ?????????
                list.add(0, client); // ????????? ??????????????? 0????????? ????????? ???'
//                binding.rvChat.smoothScrollToPosition(0);
                rvAdapter.notifyDataSetChanged();
                Log.e(TAG, "addChatToList() list.size"+list.size());
            }
        });
    }


    // rv ??????
    @SuppressLint("NotifyDataSetChanged")
    private void recyclerview_setAdapter(RecyclerView rv, ArrayList<ClientInfo> list) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, true);
//        layoutManager.setStackFromEnd(true);
        rvAdapter_chat = new rv_chatting(getApplicationContext(), list); // rv ????????? ?????? ??????
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(rvAdapter_chat);
        Log.e(TAG, "setStackFromEnd() smoothScrollToPosition()");
    }

    @Override
    protected void onResume() { Log.e(TAG, "onResume()");
        super.onResume();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                    isChatShow = true;
                    Log.e(TAG, "isChatShow:"+isChatShow);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        Log.e(TAG, "id:" + getIntent().getStringExtra("id"));
//        getIntentFromService(getIntent());
    }

    @Override
    protected void onStart() { Log.e(TAG, "onStart()");
        super.onStart();
    }

    @Override
    protected void onRestart() { Log.e(TAG, "onRestart()");
        super.onRestart();
    }

    @Override
    protected void onPause() { Log.e(TAG, "onPause()");
        super.onPause();
    }

    @Override
    protected void onStop() { Log.e(TAG, "onStop()");
        super.onStop();
        isChatShow = false;
        Log.e(TAG, "isChatShow:"+isChatShow);
    }

    @Override
    public void onDestroy(){ Log.e(TAG, "onDestroy() ");
        super.onDestroy();
        // ????????? ?????? x
        // ?????? ??????????????? ????????? ????????? ??????
//        sendService("??????", "??????"); // ???????????? ???????????? ?????? intent??? ??????
    }

    // ????????????
    // not finish.. startActivity -> clubActivity (?????????)
    @Override
    public void onBackPressed() { Log.e(TAG, "onBackPressed");
        Intent intent = new Intent(chatting.this, clubActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // [ABCDE]??? ??????, E?????? C??? ?????? ?????? DE??????
        Log.e(TAG, "startActivity() \n"+TAG+" -> clubActivity.class ");
        startActivity(intent);
    }


}

    



