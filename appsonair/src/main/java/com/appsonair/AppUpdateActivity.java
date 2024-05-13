package com.appsonair;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class AppUpdateActivity extends AppCompatActivity {

    Boolean activityClose = false;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_update);
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle1 = ai.metaData;
            int icon = 0;
            String name = "";

            if (bundle1 != null) {
                icon = bundle1.getInt("com.appsonair.icon");
                name = bundle1.getString("com.appsonair.name");
                if (name == "" || name == null) {
                    name = "Your";

                }
            } else if (name == null || name == "") {
                name = "Your";
                icon = getResources().getIdentifier(String.valueOf(R.drawable.maintenance_icon), "drawable", this.getPackageName());
            }

            Bundle bundle = this.getIntent().getExtras();
            String data = bundle.getString("res"); // NullPointerException.
            JSONObject jsonObject = new JSONObject(data);
            if (jsonObject.isNull(data)) {
                JSONObject updateData = jsonObject.getJSONObject("updateData");
                Boolean isAndroidForcedUpdate = updateData.getBoolean("isAndroidForcedUpdate");
                Boolean isAndroidUpdate = updateData.getBoolean("isAndroidUpdate");
                String androidBuildNumber = updateData.getString("androidBuildNumber");
                String playStoreURL = updateData.getString("androidUpdateLink");
                PackageInfo info = getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0);
                int versionCode = info.versionCode;
                int buildNum = 0;

                if (!(androidBuildNumber.equals(null))) {
                    buildNum = Integer.parseInt(androidBuildNumber);
                }
                boolean isUpdate = versionCode < buildNum;

                ImageView img_icon = findViewById(R.id.img_icon);

                if ((isAndroidForcedUpdate || isAndroidUpdate) && (isUpdate)) {
                    TextView txt_title = findViewById(R.id.txt_title);
                    TextView txt_des = findViewById(R.id.txt_des);
                    TextView txt_no_thanks = findViewById(R.id.txt_no_thanks);
                    TextView btn_update = findViewById(R.id.btn_update);
                    if (icon != 0) {
                        img_icon.setImageResource(icon);
                    }
                    txt_title.setText(name + " " + getString(R.string.update_title));
                    if (isAndroidForcedUpdate) {
                        txt_no_thanks.setVisibility(View.GONE);
                        txt_des.setText(getString(R.string.update_force_dsc));
                    } else {
                        txt_no_thanks.setVisibility(View.VISIBLE);
                        txt_des.setText(getString(R.string.update_dsc));
                        txt_no_thanks.setOnClickListener(v -> {
                            activityClose = true;
                            onBackPressed();
                        });

                    }
                    btn_update.setOnClickListener(v -> {
                        try {
                            Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(playStoreURL));
                            startActivity(marketIntent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            }

        } catch (JSONException | PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onBackPressed() {
        if (activityClose) {
            super.onBackPressed();
        }
    }
}