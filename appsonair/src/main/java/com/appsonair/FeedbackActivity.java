package com.appsonair;

import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class FeedbackActivity extends AppCompatActivity {

    private static final String TAG = "FeedbackActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        Spinner spinner = findViewById(R.id.sp_ticket_type);
        FrameLayout flBugView = findViewById(R.id.fl_bug_view);
        ImageView icDropDown = findViewById(R.id.ic_drop_down);
        ImageView imgBug = findViewById(R.id.img_bug);
        ImageView icRemove = findViewById(R.id.ic_remove);
        ImageView icClose = findViewById(R.id.ic_close);
        Button btnSubmit = findViewById(R.id.btn_submit);
        EditText etEmail = findViewById(R.id.et_email);
        EditText etDescription = findViewById(R.id.et_description);

        icClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        icRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flBugView.setVisibility(View.GONE);
            }
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedItem = spinner.getSelectedItem().toString();
                // imgDropDown.setImageResource(R.drawable.ic_down_arrow);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = etEmail.getText().toString().trim();
                String description = etDescription.getText().toString().trim();
                if (description.isEmpty() && email.isEmpty()) {
                    etDescription.setError(getResources().getString(R.string.description_required));
                    etEmail.setError(getResources().getString(R.string.email_required));
                } else if (description.isEmpty()) {
                    etDescription.setError(getResources().getString(R.string.description_required));
                } else if (email.isEmpty()) {
                    etEmail.setError(getResources().getString(R.string.email_required));
                } else if (!isValidEmail(email)) {
                    etEmail.setError(getResources().getString(R.string.invalid_email));
                } else {
                    hideKeyboard();
                    etEmail.setError(null);
                    etDescription.setError(null);
                }
            }
        });
    }

    private boolean isValidEmail(CharSequence email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}