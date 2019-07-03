package com.hackathon.arproject;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hackathon.arproject.R;

import net.daum.mf.map.api.MapCircle;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapReverseGeoCoder;
import net.daum.mf.map.api.MapView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements SurfaceHolder.Callback, OnLocationChangedListener, OnAzimuthChangedListener, View.OnClickListener,
            MapView.CurrentLocationEventListener, MapReverseGeoCoder.ReverseGeoCodingResultListener {

    private static final int MY_PERMISSIONS_REQUEST_CODE = 123;

    private Camera mCamera;
    private SurfaceHolder mSurfaceHolder;
    private boolean isCameraViewOn = false;
    private AugmentedPOI mPoi;

    private double mAzimuthReal = 0;
    private double mAzimuthTheoretical = 0;
    private static double AZIMUTH_ACCURACY = 25;
    private double mMyLatitude = 0;
    private double mMyLongitude = 0;

    private MyCurrentAzimuth myCurrentAzimuth;
    private MyCurrentLocation myCurrentLocation;

    TextView descriptionTextView;
    TextView txtAndroid;
    ImageView pointerIcon;
    Display display;

    //int length;

    float ratio;

    //==========

    private static final String LOG_TAG = "MainActivity";
    static private MapView mMapView;
    private MapCircle mMapCircle;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;

    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION};

    public class MarkerPoint{
        private double lati;     //위도
        private double longi;    //경도
        private String itemName = null;

        MarkerPoint(double lati, double longi, String itemName){
            this.lati = lati;
            this.longi = longi;
            this.itemName = itemName;
        }

        public void pointStart(){
            MapPOIItem marker = new MapPOIItem();
            marker.setItemName(itemName);
            // marker.setTag(0);
            MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(lati, longi);
            marker.setMapPoint(mapPoint);
            mMapView.addPOIItem(marker);
        }
    }

    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint currentLocation, float accuracyInMeters) {
        MapPoint.GeoCoordinate mapPointGeo = currentLocation.getMapPointGeoCoord();
        Log.i(LOG_TAG, String.format("MapView onCurrentLocationUpdate (%f,%f) accuracy (%f)", mapPointGeo.latitude, mapPointGeo.longitude, accuracyInMeters));
    }


    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {

    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {

    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {

    }

    @Override
    public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder mapReverseGeoCoder, String s) {
        mapReverseGeoCoder.toString();
        onFinishReverseGeoCoding(s);
    }

    @Override
    public void onReverseGeoCoderFailedToFindAddress(MapReverseGeoCoder mapReverseGeoCoder) {
        onFinishReverseGeoCoding("Fail");
    }

    private void onFinishReverseGeoCoding(String result) {
//        Toast.makeText(LocationDemoActivity.this, "Reverse Geo-coding : " + result, Toast.LENGTH_SHORT).show();
    }






    void checkRunTimePermission(){

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED ) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            // 3.  위치 값을 가져올 수 있음
            mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading);


        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(MainActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }



    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }

                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    // =======================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtAndroid = (TextView)findViewById(R.id.txtAndroid);
        relativeLayout = (RelativeLayout)findViewById(R.id.layout);
        pointerIcon = (ImageView) findViewById(R.id.icon);

        checkPermission();


        //=======================

        mMapView = (MapView) findViewById(R.id.map_view);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        mMapView.setClipToOutline(true);
        mMapView.setBackground(new ShapeDrawable(new OvalShape()));






        //mMapView.setDaumMapApiKey(MapApiConst.DAUM_MAPS_ANDROID_APP_API_KEY);
        mMapView.setCurrentLocationEventListener(this);

        if (!checkLocationServicesStatus()) {

            showDialogForLocationServiceSetting();
        }else {
            checkRunTimePermission();
        }
