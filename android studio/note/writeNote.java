package com.example.mybooks.note;

import static com.example.mybooks.myPage.myPageEditActivity.GET_GALLERY_IMG;

import static okhttp3.MultipartBody.Part.createFormData;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.loader.content.CursorLoader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mybooks.R;
import com.example.mybooks.databinding.ActivityWriteNoteBinding;
import com.example.mybooks.home.showBookDetail;
import com.example.mybooks.home_bottom_1to5.myNoteActivity;
import com.example.mybooks.home_bottom_1to5.myPageActivity;
import com.example.mybooks.model.Response;
import com.example.mybooks.myPage.myPageEditActivity;
import com.example.mybooks.recyclerview.LinePagerIndicatorDecoration;
import com.example.mybooks.recyclerview.MultiImageAdapter;
import com.example.mybooks.retrofit.HttpRequest;
import com.example.mybooks.retrofit.RetrofitClient;
import com.kakao.util.helper.Utility;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class writeNote extends AppCompatActivity {
    /**
     * 노트 작성
     * 주의) cropper 라이브러리 불안정함. 만약 수행할 수 있는 앱이 없습니다-라고 에러뜨면
     * CropImage.activity() 주석, 에러대비용 코드 run돌리기, 그러곤 다시 원하는대로 작동하기기     */
    private final String TAG=this.getClass().getSimpleName();
    private ActivityWriteNoteBinding binding;
    static final int IMAGE_BITMAP = 3333;
    private MultipartBody.Part body;
    private boolean switch_open = true;
    private Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWriteNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setView(); // 초기셋팅
        setIntentItem(getIntent()); // 수정하기



        // 이미지 선택
        binding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                requestPermission(); // 권한

            }
        });


        // 이미지 삭제
        binding.btnImageX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { Log.e(TAG, "imageTest.setImageURI(null)");

                hideImgLayout();

            }
        });


        // 스위치 - 메모 공개
        binding.switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { Log.e(TAG, "스위치 isChecked:"+isChecked);

                switch_open = isChecked;

            }
        });


        // 저장 버튼
        binding.btnSave3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "body:"+body);

                insertMyNoteToDB(
                    getSharedPreferences("autoLogin", Activity.MODE_PRIVATE).getString("userEmail", null)
                    , getIntent().getStringExtra("isbn")   // intent로 받아온 값 대입하기!
                    , binding.etPage.getText().toString()
                    , binding.etNote.getText().toString(), switch_open, body
                );

            }
        });

    } // ~onCreate()



    // Permission 권한 요청
    private void requestPermission() { Log.e(TAG, "requestPermission()");
        // 마시멜로우 버전 이후라면 권한을 요청해라
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // 승인
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                Log.e(TAG, "000허용된상태");
                Toast.makeText(getApplicationContext(), "허용된상태", Toast.LENGTH_SHORT).show();


                // 에러 대비용 코드
//                whenErrorShow();

                // 편집 액티비티 show
                cropImageToolShow();

                // 거부
            } else {

                Log.e(TAG, "거부된상태");
                Toast.makeText(getApplicationContext(), "거부된상태", Toast.LENGTH_SHORT).show();
                String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE};
                ActivityCompat.requestPermissions(this, permission, 1); // 권한요청

            }
        }
    }


    private void setIntentItem(Intent i) {
        if (i.getStringExtra("수정")!=null) { // 수정이라면
            Log.e(TAG, "수정");
            Log.e(TAG, "imgUrl:"+i.getStringExtra("imgUrl"));
            String page = String.valueOf(i.getIntExtra("page", 0)); // String으로 감싸야 에러안남

            if (i.getStringExtra("imgUrl") != null) {
                Glide.with(getApplicationContext()).load(i.getStringExtra("imgUrl")).into(binding.imageTest); // 책 표지
                binding.constraint.setVisibility(View.VISIBLE);
            } else {
                binding.constraint.setVisibility(View.GONE);
            }
            binding.etPage.setText(page);
            binding.etNote.setText(i.getStringExtra("content"));
            binding.tvThisDate.setText(i.getStringExtra("Date"));
            binding.switch1.setChecked(i.getBooleanExtra("open", false));
            switch_open = i.getBooleanExtra("open", false);
            // isbn은 retrofit에서 요청할 파라미터임. 여기선 필요x
        }

    }


    private void hideImgLayout() {
        binding.imageTest.setImageURI(null); // get할 때 비어있어야 함
        binding.constraint.setVisibility(View.GONE);

        SharedPreferences auto = getSharedPreferences("autoLogin", Activity.MODE_PRIVATE);
        SharedPreferences.Editor edit = auto.edit();

        body = null;
        edit.remove("paint");
        edit.apply();
    }

    private void showImgLayout(Bitmap b) {
        binding.imageTest.setImageBitmap(b);
        binding.constraint.setVisibility(View.VISIBLE);
    }


    private void setView() {

        // 맨 밑 오늘날짜
        binding.tvThisDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

    }



    private void whenErrorShow() {

        startActivityForResult( // (intent, 요청코드)
        new Intent().setType("image/*").putExtra("crop", true).setAction(Intent.ACTION_PICK) // 한 장
        , GET_GALLERY_IMG);
    }


    private void cropImageToolShow() {

        CropImage.activity()   // 주의! startActivityForResult()는 필요없다
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(16,10)
                .start(this);
    }


    // 거부 -> 승인/거부
    // 갤러리 접근권한 결과
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                Log.e(TAG, "case1 입장");
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(getApplicationContext(), "거부->승인", Toast.LENGTH_SHORT).show();

                    // 편집 액티비티 show
                    CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(16,10)
                    .start(this);

                } else {
                    Toast.makeText(getApplicationContext(), "거부->거부", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }




    // 앨범에서 액티비티로 돌아온 후 실행되는 메서드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "onActivityResult() \nGET_GALLERY_IMG:"+GET_GALLERY_IMG+"\nrequestCode:"+requestCode+"\nresultCode:"+resultCode+"\ndata:"+data);
        Log.e(TAG, "CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:"+CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE);


        // 사진 편집까지 완료 이후
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            Log.e(TAG, "000CROP_IMAGE_ACTIVITY_REQUEST_CODE 입장");
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                Log.e(TAG, "000result.getBitmap():"+result.getBitmap()); // null임
                Log.e(TAG, "000result.getUri():"+result.getUri());


                Intent intent = new Intent(writeNote.this, paintView.class);
                intent.putExtra("uri", result.getUri());
                startActivity(intent);
                Log.e(TAG, "startActivity() \n"+TAG+" -> paintView.class ");


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) { Log.e(TAG, "000error:"+result.getError()); }
        }

    }


    private void insertMyNoteToDB(String email, String isbn, String page, String note, boolean open, MultipartBody.Part body) { Log.e(TAG, "insertMyNoteToDB()");

        RetrofitClient retrofitClient = RetrofitClient.getInstance(); // 요청 인터페이스, 빌드, 로그 한 데 모아둔 클래스
        HttpRequest httpRequest = retrofitClient.getRetrofitInterface(); // 에서 꺼내쓴다.



        // 페이지 형변환 예외처리
        int intPage =0;
        try {
            intPage = Integer.parseInt(page);
        } catch (NumberFormatException e) {
            intPage = 0;
//            intPage = null; // 에러나면 null로 대입하기
            Log.e("MainActivity catch", String.valueOf(intPage));
        }


        // 수정하기 -> update
        int update_id = 0;
        if (getIntent().getStringExtra("수정")!=null) {
            update_id = getIntent().getIntExtra("id",0);
        }


        // 요청 메소드 이름 : insertMyNoteToDB
        httpRequest.getUploadNoteImg(email, isbn, intPage, note, open, update_id, body).enqueue(new Callback<Response>() { // 파라미터 3개보내면서 요청하면 결과로 응답/실패 // 비동기
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                Response response1 = response.body();
                assert response1 != null;
                Log.e(TAG, "onResponse() / response1 : "+response1.getMessage()); // ImgUrl


                if (response1.isResponse()) { // UPDATE 완료했다면
                    Toast.makeText(getApplicationContext(), "작성 완료", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "다시 확인해주세요", Toast.LENGTH_SHORT).show();
                }


            }
            @Override
            public void onFailure(Call<Response> call, Throwable t) {
                Log.e(TAG, "onFailure() "+t.getMessage());
            }
        });

    }



    // 정말 뒤로갈건가요?
    @Override
    public void onBackPressed() { Log.e(TAG, "onBackPressed");
        if (!binding.etNote.getText().toString().equals("") || !binding.etPage.getText().toString().equals("") || (binding.constraint.getVisibility()==View.VISIBLE)) {
            dialog_reallyLeave(getApplicationContext(), writeNote.this);
        } else {
            super.onBackPressed(); // 입력된 텍스트나 사진이 없으면 바로 뒤로가기
        }
    }


    // 설계 중
    // 떠나기 다이얼로그
    public void dialog_reallyLeave(Context context, Context activity) {

        AlertDialog.Builder msgBuilder = new AlertDialog.Builder(activity)
                .setTitle("떠나시겠습니까?")
                .setMessage("작성 중인 내용은 저장되지 않습니다.")
                .setPositiveButton("떠나기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Toast.makeText(context, "작성이 취소되었습니다.", Toast.LENGTH_SHORT).show();
                        ((Activity) activity).finish();

                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Log.e(TAG, "다이얼로그 닫기");

                    }
                });
        AlertDialog msgDlg = msgBuilder.create();
        msgDlg.show();
    }



    private Bitmap StringToBitmap(String encodedString) { //마커에 사진삽입하려면 bitmap형태여야함
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }



    private File bitmapToFile(Bitmap bitmap, String fileName) { String method = "bitmapToFile() "; Log.e(TAG, method+" 입장");


        File filesDir = getApplicationContext().getFilesDir(); // retrofit에 파일타입을 장착해야되기 때문에 file을 생성한다.
        File imageFile = new File(filesDir, fileName); // 이미 기존 imgurl이름이 .jpg_000으로 끝나서 또 .jpg를 붙일필요 없다

        Log.e(TAG, method+" filesDir :"+filesDir +
                "\n imageFile:"+imageFile);

        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);

            os.flush();
            os.close();
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
        }

        Log.e(TAG, method+" imageFile:"+imageFile);
        Log.e(TAG, method+" 퇴장");

        return imageFile;
    }


    // bitmap to uri to path to file to body
    private MultipartBody.Part fileToBody (Bitmap bitmap) throws IOException {


        // MultipartBody 객체를 만들어 http통신한다.
//        File file = bitmapToFile2(bitmap, fileName);


        // retrofit 으로 보내기 위한 이미지 파일 가공
        Uri uri = getImageUri(getApplicationContext(), bitmap); // fileUri
        String path = getRealPathFromUri(uri); // filePath
        File file = new File(path);


        Log.e(TAG, "path : "+path); // /storage/emulated/0/Pictures/jsqoj.jpg
        Log.e(TAG, "file : "+file); // /storage/emulated/0/Pictures/jsqoj.jpg
        Log.e(TAG, "file.getName() : "+file.getName()); // jsqoj.jpg

        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file); // 실제파일객체에 타입정한다.
        MultipartBody.Part body = createFormData("uploaded_file", file.getName(), requestFile); // body안에 여러개 집어넣음. uploaded_MarkerFile이란 이름으로 받아온 data안에 body가 있다.

        Log.e(TAG, "requestFile : "+requestFile); // okhttp3.RequestBody$Companion$asRequestBody$1@df4b7b9
        Log.e(TAG, "body : "+body); // okhttp3.MultipartBody$Part@342c7fe
        return body;
    }

    //파일의 Uri로 경로를 찾아주는 함수(복붙)
    // uri to path
    private String getRealPathFromUri(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(getApplicationContext(), uri, projection, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();
        int column = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column);
        cursor.close();
        return result;
    }



    // bitmap to uri
    private Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 5;
        Random random = new Random(); // 중복삭제될까봐 랜덤이름설정
        String strRandom = random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        Log.e(TAG, "strRandom:"+strRandom);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, strRandom, null); // 이미지 추가하기
        return Uri.parse(path);
    }



        @Override
    protected void onResume() {
        super.onResume(); Log.e(TAG, "onResume");
    }


    @Override
    protected void onRestart() {
        super.onRestart(); Log.e(TAG, "onRestart");

        new Thread(() -> { //별개의 스레드로 해야 에러안남
            handler.post(new Runnable() { //post : 다른 스레드로 메세지(객체)를 전달하는 함수
                @Override
                public void run() { //마커의 위치만 변경

                    SharedPreferences auto = getSharedPreferences("autoLogin", Activity.MODE_PRIVATE);
                    Bitmap bitImg = StringToBitmap(auto.getString("paint", null));

                    if (auto.getString("paint", null) != null) {
                        Log.e(TAG, "bitImg:"+bitImg);

                        showImgLayout(bitImg);
                        try {
                            body = fileToBody(bitImg);  // 임시 파일 이름
                            Log.e(TAG, "body:"+body);

                        } catch (IOException e) { e.printStackTrace(); }

                    }

                }
            });
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy(); Log.e(TAG, "onDestroy"); Log.e(TAG, "paint null");

        SharedPreferences auto = getSharedPreferences("autoLogin", Activity.MODE_PRIVATE);
        SharedPreferences.Editor edit = auto.edit();

        edit.putString("paint", null);
        edit.apply(); //실질 저장

    }

}