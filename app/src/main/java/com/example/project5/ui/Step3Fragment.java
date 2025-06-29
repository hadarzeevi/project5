package com.example.project5.ui;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
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
import okhttp3.*;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.project5.R;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

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
        Log.d("DEBUG", "start OkHttp");

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
                Log.d("DEBUG", "first_name = " + parentActivity.firstName);
                Log.d("DEBUG", "last_name = " + parentActivity.lastName);
                Log.d("DEBUG", "username = " + parentActivity.username);
                Log.d("DEBUG", "birth_date = " + parentActivity.birthDate);
                Log.d("DEBUG", "gender = " + parentActivity.gender);
                Log.d("DEBUG", "mail = " + parentActivity.email);
                Log.d("DEBUG", "password = " + parentActivity.password);
                Log.d("DEBUG", "reWritePass = " + parentActivity.password); // בהנחה שזה אותו ערך
                Log.d("DEBUG", "profileImageUri = " + (parentActivity.profileImageUri != null ? parentActivity.profileImageUri.toString() : "null"));

                // הוספת שדות טקסט
                writeFormField(request, boundary, "first_name", parentActivity.firstName);
                writeFormField(request, boundary, "last_name", parentActivity.lastName);
                writeFormField(request, boundary, "username", parentActivity.username);
                writeFormField(request, boundary, "birth_date", parentActivity.birthDate);
                writeFormField(request, boundary, "gender", parentActivity.gender);
                writeFormField(request, boundary, "mail", parentActivity.email);
                writeFormField(request, boundary, "password", parentActivity.password);
                writeFormField(request, boundary, "reWritePass", parentActivity.reWritePass);


                InputStream imageStream;

                if (parentActivity.profileImageUri != null) {
                    imageStream = getContext().getContentResolver().openInputStream(parentActivity.profileImageUri);

                } else {
                    // צור תמונת ברירת מחדל לפי האות הראשונה של המשתמש
                    Log.d("DEBUG", "ENTER");
                    String firstLetter = parentActivity.username != null && !parentActivity.username.isEmpty()
                            ? parentActivity.username.substring(0, 1)
                            : "U";
                    Bitmap defaultBitmap = generateProfileBitmap(firstLetter);
                    imageStream = bitmapToInputStream(defaultBitmap);
                }
                Log.d("DEBUG", "Writing file field with name: profileImage");
