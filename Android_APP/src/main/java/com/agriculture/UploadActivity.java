package com.agriculture;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import static java.lang.System.exit;

public class UploadActivity extends AppCompatActivity {
    private String fileName = null;
    private File imgFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        System.out.println("--------------------------Here in UploadActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        int SDK_INT = Build.VERSION.SDK_INT;
        if(SDK_INT > 8){
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        fileName = getIntent().getStringExtra("fileName");
        if(fileName == null){
            System.out.println("------------------------fileName = null");
            exit(1);
        }
        System.out.println("-------------IN UPLOADACTIVITY fileName = " + fileName);
        imgFile = new File(fileName);
        if(imgFile.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            Matrix matrix = new Matrix();
            matrix.postRotate(getDisplayOrientation());
            Bitmap rotatedBitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true);
            ImageView myImage = (ImageView) findViewById(R.id.upload_preview);
            myImage.setImageBitmap(rotatedBitmap);
        }

        Button button = findViewById(R.id.upload_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Start Upload File" + fileName, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                Thread thread = new Thread(sendHTTP);
                thread.start();
                Snackbar.make(v, "Finished Upload", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });
    }

    private Runnable sendHTTP = new Runnable() {
        @Override
        public void run() {
            HttpURLConnection connection = null;

            try{
                URL url = new URL("http://18.219.71.129:5566");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "multipart/from-data ;boundary=*****");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setUseCaches(false);

                OutputStream ostream = connection.getOutputStream();
                DataOutputStream writer = new DataOutputStream(ostream);
                writer.writeBytes("--*****\r\n");
                writer.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n");
                writer.writeBytes("Content-Type: image/jpeg\r\n\r\n");

                byte[] picData = new byte[(int) imgFile.length()];
                DataInputStream distream = new DataInputStream(new FileInputStream(imgFile));
                distream.readFully(picData);
                distream.close();
                writer.write(picData);

                writer.flush();
                writer.close();
                ostream.close();

                InputStream istream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(istream));
                String line;
                StringBuilder response = new StringBuilder();

                while((line = reader.readLine()) != null){
                    response.append(line + "\n");
                }
                reader.close();
                istream.close();
                System.out.println("\n\n" + response);
            }catch(MalformedURLException mfURLe){
                System.out.println("MalformedURLException");
                mfURLe.printStackTrace();
                System.exit(1);
            }catch (IOException ioe){
                System.out.println("IOException");
                ioe.printStackTrace();
                System.exit(1);
            }finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
    };





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
}
