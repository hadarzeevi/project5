package com.example.project5.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import com.bumptech.glide.Glide;
import com.example.project5.R;
import com.example.project5.model.Mail;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

public class MailDetailActivity extends AppCompatActivity {

    private TextView tvSubject, tvSenderName, tvSenderEmail, tvDate, tvBody;
    private ImageView ivStar, ivAvatar, ivBack, ivTrash, ivMore;
    private LinearLayout btnReply, btnForward;
    private Mail mail;
    private String selectedLabel = null;
    private final Map<String, String> labelIdMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_detail);

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
            selectedLabel = getIntent().getStringExtra("SELECTED_LABEL");
            JSONObject json = new JSONObject(mailJson);
            mail = Mail.fromJson(json);
            loadMailData();
        } catch (Exception e) {
            Toast.makeText(this, "שגיאה בטעינת מייל", Toast.LENGTH_SHORT).show();
            finish();
        }

        ivMore.setOnClickListener(this::showPopupMenu);
        ivBack.setOnClickListener(v -> finish());

        ivTrash.setOnClickListener(v -> {
            if ("trash".equalsIgnoreCase(mail.category)) {
                // אם כבר באשפה – מחיקה סופית
                showDeleteForeverDialog(mail.id);
            } else {
                // אחרת – העברה לאשפה
                moveToTrash(mail.id);

                // סגירת המסך וחזרה עם תוצאה (אופציונלי)
                Intent resultIntent = new Intent();
                resultIntent.putExtra("deletedMailId", mail.id);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });




        ivStar.setOnClickListener(v -> {
            mail.isStarred = !mail.isStarred;
            ivStar.setImageResource(mail.isStarred ? R.drawable.ic_star : R.drawable.ic_star_border);
            Toast.makeText(this, mail.isStarred ? "סומן בכוכב" : "הוסר מכוכבים", Toast.LENGTH_SHORT).show();
        });

        btnReply.setOnClickListener(v -> {
            Intent intent = new Intent(MailDetailActivity.this, ComposeActivity.class);
            intent.putExtra("replyTo", mail.fromMail);
            intent.putExtra("replySubject", "Re: " + (mail.subject != null ? mail.subject : ""));
            intent.putExtra("replyBody", "\n\n---------- original text ----------\n" +
                    "from: " + mail.firstName + " " + mail.lastName + " <" + mail.fromMail + ">\n" +
                    "date: " + formatDate(mail.date) + "\n\n" +
                    (mail.body != null ? mail.body : ""));
            startActivity(intent);
        });

        btnForward.setOnClickListener(v -> {
            Intent intent = new Intent(MailDetailActivity.this, ComposeActivity.class);
            intent.putExtra("replyTo", "");
            intent.putExtra("replySubject", "Fwd: " + (mail.subject != null ? mail.subject : ""));
            intent.putExtra("replyBody", "\n\n---------- message forward ----------\n" +
                    "from: " + mail.firstName + " " + mail.lastName + " <" + mail.fromMail + ">\n" +
                    "date: " + formatDate(mail.date) + "\n\n" +
                    (mail.body != null ? mail.body : ""));
            startActivity(intent);
        });
    }
    private void showDeleteForeverDialog(String mailId) {
        new AlertDialog.Builder(this)
                .setTitle("מחיקה סופית")
                .setMessage("האם אתה בטוח שברצונך למחוק את המייל לצמיתות?")
                .setPositiveButton("מחק", (dialog, which) -> deleteMailPermanently(mailId))
                .setNegativeButton("בטל", null)
                .show();
    }
    private void deleteMailPermanently(String mailId) {
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

                InputStream stream = (responseCode >= 200 && responseCode < 300) ? conn.getInputStream() : conn.getErrorStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder msg = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    msg.append(line);
                }
                reader.close();

                runOnUiThread(() -> {
                    if (responseCode >= 200 && responseCode < 300) {
                        Toast.makeText(this, "המייל נמחק לצמיתות", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "שגיאה במחיקה: " + responseCode, Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "שגיאת רשת: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void moveToTrash(String mailId) {
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
                payload.put("category", "trash");

                OutputStream os = conn.getOutputStream();
                os.write(payload.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = conn.getResponseCode();

                runOnUiThread(() -> {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Toast.makeText(this, "הועבר לאשפה", Toast.LENGTH_SHORT).show();
                        finish(); // חזור למסך הקודם
                    } else {
                        Toast.makeText(this, "שגיאה בהעברה לאשפה", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "שגיאת רשת: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void loadMailData() {
        tvSubject.setText(mail.subject != null ? mail.subject : "(ללא נושא)");
        tvBody.setText(mail.body != null ? mail.body : "(ללא תוכן)");
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
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_add_label) {
                fetchLabelsFromServerAndShowDialog();
                return true;
            } else if (item.getItemId() == R.id.action_remove_label) {
                showRemoveLabelDialog(mail);
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void showRemoveLabelDialog(Mail mail) {
        if (mail.label == null || mail.label.isEmpty()) {
            Toast.makeText(this, "אין תוויות להסיר", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] labelArray = mail.label.toArray(new String[0]);
        new AlertDialog.Builder(this)
                .setTitle("בחר תווית להסרה")
                .setItems(labelArray, (dialog, which) -> removeLabelFromMail(mail, labelArray[which]))
                .show();
    }

    private void fetchLabelsFromServerAndShowDialog() {
        Log.d("Labels", "fetchLabelsFromServerAndShowDialog התחיל");
        new Thread(() -> {
            List<String> labelsToShow = new ArrayList<>();
            try {
                URL url = new URL("http://10.0.2.2:4000/api/labels");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                String token = prefs.getString("token", null);
                if (token != null) conn.setRequestProperty("Authorization", "Bearer " + token);
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                    reader.close();
                    JSONArray jsonArray = new JSONArray(sb.toString());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        String labelId = obj.getString("_id");
                        String labelName = obj.getString("name");
                        labelIdMap.put(labelName, labelId);
                        if (mail.label == null || !mail.label.contains(labelName)) {
                            labelsToShow.add(labelName);
                        }
                    }
                }
                runOnUiThread(() -> showLabelSelectionDialog(mail, labelsToShow));
            } catch (Exception e) {
                Log.e("Labels", "שגיאה בטעינת תוויות", e);
                runOnUiThread(() -> Toast.makeText(this, "שגיאה בטעינת תוויות", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void showLabelSelectionDialog(Mail mail, List<String> labelList) {
        if (labelList == null || labelList.isEmpty()) {
            Toast.makeText(this, "אין תוויות זמינות", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] labels = labelList.toArray(new String[0]);
        new AlertDialog.Builder(this)
                .setTitle("בחר תווית")
                .setItems(labels, (dialog, which) -> addLabelToMail(mail, labels[which]))
                .show();
    }

    private void addLabelToMail(Mail mail, String label) {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:4000/api/mails/" + mail.id);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PATCH");
                conn.setRequestProperty("Content-Type", "application/json");
                SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                String token = prefs.getString("token", null);
                if (token != null) conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setDoOutput(true);
                if (mail.label == null) mail.label = new ArrayList<>();
                if (!mail.label.contains(label)) mail.label.add(label);
                JSONObject payload = new JSONObject();
                payload.put("label", new JSONArray(mail.label));
                OutputStream os = conn.getOutputStream();
                os.write(payload.toString().getBytes("UTF-8"));
                os.close();
                int responseCode = conn.getResponseCode();
                runOnUiThread(() -> {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Toast.makeText(this, "המייל נוסף לתווית: " + label, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "שגיאה בעת עדכון התווית", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "שגיאה: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void removeLabelFromMail(Mail mail, String labelToRemove) {
        new Thread(() -> {
            try {
                if (mail.label == null) return;
                mail.label.remove(labelToRemove);
                URL url = new URL("http://10.0.2.2:4000/api/mails/" + mail.id);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PATCH");
                conn.setRequestProperty("Content-Type", "application/json");
                SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                String token = prefs.getString("token", null);
                if (token != null) conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setDoOutput(true);
                JSONObject payload = new JSONObject();
                payload.put("label", new JSONArray(mail.label));
                OutputStream os = conn.getOutputStream();
                os.write(payload.toString().getBytes("UTF-8"));
                os.close();
                int responseCode = conn.getResponseCode();
                runOnUiThread(() -> {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Toast.makeText(this, "התווית הוסרה", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "שגיאה בהסרת התווית", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "שגיאה: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private List<String> loadLabelsFromPrefs() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String json = prefs.getString("userLabels", "[]");
        Type type = new TypeToken<List<String>>(){}.getType();
        return new Gson().fromJson(json, type);
    }
}
