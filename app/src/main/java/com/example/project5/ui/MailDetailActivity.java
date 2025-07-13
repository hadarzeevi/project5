package com.example.project5.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import com.bumptech.glide.Glide;
import com.example.project5.R;
import com.example.project5.model.Mail;
import com.example.project5.utils.SpamUtils;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

public class MailDetailActivity extends AppCompatActivity {

    private TextView tvSubject, tvSenderName, tvSenderEmail, tvDate, tvBody;
    private ImageView ivStar, ivAvatar, ivBack, ivTrash, ivMore;
    private LinearLayout btnReply, btnForward;
    private Mail mail;
    private boolean isSpamCategory = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_detail);

        // רכיבי ממשק
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

        try {
            String mailJson = getIntent().getStringExtra("MAIL_JSON");
            JSONObject json = new JSONObject(mailJson);
            mail = Mail.fromJson(json);
            isSpamCategory = "spam".equalsIgnoreCase(mail.category);
            loadMailData();
        } catch (Exception e) {
            Toast.makeText(this, "שגיאה בטעינת מייל", Toast.LENGTH_SHORT).show();
            finish();
        }

        setupClickListeners();
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
            intent.putExtra("replyBody", "\n\n--- הודעה מקורית ---\n" + mail.body);
            startActivity(intent);
        });

        btnForward.setOnClickListener(v -> {
            Intent intent = new Intent(this, ComposeActivity.class);
            intent.putExtra("replySubject", "Fwd: " + mail.subject);
            intent.putExtra("replyBody", "\n\n--- העברת הודעה ---\n" + mail.body);
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
            Glide.with(this).load(url).placeholder(R.drawable.ic_avatar).into(ivAvatar);
        }

        updateStarIcon();
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

        if ("spam".equalsIgnoreCase(mail.category)) {
            popup.getMenu().add("סמן כלא ספאם")
                    .setOnMenuItemClickListener(item -> {
                        removeMailFromSpam();
                        return true;
                    });
        }


        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_add_label) {
                Toast.makeText(this, "תמיכה עתידית: הוסף תווית", Toast.LENGTH_SHORT).show();
                return true;
            } else if (item.getItemId() == R.id.action_remove_label) {
                Toast.makeText(this, "תמיכה עתידית: הסר תווית", Toast.LENGTH_SHORT).show();
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

                OutputStream os = conn.getOutputStream();
                os.write(payload.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = conn.getResponseCode();

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
}