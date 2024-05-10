package com.appsonair;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONObject;

public class MaintenanceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance);
        try {
            Bundle bundle = this.getIntent().getExtras();
            String data = bundle.getString("res"); // NullPointerException.

            JSONObject jsonObject = new JSONObject(data);
            boolean isMaintenance = jsonObject.getBoolean("isMaintenance");
            if (isMaintenance) {
                LinearLayout maintenance_layout = findViewById(R.id.ll_root);
                LinearLayout maintenance_layout1 = findViewById(R.id.lls_root);
                maintenance_layout.setVisibility(View.GONE);
                maintenance_layout1.setVisibility(View.GONE);
                JSONObject maintenanceData = jsonObject.getJSONObject("maintenanceData");
                if (!maintenanceData.toString().equals("{}")) {
                    String title = maintenanceData.getString("title");
                    String description = maintenanceData.getString("description");
                    String image = maintenanceData.getString("image");
                    String textColorCode = maintenanceData.getString("textColorCode");
                    String backgroundColorCode = maintenanceData.getString("backgroundColorCode");

                    if (!title.equals("") && !description.equals("")) {
                        maintenance_layout.setVisibility(View.VISIBLE);

                        if (!backgroundColorCode.equals("")) {
                            maintenance_layout.setBackgroundColor(Color.parseColor(backgroundColorCode));
                        }
                        ImageView img_icon = findViewById(R.id.img_icon);
                        TextView txt_title_maintain = findViewById(R.id.txt_title_maintain);
                        TextView txt_des_maintain = findViewById(R.id.txt_des_maintain);
                        TextView txt_app_name = findViewById(R.id.txt_app_name);
                        if (!image.equals("") && !image.equals("null") && image != null) {
                            new DownloadImageTask(img_icon)
                                    .execute(image);
                        } else {
                            img_icon.setImageResource(R.drawable.maintenance_icon);
                        }
                        txt_title_maintain.setText(title);
                        txt_des_maintain.setText(description);

                        ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
                        Bundle bundle1 = ai.metaData;
                        String appName = bundle1.getString("com.appsonair.name");
                        txt_app_name.setText(appName);
                        if (textColorCode != "") {
                            txt_title_maintain.setTextColor(Color.parseColor(textColorCode));
                            txt_des_maintain.setTextColor(Color.parseColor(textColorCode));
                            txt_app_name.setTextColor(Color.parseColor(textColorCode));
                        }
                    } else {
                        maintenance_layout1.setVisibility(View.VISIBLE);
                        ImageView img_icon = findViewById(R.id.img2_icon);
                        TextView txt_title2_maintain = findViewById(R.id.txt_title2_maintain);

                        ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
                        Bundle bundle1 = ai.metaData;
                        String appName = bundle1.getString("com.appsonair.name");
                        txt_title2_maintain.setText(appName + " " + getString(R.string.maintenance));

                        if (!maintenanceData.toString().equals("{}")) {
                            if (!image.equals("") && !image.equals("null") && image != null) {
                                new DownloadImageTask(img_icon)
                                        .execute(image);
                            } else {
                                img_icon.setImageResource(R.drawable.maintenance_icon);
                            }
                        }
                    }
                } else {
                    maintenance_layout1.setVisibility(View.VISIBLE);
                    ImageView img_icon = findViewById(R.id.img2_icon);
                    TextView txt_title2_maintain = findViewById(R.id.txt_title2_maintain);

                    ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
                    Bundle bundle1 = ai.metaData;
                    String appName = bundle1.getString("com.appsonair.name");
                    txt_title2_maintain.setText(appName + " " + getString(R.string.maintenance));

                    if (!maintenanceData.toString().equals("{}")) {
                        String image = maintenanceData.getString("image");
                        if (!image.equals("") && !image.equals("null") && image != null) {
                            new DownloadImageTask(img_icon)
                                    .execute(image);
                        } else {
                            img_icon.setImageResource(R.drawable.maintenance_icon);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
    }
}