package com.example.project5.ui;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.project5.R;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Step3Fragment extends Fragment {

    EditText etEmail, etPassword, etConfirm;
    ImageView ivProfile;
    Button btnUpload, btnBack, btnRegister;
    Uri imageUri;
    TextView tvError;

    private static final int PICK_IMAGE_REQUEST = 1;

    RegisterActivity parentActivity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d("DEBUG", " on create!");

        View view = inflater.inflate(R.layout.fragment_step3, container, false);

        parentActivity = (RegisterActivity) getActivity();

        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        etConfirm = view.findViewById(R.id.etConfirmPassword);
        ivProfile = view.findViewById(R.id.ivProfile);
        btnUpload = view.findViewById(R.id.btnUploadImage);
        btnBack = view.findViewById(R.id.btnBack);
        btnRegister = view.findViewById(R.id.btnRegister);
        tvError = view.findViewById(R.id.tvError);

        // טענת ערכים קיימים
        if (parentActivity.email != null) etEmail.setText(parentActivity.email);
        if (parentActivity.password != null) etPassword.setText(parentActivity.password);
        if (parentActivity.password != null) etConfirm.setText(parentActivity.password);
        if (parentActivity.profileImageUri != null) ivProfile.setImageURI(parentActivity.profileImageUri);

        btnUpload.setOnClickListener(v -> openImageChooser());
        btnBack.setOnClickListener(v -> parentActivity.goToPreviousStep());
        Log.d("DEBUG", " go handle!");

        btnRegister.setOnClickListener(v -> handleRegister());
        Log.d("DEBUG", " after handle!");

        return view;
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.getData();
            ivProfile.setImageURI(imageUri);
            parentActivity.profileImageUri = imageUri; // שמירת ה-Uri ב-Activity
        }
    }

    private void handleRegister() {
        tvError.setVisibility(View.GONE);
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString();
        String confirm = etConfirm.getText().toString();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Invalid email");
            return;
        }

        if (pass.length() < 8 || !pass.matches(".*\\d.*")) {
            showError("Password must be at least 8 characters and contain a number");
            return;
        }
        Log.d("DEBUG", " pass!");

        if (!pass.equals(confirm)) {
            showError("Passwords do not match");
            return;
        }
        Log.d("DEBUG", " handle!");

        parentActivity.email = email;
        parentActivity.password = pass;
        Log.d("DEBUG", " before!");

        // כאן שולחים את כל הנתונים לשרת (כולל תמונת פרופיל)
        sendRegistrationToServer();
    }

    private void sendRegistrationToServer() {
        Log.d("DEBUG", "start");

        new Thread(() -> {
            Log.d("DEBUG", "thread");

            try {
                Log.d("DEBUG", "try");

                // בנייה של בקשת POST multipart/form-data
                String boundary = "===" + System.currentTimeMillis() + "===";
                URL url = new URL("http://10.0.2.2:4000/api/users/register");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                Log.d("DEBUG", "register clicked!");

                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                DataOutputStream request = new DataOutputStream(connection.getOutputStream());

                // הוספת שדות טקסט
                writeFormField(request, boundary, "first_name", parentActivity.firstName);
                writeFormField(request, boundary, "last_name", parentActivity.lastName);
                writeFormField(request, boundary, "username", parentActivity.username);
                writeFormField(request, boundary, "birth_date", parentActivity.birthDate);
                writeFormField(request, boundary, "gender", parentActivity.gender);
                writeFormField(request, boundary, "mail", parentActivity.email);
                writeFormField(request, boundary, "password", parentActivity.password);
                writeFormField(request, boundary, "reWritePassword", parentActivity.reWritePassword);


                // הוספת תמונת פרופיל אם קיימת
                if (parentActivity.profileImageUri != null) {
                    InputStream imageStream = getContext().getContentResolver().openInputStream(parentActivity.profileImageUri);
                    if (imageStream != null) {
                        writeFileField(request, boundary, "profileImage", "profile.png", "image/png", imageStream);
                        imageStream.close();
                    }
                }

                // סוף הפוסט
                request.writeBytes("--" + boundary + "--\r\n");
                request.flush();
                request.close();

                int responseCode = connection.getResponseCode();
                InputStream responseStream = responseCode == 200 ?
                        connection.getInputStream() : connection.getErrorStream();

                BufferedReader in = new BufferedReader(new InputStreamReader(responseStream));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                Log.d("DEBUG", "hello" + responseCode);

                if (responseCode == 200) {
                    Log.d("DEBUG", " if!");

                    // הצלחה - חזרה ל-login (ב-UI thread)
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Registration successful!", Toast.LENGTH_SHORT).show();
                        requireActivity().finish();  // סוגר את המסך, או לנווט ל-login
                    });
                } else {
                    Log.d("DEBUG", " else!");
                    requireActivity().runOnUiThread(() -> {


                        Toast.makeText(getContext(), "Registration failed: " + response.toString(), Toast.LENGTH_LONG).show();
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void writeFormField(DataOutputStream out, String boundary, String fieldName, String fieldValue) throws IOException {
        out.writeBytes("--" + boundary + "\r\n");
        out.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"\r\n\r\n");
        out.writeBytes(fieldValue + "\r\n");
    }

    private void writeFileField(DataOutputStream out, String boundary, String fieldName, String fileName,
                                String mimeType, InputStream inputStream) throws IOException {
        out.writeBytes("--" + boundary + "\r\n");
        out.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"\r\n");
        out.writeBytes("Content-Type: " + mimeType + "\r\n\r\n");

        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        out.writeBytes("\r\n");
    }
    private void showError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }
}