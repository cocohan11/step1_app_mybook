package com.example.mybooks.note;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.mybooks.R;
import com.example.mybooks.databinding.ActivityTestPaintBinding;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.overlay.OverlayImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.TimerTask;

import yuku.ambilwarna.AmbilWarnaDialog;

public class paintView extends AppCompatActivity {
    private final String TAG=this.getClass().getSimpleName();
    private ActivityTestPaintBinding binding;
    private MyPaintView myView;
    int count = 0;
    int tColor,n=0;
//    String testUrl = "http://15.164.129.103/myNoteOfBook/cropped5639824668064416550.jpg";
    Bitmap bitmapImg; // url to bitmap
    private Handler handler = new Handler();
    private Uri uri;


    // 두께
    // 지우기
    // 캔버스와 뷰 합치기
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTestPaintBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("간단 s그림판");


        new Thread(() -> { //별개의 스레드로 해야 에러안남

            uri = getIntent().getParcelableExtra("uri"); Log.e(TAG,"uri:"+uri);
            if (uri != null) {
                try {
                    bitmapImg = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), uri);
                    Log.e(TAG,"bitmapImg:"+bitmapImg);
                } catch (IOException e) { e.printStackTrace(); }
            }


//            bitmapImg = getBitmapFromURL(testUrl);
            handler.post(new Runnable() { //post : 다른 스레드로 메세지(객체)를 전달하는 함수
                @Override
                public void run() { //마커의 위치만 변경


                    myView = new MyPaintView(getApplicationContext(), bitmapImg);
                    binding.paintLayout.addView(myView); // 도화지 (LinearLayout이어야 함)

                }
            });
        }).start();



        // 지우기
        ((Button)findViewById(R.id.btnClear)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { Log.e(TAG,"btnClear onClick / bitmapImg:"+bitmapImg);
                myView.mBitmap.eraseColor(Color.TRANSPARENT);

                Bitmap bImg = bitmapImg.copy(Bitmap.Config.ARGB_8888,true);
                bImg = Bitmap.createScaledBitmap(bImg, binding.paintLayout.getWidth(), binding.paintLayout.getHeight(), true);   // !! 위치 여기
                Log.e(TAG,"bitmapImg2:"+bitmapImg);
                Log.e(TAG,"bImg2:"+bImg);
                myView.mBitmap = bImg;
                myView.mCanvas.setBitmap(myView.mBitmap); // 객체 주소 주의!! 바로 bitmapImg넣으면 안 됨
                myView.invalidate(); // 무효화하다

            }
        });

        // 색상 선택
        ((ImageView)findViewById(R.id.choice)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { Log.e(TAG,"choice() onClick");
                openColorPicker();
            }
        });

        // 저장
        ((Button)findViewById(R.id.save)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { Log.e(TAG,"choice() save");

                // bitmap 저장.. 어디에?
                saveThePaintToShared();
                finish();

            }
        });


    } // ~onCreate()

    private void saveThePaintToShared() {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        myView.mBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos); //bm is the bitmap object
        byte[] b = baos.toByteArray();
        String encoded = Base64.encodeToString(b, Base64.DEFAULT);


        // 쉐어드에 저장
        SharedPreferences auto = getSharedPreferences("autoLogin", Activity.MODE_PRIVATE); // error! context를 앞에 붙여줌
        SharedPreferences.Editor edit = auto.edit();

        edit.putString("paint", encoded);
        edit.apply(); //실질 저장

    }




    private Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private static class MyPaintView extends View {
        private final String TAG=this.getClass().getSimpleName();
        private Bitmap originalBitmap;
        private Bitmap mBitmap;
        private Canvas mCanvas;
        private Path mPath;
        private Paint mPaint;
        private LinearLayout linearLayout;

        public MyPaintView(Context context, Bitmap bitmap) {
            super(context);
//            drawable = (BitmapDrawable) imageView.getDrawable(); // 안 됨!! >> 해결!! 원인) src속성

            originalBitmap = bitmap; // url to bitmap 이미지
            mPath = new Path();
            mPaint = new Paint();
            mPaint.setColor(Color.parseColor("#4Dd55151"));
            mPaint.setAntiAlias(true);
            mPaint.setStrokeWidth(35);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeCap(Paint.Cap.BUTT);

        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh); Log.e(TAG,"onSizeChanged()"); Log.e(TAG,"w:"+w+"/h:"+h);


            mBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888,true);
            mBitmap = Bitmap.createScaledBitmap(mBitmap, w, h, true);   // !! 위치 여기
            mCanvas = new Canvas(mBitmap); // 도화지에 비트맵 가루를 뿌려넣는다

        }

        @SuppressLint("DrawAllocation")
        @Override
        protected void onDraw(Canvas canvas) {
            Log.e(TAG,"onDraw()");
            Log.e(TAG,"width:"+ getWidth()+"/height:"+getHeight());


            canvas.drawBitmap(mBitmap, 0, 0, null); //지금까지 그려진 내용
            canvas.drawPath(mPath, mPaint); //현재 그리고 있는 내용
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            Log.e(TAG,"onTouchEvent()");
            int x = (int)event.getX();
            int y = (int)event.getY();

            Log.e(TAG,"x:"+x+"y:"+y);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.e(TAG,"ACTION_DOWN");
                    mPath.reset();
                    mPath.moveTo(x, y);
                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.e(TAG,"ACTION_MOVE");
                    mPath.lineTo(x, y);
                    break;
                case MotionEvent.ACTION_UP:
                    Log.e(TAG,"ACTION_UP");
                    mPath.lineTo(x, y);
                    mCanvas.drawPath(mPath, mPaint); //mBitmap 에 기록
                    mPath.reset();
                    break;
            }
            this.invalidate();
            return true;
        }
    }

    private void openColorPicker() {
        AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(this, tColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                tColor = color;
                Toast.makeText(getApplicationContext(),""+tColor,Toast.LENGTH_LONG).show();
                Log.e(TAG,"tColor:"+tColor);
                String hexColor = Integer.toHexString(color).substring(2);
                hexColor = "#4D"+hexColor;
                Log.e(TAG,"hexColor:"+hexColor); // 9자리


                myView.mPaint.setColor(Color.parseColor(hexColor));

            }
        });
        colorPicker.show();
    }


}