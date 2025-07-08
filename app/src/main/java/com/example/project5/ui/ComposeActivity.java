package com.example.project5.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.project5.R;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ComposeActivity extends Activity {

    private EditText etTo, etSubject, etBody;
    private ImageButton btnSend, btnClear, btnClose;
    private TextView tvSenderEmail;
    private final OkHttpClient client = new OkHttpClient();
    private final String SPAM_CHECK_URL = "http://10.0.2.2:4000/api/blacklist/check";
    private final String MAIL_SEND_URL = "http://10.0.2.2:4000/api/mails";

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

        Intent intent = getIntent();
        if (intent != null) {
            String replyTo = intent.getStringExtra("replyTo");
            String replySubject = intent.getStringExtra("replySubject");
            String replyBody = intent.getStringExtra("replyBody");

            if (replyTo != null) etTo.setText(replyTo);
            if (replySubject != null) etSubject.setText(replySubject);
            if (replyBody != null) etBody.setText(replyBody);
        }

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String senderEmail = prefs.getString("email", "unknown@example.com");
        String token = prefs.getString("token", null);
        tvSenderEmail.setText(senderEmail);

        btnSend.setOnClickListener(v -> sendMail(token));

        View.OnClickListener goHome = v -> {
            Intent intent2 = new Intent(ComposeActivity.this, MailHomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        };

        btnClear.setOnClickListener(goHome);
        btnClose.setOnClickListener(goHome);
    }

    private void sendMail(String token) {
        String to = etTo.getText().toString().trim();
        String subject = etSubject.getText().toString().trim();
        String body = etBody.getText().toString().trim();

        if (to.isEmpty() || subject.isEmpty()) {
            Toast.makeText(this, "נא למלא כתובת ונושא", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject checkPayload = new JSONObject();
            checkPayload.put("subject", subject);
            checkPayload.put("body", body);

            RequestBody checkBody = RequestBody.create(checkPayload.toString(), MediaType.get("application/json"));
            Request checkRequest = new Request.Builder()
                    .url(SPAM_CHECK_URL)
                    .post(checkBody)
                    .addHeader("Authorization", "Bearer " + token)
                    .build();

            client.newCall(checkRequest).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(ComposeActivity.this, "שגיאה בבדיקת ספאם", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        JSONObject result = new JSONObject(response.body().string());
                        boolean isSpam = result.optBoolean("spam", false);

                        JSONObject mailPayload = new JSONObject();
                        mailPayload.put("sendTo", to);
                        mailPayload.put("subject", subject);
                        mailPayload.put("body", body);
                        mailPayload.put("isSpam", isSpam);

                        RequestBody mailBody = RequestBody.create(mailPayload.toString(), MediaType.get("application/json"));
                        Request mailRequest = new Request.Builder()
                                .url(MAIL_SEND_URL)
                                .post(mailBody)
                                .addHeader("Authorization", "Bearer " + token)
                                .build();

                        client.newCall(mailRequest).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                runOnUiThread(() -> Toast.makeText(ComposeActivity.this, "שגיאה בשליחת מייל", Toast.LENGTH_SHORT).show());
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                runOnUiThread(() -> {
                                    if (response.isSuccessful()) {
                                        Toast.makeText(ComposeActivity.this, "המייל נשלח!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(ComposeActivity.this, MailHomeActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(ComposeActivity.this, "שגיאה בשליחה", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(ComposeActivity.this, "שגיאה פנימית", Toast.LENGTH_SHORT).show());
                    }
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "שגיאה בבניית הבקשה", Toast.LENGTH_SHORT).show();
        }
    }
}
