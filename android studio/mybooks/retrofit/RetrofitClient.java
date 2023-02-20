package com.example.mybooks.retrofit;

import android.util.Log;

import androidx.annotation.NonNull;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    /**
     * 싱글톤 패턴 (to 내 서버)
     */
    // 요청 인터페이스, 빌드, 로그 한 데 모아둔 클래스

    private final static String baseUrl = "http://15.164.129.103/";
    private static HttpRequest httpRequest;
    private static RetrofitClient instance = null; // 싱글톤 패턴 (for 메모리 절약, 가독성)

    public static HttpRequest getRetrofitInterface(){ return httpRequest; }
    public static RetrofitClient getInstance(){ // 객체가 존재하면 재사용
        if(instance == null){
            instance = new RetrofitClient();
        }
        return instance;
    }

    private RetrofitClient(){
        /**
         OkHttpClient client = new OkHttpClient.Builder()
         .addInterceptor(interceptor)
         .connectTimeout(1, TimeUnit.MINUTES)
         .readTimeout(30,TimeUnit.SECONDS)
         .writeTimeout(15,TimeUnit.SECONDS)
         .build(); **/
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(createOkHttpClient()) // 네트워크 통신 로그보기(서버로 주고받는 파라미터)
                .build();

        httpRequest = retrofit.create(HttpRequest.class);
    }


    private OkHttpClient createOkHttpClient() { Log.e("createOkHttpClient ()", "네트워크 통신 로그(서버로 보내는 파라미터 및 받는 파라미터)");

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(@NonNull String s) {
                android.util.Log.e("로그찍음 ",  " s : " +s);
            }
        });

        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.interceptors().add(interceptor); //추가함
        builder.addNetworkInterceptor(interceptor); //추가함
        builder.addInterceptor(interceptor);

        return builder.build();
    }

}