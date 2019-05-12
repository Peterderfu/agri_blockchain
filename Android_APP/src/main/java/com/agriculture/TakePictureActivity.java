package com.agriculture;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import android.location.LocationListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TakePictureActivity extends AppCompatActivity implements LocationListener {

    private SurfaceView main_sfv_id;
    private Camera camera;
    private Button take;
    private TextView mTxv;
    private static double longi, lati;
    private static Location lastlocation;

    private String preferred;
    private LocationManager mgr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_takepicture);

        mgr = null;
        preferred = null;
        lastlocation = null;
        main_sfv_id = (SurfaceView) findViewById(R.id.preview);
        take = findViewById(R.id.button);
        mTxv = findViewById(R.id.locationText);

        CheckPermission();
        getService();
        updateLocation();
        System.out.println("---------------------------------finished updateLocation");

        main_sfv_id.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                camera = Camera.open();

                Camera.Parameters parameters = camera.getParameters();
                //畫面旋轉
                int Or = getDisplayOrientation();
                camera.setDisplayOrientation(Or);
                parameters.setRotation(Or);
                //相機會自動對焦
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);


                camera.setParameters(parameters);

                //將畫面顯示到SurfaceView
                try{
                    camera.setPreviewDisplay(main_sfv_id.getHolder());
                    camera.startPreview();
                }catch (IOException e){
                    e.printStackTrace();
                }

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if(camera != null){
                    camera.stopPreview();
                    camera.release();
                }
            }
        });


        take.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lastlocation != null) {
                    System.out.println("-------------------camera start autoFocus");
                    camera.autoFocus(afcb);
                }
            }
        });

    }

    //讓呈現的畫面跟使用者是同一個方向
    public int getDisplayOrientation(){
        WindowManager mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display mDisplay = mWindowManager.getDefaultDisplay();
        Log.d("MYLOG_ORIENTAION_TEST", "getOrientation(): " + mDisplay.getOrientation());

        int rotation = mDisplay.getRotation(), degrees = 0;
        switch (rotation){
            case Surface.ROTATION_0:
                degrees = 0; break;
            case Surface.ROTATION_90:
                degrees = 90; break;
            case Surface.ROTATION_180:
                degrees = 180; break;
            case Surface.ROTATION_270:
                degrees = 270; break;
        }
        Camera.CameraInfo camInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, camInfo);
        return (camInfo.orientation - degrees + 360) % 360;
    }

    Camera.AutoFocusCallback afcb = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if(success){
                //對焦成功才拍照
                System.out.println("---------------camera start takePicture");
                camera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        String fileName = getFileNameForPicture();
                        System.out.println("------fileName = " + fileName);
                        try{
                            FileOutputStream fos = new FileOutputStream(fileName);
                            fos.write(data);
                            fos.close();
                            putExif(fileName);

                            Intent intent = new Intent(TakePictureActivity.this, UploadActivity.class);
                            intent.putExtra("fileName", fileName);
                            startActivity(intent);
                        }catch(IOException e){
                            System.out.print("-------------IOException when write picture " + e);
                        }
                    }

                    private String getFileNameForPicture(){
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd__HH:mm:ss");
                        String dateString = sdf.format(new Date());
                        File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM );
                        if(!folder.exists()) {
                            Log.d("MYLOG", "------------------------In get File Name For Picture-----------------------DCIM not exist!");
                            folder.mkdirs();
                        }
                        String ret = folder.getAbsolutePath() + "/PROJ";
                        folder = new File(ret);
                        if(!folder.exists()){
                            Log.d("MYLOG", "------------------------In get File Name For Picture-----------------------PROJ first created!");
                            folder.mkdirs();
                        }
                        ret += ("/" + dateString + ".jpg");
                        System.out.println("==============getFileNameForPicture:  returned " + ret);
                        return ret;
                    }

                    private void putExif(String fileName){
                        ExifInterface exif = null;
                        try{
                            exif = new ExifInterface(fileName);

                        }catch(IOException e){
                            Log.e("=====MYLOG=====", "cannot new ExifInterface", e);
                        }
                        System.out.println("=====================lati : "+String.valueOf(lati));
                        System.out.println("=====================longi: "+String.valueOf(longi));
                        if(lati > 0) {
                            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, String.valueOf(longi));
                            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "E");
                        }else{
                            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, String.valueOf(-longi));
                            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "W");
                        }
                        if(lati > 0){
                            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, String.valueOf(lati));
                            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "N");
                        }else{
                            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, String.valueOf(-lati));
                            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "S");
                        }
                        /*
                        try{
                            exif.saveAttributes();
                        }catch(IOException e){
                            Log.e("=====MYLOG=====", "cannot save exifAttributes", e);
                        }
                        try{
                            exif = new ExifInterface(fileName);
                        }catch(IOException e){
                            System.out.println("------------------when checking exif IOException");
                        }
                        System.out.println("Lati : " + exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE) + "  " + exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF));
                        System.out.println("Longi: " + exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE) + "  " + exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF));
                        */
                    }

                });
             }
        }
    };

    @Override
    public void onResume(){
        System.out.println("-------------------------Here in onResume");
        super.onResume();
        if(mgr == null){
            getService();
        }
        updateLocation();
    }

    @Override
    protected void onPause(){
        System.out.println("==========------------Here in onPause---------------------==========");
        super.onPause();
        if(mgr != null) {
            mgr.removeUpdates(this);
        }
        mgr = null;
        preferred = null;
    }

    public void CheckPermission(){
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 103);
        }
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 104);
        }
    }

    @Override
    public void onLocationChanged(Location location){
        System.out.println("-------------------------Here in onLocationChanged   location = " + location + "  last location " + lastlocation);
        if(location != null) {
            lastlocation = location;
            lati = location.getLatitude();
            longi = location.getLongitude();
            mTxv.setText("Latitude: " + Double.toString(lati) + "\nLongitude: " + Double.toString(longi));
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras){
        System.out.println("------------------GPS provider changed: " + provider);
    }

    @Override
    public void onProviderEnabled(String provider){
        System.out.println("------------------GPS provider enabled: " + provider);
    }

    @Override
    public void onProviderDisabled(String provider){
        System.out.println("------------------GPS provider disabled: " + provider);
    }

    private void getService(){
        mgr = (LocationManager) getSystemService(LOCATION_SERVICE);
        for(String prov: mgr.getAllProviders()){
            System.out.println("------------------PROVIDER: " + prov);
        }

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        List<String> providers = null;
        providers = mgr.getProviders(criteria, true);
        if(providers == null || providers.size() == 0){
            System.out.println("====================PROVIDERS EMPTY");
            System.exit(1);
        }
        preferred = providers.get(0);
        System.out.println("========preferred set to " + preferred);

        try {
            mgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, TakePictureActivity.this);
            System.out.println("-------------------------requestedUpdates");
        }catch(SecurityException e){
            System.out.println("============================SecuriteException in OnClick requestLocation");
        }
        System.out.println("-------------------------getService finished");
    }

    private void updateLocation(){
        try {
            System.out.println("------------------------start getLastKnownLocation");
            lastlocation = mgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            onLocationChanged(lastlocation);
            System.out.println("------------------------------------finished");
        }catch(SecurityException e){
            System.out.println("============================SecuriteException in OnClick requestLocation");
        }
    }
}