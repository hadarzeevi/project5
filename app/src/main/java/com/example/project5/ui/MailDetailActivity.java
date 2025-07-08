package com.example.project5.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.project5.R;
import com.example.project5.model.Mail;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MailDetailActivity extends AppCompatActivity {

    private TextView tvSubject, tvSenderName, tvSenderEmail, tvDate, tvBody;
    private ImageView ivStar, ivAvatar, ivBack, ivTrash;
    private LinearLayout btnReply, btnForward;
    private Mail mail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_detail);

        // קישור לרכיבים
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

        try {
            String mailJson = getIntent().getStringExtra("MAIL_JSON");
            JSONObject json = new JSONObject(mailJson);
            mail = Mail.fromJson(json);
            loadMailData();
        } catch (Exception e) {
            Toast.makeText(this, "שגיאה בטעינת מייל", Toast.LENGTH_SHORT).show();
            finish();
        }

        // כפתור חזרה
        ivBack.setOnClickListener(v -> finish());

        // כפתור מחיקה (פח)
        ivTrash.setOnClickListener(v -> {
            Toast.makeText(this, "הועבר לאשפה", Toast.LENGTH_SHORT).show();
            // TODO: שליחה לשרת להעברה ל־trash
            finish();
        });

        // כוכב
        ivStar.setOnClickListener(v -> {
            mail.isStarred = !mail.isStarred;
            ivStar.setImageResource(mail.isStarred ? R.drawable.ic_star : R.drawable.ic_star_border);
            Toast.makeText(this, mail.isStarred ? "סומן בכוכב" : "הוסר מכוכבים", Toast.LENGTH_SHORT).show();
            // TODO: שליחה לשרת אם צריך
        });

        // כפתורים
        btnReply.setOnClickListener(v -> {
            Intent intent = new Intent(MailDetailActivity.this, ComposeActivity.class);
            intent.putExtra("replyTo", mail.fromMail); // כתובת אימייל של השולח
            intent.putExtra("replySubject", "Re: " + (mail.subject != null ? mail.subject : ""));
            intent.putExtra("replyBody", "\n\n---------- original text ----------\n" +
                    "from: " + mail.firstName + " " + mail.lastName + " <" + mail.fromMail + ">\n" +
                    "date: " + formatDate(mail.date) + "\n\n" +
                    (mail.body != null ? mail.body : ""));
            startActivity(intent);
        });
        btnForward.setOnClickListener(v -> {
            Intent intent = new Intent(MailDetailActivity.this, ComposeActivity.class);
            intent.putExtra("replyTo", ""); // To ריק – כי זה העברה
            intent.putExtra("replySubject", "Fwd: " + (mail.subject != null ? mail.subject : ""));
            intent.putExtra("replyBody", "\n\n---------- massage forward ----------\n" +
                    "from: " + mail.firstName + " " + mail.lastName + " <" + mail.fromMail + ">\n" +
                    "date: " + formatDate(mail.date) + "\n\n" +
                    (mail.body != null ? mail.body : ""));
            startActivity(intent);
        });
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
            Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.ic_avatar)
                    .into(ivAvatar);
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
}
