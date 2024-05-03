package com.appsonair;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AppsOnAirServices {

    static String appId;
    static Boolean showNativeUI;
    private static final String TAG = "AppsOnAirServices";

    public static void setAppId(String appId, boolean showNativeUI) {
        AppsOnAirServices.appId = appId;
        AppsOnAirServices.showNativeUI = showNativeUI;
    }

    public static void getResponse(@NonNull Response response, Context context, UpdateCallBack callBack, boolean isFromCDN) {
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
                }
                callBack.onSuccess(myResponse);
            } else if (isFromCDN) {
                callServiceApi(context, callBack);
            }
        } catch (Exception e) {
            callBack.onFailure(e.getMessage());
            Log.d(TAG, "getResponse: " + e.getMessage());
        }
    }

    public static void callCDNServiceApi(Context context, UpdateCallBack callBack) {
        String url = BuildConfig.CDN_BASE_URL + AppsOnAirServices.appId + ".json";
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request request = new Request.Builder().url(url).method("GET", null).build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d(TAG, "onFailure: AppsOnAirCDNApi" + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                getResponse(response, context, callBack, true);
            }
        });
    }

    public static void callServiceApi(Context context, UpdateCallBack callBack) {
        String url = BuildConfig.Base_URL + AppsOnAirServices.appId;
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request request = new Request.Builder().url(url).method("GET", null).build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d(TAG, "onFailure: AppsOnAirServiceApi" + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                getResponse(response, context, callBack, false);
            }
        });
    }

    public static void checkForAppUpdate(Context context, UpdateCallBack callBack) {
        ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                callCDNServiceApi(context, callBack);
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