//
                writeFileField(request, boundary, "profileImage", "default.png", "image/png", imageStream);
                imageStream.close();


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
                Log.d("DEBUG", "" + responseCode);

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
//        new Thread(() -> {
//            try {
//                OkHttpClient client = new OkHttpClient();
//                Log.d("DEBUG", "first_name = " + parentActivity.firstName);
//                Log.d("DEBUG", "last_name = " + parentActivity.lastName);
//                Log.d("DEBUG", "username = " + parentActivity.username);
//                Log.d("DEBUG", "birth_date = " + parentActivity.birthDate);
//                Log.d("DEBUG", "gender = " + parentActivity.gender);
//                Log.d("DEBUG", "mail = " + parentActivity.email);
//                Log.d("DEBUG", "password = " + parentActivity.password);
//                Log.d("DEBUG", "reWritePass = " + parentActivity.password); // בהנחה שזה אותו ערך
//                Log.d("DEBUG", "profileImageUri = " + (parentActivity.profileImageUri != null ? parentActivity.profileImageUri.toString() : "null"));
//
//                MultipartBody.Builder builder = new MultipartBody.Builder()
//                        .setType(MultipartBody.FORM)
//                        .addFormDataPart("first_name", parentActivity.firstName)
//                        .addFormDataPart("last_name", parentActivity.lastName)
//                        .addFormDataPart("username", parentActivity.username)
//                        .addFormDataPart("birth_date", parentActivity.birthDate)
//                        .addFormDataPart("gender", parentActivity.gender)
//                        .addFormDataPart("mail", parentActivity.email)
//                        .addFormDataPart("password", parentActivity.password)
//                        .addFormDataPart("reWritePass", parentActivity.reWritePass);
//
//                // טיפול בתמונה
//                byte[] imageBytes;
//                if (parentActivity.profileImageUri != null) {
//                    InputStream is = getContext().getContentResolver().openInputStream(parentActivity.profileImageUri);
//                    imageBytes = readBytes(is);
//                    is.close();
//                } else {
//                    String firstLetter = parentActivity.username != null && !parentActivity.username.isEmpty()
//                            ? parentActivity.username.substring(0, 1)
//                            : "U";
//                    Bitmap defaultBitmap = generateProfileBitmap(firstLetter);
//                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                    defaultBitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
//                    imageBytes = bos.toByteArray();
//                }
//
//                if (imageBytes != null) {
//                    builder.addFormDataPart(
//                            "profileImage",
//                            "profile.png",
//                            RequestBody.create(imageBytes, MediaType.parse("image/png"))
//                    );
//                }
//
//                RequestBody requestBody = builder.build();
//
//                Request request = new Request.Builder()
//                        .url("http://10.0.2.2:4000/api/users/register")
//                        .post(requestBody)
//                        .build();
//
//                client.newCall(request).enqueue(new Callback() {
//                    @Override
//                    public void onFailure(Call call, IOException e) {
//                        e.printStackTrace();
//                        requireActivity().runOnUiThread(() ->
//                                Toast.makeText(getContext(), "Network error: " + e.getMessage(), Toast.LENGTH_LONG).show()
//                        );
//                    }
//
//                    @Override
//                    public void onResponse(Call call, Response response) throws IOException {
//                        String resBody = response.body().string();
//                        Log.d("DEBUG", "Response: " + resBody);
//                        if (response.isSuccessful()) {
//                            requireActivity().runOnUiThread(() -> {
//                                Toast.makeText(getContext(), "Registration successful!", Toast.LENGTH_SHORT).show();
//                                requireActivity().finish();
//                            });
//                        } else {
//                            requireActivity().runOnUiThread(() ->
//                                    Toast.makeText(getContext(), "Registration failed: " + resBody, Toast.LENGTH_LONG).show()
//                            );
//                        }
//                    }
//                });
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                requireActivity().runOnUiThread(() ->
//                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
//                );
//            }
//        }).start();
//    }
//    private byte[] readBytes(InputStream inputStream) throws IOException {
//        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
//        int bufferSize = 4096;
//        byte[] buffer = new byte[bufferSize];
//
//        int len;
//        while ((len = inputStream.read(buffer)) != -1) {
//            byteBuffer.write(buffer, 0, len);
//        }
//
//        return byteBuffer.toByteArray();
//    }

    private void writeFormField(DataOutputStream out, String boundary, String fieldName, String fieldValue) throws IOException {
        out.writeBytes("--" + boundary + "\r\n");
        out.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"\r\n\r\n");
        out.writeBytes(fieldValue + "\r\n");
    }

    private void writeFileField(DataOutputStream out, String boundary, String fieldName, String fileName,
                                String mimeType, InputStream inputStream) throws IOException {
        Log.d("DEBUG", ">>>>> writing file field");
        Log.d("DEBUG", ">>>>> field name: " + fieldName);
        Log.d("DEBUG", ">>>>> file name: " + fileName);
        Log.d("DEBUG", ">>>>> mime type: " + mimeType);
        Log.d("DEBUG", ">>>>> stream available: " + inputStream.available());

        out.writeBytes("--" + boundary + "\r\n");
        out.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"\r\n");
        out.writeBytes("Content-Type: " + mimeType + "\r\n");
        // הוסיפי את אורך התוכן
        if (inputStream instanceof ByteArrayInputStream) {
            int length = ((ByteArrayInputStream) inputStream).available();
            out.writeBytes("Content-Length: " + length + "\r\n");
        }
        out.writeBytes("\r\n");

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
    public Bitmap generateProfileBitmap(String letter) {
        int size = 200;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // צבע רקע רנדומלי
        int[] colors = {Color.parseColor("#f59e0b"), Color.parseColor("#10b981"),
                Color.parseColor("#3b82f6"), Color.parseColor("#ec4899"),
                Color.parseColor("#8b5cf6")};
        int bgColor = colors[new Random().nextInt(colors.length)];
        canvas.drawColor(bgColor);

        // צייר את האות
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(100);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float x = size / 2;
        float y = size / 2 - (fontMetrics.ascent + fontMetrics.descent) / 2;
        canvas.drawText(letter.toUpperCase(), x, y, paint);

        return bitmap;
    }
    public InputStream bitmapToInputStream(Bitmap bitmap) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] byteArray = bos.toByteArray();
        Log.d("DEBUG", "Generated default image byte size: " + byteArray.length);
        return new ByteArrayInputStream(byteArray);
    }

}