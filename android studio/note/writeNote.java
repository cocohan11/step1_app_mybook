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
     * ?????? ??????
     * ??????) cropper ??????????????? ????????????. ?????? ????????? ??? ?????? ?????? ????????????-?????? ????????????
     * CropImage.activity() ??????, ??????????????? ?????? run?????????, ????????? ?????? ??????????????? ???????????????     */
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
        setView(); // ????????????
        setIntentItem(getIntent()); // ????????????



        // ????????? ??????
        binding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                requestPermission(); // ??????

            }
        });


        // ????????? ??????
        binding.btnImageX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { Log.e(TAG, "imageTest.setImageURI(null)");

                hideImgLayout();

            }
        });


        // ????????? - ?????? ??????
        binding.switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { Log.e(TAG, "????????? isChecked:"+isChecked);

                switch_open = isChecked;

            }
        });


        // ?????? ??????
        binding.btnSave3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "body:"+body);

                insertMyNoteToDB(
                    getSharedPreferences("autoLogin", Activity.MODE_PRIVATE).getString("userEmail", null)
                    , getIntent().getStringExtra("isbn")   // intent??? ????????? ??? ????????????!
                    , binding.etPage.getText().toString()
                    , binding.etNote.getText().toString(), switch_open, body
                );

            }
        });

    } // ~onCreate()



    // Permission ?????? ??????
    private void requestPermission() { Log.e(TAG, "requestPermission()");
        // ??????????????? ?????? ???????????? ????????? ????????????
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // ??????
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                Log.e(TAG, "000???????????????");
                Toast.makeText(getApplicationContext(), "???????????????", Toast.LENGTH_SHORT).show();


                // ?????? ????????? ??????
//                whenErrorShow();

                // ?????? ???????????? show
                cropImageToolShow();

                // ??????
            } else {

                Log.e(TAG, "???????????????");
                Toast.makeText(getApplicationContext(), "???????????????", Toast.LENGTH_SHORT).show();
                String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE};
                ActivityCompat.requestPermissions(this, permission, 1); // ????????????

            }
        }
    }


    private void setIntentItem(Intent i) {
        if (i.getStringExtra("??????")!=null) { // ???????????????
            Log.e(TAG, "??????");
            Log.e(TAG, "imgUrl:"+i.getStringExtra("imgUrl"));
            String page = String.valueOf(i.getIntExtra("page", 0)); // String?????? ????????? ????????????

            if (i.getStringExtra("imgUrl") != null) {
                Glide.with(getApplicationContext()).load(i.getStringExtra("imgUrl")).into(binding.imageTest); // ??? ??????
                binding.constraint.setVisibility(View.VISIBLE);
            } else {
                binding.constraint.setVisibility(View.GONE);
            }
            binding.etPage.setText(page);
            binding.etNote.setText(i.getStringExtra("content"));
            binding.tvThisDate.setText(i.getStringExtra("Date"));
            binding.switch1.setChecked(i.getBooleanExtra("open", false));
            switch_open = i.getBooleanExtra("open", false);
            // isbn??? retrofit?????? ????????? ???????????????. ????????? ??????x
        }

    }


    private void hideImgLayout() {
        binding.imageTest.setImageURI(null); // get??? ??? ??????????????? ???
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

        // ??? ??? ????????????
        binding.tvThisDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

    }



    private void whenErrorShow() {

        startActivityForResult( // (intent, ????????????)
        new Intent().setType("image/*").putExtra("crop", true).setAction(Intent.ACTION_PICK) // ??? ???
        , GET_GALLERY_IMG);
    }


    private void cropImageToolShow() {

        CropImage.activity()   // ??????! startActivityForResult()??? ????????????
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(16,10)
                .start(this);
    }


    // ?????? -> ??????/??????
    // ????????? ???????????? ??????
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                Log.e(TAG, "case1 ??????");
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(getApplicationContext(), "??????->??????", Toast.LENGTH_SHORT).show();

                    // ?????? ???????????? show
                    CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(16,10)
                    .start(this);

                } else {
                    Toast.makeText(getApplicationContext(), "??????->??????", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }




    // ???????????? ??????????????? ????????? ??? ???????????? ?????????
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "onActivityResult() \nGET_GALLERY_IMG:"+GET_GALLERY_IMG+"\nrequestCode:"+requestCode+"\nresultCode:"+resultCode+"\ndata:"+data);
        Log.e(TAG, "CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:"+CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE);


        // ?????? ???????????? ?????? ??????
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            Log.e(TAG, "000CROP_IMAGE_ACTIVITY_REQUEST_CODE ??????");
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                Log.e(TAG, "000result.getBitmap():"+result.getBitmap()); // null???
                Log.e(TAG, "000result.getUri():"+result.getUri());


                Intent intent = new Intent(writeNote.this, paintView.class);
                intent.putExtra("uri", result.getUri());
                startActivity(intent);
                Log.e(TAG, "startActivity() \n"+TAG+" -> paintView.class ");


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) { Log.e(TAG, "000error:"+result.getError()); }
        }

    }


    private void insertMyNoteToDB(String email, String isbn, String page, String note, boolean open, MultipartBody.Part body) { Log.e(TAG, "insertMyNoteToDB()");

        RetrofitClient retrofitClient = RetrofitClient.getInstance(); // ?????? ???????????????, ??????, ?????? ??? ??? ????????? ?????????
        HttpRequest httpRequest = retrofitClient.getRetrofitInterface(); // ?????? ????????????.



        // ????????? ????????? ????????????
        int intPage =0;
        try {
            intPage = Integer.parseInt(page);
        } catch (NumberFormatException e) {
            intPage = 0;
//            intPage = null; // ???????????? null??? ????????????
            Log.e("MainActivity catch", String.valueOf(intPage));
        }


        // ???????????? -> update
        int update_id = 0;
        if (getIntent().getStringExtra("??????")!=null) {
            update_id = getIntent().getIntExtra("id",0);
        }


        // ?????? ????????? ?????? : insertMyNoteToDB
        httpRequest.getUploadNoteImg(email, isbn, intPage, note, open, update_id, body).enqueue(new Callback<Response>() { // ???????????? 3??????????????? ???????????? ????????? ??????/?????? // ?????????
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                Response response1 = response.body();
                assert response1 != null;
                Log.e(TAG, "onResponse() / response1 : "+response1.getMessage()); // ImgUrl


                if (response1.isResponse()) { // UPDATE ???????????????
                    Toast.makeText(getApplicationContext(), "?????? ??????", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "?????? ??????????????????", Toast.LENGTH_SHORT).show();
                }


            }
            @Override
            public void onFailure(Call<Response> call, Throwable t) {
                Log.e(TAG, "onFailure() "+t.getMessage());
            }
        });

    }



    // ?????? ???????????????????
    @Override
    public void onBackPressed() { Log.e(TAG, "onBackPressed");
        if (!binding.etNote.getText().toString().equals("") || !binding.etPage.getText().toString().equals("") || (binding.constraint.getVisibility()==View.VISIBLE)) {
            dialog_reallyLeave(getApplicationContext(), writeNote.this);
        } else {
            super.onBackPressed(); // ????????? ???????????? ????????? ????????? ?????? ????????????
        }
    }


    // ?????? ???
    // ????????? ???????????????
    public void dialog_reallyLeave(Context context, Context activity) {

        AlertDialog.Builder msgBuilder = new AlertDialog.Builder(activity)
                .setTitle("??????????????????????")
                .setMessage("?????? ?????? ????????? ???????????? ????????????.")
                .setPositiveButton("?????????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Toast.makeText(context, "????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                        ((Activity) activity).finish();

                    }
                })
                .setNegativeButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Log.e(TAG, "??????????????? ??????");

                    }
                });
        AlertDialog msgDlg = msgBuilder.create();
        msgDlg.show();
    }



    private Bitmap StringToBitmap(String encodedString) { //????????? ????????????????????? bitmap???????????????
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }



    private File bitmapToFile(Bitmap bitmap, String fileName) { String method = "bitmapToFile() "; Log.e(TAG, method+" ??????");


        File filesDir = getApplicationContext().getFilesDir(); // retrofit??? ??????????????? ?????????????????? ????????? file??? ????????????.
        File imageFile = new File(filesDir, fileName); // ?????? ?????? imgurl????????? .jpg_000?????? ????????? ??? .jpg??? ???????????? ??????

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
        Log.e(TAG, method+" ??????");

        return imageFile;
    }


    // bitmap to uri to path to file to body
    private MultipartBody.Part fileToBody (Bitmap bitmap) throws IOException {


        // MultipartBody ????????? ????????? http????????????.
//        File file = bitmapToFile2(bitmap, fileName);


        // retrofit ?????? ????????? ?????? ????????? ?????? ??????
        Uri uri = getImageUri(getApplicationContext(), bitmap); // fileUri
        String path = getRealPathFromUri(uri); // filePath
        File file = new File(path);


        Log.e(TAG, "path : "+path); // /storage/emulated/0/Pictures/jsqoj.jpg
        Log.e(TAG, "file : "+file); // /storage/emulated/0/Pictures/jsqoj.jpg
        Log.e(TAG, "file.getName() : "+file.getName()); // jsqoj.jpg

        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file); // ????????????????????? ???????????????.
        MultipartBody.Part body = createFormData("uploaded_file", file.getName(), requestFile); // body?????? ????????? ????????????. uploaded_MarkerFile?????? ???????????? ????????? data?????? body??? ??????.

        Log.e(TAG, "requestFile : "+requestFile); // okhttp3.RequestBody$Companion$asRequestBody$1@df4b7b9
        Log.e(TAG, "body : "+body); // okhttp3.MultipartBody$Part@342c7fe
        return body;
    }

    //????????? Uri??? ????????? ???????????? ??????(??????)
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
        Random random = new Random(); // ????????????????????? ??????????????????
        String strRandom = random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        Log.e(TAG, "strRandom:"+strRandom);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, strRandom, null); // ????????? ????????????
        return Uri.parse(path);
    }



        @Override
    protected void onResume() {
        super.onResume(); Log.e(TAG, "onResume");
    }


    @Override
    protected void onRestart() {
        super.onRestart(); Log.e(TAG, "onRestart");

        new Thread(() -> { //????????? ???????????? ?????? ????????????
            handler.post(new Runnable() { //post : ?????? ???????????? ?????????(??????)??? ???????????? ??????
                @Override
                public void run() { //????????? ????????? ??????

                    SharedPreferences auto = getSharedPreferences("autoLogin", Activity.MODE_PRIVATE);
                    Bitmap bitImg = StringToBitmap(auto.getString("paint", null));

                    if (auto.getString("paint", null) != null) {
                        Log.e(TAG, "bitImg:"+bitImg);

                        showImgLayout(bitImg);
                        try {
                            body = fileToBody(bitImg);  // ?????? ?????? ??????
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
        edit.apply(); //?????? ??????

    }

}