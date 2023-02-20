package com.example.mybooks.home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;

import com.example.mybooks.R;
import com.example.mybooks.databinding.ActivityMainBinding;
import com.example.mybooks.databinding.ActivityMapBinding;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.util.FusedLocationSource;

public class map extends AppCompatActivity implements OnMapReadyCallback {
    private final String TAG=this.getClass().getSimpleName();
    private ActivityMapBinding binding;
    private NaverMap 네이버Map;
    private FusedLocationSource locationSource; //런타임권한얻은 현재위치값
    final String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}; //대략,정확한 위치 권한
    final int LOCATION_PERMISSION_REQUEST_CODE = 1000; //런타임권한요청코드


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.navermapMapView.onCreate(savedInstanceState); // 네이버지도에서 반환되는 콜백함수를 자신(this)으로 지정하는 역할
        binding.navermapMapView.getMapAsync(this);  // Async : 비동기(로 NaverMap객체를 얻는다)

//        locationSource = new FusedLocationSource(map.this, LOCATION_PERMISSION_REQUEST_CODE); //권한요청객체생성(GPS)



    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {

        Log.e(TAG, "onMapReady() 입장");
        네이버Map = naverMap; //전역변수에 대입(스레드에서 쓰려고)


        //런타임 권한
        Log.e(TAG, "런타임 권한을 맵에 지정");
//        ActivityCompat.requestPermissions(map.this, PERMISSIONS, LOCATION_PERMISSION_REQUEST_CODE); //현재위치 표시할 때 권한 확인(이미 M_main에서 통과됐기때문에 여기서는 런타임권한메소드 x)



    }


    /******************************************** 지도 **********************************************/



/*
    //런타임 권한요청 결과를 여기에 전달
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e(TAG, "onRequestPermissionsResult() 입장");

        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {

            if (!locationSource.isActivated()) { //권한 거부됨
                Log.e(TAG, "onRequestPermissionsResult()의 .. if() 입장22");
                네이버Map.setLocationTrackingMode(LocationTrackingMode.None);

            } else { //권한 수락
                Log.e(TAG, "onRequestPermissionsResult()의 .. else() 입장");

            }
        }
    }
*/


}