package com.example.mybooks.retrofit;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitAladin {
    /**
     * 싱글톤 패턴 (to 알라딘)
     */
    // 따로 만든 이유 : retrofit 속성이 달라서
    // 요청 인터페이스, 빌드, 로그 한 데 모아둔 클래스
    // 싱글톤 - 아직 이해 다 못함

    private final static String baseUrl_aladin = "https://www.aladin.co.kr/ttb/api/";
    public final static String ttbkey = "ttbrlagksdl961257001";
    private static HttpRequest httpRequest_aladin;
    private static RetrofitAladin instance_aladin = null; // 싱글톤 패턴 (for 메모리 절약, 가독성)

    public static HttpRequest getRetrofitInterface(){ return httpRequest_aladin; }
    public static RetrofitAladin getInstance(){ // 객체가 존재하면 재사용
        if(instance_aladin == null){
            instance_aladin = new RetrofitAladin();
        }
        return instance_aladin;
    }

    private RetrofitAladin(){
        /**
         OkHttpClient client = new OkHttpClient.Builder()
         .addInterceptor(interceptor)
         .connectTimeout(1, TimeUnit.MINUTES)
         .readTimeout(30,TimeUnit.SECONDS)
         .writeTimeout(15,TimeUnit.SECONDS)
         .build(); **/

//        Gson gson = new GsonBuilder()
//                .setLenient()
//                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl_aladin)
                .addConverterFactory(ScalarsConverterFactory.create()) // 아래 에러나서 이 코드로 변경함
//                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(createOkHttpClient()) // 네트워크 통신 로그보기(서버로 주고받는 파라미터)
                .build();

        httpRequest_aladin = retrofit.create(HttpRequest.class);
    }


    private OkHttpClient createOkHttpClient() { Log.e("createOkHttpClient ()", "네트워크 통신 로그(서버로 보내는 파라미터 및 받는 파라미터)");

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(@NonNull String s) {
                Log.e("로그찍음 ",  " s : " +s);
            }
        });

        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.interceptors().add(interceptor); //추가함
        builder.addNetworkInterceptor(interceptor); //추가함
        builder.addInterceptor(interceptor);

        return builder.build();
    }

}