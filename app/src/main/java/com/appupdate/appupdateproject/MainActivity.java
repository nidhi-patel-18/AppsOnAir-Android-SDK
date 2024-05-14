package com.appupdate.appupdateproject;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.appsonair.AppsOnAirServices;
import com.appsonair.UpdateCallBack;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get your appId from https://appsonair.com/
        AppsOnAirServices.setAppId("bf299f68-7fa0-4d2e-a658-f75de4e860f7", true);
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
    }
}