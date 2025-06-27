package com.example.project5.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewStructure;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project5.MainActivity;
import com.example.project5.R;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

import okhttp3.*;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button nextButton, signInButton;
    private CheckBox showPasswordCheckbox;
    private OkHttpClient client = new OkHttpClient();
    private String userEmail;
    TextView tvError;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("LOGIN_DEBUG", "1");

        super.onCreate(savedInstanceState);
        Log.d("LOGIN_DEBUG", "2!");

        setContentView(R.layout.activity_login);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        nextButton = findViewById(R.id.nextButton);
        signInButton = findViewById(R.id.signInButton);
        showPasswordCheckbox = findViewById(R.id.showPasswordCheckbox);
        nextButton.setOnClickListener(v -> verifyEmail());
        showPasswordCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT);
            } else {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        });
        signInButton.setOnClickListener(v -> login());
        TextView registerText = findViewById(R.id.registerText);
        registerText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
    private void verifyEmail() {
        userEmail = emailEditText.getText().toString().trim();
        if (!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
            showError("Invalid email address");
            return;
        }


        try {
            JSONObject json = new JSONObject();
            json.put("mail", userEmail);
            RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));
            Request request = new Request.Builder()
                    .url("http://10.0.2.2:4000/api/tokens/verify-mail")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("ERROR", "Request failed: " + e.getMessage());
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Connection error", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    if (response.isSuccessful()) {
                        runOnUiThread(() -> {
                            showPasswordStep();
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Email not found", Toast.LENGTH_SHORT).show());
                    }
                }
            });

        } catch (Exception e) {
            Log.e("ERROR", "Exception in verifyEmail: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showPasswordStep() {
        passwordEditText.setVisibility(View.VISIBLE);
        signInButton.setVisibility(View.VISIBLE);
        showPasswordCheckbox.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.GONE);
        emailEditText.setEnabled(false);
    }

    private void login() {
        String password = passwordEditText.getText().toString().trim();

        if (password.isEmpty()) {
            Toast.makeText(this, "Enter password", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject json = new JSONObject();
            json.put("mail", userEmail);
            json.put("password", password);

            RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));
            Request request = new Request.Builder()
                    .url("http://10.0.2.2:4000/api/tokens")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Connection error", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        JSONObject data = null;
                        try {
                            data = new JSONObject(responseBody);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        String token = null;
                        try {
                            token = data.getString("token");
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        String userId = null;
                        try {
                            userId = data.getString("userId");
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                        // שמירה ב־SharedPreferences
                        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                        prefs.edit()
                                .putString("token", token)
                                .putString("userId", userId)
                                .apply();

                        // מעבר למסך הראשי
                        Intent intent = new Intent(LoginActivity.this, MailHomeActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show());
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void showError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }
}