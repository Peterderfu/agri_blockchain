package com.example.newgal;

import android.Manifest.permission;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class checkPermissions {
    public static void check(Activity act) {
        if (ContextCompat.checkSelfPermission(act, permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(act, new String[]{permission.CAMERA}, 101);
        }
        if (ContextCompat.checkSelfPermission(act, permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(act, new String[]{permission.WRITE_EXTERNAL_STORAGE}, 102);
        }
        if (ContextCompat.checkSelfPermission(act, permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //System.out.println("==============ACCESS_FINE_LOCATION NOT GRANTED========================");
            ActivityCompat.requestPermissions(act, new String[]{permission.ACCESS_FINE_LOCATION}, 103);
        }
        if (ContextCompat.checkSelfPermission(act, permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(act, new String[]{permission.ACCESS_FINE_LOCATION}, 104);
        }
        if(ContextCompat.checkSelfPermission(act, permission.INTERNET) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(act, new String[]{permission.INTERNET}, 105);
        }
    }
}