/*
         MapPOIItem marker = new MapPOIItem();
        marker.setItemName("test");
        marker.setTag(0);
        MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(36.326279, 127.338651);
        marker.setMapPoint(mapPoint);
        mMapView.addPOIItem(marker);
        */

        MarkerPoint acc = new MarkerPoint(36.326279, 127.338651, "목원대학교");
        acc.pointStart();

        //
        acc = new MarkerPoint(36.337692, 127.346852, "목원대학교");
        acc.pointStart();


    }

    //==============================


    @Override
    protected void onDestroy() {
        super.onDestroy();

        mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
        mMapView.setShowCurrentLocationMarker(false);

    }

    protected void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                + ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                + ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                + ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS)
                != PackageManager.PERMISSION_GRANTED) {

            // Do something, when permissions not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.CAMERA)
                    || ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    || ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.ACCESS_FINE_LOCATION)
                    || ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.BODY_SENSORS)) {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                ;
                builder.setTitle("Please grant those permissions");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(
                                MainActivity.this,
                                new String[]{
                                        Manifest.permission.CAMERA,
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.BODY_SENSORS
                                },
                                MY_PERMISSIONS_REQUEST_CODE
                        );
                    }
                });
                builder.setNeutralButton("Cancel", null);
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                // Directly request for required permissions, without explanation
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{
                                Manifest.permission.CAMERA,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.BODY_SENSORS
                        },
                        MY_PERMISSIONS_REQUEST_CODE
                );
            }
        } else {
            display = ((android.view.WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

            setupListeners();
            setupLayout();
            setAugmentedRealityPoint();
            //txtAndroid.setText(DistanceByDegreeAndroid(mPoi.getPoiLatitude(), mPoi.getPoiLongitude(), mMyLatitude, mMyLongitude)+"m");
        }
    }


    private void setAugmentedRealityPoint() {
        mPoi = new AugmentedPOI(
                "EcoPark",
                "Bragança",
                36.330518,
                127.341498
        );
    }

    public double calculateTheoreticalAzimuth() {
        // Calculates azimuth angle (phi) of POI
        double dy = mPoi.getPoiLatitude() - mMyLatitude;
        double dx = mPoi.getPoiLongitude() - mMyLongitude;

        double phiAngle;
        double tanPhi;

        tanPhi = Math.abs(dx / dy);
        phiAngle = Math.atan(tanPhi);
        phiAngle = Math.toDegrees(phiAngle);

        // phiAngle = [0,90], check quadrant and return correct phiAngle
        if (dy > 0 && dx > 0) { // I quadrant
            return phiAngle;
        } else if (dy < 0 && dx > 0) { // II
            return 180 - phiAngle;
        } else if (dy < 0 && dx < 0) { // III
            return 180 + phiAngle;
        } else if (dy > 0 && dx < 0) { // IV
            return 360 - phiAngle;
        }

        return phiAngle;
    }

    private List<Double> calculateAzimuthAccuracy(double azimuth) {
        // Returns the Camera View Sector
        List<Double> minMax = new ArrayList<Double>();
        double minAngle = (azimuth - AZIMUTH_ACCURACY + 360) % 360;
        double maxAngle = (azimuth + AZIMUTH_ACCURACY) % 360;
        minMax.clear();
        minMax.add(minAngle);
        minMax.add(maxAngle);
        return minMax;
    }

    private boolean isBetween(double minAngle, double maxAngle, double azimuth) {
        // Checks if the azimuth angle lies in minAngle and maxAngle of Camera View Sector
        if (minAngle > maxAngle) {
            if (isBetween(0, maxAngle, azimuth) || isBetween(minAngle, 360, azimuth))
                return true;
        } else if (azimuth > minAngle && azimuth < maxAngle)
            return true;
        return false;
    }

    private void updateDescription() {
        descriptionTextView.setText(mPoi.getPoiName() + " azimuthTheoretical "
                + mAzimuthTheoretical + " azimuthReal " + mAzimuthReal + " latitude "
                + mMyLatitude + " longitude " + mMyLongitude);
        txtAndroid.setText((int)DistanceByDegree(mPoi.getPoiLatitude(), mPoi.getPoiLongitude(), mMyLatitude, mMyLongitude)+"m");


    }

    @Override
    public void onLocationChanged(Location location) {
        // Function to handle Change in Location
        mMyLatitude = location.getLatitude();
        mMyLongitude = location.getLongitude();
        mAzimuthTheoretical = calculateTheoreticalAzimuth();
        updateDescription();
    }

    RelativeLayout relativeLayout;

    @Override
    public void onAzimuthChanged(float azimuthChangedFrom, float azimuthChangedTo) {

        // Function to handle Change in azimuth angle
        mAzimuthReal = azimuthChangedTo;
        mAzimuthTheoretical = calculateTheoreticalAzimuth();



        //length = (int)DistanceByDegree(mPoi.getPoiLatitude(), mPoi.getPoiLongitude(), mMyLatitude, mMyLongitude);

        // Since Camera View is perpendicular to device plane
        mAzimuthReal = (mAzimuthReal + 90) % 360;


        pointerIcon.setOnClickListener(this);

        if((int)DistanceByDegree(mPoi.getPoiLatitude(), mPoi.getPoiLongitude(), mMyLatitude, mMyLongitude) <= 200 &&
              (int)DistanceByDegree(mPoi.getPoiLatitude(), mPoi.getPoiLongitude(), mMyLatitude, mMyLongitude) >= 101){
            Bitmap orgImage = BitmapFactory.decodeResource(getResources(), R.drawable.circle2);
            Bitmap resize = Bitmap.createScaledBitmap(orgImage,103,39,true);
            pointerIcon.setImageBitmap(resize);
        }else if((int)DistanceByDegree(mPoi.getPoiLatitude(), mPoi.getPoiLongitude(), mMyLatitude, mMyLongitude) <= 100 &&
                (int)DistanceByDegree(mPoi.getPoiLatitude(), mPoi.getPoiLongitude(), mMyLatitude, mMyLongitude) > 0){
            Bitmap orgImage = BitmapFactory.decodeResource(getResources(), R.drawable.circle2);
            pointerIcon.setImageBitmap(orgImage);
        }else{
            Bitmap orgImage = BitmapFactory.decodeResource(getResources(), R.drawable.circle2);
            pointerIcon.setImageBitmap(orgImage);
        }

        double minAngle = calculateAzimuthAccuracy(mAzimuthReal).get(0);
        double maxAngle = calculateAzimuthAccuracy(mAzimuthReal).get(1);

//        if (isBetween(minAngle, maxAngle, mAzimuthTheoretical)) {
//            //float ratio = ((float) (mAzimuthTheoretical - minAngle + 360.0) % 360) / ((float) (maxAngle - minAngle + 360.0) % 360);
//            ratio = ((float) (mAzimuthTheoretical - minAngle + 360.0) % 360) / ((float) (maxAngle - minAngle + 360.0) % 360);
//
//           // RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT , ViewGroup.LayoutParams.WRAP_CONTENT);
//
//           // lp.topMargin = (int) (display.getHeight() * ratio);
//           // lp.leftMargin = display.getWidth() / 2 - pointerIcon.getWidth();
//
//
//            //lp.leftMargin = (int) (display.getWidth() * ratio);
//           // lp.leftMargin = 0;
//            Log.e("onAzimuthChanged: ",(int) (display.getWidth() * ratio)+ "" );
//
//           // lp.topMargin = display.getHeight() / 2 - pointerIcon.getHeight();
//
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    pointerIcon.setX((int) (display.getWidth() * ratio));
//                    pointerIcon.setY(display.getHeight() / 2 - pointerIcon.getHeight());
//                }
//            });
//
//
//            //pointerIcon.setLayoutParams(lp);
//            pointerIcon.setVisibility(View.VISIBLE);
//        } else {
//            //pointerIcon.setVisibility(View.GONE);
//        }

        if (isBetween(minAngle, maxAngle, mAzimuthTheoretical)) {


            Log.e("onAzimuthChanged: ", minAngle + ", " + maxAngle + ", " +  mAzimuthTheoretical + "");

            //float ratio = ((float) (mAzimuthTheoretical - minAngle + 360.0) % 360) / ((float) (maxAngle - minAngle + 360.0) % 360);
            ratio = ((float) (mAzimuthTheoretical - minAngle + 360.0) % 360) / ((float) (maxAngle - minAngle + 360.0) % 360);

            // RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT , ViewGroup.LayoutParams.WRAP_CONTENT);

            // lp.topMargin = (int) (display.getHeight() * ratio);
            // lp.leftMargin = display.getWidth() / 2 - pointerIcon.getWidth();


            //lp.leftMargin = (int) (display.getWidth() * ratio);
            // lp.leftMargin = 0;
            Log.e("onAzimuthChanged: ",(int) (display.getWidth() * ratio)+ "" );

            // lp.topMargin = display.getHeight() / 2 - pointerIcon.getHeight();

//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
                    pointerIcon.setX((int) (display.getWidth() * ratio));
                    pointerIcon.setY(display.getHeight() / 2 - pointerIcon.getHeight());


//                }
//            });


            //pointerIcon.setLayoutParams(lp);
            pointerIcon.setVisibility(View.VISIBLE);
        } else if (isBetween(minAngle, maxAngle, mAzimuthTheoretical)) {


        } else {
            //pointerIcon.setVisibility(View.GONE);
        }



        updateDescription();
    }

    @Override
    protected void onStop() {
        myCurrentAzimuth.stop();
        myCurrentLocation.stop();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                + ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                + ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                + ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS)
                == PackageManager.PERMISSION_GRANTED) {
            myCurrentAzimuth.start();
            myCurrentLocation.start();
        }
    }

    private void setupListeners() {
        myCurrentLocation = new MyCurrentLocation(this);
        myCurrentLocation.buildGoogleApiClient(this);
        myCurrentLocation.start();

        myCurrentAzimuth = new MyCurrentAzimuth(this, this);
        myCurrentAzimuth.start();
    }

    private void setupLayout() {
        descriptionTextView = (TextView) findViewById(R.id.cameraTextView);

        getWindow().setFormat(PixelFormat.UNKNOWN);
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.cameraview);
        mSurfaceHolder = surfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {



        if (isCameraViewOn) {
            mCamera.stopPreview();
            isCameraViewOn = false;
        }

        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(mSurfaceHolder);
                mCamera.startPreview();
                isCameraViewOn = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mCamera = Camera.open();
        mCamera.setDisplayOrientation(0);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
        isCameraViewOn = false;
    }

    @Override
    public void onClick(View view) {
        String url = "https://www.itsector.pt/pt/carreiras";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CODE: {
                // When request is cancelled, the results array are empty
                if (
                        (grantResults.length > 0) &&
                                (grantResults[0]
                                        + grantResults[1]
                                        + grantResults[2]
                                        + grantResults[3]
                                        == PackageManager.PERMISSION_GRANTED
                                )
                ) {
                    display = ((android.view.WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

                    setupListeners();
                    setupLayout();
                    setAugmentedRealityPoint();
                    //txtAndroid.setText(DistanceByDegreeAndroid(mPoi.getPoiLatitude(), mPoi.getPoiLongitude(), mMyLatitude, mMyLongitude)+"m");
                } else {
                    display = ((android.view.WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

                    setupListeners();
                    setupLayout();
                    setAugmentedRealityPoint();
                    //txtAndroid.setText(DistanceByDegreeAndroid(mPoi.getPoiLatitude(), mPoi.getPoiLongitude(), mMyLatitude, mMyLongitude)+"m");
                }
                return;
            }
        }
    }
    public double DistanceByDegreeAndroid(double _latitude1, double _longitude1, double _latitude2, double _longitude2){
        Location startPos = new Location("PointA");
        Location endPos = new Location("PointB");

        startPos.setLatitude(_latitude1);
        startPos.setLongitude(_longitude1);
        endPos.setLatitude(_latitude2);
        endPos.setLongitude(_longitude2);

        double distance = startPos.distanceTo(endPos);

        return distance;
    }

    public double DistanceByDegree(double _latitude1, double _longitude1, double _latitude2, double _longitude2){
        double theta, dist;
        theta = _longitude1 - _longitude2;
        dist = Math.sin(DegreeToRadian(_latitude1)) * Math.sin(DegreeToRadian(_latitude2)) + Math.cos(DegreeToRadian(_latitude1))
                * Math.cos(DegreeToRadian(_latitude2)) * Math.cos(DegreeToRadian(theta));
        dist = Math.acos(dist);
        dist = RadianToDegree(dist);

        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;    // 단위 mile 에서 km 변환.
        dist = dist * 1000.0;      // 단위  km 에서 m 로 변환

        return dist;
    }

    //degree->radian 변환
    public double DegreeToRadian(double degree){
        return degree * Math.PI / 180.0;
    }

    //randian -> degree 변환
    public double RadianToDegree(double radian){
        return radian * 180d / Math.PI;
    }
}
