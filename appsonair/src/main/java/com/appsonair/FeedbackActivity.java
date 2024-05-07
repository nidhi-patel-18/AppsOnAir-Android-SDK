package com.appsonair;

import android.app.ActivityOptions;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;


public class FeedbackActivity extends AppCompatActivity {
    ImageView imaBug,imgClose,imgRemove;
    Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
    }
}