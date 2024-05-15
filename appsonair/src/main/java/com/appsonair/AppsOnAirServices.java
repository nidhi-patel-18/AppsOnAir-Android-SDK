package com.appsonair;

import static android.content.Context.SENSOR_SERVICE;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class AppsOnAirServices {

    static String appId;
    static Boolean showNativeUI;
    private static final String TAG = "AppsOnAirServices";

    static SensorManager mSensorManager;
    static float mAccel;
    static float mAccelCurrent;
    static float mAccelLast;

    public static void setAppId(String appId, boolean showNativeUI) {
        AppsOnAirServices.appId = appId;
        AppsOnAirServices.showNativeUI = showNativeUI;
    }

    public static void shakeBug(Context context) {
        mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        Objects.requireNonNull(mSensorManager).registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                float x = sensorEvent.values[0];
                float y = sensorEvent.values[1];
                float z = sensorEvent.values[2];
                mAccelLast = mAccelCurrent;
                mAccelCurrent = (float) Math.sqrt(x * x + y * y + z * z);
                float delta = mAccelCurrent - mAccelLast;
                mAccel = mAccel * 0.9f + delta;
                if (mAccel > 12) {
                    captureScreen(context);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        }, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 10f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;
    }

    public static void captureScreen(Context context) {
        View rootView = ((Activity) context).getWindow().getDecorView().getRootView();
        rootView.setDrawingCacheEnabled(true);
        Bitmap screenshotBitmap = Bitmap.createBitmap(rootView.getDrawingCache());
        rootView.setDrawingCacheEnabled(false);
        String screenshotPath = saveBitmapToFile(screenshotBitmap, context);

        File imageFile = new File(screenshotPath);
        if (!imageFile.exists()) {
            return;
        }
        Uri imageUri = Uri.fromFile(imageFile);

        Intent intent = new Intent(context, EditImageActivity.class);
        intent.setAction(Intent.ACTION_EDIT);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.putExtra("IMAGE_PATH", imageUri);
        context.startActivity(intent);
    }

    public static String getCurrentDateTimeString() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }

    public static String saveBitmapToFile(Bitmap bitmap, Context context) {
        try {
            File cacheDir = context.getCacheDir();
            String fileName = context.getString(R.string.app_name) + "_" + getCurrentDateTimeString() + ".jpg";
            File screenshotFile = new File(cacheDir, fileName);

            FileOutputStream outputStream = new FileOutputStream(screenshotFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            return screenshotFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Error saving screenshot", e);
            return null;
        }
    }

    public static void checkForAppUpdate(Context context, UpdateCallBack callback) {
        ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                String url = BuildConfig.Base_URL + AppsOnAirServices.appId;
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();
                Request request = new Request.Builder()
                        .url(url)
                        .method("GET", null)
                        .build();
                client.newCall(request).enqueue(new okhttp3.Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.d("EX:", String.valueOf(e));
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) {
                        try {
                            if (response.code() == 200) {
                                String myResponse = response.body().string();
                                JSONObject jsonObject = new JSONObject(myResponse);
                                JSONObject updateData = jsonObject.getJSONObject("updateData");
                                boolean isAndroidUpdate = updateData.getBoolean("isAndroidUpdate");
                                boolean isMaintenance = jsonObject.getBoolean("isMaintenance");
                                if (isAndroidUpdate) {
                                    boolean isAndroidForcedUpdate = updateData.getBoolean("isAndroidForcedUpdate");
                                    String androidBuildNumber = updateData.getString("androidBuildNumber");
                                    PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                                    int versionCode = info.versionCode;
                                    int buildNum = 0;

                                    if (!(androidBuildNumber.equals(null))) {
                                        buildNum = Integer.parseInt(androidBuildNumber);
                                    }
                                    boolean isUpdate = versionCode < buildNum;
                                    if (showNativeUI && isUpdate && (isAndroidForcedUpdate || isAndroidUpdate)) {
                                        Intent intent = new Intent(context, AppUpdateActivity.class);
                                        intent.putExtra("res", myResponse);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        context.startActivity(intent);
                                    }
                                } else if (isMaintenance && showNativeUI) {
                                    Intent intent = new Intent(context, MaintenanceActivity.class);
                                    intent.putExtra("res", myResponse);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(intent);
                                } else {
                                    //TODO : There is No Update and No Maintenance.
                                }
                                callback.onSuccess(myResponse);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            callback.onFailure(e.getMessage());
                            Log.d("AAAA", String.valueOf(e.getMessage()));

                        }
                    }
                });
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
            }
        };

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback);
        } else {
            NetworkRequest request = new NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build();
            connectivityManager.registerNetworkCallback(request, networkCallback);
        }
    }

}
