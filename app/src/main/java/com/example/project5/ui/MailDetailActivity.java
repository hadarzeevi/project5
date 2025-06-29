package com.example.project5.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project5.R;
import com.example.project5.model.Mail;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MailDetailActivity extends AppCompatActivity {

    private TextView tvSubject, tvSenderName, tvSenderEmail, tvDate, tvBody;
    private ImageView ivStar, ivAvatar;
    private Button btnReply, btnForward, btnDelete;
    private Mail mail;

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
        btnReply = findViewById(R.id.btnReply);
        btnForward = findViewById(R.id.btnForward);
        btnDelete = findViewById(R.id.btnDelete);

        // קבלת המייל שהועבר מהמסך הקודם
        try {
            String mailJson = getIntent().getStringExtra("MAIL_JSON");
            JSONObject json = new JSONObject(mailJson);
            mail = Mail.fromJson(json);
            loadMailData();
        } catch (Exception e) {
            Toast.makeText(this, "Error loading mail", Toast.LENGTH_SHORT).show();
            finish();
        }

        ivStar.setOnClickListener(v -> {
            mail.isStarred = !mail.isStarred;
            ivStar.setImageResource(mail.isStarred ? R.drawable.ic_star : R.drawable.ic_star);
            // עדכון בשרת בהמשך
            //             ivStar.setImageResource(mail.isStarred ? R.drawable.ic_star_filled : R.drawable.ic_star);
        });

        btnReply.setOnClickListener(v -> Toast.makeText(this, "Reply clicked", Toast.LENGTH_SHORT).show());
        btnForward.setOnClickListener(v -> Toast.makeText(this, "Forward clicked", Toast.LENGTH_SHORT).show());
        btnDelete.setOnClickListener(v -> {
            // מחיקה מהשרת בהמשך
            Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void loadMailData() {
        tvSubject.setText(mail.subject);
        tvBody.setText(mail.preview);
        tvSenderName.setText(mail.from);
        tvSenderEmail.setText(mail.from);
        tvDate.setText(formatDate(mail.date));

        if (mail.isStarred) {
            ivStar.setImageResource(R.drawable.ic_star);
            // ic_star_filled ----?
        } else {
            ivStar.setImageResource(R.drawable.ic_star);
        }

        // אות ראשונה של השולח כאווטאר
        String firstLetter = mail.from != null && !mail.from.isEmpty()
                ? mail.from.substring(0, 1).toUpperCase(Locale.ROOT)
                : "?";
        TextView tvLetter = findViewById(R.id.tvAvatarLetter);
        tvLetter.setText(firstLetter);
    }

    private String formatDate(String dateStr) {
        return dateStr; // ניתן להמיר לתאריך קריא אם תצטרכי
    }
}
