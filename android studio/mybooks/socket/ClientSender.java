//package com.example.mybooks.socket;
//
//import android.content.Context;
//import android.util.Log;
//
//import java.io.IOException;
//import java.io.PrintWriter;
//
////ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
//// 메시지를 전송하는 Thread
////ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
//public class ClientSender extends Thread {
//
//    private String TAG = "ClientSender.class";
//    private Context mContext;
//    private PrintWriter pw;
//
//    // 생성자. 방장과 참여자 두 곳에서 사용 됨
//    public ClientSender(PrintWriter pw) {
//        this.pw = pw;
//    }
//
//
//    /***************************************** 소켓 통신 ******************************************/
//    @Override
//    public void run() { //  this myName : Thread-3
//        Log.e(TAG, "run() socket : "+socket);
//
//
//        // 시작하자마자 문자열 8개를 서버로 전송 (1번만)
//        if (!socket.isClosed()) { // 소켓이 열려있다면
//
//            Log.e(TAG, "!socket.isClosed() : "+socket); // ok
//
//
//
//            pw.flush();
//
//        }
//
//        ;
//        //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
//        // 위치 전송 (마커용)
//        //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
//        Log.e(TAG, "11 위치 전송 thread while() 직전 myRoomActive : "+myRoomActive);
//    }
//
//    /*********************************************************************************************/
//
//} // ~inner class(송신)
