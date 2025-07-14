package com.example.project5.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import com.bumptech.glide.Glide;
import com.example.project5.R;
import com.example.project5.model.Attachment;
import com.example.project5.model.Mail;
import com.example.project5.utils.SpamUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.ActivityNotFoundException;
import android.net.Uri;

public class MailDetailActivity extends AppCompatActivity {

    private TextView tvSubject, tvSenderName, tvSenderEmail, tvDate, tvBody;
    private ImageView ivStar, ivAvatar, ivBack, ivTrash, ivMore;
    private LinearLayout btnReply, btnForward;
    private Mail mail;
    private boolean isSpamCategory = false;

    private LinearLayout attachmentsContainer;
    private RecyclerView rvAttachments;
    private TextView tvAttachmentsTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_detail);

        // אתחול רכיבי ממשק
        initializeViews();

        try {
            String mailJson = getIntent().getStringExtra("MAIL_JSON");
            if (mailJson == null || mailJson.isEmpty()) {
                Log.e("MailDetailActivity", "No mail JSON provided");
                Toast.makeText(this, "שגיאה: אין נתוני מייל", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            Log.d("MailDetailActivity", "Received mail JSON: " + mailJson);

            JSONObject json = new JSONObject(mailJson);
            mail = Mail.fromJson(json);
            mail.debugPrint(); // ← זה יראה את כל הפרטים כולל מצורפים


            if (mail == null) {
                Log.e("MailDetailActivity", "Failed to parse mail from JSON");
                Toast.makeText(this, "שגיאה בפענוח נתוני המייל", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            Log.d("MailDetailActivity", "Successfully parsed mail: " + mail.id);

            isSpamCategory = "spam".equalsIgnoreCase(mail.category);
            loadMailData();

        } catch (JSONException e) {
            Log.e("MailDetailActivity", "JSON parsing error: " + e.getMessage());
            Toast.makeText(this, "שגיאה בפענוח נתוני המייל", Toast.LENGTH_SHORT).show();
            finish();
            return;
        } catch (Exception e) {
            Log.e("MailDetailActivity", "General error: " + e.getMessage());
            Toast.makeText(this, "שגיאה בטעינת המייל", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupClickListeners();
    }
    private void initializeViews() {
        tvSubject = findViewById(R.id.tvSubject);
        tvSenderName = findViewById(R.id.tvSenderName);
        tvSenderEmail = findViewById(R.id.tvSenderEmail);
        tvDate = findViewById(R.id.tvDate);
        tvBody = findViewById(R.id.tvBody);
        ivStar = findViewById(R.id.ivStar);
        ivAvatar = findViewById(R.id.ivAvatar);
        ivBack = findViewById(R.id.ivBack);
        ivTrash = findViewById(R.id.ivTrash);
        btnReply = findViewById(R.id.btnReply);
        btnForward = findViewById(R.id.btnForward);
        ivMore = findViewById(R.id.ivMore);
        attachmentsContainer = findViewById(R.id.attachmentsContainer);
        rvAttachments = findViewById(R.id.rvAttachments);
        tvAttachmentsTitle = findViewById(R.id.tvAttachmentsTitle);
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> finish());

        ivTrash.setOnClickListener(v -> {
            if (isSpamCategory) {
                // אם המייל בספאם, מחיקה לצמיתות
                deletePermanently(mail.id);
            } else {
                // אחרת, העברה לאשפה
                moveToTrash(mail.id);
            }
        });

        ivStar.setOnClickListener(v -> {
            mail.isStarred = !mail.isStarred;
            updateStarIcon();
            updateStarOnServer(mail.id, mail.isStarred);
            Toast.makeText(this, mail.isStarred ? "סומן בכוכב" : "הוסר מכוכבים", Toast.LENGTH_SHORT).show();
        });

        btnReply.setOnClickListener(v -> {
            Intent intent = new Intent(this, ComposeActivity.class);
            intent.putExtra("replyTo", mail.fromMail);
            intent.putExtra("replySubject", "Re: " + mail.subject);
            intent.putExtra("replyBody", "\n\n--- original message ---\n" + mail.body);
            startActivity(intent);
        });

        btnForward.setOnClickListener(v -> {
            Intent intent = new Intent(this, ComposeActivity.class);
            intent.putExtra("replySubject", "Fwd: " + mail.subject);
            intent.putExtra("replyBody", "\n\n--- message forward ---\n" + mail.body);
            startActivity(intent);
        });

        ivMore.setOnClickListener(this::showPopupMenu);
    }

    private void loadMailData() {
        tvSubject.setText(mail.subject != null ? mail.subject : "(ללא נושא)");
        tvBody.setText(mail.body != null ? mail.body : "(אין תוכן)");
        tvSenderEmail.setText(mail.fromMail != null ? mail.fromMail : "(שולח לא ידוע)");
        tvDate.setText(formatDate(mail.date));

        String senderName = (mail.firstName != null && mail.lastName != null)
                ? mail.firstName + " " + mail.lastName
                : mail.fromMail != null ? mail.fromMail : "(שולח)";
        tvSenderName.setText(senderName);

        if (mail.profileImage != null && !mail.profileImage.isEmpty()) {
            String url = "http://10.0.2.2:4000/" + mail.profileImage.replace("\\", "/");
            Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.ic_avatar)
                    .error(R.drawable.ic_avatar)
                    .circleCrop()
                    .into(ivAvatar);
        } else {
            ivAvatar.setImageResource(R.drawable.ic_avatar);
        }

        updateStarIcon();

        // תיקון בטיפול במצורפים
        try {
            displayAttachments();
        } catch (Exception e) {
            Log.e("MailDetailActivity", "Error displaying attachments: " + e.getMessage());
            // הסתרת קונטיינר המצורפים במקרה של שגיאה
            if (attachmentsContainer != null) {
                attachmentsContainer.setVisibility(View.GONE);
            }
            if (tvAttachmentsTitle != null) {
                tvAttachmentsTitle.setVisibility(View.GONE);
            }
        }
    }
    private void updateStarIcon() {
        ivStar.setImageResource(mail.isStarred ? R.drawable.ic_star : R.drawable.ic_star_border);
    }

    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = serverFormat.parse(dateStr);
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return displayFormat.format(date);
        } catch (Exception e) {
            return dateStr;
        }
    }

    private void showPopupMenu(View anchorView) {
        PopupMenu popup = new PopupMenu(this, anchorView);
        popup.getMenuInflater().inflate(R.menu.menu_mail_option, popup.getMenu());

        if ("inbox".equalsIgnoreCase(mail.category)) {
            popup.getMenu().findItem(R.id.action_mark_as_spam).setVisible(true);
        } else {
            popup.getMenu().findItem(R.id.action_mark_as_spam).setVisible(false);
        }

        // ✨ הצגת כפתור הסרת תווית רק אם יש תוויות
        if (mail.label != null && !mail.label.isEmpty()) {
            popup.getMenu().findItem(R.id.action_remove_label).setVisible(true);
        } else {
            popup.getMenu().findItem(R.id.action_remove_label).setVisible(false);
        }

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_add_label) {
                showLabelSelectionDialog();
                return true;
            } else if (item.getItemId() == R.id.action_remove_label) {
                showLabelRemovalDialog(); // ← פונקציה חדשה שניצור מיד
                return true;
            } else if (item.getItemId() == R.id.action_mark_as_spam) {
                markMailAsSpam();
                return true;
            } else if (item.getItemId() == R.id.action_not_spam) {
                removeMailFromSpam();
                return true;
            }
            return false;
        });

        popup.show();
    }
    private void showLabelRemovalDialog() {
        List<String> currentLabels = mail.label;
        if (currentLabels == null || currentLabels.isEmpty()) {
            Toast.makeText(this, "אין תוויות להסרה", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] labelArray = currentLabels.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("choose label");
        builder.setItems(labelArray, (dialog, which) -> {
            String selectedLabel = labelArray[which];
            removeLabelFromMail(mail.id, selectedLabel);
        });
        builder.setNegativeButton("cancel", null);
        builder.show();
    }
    private void removeLabelFromMail(String mailId, String labelToRemove) {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:4000/api/mails/" + mailId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PATCH");
                conn.setRequestProperty("Content-Type", "application/json");

                SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                String token = prefs.getString("token", null);
                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }

                conn.setDoOutput(true);

                List<String> updatedLabels = new ArrayList<>(mail.label != null ? mail.label : new ArrayList<>());
                updatedLabels.remove(labelToRemove); // ← הסרה

                JSONObject payload = new JSONObject();
                payload.put("label", new JSONArray(updatedLabels));

                OutputStream os = conn.getOutputStream();
                os.write(payload.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = conn.getResponseCode();

                runOnUiThread(() -> {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Toast.makeText(this, "תוית הוסרה: " + labelToRemove, Toast.LENGTH_SHORT).show();
                        mail.label = updatedLabels; // ← עדכון מקומי
                    } else {
                        Toast.makeText(this, "שגיאה בהסרת תוית", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "שגיאה ברשת: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void showLabelSelectionDialog() {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:4000/api/labels"); // ← כתובת ל־GET של כל התוויות
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                String token = prefs.getString("token", null);
                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Scanner scanner = new Scanner(conn.getInputStream());
                    StringBuilder response = new StringBuilder();
                    while (scanner.hasNext()) {
                        response.append(scanner.nextLine());
                    }
                    scanner.close();

                    JSONArray jsonArray = new JSONArray(response.toString());
                    List<String> labelNames = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject labelObj = jsonArray.getJSONObject(i);
                        String name = labelObj.getString("name");
                        labelNames.add(name);
                    }

                    runOnUiThread(() -> showLabelPickerDialog(labelNames));

                } else {
                    runOnUiThread(() -> Toast.makeText(this, "שגיאה בקבלת תוויות", Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "שגיאה ברשת: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
    private void showLabelPickerDialog(List<String> labels) {
        if (labels == null || labels.isEmpty()) {
            Toast.makeText(this, "לא נמצאו תוויות", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] labelArray = labels.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("choose label");
        builder.setItems(labelArray, (dialog, which) -> {
            String selectedLabel = labelArray[which];
            addLabelToMail(mail.id, selectedLabel);
        });
        builder.setNegativeButton("cancel", null);
        builder.show();
    }private void addLabelToMail(String mailId, String label) {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:4000/api/mails/" + mailId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PATCH");
                conn.setRequestProperty("Content-Type", "application/json");

                SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                String token = prefs.getString("token", null);
                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }

                conn.setDoOutput(true);

                // בניית רשימת תוויות נקייה
                Set<String> cleanLabels = new HashSet<>();
                if (mail.label != null) {
                    for (String lbl : mail.label) {
                        if (lbl != null && !lbl.trim().isEmpty() && !lbl.contains("[") && !lbl.contains("]")) {
                            cleanLabels.add(lbl.trim());
                        }
                    }
                }

                if (label != null && !label.trim().isEmpty()) {
                    cleanLabels.add(label.trim());
                }

                // בניית ה־JSON לשליחה
                JSONObject payload = new JSONObject();
                payload.put("label", new JSONArray(new ArrayList<>(cleanLabels)));

                Log.d("DEBUG", "שולחת לשרת תוויות: " + payload.toString());

                OutputStream os = conn.getOutputStream();
                os.write(payload.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = conn.getResponseCode();
                runOnUiThread(() -> {
                    Log.d("DEBUG", "קיבלתי קוד תגובה: " + responseCode);
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Toast.makeText(this, "תוית \"" + label + "\" נוספה", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "שגיאה בהוספת תוית", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "שגיאה ברשת: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("DEBUG", "שגיאה ב־addLabelToMail: " + e.getMessage());
                });
            }
        }).start();
    }


    private void moveToTrash(String mailId) {
        updateMailCategory(mailId, "trash", "הועבר לאשפה");
    }

    private void deletePermanently(String mailId) {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:4000/api/mails/" + mailId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");

                SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                String token = prefs.getString("token", null);
                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }

                int responseCode = conn.getResponseCode();

                runOnUiThread(() -> {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Toast.makeText(this, "המייל נמחק לצמיתות", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "שגיאה במחיקת המייל", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "שגיאת רשת: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void markMailAsSpam() {
        // שימוש בפונקציה המשופרת מ-SpamUtils
        SpamUtils.markMailAsSpam(this, mail.id, mail.fromMail, mail.subject, mail.body);

        // הצגת הודעה למשתמש
        Toast.makeText(this, "המייל הועבר לספאם וקישורים נוספו לרשימה השחורה", Toast.LENGTH_LONG).show();

        // הדפסת הקישורים שנמצאו (לבדיקה)
        List<String> links = SpamUtils.extractLinksFromText(mail.subject + " " + mail.body);
        if (!links.isEmpty()) {
            Log.d("MailDetailActivity", "Found links: " + links);
            Toast.makeText(this, "נמצאו " + links.size() + " קישורים ונוספו לרשימה השחורה", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    private void removeMailFromSpam() {
        // שימוש בפונקציה המשופרת מ-SpamUtils
        SpamUtils.removeMailFromSpam(this, mail.id, mail.fromMail, mail.subject, mail.body, "inbox");

        // הצגת הודעה למשתמש
        Toast.makeText(this, "המייל הוסר מספאם וקישורים הוסרו מהרשימה השחורה", Toast.LENGTH_LONG).show();

        // הדפסת הקישורים שהוסרו (לבדיקה)
        List<String> links = SpamUtils.extractLinksFromText(mail.subject + " " + mail.body);
        if (!links.isEmpty()) {
            Log.d("MailDetailActivity", "Removed links: " + links);
            Toast.makeText(this, "הוסרו " + links.size() + " קישורים מהרשימה השחורה", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    private void updateMailCategory(String mailId, String category, String successMessage) {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:4000/api/mails/" + mailId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PATCH");
                conn.setRequestProperty("Content-Type", "application/json");

                SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                String token = prefs.getString("token", null);
                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }

                conn.setDoOutput(true);
                JSONObject payload = new JSONObject();
                payload.put("category", category);

                Log.d("DEBUG", "שולחת עדכון קטגוריה: " + payload.toString() + " למייל: " + mailId);

                OutputStream os = conn.getOutputStream();
                os.write(payload.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = conn.getResponseCode();
                Log.d("DEBUG", "קוד תגובה מהשרת: " + responseCode);

                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream()
                ));
                StringBuilder responseText = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseText.append(line);
                }
                reader.close();
                Log.d("DEBUG", "תשובת שרת: " + responseText);

                runOnUiThread(() -> {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "שגיאה בעדכון הקטגוריה", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "שגיאת רשת: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                Log.e("DEBUG", "שגיאה ב־updateMailCategory: " + e.getMessage(), e);
            }
        }).start();
    }


    private void updateStarOnServer(String mailId, boolean isStarred) {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:4000/api/mails/" + mailId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PATCH");
                conn.setRequestProperty("Content-Type", "application/json");

                SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                String token = prefs.getString("token", null);
                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }

                conn.setDoOutput(true);
                JSONObject payload = new JSONObject();
                payload.put("isStarred", isStarred);

                OutputStream os = conn.getOutputStream();
                os.write(payload.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    Log.e("MailDetailActivity", "Failed to update star status");
                }

            } catch (Exception e) {
                Log.e("MailDetailActivity", "Error updating star status: " + e.getMessage());
            }
        }).start();
    }
    // פונקציה חדשה להצגת המצורפים
    private void displayAttachments() {
        Log.d("MailDetailActivity", "Displaying attachments for mail: " + mail.id);

        if (attachmentsContainer == null || tvAttachmentsTitle == null || rvAttachments == null) {
            Log.e("MailDetailActivity", "Attachment views not found");
            return;
        }

        if (mail.attachments != null && !mail.attachments.isEmpty()) {
            Log.d("MailDetailActivity", "Found " + mail.attachments.size() + " attachments");

            // הדפסת מידע על כל מצורף לדיבוג
            for (int i = 0; i < mail.attachments.size(); i++) {
                Attachment attachment = mail.attachments.get(i);
                Log.d("MailDetailActivity", "Attachment " + i + ": " +
                        "filename=" + attachment.filename +
                        ", size=" + attachment.size +
                        ", contentType=" + attachment.contentType +
                        ", path=" + attachment.path);
            }

            attachmentsContainer.setVisibility(View.VISIBLE);
            tvAttachmentsTitle.setVisibility(View.VISIBLE);
            tvAttachmentsTitle.setText("קבצים מצורפים (" + mail.attachments.size() + ")");

            try {
                AttachmentAdapter adapter = new AttachmentAdapter(mail.attachments, this::downloadAttachment);
                rvAttachments.setLayoutManager(new LinearLayoutManager(this));
                rvAttachments.setAdapter(adapter);
            } catch (Exception e) {
                Log.e("MailDetailActivity", "Error setting up attachment adapter: " + e.getMessage());
                Toast.makeText(this, "שגיאה בהצגת קבצים מצורפים", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d("MailDetailActivity", "No attachments found");
            attachmentsContainer.setVisibility(View.GONE);
            tvAttachmentsTitle.setVisibility(View.GONE);
        }
    }

    // פונקציה להורדת מצורף
    private void downloadAttachment(Attachment attachment) {
        Log.d("MailDetailActivity", "Downloading attachment: " + attachment.filename);

        if (attachment == null) {
            Toast.makeText(this, "שגיאה: קובץ לא תקין", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String downloadUrl;

            // בדיקה אם יש נתיב לקובץ
            if (attachment.filename != null && !attachment.filename.isEmpty()) {
                downloadUrl = "http://10.0.2.2:4000/uploads/" + attachment.filename;
                Log.d("MailDetailActivity", "Using download URL: " + downloadUrl);
            } else {
                Toast.makeText(this, "אין שם קובץ תקין למצורף", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("MailDetailActivity", "Download URL: " + downloadUrl);

            // פתיחת דפדפן להורדה
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl));
            startActivity(intent);

        } catch (ActivityNotFoundException e) {
            Log.e("MailDetailActivity", "No app to handle download: " + e.getMessage());
            Toast.makeText(this, "לא ניתן לפתוח קובץ מסוג זה", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("MailDetailActivity", "Error downloading attachment: " + e.getMessage());
            Toast.makeText(this, "שגיאה בהורדת הקובץ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}