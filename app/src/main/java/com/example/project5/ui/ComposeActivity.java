package com.example.project5.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.project5.utils.SpamUtils;


import com.example.project5.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ComposeActivity extends Activity {

    private EditText etTo, etSubject, etBody;
    private ImageButton btnSend, btnClear, btnClose, btnAttach, btnRemoveAttachment;
    private TextView tvSenderEmail, tvAttachmentName;
    private LinearLayout attachmentContainer;
    private String draftId = null;

    private final OkHttpClient client = new OkHttpClient();
    private final String SPAM_CHECK_URL = "http://10.0.2.2:4000/api/blacklist/check";
    private final String MAIL_SEND_URL = "http://10.0.2.2:4000/api/files/send";

    private static final int PICK_FILE_REQUEST_CODE = 1;
    private Uri selectedFileUri = null;

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
        btnAttach = findViewById(R.id.btnAttach);
        attachmentContainer = findViewById(R.id.attachmentContainer);
        tvAttachmentName = findViewById(R.id.tvAttachmentName);
        btnRemoveAttachment = findViewById(R.id.btnRemoveAttachment);

        // תמיכה במענה למייל

        Intent intent = getIntent();
        if (intent != null) {
            draftId = intent.getStringExtra("draftId");

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

        btnAttach.setOnClickListener(v -> {
            Intent pickFile = new Intent(Intent.ACTION_GET_CONTENT);
            pickFile.setType("*/*");
            startActivityForResult(Intent.createChooser(pickFile, "Select File"), PICK_FILE_REQUEST_CODE);
        });

        btnRemoveAttachment.setOnClickListener(v -> {
            selectedFileUri = null;
            attachmentContainer.setVisibility(View.GONE);
            Toast.makeText(this, "הקובץ הוסר", Toast.LENGTH_SHORT).show();
        });

        View.OnClickListener goHome = v -> {
            Intent intent2 = new Intent(ComposeActivity.this, MailHomeActivity.class);
            intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent2);
        };

        btnClear.setOnClickListener(goHome);
        btnClose.setOnClickListener(v -> {
            saveAsDraft();  // קודם שומר טיוטה

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            if (selectedFileUri != null) {
                String fileName = getFileName(selectedFileUri);
                long fileSize = getFileSize(selectedFileUri);
                String formattedSize = formatFileSize(fileSize);
                tvAttachmentName.setText(fileName + " (" + formattedSize + ")");
                attachmentContainer.setVisibility(View.VISIBLE);
                Toast.makeText(this, "קובץ נבחר לצירוף", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendMail(String token) {
        String to = etTo.getText().toString().trim();
        String subject = etSubject.getText().toString().trim();
        String body = etBody.getText().toString().trim();

        Log.d("SEND_MAIL", "Trying to send mail to: " + to + ", subject: " + subject);

        if (to.isEmpty() || subject.isEmpty()) {
            Toast.makeText(this, "נא למלא כתובת ונושא", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isSpam = SpamUtils.isEmailBlacklisted(this, to);
        Log.d("SEND_MAIL", "isSpam? " + isSpam);

        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("sendTo", to)
                .addFormDataPart("subject", subject)
                .addFormDataPart("body", body)
                .addFormDataPart("isSpam", String.valueOf(isSpam));

        if (selectedFileUri != null) {
            String fileName = getFileName(selectedFileUri);
            String mimeType = getContentResolver().getType(selectedFileUri);

            Log.d("SEND_MAIL", "File selected: " + fileName + " | MIME type: " + mimeType);

            if (mimeType == null) {
                mimeType = "application/octet-stream";
                Log.w("SEND_MAIL", "MIME type was null, using default: " + mimeType);
            }

            try {
                InputStream inputStream = getContentResolver().openInputStream(selectedFileUri);
                byte[] fileBytes = getBytes(inputStream);

                RequestBody fileBody = RequestBody.create(fileBytes, MediaType.parse(mimeType));
                builder.addFormDataPart("attachments", fileName, fileBody);
                Log.d("SEND_MAIL", "Attachment added to request");
            } catch (IOException e) {
                Log.e("SEND_MAIL", "Failed to read selected file", e);
                Toast.makeText(this, "שגיאה בקריאת הקובץ", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        RequestBody requestBody = builder.build();

        Request mailRequest = new Request.Builder()
                .url(MAIL_SEND_URL)
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        Log.d("SEND_MAIL", "Sending request to server...");

        client.newCall(mailRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("SEND_MAIL", "Network error while sending mail", e);
                runOnUiThread(() -> Toast.makeText(ComposeActivity.this, "שגיאה בשליחת מייל", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "No response body";
                Log.d("SEND_MAIL", "Server responded with code: " + response.code());
                Log.d("SEND_MAIL", "Response body: " + responseBody);

                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Log.i("SEND_MAIL", "Mail sent successfully");

                        if (draftId != null && !draftId.isEmpty()) {
                            Log.d("SEND_MAIL", "Deleting draft: " + draftId);
                            Request deleteRequest = new Request.Builder()
                                    .url("http://10.0.2.2:4000/api/mails/" + draftId)
                                    .delete()
                                    .addHeader("Authorization", "Bearer " + token)
                                    .build();

                            client.newCall(deleteRequest).enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    Log.e("SEND_MAIL", "Failed to delete draft", e);
                                }

                                @Override
                                public void onResponse(Call call, Response response) {
                                    Log.d("SEND_MAIL", "Draft deletion response: " + response.code());
                                }
                            });
                        }

                        Toast.makeText(ComposeActivity.this, "המייל נשלח בהצלחה!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ComposeActivity.this, MailHomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                    } else {
                        Log.e("SEND_MAIL", "Server returned error: " + response.code());
                        Toast.makeText(ComposeActivity.this, "שגיאה בשליחה: " + responseBody, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private long getFileSize(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
        cursor.moveToFirst();
        long size = cursor.getLong(sizeIndex);
        cursor.close();
        return size;
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private void saveAsDraft() {
        String to = etTo.getText().toString().trim();
        String subject = etSubject.getText().toString().trim();
        String body = etBody.getText().toString().trim();

        // 🛑 אם הכל ריק – אין מה לשמור
        if (to.isEmpty() && subject.isEmpty() && body.isEmpty()) {
            finish();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("token", null);

        JSONObject json = new JSONObject();
        try {
            json.put("sendTo", to);
            json.put("subject", subject);
            json.put("body", body);
            json.put("category", "drafts");
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "שגיאה בשמירת טיוטה", Toast.LENGTH_SHORT).show();
            return;
        }

        // בוחרים URL ו־method לפי האם זו טיוטה חדשה או קיימת
        String url;
        Request.Builder requestBuilder;

        if (draftId != null && !draftId.isEmpty()) {
            // טיוטה קיימת → נבצע PATCH
            url = "http://10.0.2.2:4000/api/mails/" + draftId;
            requestBuilder = new Request.Builder()
                    .url(url)
                    .patch(RequestBody.create(json.toString(), MediaType.get("application/json")));
        } else {
            // טיוטה חדשה → POST
            url = "http://10.0.2.2:4000/api/mails";
            requestBuilder = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(json.toString(), MediaType.get("application/json")));
        }

        // הוספת טוקן
        requestBuilder.addHeader("Authorization", "Bearer " + token);

        // שליחה
        client.newCall(requestBuilder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(ComposeActivity.this, "שגיאה בשמירת טיוטה", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(ComposeActivity.this, "הטיוטה נשמרה בהצלחה", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ComposeActivity.this, "השרת לא הצליח לשמור טיוטה", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                });
            }
        });
    }


}
