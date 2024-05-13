package com.appsonair;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class FullscreenActivity extends AppCompatActivity {

    private static final String TAG = "FullscreenActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        ImageView imgScreenshot = findViewById(R.id.img_screenshot);
        ImageView icEdit = findViewById(R.id.ic_edit);
        ImageView icBack = findViewById(R.id.ic_back);

        // Retrieve image path from Intent extras
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("IMAGE_PATH")) {
            final Uri imagePath = intent.getParcelableExtra("IMAGE_PATH");
            imgScreenshot.setImageURI(imagePath);

            icEdit.setOnClickListener(v -> {
                Intent intent1 = new Intent(FullscreenActivity.this, EditImageActivity.class);
                intent1.setAction(Intent.ACTION_EDIT);
                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent1.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent1.setDataAndType(imagePath, "image/*");
                startActivity(intent1);
                finish();
            });
        } else {
            Log.d(TAG, "Handle the case where no image path is provided");
        }

        icBack.setOnClickListener(v -> onBackPressed());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}