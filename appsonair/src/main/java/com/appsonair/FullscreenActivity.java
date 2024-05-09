package com.appsonair;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.FileNotFoundException;
import java.io.InputStream;

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

            icEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(FullscreenActivity.this, EditImageActivity.class);
                    intent.setAction(Intent.ACTION_EDIT);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    intent.setDataAndType(imagePath, "image/*");
                    startActivity(intent);
                }
            });
        } else {
            // Handle the case where no image path is provided
        }

        icBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void editScreenshotWithPhotoEditor(Context context, Uri imageUri) {
        Log.d(TAG, "editScreenshotWithPhotoEditor called");
        try {
            // Use ContentResolver to open the input stream
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);

            if (inputStream != null) {
                // If the inputStream is not null, proceed to open the EditImageActivity
                Intent intent = new Intent(context, EditImageActivity.class);
                intent.setAction(Intent.ACTION_EDIT);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.setDataAndType(imageUri, "image/*");
                context.startActivity(intent);
                finish();
            } else {
                Log.e(TAG, "InputStream is null for URI: " + imageUri);
            }

        } catch (Exception e) {
            Log.e(TAG, "editScreenshotWithPhotoEditor " + e);
            e.printStackTrace();
        }
    }
}