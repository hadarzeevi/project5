package com.example.project5.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.project5.R;

public class ComposeActivity extends Activity {

    private EditText etTo, etSubject, etBody;
    private ImageButton btnSend, btnClear, btnClose;
    private TextView tvSenderEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compose_modal);

        View modal = findViewById(R.id.composeModal);
        Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_top);
        modal.startAnimation(slideIn);

        etTo = findViewById(R.id.etTo);
        etSubject = findViewById(R.id.etSubject);
        etBody = findViewById(R.id.etBody);
        tvSenderEmail = findViewById(R.id.tvSenderEmail);

        btnSend = findViewById(R.id.btnSend);
        btnClear = findViewById(R.id.btnClear);
        btnClose = findViewById(R.id.btnClose);

        // שליפת מייל השולח מההעדפות
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String senderEmail = prefs.getString("email", "unknown@example.com");
        tvSenderEmail.setText(senderEmail);

        btnSend.setOnClickListener(v -> {
            String to = etTo.getText().toString();
            String subject = etSubject.getText().toString();
            String body = etBody.getText().toString();

            if (to.isEmpty() || subject.isEmpty()) {
                Toast.makeText(this, "נא למלא כתובת ונושא", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "נשלח ל: " + to, Toast.LENGTH_SHORT).show();
            finish(); // סגירה לאחר שליחה
        });

        View.OnClickListener goHome = v -> {
            Intent intent = new Intent(ComposeActivity.this, MailHomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        };

        btnClear.setOnClickListener(goHome);
        btnClose.setOnClickListener(goHome);
    }
}
