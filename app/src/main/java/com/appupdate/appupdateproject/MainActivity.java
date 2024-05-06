package com.appupdate.appupdateproject;


import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;

import androidx.core.app.ActivityCompat;

import com.appsonair.AppsOnAirServices;
import com.appsonair.FileSaveHelper;
import com.appsonair.ShakeDetector;
import com.appsonair.UpdateCallBack;
import com.appsonair.ScreenshotDetectionDelegate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;


public class MainActivity extends Activity {

    private ScreenshotDetectionDelegate screenshotDetectionDelegate;
    private ShakeDetector shakeDetector;
    private static final int REQUEST_EXTERNAL_STORAGe = 1;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View rootView = getWindow().getDecorView();

        //Get your appId from https://appsonair.com/
        AppsOnAirServices.setAppId("f79d23d0-c65e-4680-916b-513433049bd8", true);
        AppsOnAirServices.checkForAppUpdate(this, new UpdateCallBack() {
            @Override
            public void onSuccess(String response) {
                Log.e("mye", "" + response);
            }

            @Override
            public void onFailure(String message) {
                Log.e("mye", "onFailure" + message);

            }
        });

        shakeDetector = new ShakeDetector(this);
        screenshotDetectionDelegate = new ScreenshotDetectionDelegate(this);
        screenshotDetectionDelegate.startScreenshotDetection();
        shakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {
            @Override
            public void onShake() {
                // Capture screen when shaken
                Log.d(TAG, "onShake: ");
                captureScreen(rootView);
            }
        });
    }

    private void captureScreen(View view) {
        view.measure(MeasureSpec.makeMeasureSpec(view.getWidth(), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(view.getHeight(), MeasureSpec.EXACTLY));
        view.layout((int) view.getX(), (int) view.getY(), (int) view.getX() + view.getMeasuredWidth(), (int) view.getY() + view.getMeasuredHeight());
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache(true);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);

        //  String path = Environment.getExternalStorageDirectory().toString() + "/test.png";
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString() + "/test.png";

        File imageFile = new File(path);
        if (!imageFile.getParentFile().exists()) {
            imageFile.getParentFile().mkdirs();
        }

        try {
            OutputStream outputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        shakeDetector.onResume();
    }

    @Override
    protected void onPause() {
        shakeDetector.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop screenshot detection to avoid memory leaks
        screenshotDetectionDelegate.stopScreenshotDetection();
    }

}