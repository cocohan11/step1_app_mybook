package com.example.mybooks.socket;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.mybooks.R;
import com.example.mybooks.club.chatting;
import com.example.mybooks.model.ClientInfo;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class myService extends Service {

    // activity to service for msg.what
    private final String TAG=this.getClass().getSimpleName();
    public static boolean isChatShow = false; // 채팅보일 때는 알림 안 보내기

    // socket 통신
    public Socket socket; //서버와 연결될 소켓
    public BufferedReader br; // 스트림(데이터 통로)
    public PrintWriter pw;
    public ClientInfo client;
    public ClientSender sender;
    public ClientReceiver receiver;
    public ResultReceiver resultReceiver; // A <-> S 통로만들기


    // 고정 숫자값
    private final int port = 7777;
//    private final String ip = "192.168.0.22"; // 2학원 ip주소
    private final String ip = "172.30.1.35"; // 가정용 ip주소
//    private final String ip = "192.168.0.155"; // 3학원 ip주소
//    private final String ip = "172.20.10.4"; // 핫스팟 ip주소
    /*
        채팅 알고리즘
        1. chatting.class에서 service 시작
        2. myService에서 message를 콜백받아서 입장 코드를 실행한다.
        3. 소켓이 연결되고 채팅 스레드가 돌아간다. (발송, 수신 스레드)
        4. 발송 스레드 - 입장msg
        5.
        6.
        7.
        8.
    */
    @Override
    public void onCreate() { Log.e(TAG, "onCreate() ");
        super.onCreate();
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) { Log.e(TAG, "onBind() ");
        /* 입장 콜백 */
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { Log.e(TAG, "onStartCommand() ");
        super.onStartCommand(intent, flags, startId);
        if (intent != null) {
            Log.e(TAG, "intent:"+intent);
            String purpose = intent.getStringExtra("purpose");
            if (purpose != null) { // 필수!! 에러남
                Log.e(TAG, "purpose:"+purpose);
                ClientInfo client = returnClient(intent);
                Log.e(TAG, "보내는 client값 :"+client);


                switch(purpose){
                    case "목록":
                        resultReceiver = intent.getParcelableExtra("RECEIVER"); // 객체만들기까지만. 채팅할 때 send()
                        Log.e(TAG, "목록에 들어옴");
                        break;

                    case "시작":
                        socketCreate(client); // socket, br, pw, sender, receiver 객체생성
                        break;

                    case "생성":
                    case "가입":
                        try {
                            resultReceiver = intent.getParcelableExtra("RECEIVER");
                            sender = new ClientSender(socket, pw, br, client); // purpose, chat만 내용 바꿔서 보내기
                            sender.start();
                        } catch (IOException e) { e.printStackTrace(); }
                        break;


                    case "입장":
                        resultReceiver = intent.getParcelableExtra("RECEIVER");
                        Log.e(TAG, "resultReceiver 2:"+resultReceiver);
                        break;


                    case "채팅":
                    case "퇴장":
                        // 발송
                        try {
                            Log.e(TAG, "채팅/퇴장");
                            sender = new ClientSender(socket, pw, br, client); // purpose, chat만 내용 바꿔서 보내기
                            sender.start();
                        } catch (IOException e) { e.printStackTrace(); }
                        break;
                }
            }
            Log.e(TAG, "pw:"+pw);
            Log.e(TAG, "br:"+br);
        }

        return Service.START_STICKY;
    }
    @Override
    public void onDestroy(){ Log.e(TAG, "onDestroy() ");
        super.onDestroy();
    }


    private ClientInfo returnClient(Intent intent) {
        Log.e(TAG, "intent.toString :"+intent.toString());
        return client = new ClientInfo(null
                                , intent.getStringExtra("clubNum")
                                , intent.getStringExtra("purpose")
                                , intent.getStringExtra("email")
                                , intent.getStringExtra("name")
                                , intent.getStringExtra("img")
                                , intent.getStringExtra("chat")
                                , 0
        );
    }



    // 단 한 번 실행되는 메소드(입장)
    public void socketCreate(ClientInfo client)  {
        new Thread() { //error : android.os.NetworkOnMainThreadException 나기 때문에 스레드로 빼줘야함
            public void run() {
                try {
                    // 소켓 생성, 서버에 입장
                    socket = new Socket(ip, port);
                    Log.e(TAG, "socket:"+socket);


                    // 데이터 쓰기, 읽기 객체
                    pw = new PrintWriter(socket.getOutputStream());
                    br = new BufferedReader(new InputStreamReader(socket.getInputStream()));


                    // 수신
                    receiver = new ClientReceiver(socket, pw, br);
                    receiver.start(); // while()

                    sender = new ClientSender(socket, pw, br, client); // purpose, chat만 내용 바꿔서 보내기
                    sender.start();

                    Log.e(TAG, "socketCreate pw:"+pw);
                    Log.e(TAG, "socketCreate br:"+br);
                } catch (IOException e) { e.printStackTrace(); }
            }
        }.start();
    }



    //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
    // 클라이언트 발송 클래스
    //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
    public class ClientSender extends Thread {
        Socket socket;
        PrintWriter pw;
        BufferedReader br;
        ClientInfo client;
        public ClientSender(Socket socket, PrintWriter pw, BufferedReader br, ClientInfo client) throws IOException {
//            Log.e(TAG, "pw:"+pw);
            this.socket = socket;
            this.pw = pw;
            this.br = br;
            this.client = client;
        }



        @Override
        public void run() { // ServerReceiver 클래스가 시작할 때 실행될 코드
            Log.e(TAG, "run() 시작");
            switch(client.getPurpose()){
                case "시작":
                    sendStartOrEntry("시작"); // 한 번 전송
                    break;


                case "생성":
                    sendStartOrEntry("생성"); // 한 번 전송
                    break;


                case "가입":
                    sendStartOrEntry("가입"); // 한 번 전송
                    break;


                case "채팅":
                    sendChat(); // 한 번 전송
                    break;


                case "퇴장":
                    sendExit(); // 한 번 전송
                    break;
            }
            Log.e(TAG, "run() 탈출");
        }



        public void sendStartOrEntry(String purpose) {
            Log.e(TAG, "sendEntry() client:" + client);
            Log.e(TAG, "sendEntry() pw:" + pw);
            new Thread(() -> {
                pw.println(client.getClubNum());
                pw.println(purpose);
                pw.println(client.getEmail());
                pw.println(client.getName());
                pw.println(client.getImg());
                pw.println(client.getChat());
                pw.flush();
            }).start();
        }
        public void sendChat() {
            /******************* 반복문 *****************/
            if (client.getChat().equals("exit")) { // ~~~~~~~~ 수정 예정
//                pw.println("퇴장"); // 1.목적
//                pw.println(client.getChat()); // 2.채팅메세지
//                pw.flush();
//
//                pw = null; // 클라이언트가 스레드를 종료하도록 while(조건식)변수를 변경
//                br = null;
//                try {
//                    socket.close(); // null 안 해도 에러안나서 close만 했음
//                } catch (IOException e) { e.printStackTrace(); }
//                Log.e(TAG, "스레드를 종료합니다.");


            } else {
                Log.e(TAG, "sendChat() client:" + client);
                Log.e(TAG, "sendChat() pw:" + pw);
                new Thread(() -> {
                    pw.println(client.getClubNum());
                    pw.println("채팅");
                    pw.println(client.getEmail());
                    pw.println(client.getName());
                    pw.println(client.getImg());
                    pw.println(client.getChat());
                    pw.flush();
                }).start();
            }
            /*********************************************/
        }
        public void sendExit() {
            Log.e(TAG, "sendExit() client:" + client);
            new Thread(() -> {
                pw.println(client.getClubNum());
                pw.println("퇴장");
                pw.println(client.getEmail());
                pw.println(client.getName());
                pw.println(client.getImg());
                pw.println(client.getChat());
                pw.flush();
            }).start();
        }
    } // ~전송 스레드드



    //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
    // 클라이언트 수신 클래스
    //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
    class ClientReceiver extends Thread {
        Socket socket;
        BufferedReader br;
        PrintWriter pw;
        public ClientReceiver(Socket socket, PrintWriter pw, BufferedReader br) {
//            Log.e(TAG, "socket:"+socket);
            this.socket = socket;
            this.pw = pw;
            this.br = br;
        }



        @Override
        public void run() { // ServerReceiver 클래스가 시작할 때 실행될 코드
            try {
                receiveChat(); // while()
            } catch (IOException e) {
                br = null;
                pw = null;
            }
        }



        public void receiveChat() throws IOException {
            /******************* 반복문 *****************/
            Log.e(TAG, "receiveChat() br:"+br);
            while (br!=null) {
                // 전송받은 메세지
                String clubNum = br.readLine();
                String purpose = br.readLine();
                String email = br.readLine();
                String name = br.readLine();
                String img = br.readLine();
                String chat = br.readLine();


                // 메세지 뷰타입을 정해야 chatting rv에서 구별함
                ClientInfo client = new ClientInfo(socket, clubNum, purpose, email, name, img, chat, 0); // 스트림으로 받아온 유저정보
                Log.e(TAG, ">> "+client);


                // service ----(client객체)----> activity
                switch (purpose) {

                    case "채팅":
                        chattingNotification(client); // if문에서 threadSendBundle()작동
                        break;

                    case "생성":
                    case "가입":
                    case "퇴장":
                        threadSendBundle(client);
                        break;

                }
            } /*********************************************/
        }
    }

    private void threadSendBundle(ClientInfo client) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e("SyncService","SyncService started");
                Log.e("receiveChat()","resultReceiver:"+resultReceiver);
                Bundle bundle = new Bundle();
                bundle.putString("clubNum", client.getClubNum());
                bundle.putString("purpose", client.getPurpose()); //
                bundle.putString("email", client.getEmail());
                bundle.putString("name", client.getName());
                bundle.putString("img", client.getImg());
                bundle.putString("chat", client.getChat());


                if (resultReceiver != null) {
                    resultReceiver.send(1,bundle); // (2)
                }
            }
        }).start();
    }



    private void chattingNotification(ClientInfo client) {
        /*
             if (어플 종료/ 어플 stop~ && !본인채팅) {
                  알림 o
             } else {
                  알림 x
             }
        */
        String TAG = "chattingNotification";
        Log.e(TAG, "isChatShow:"+isChatShow);
        Log.e(TAG, "userEmail:"+getSharedPreferences("autoLogin", Activity.MODE_PRIVATE).getString("userEmail", null));
        Log.e(TAG, "client:"+client);


        // 내가 보낸 메세지도 아니고 채팅방 밖이어야 함
        if (!client.getEmail().equals(getSharedPreferences("autoLogin", Activity.MODE_PRIVATE).getString("userEmail", null))
                && !isChatShow) { // 자기채팅X, 채팅이 보이는 액티비티면 알림x


            String channelID = "chatting_notification_channel"; // 어디에 쓰는거지?
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            // notification 재료 #1
            // 앱 실행중 아니어도 intent가 작동
            Intent resultIntent = new Intent(this, chatting.class); // this!! 주의!!
            resultIntent.putExtra("id", client.getClubNum());
            resultIntent.putExtra("chat", client.getChat()); // 받질 못해서 추가
            resultIntent.setAction(client.getClubNum());
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); // ?
            // notification 재료 #2
            // 알림 누르면 인텐트 이동
            @SuppressLint("UnspecifiedImmutableFlag")
            PendingIntent pendingIntent = PendingIntent.getActivity( // PendingIntent : 보류 인텐트. 당장 수행하진 않고 특정 시점에 수행하는 특징이 있다.
                    // 보통 '앱이 구동되고 있지 않을 때' 다른 프로세스에게 권한을 허가여 intent를 마치 본인 앱에서 실행되는 것처럼 사용한다.
                    getApplicationContext(),
                    0,
                    resultIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT // 이미 생성된 PendingIntent 가 있다면, Extra Data 만 갈아끼움 (업데이트)
//                    PendingIntent.FLAG_IMMUTABLE // 이미 생성된 PendingIntent 가 있다면, Extra Data 만 갈아끼움 (업데이트)
            );
            // notification 객체 생성
            // 알림 옵션
            NotificationCompat.Builder notification = new NotificationCompat.Builder(
                    getApplicationContext(),
                    channelID
            );
            // notification 객체 완성
            notification // 알림 속성
                    .setContentTitle("myBook")
                    .setContentText(client.getName()+" : "+client.getChat())
                    .setSmallIcon(R.drawable.ic_book)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_book))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);
            // 알림 채널 만들기
            if (notificationManager != null
                    && notificationManager.getNotificationChannel(channelID) == null) { // 사실 잘 이해안됨
                NotificationChannel notificationChannel = new NotificationChannel(
                        channelID,
                        "독서모임 채팅", // 사용자가 볼 수 있는 이름
                        NotificationManager.IMPORTANCE_HIGH // 전체 화면 인텐트를 사용할 수 있다.
                );
                notificationChannel.setDescription("This channel is used by chatting service");
                notificationManager.createNotificationChannel(notificationChannel);
            }


            // notification 빌드 & 포그라운드에서 시작하기
            startForeground(123, notification.build()); // 서비스 클래스 안에서 실행해야됨 (해당알림의 고유식별정수, 알림객체)
            Log.e(TAG, "startForeground()");


        } else {
            threadSendBundle(client);
        }
    }






}