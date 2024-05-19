package com.appsonair;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;
import com.skydoves.powerspinner.PowerSpinnerView;

public class FeedbackActivity extends AppCompatActivity {

    private static final String TAG = "FeedbackActivity";
    private Uri imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        //init views
        LinearLayout linearLayout = findViewById(R.id.ll_main);
        LinearLayout llAppbar = findViewById(R.id.ll_appbar);

        TextView tvAppbarTitle = findViewById(R.id.tv_appbar_title);
        TextView tvTicketType = findViewById(R.id.tv_ticket_type);
        TextView tvDescription = findViewById(R.id.tv_description);
        TextView tvEmail = findViewById(R.id.tv_email);

        PowerSpinnerView spinner = findViewById(R.id.sp_ticket_type);
        FrameLayout flBugView = findViewById(R.id.fl_bug_view);
        Button btnSubmit = findViewById(R.id.btn_submit);

        ImageView imgBug = findViewById(R.id.img_bug);
        ImageView icRemove = findViewById(R.id.ic_remove);
        ImageView icClose = findViewById(R.id.ic_close);

        EditText etEmail = findViewById(R.id.et_email);
        TextInputEditText etDescription = findViewById(R.id.et_description);
        TextInputLayout tilDescription = findViewById(R.id.til_description);

        //set view properties
        ShakeBugService.Companion companion = ShakeBugService.Companion;
        linearLayout.setBackgroundColor(parseColorToInteger(companion.getPageBackgroundColor()));

        llAppbar.setBackgroundColor(parseColorToInteger(companion.getAppBarBackgroundColor()));
        tvAppbarTitle.setText(companion.getAppBarTitleText());
        tvAppbarTitle.setTextColor(parseColor(companion.getAppBarTitleColor()));

        tvTicketType.setText(companion.getTicketTypeLabelText());
        tvTicketType.setTextColor(parseColor(companion.getTicketTypeLabelColor()));

        tvDescription.setText(companion.getDescriptionLabelText());
        tvDescription.setTextColor(parseColor(companion.getDescriptionLabelColor()));
        tilDescription.setCounterMaxLength(companion.getDescriptionMaxLength());
        tilDescription.setCounterTextColor(parseColor(companion.getDescriptionCounterTextColor()));
        tilDescription.setPlaceholderText(companion.getDescriptionHintText());
        tilDescription.setPlaceholderTextColor(parseColor(companion.getDescriptionHintColor()));

        tvEmail.setText(companion.getEmailLabelText());
        tvEmail.setTextColor(parseColor(companion.getEmailLabelColor()));
        etEmail.setHint(companion.getEmailHintText());
        etEmail.setHintTextColor(parseColor(companion.getEmailHintColor()));

        btnSubmit.setText(companion.getButtonText());
        btnSubmit.setTextColor(parseColor(companion.getButtonTextColor()));
        btnSubmit.setBackgroundTintList(parseColor(companion.getButtonBackgroundColor()));

        // Retrieve image path from Intent extras
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("IMAGE_PATH")) {
            imagePath = intent.getParcelableExtra("IMAGE_PATH");
            imgBug.setImageURI(imagePath);
        } else {
            Log.d(TAG, "Handle the case where no image path is provided");
        }

        icClose.setOnClickListener(view -> onBackPressed());

        icRemove.setOnClickListener(view -> {
            imagePath = null;
            flBugView.setVisibility(View.GONE);
        });
        spinner.selectItemByIndex(0);
        spinner.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener<String>() {
            @Override
            public void onItemSelected(int oldIndex, @Nullable String oldItem, int newIndex, String newItem) {

            }
        });

        btnSubmit.setOnClickListener(view -> {
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

    private ColorStateList parseColor(String color) {
        return ColorStateList.valueOf(Color.parseColor(color));
    }

    private Integer parseColorToInteger(String color) {
        return Color.parseColor(color);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}