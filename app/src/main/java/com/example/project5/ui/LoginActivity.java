package com.example.project5.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
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
    private TextView tvError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        nextButton = findViewById(R.id.nextButton);
        signInButton = findViewById(R.id.signInButton);
        showPasswordCheckbox = findViewById(R.id.showPasswordCheckbox);
        tvError = findViewById(R.id.tvError);

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
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
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
                    runOnUiThread(() -> showError("Connection error"));
                }

                @Override
                public void onResponse(Call call, Response response) {
                    runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            showPasswordStep();
                        } else {
                            showError("Email not found");
                        }
                    });
                }
            });
        } catch (Exception e) {
            showError("Error verifying email");
            e.printStackTrace();
        }
    }

    private void showPasswordStep() {
        passwordEditText.setVisibility(View.VISIBLE);
        signInButton.setVisibility(View.VISIBLE);
        showPasswordCheckbox.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.GONE);
        emailEditText.setEnabled(false);
        tvError.setVisibility(View.GONE);
    }

    private void login() {
        String password = passwordEditText.getText().toString().trim();
        if (password.isEmpty()) {
            showError("Enter password");
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
                    runOnUiThread(() -> showError("Connection error"));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            String responseBody = response.body().string();
                            JSONObject data = new JSONObject(responseBody);
                            String token = data.getString("token");
                            String userId = data.getString("userId");

                            fetchUserProfileAndProceed(token, userId);

                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                            runOnUiThread(() -> showError("Error parsing server response"));
                        }
                    } else {
                        runOnUiThread(() -> showError("Incorrect password"));
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            showError("Login error");
        }
    }

    private void fetchUserProfileAndProceed(String token, String userId) {
        Request request = new Request.Builder()
                .url("http://10.0.2.2:4000/api/users/" + userId)
                .header("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> showError("Error fetching profile"));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String res = response.body().string();
                        JSONObject user = new JSONObject(res);
                        String profileImage = user.optString("profileImage", null);

                        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                        prefs.edit()
                                .putString("token", token)
                                .putString("userId", userId)
                                .putString("profileImage", profileImage)
                                .apply();

                        startActivity(new Intent(LoginActivity.this, MailHomeActivity.class));
                        finish();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> showError("Error parsing profile data"));
                    }
                } else {
                    runOnUiThread(() -> showError("Failed to fetch profile"));
                }
            }
        });
    }

    private void showError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }
}
