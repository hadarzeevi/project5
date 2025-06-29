package com.example.project5.ui;

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
            ivStar.setImageResource(mail.isStarred ? R.drawable.ic_star : R.drawable.ic_star_border);
            Toast.makeText(this, mail.isStarred ? "Starred" : "Unstarred", Toast.LENGTH_SHORT).show();
            // TODO: עדכון לשרת במידת הצורך
        });

        btnReply.setOnClickListener(v -> Toast.makeText(this, "Reply clicked", Toast.LENGTH_SHORT).show());
        btnForward.setOnClickListener(v -> Toast.makeText(this, "Forward clicked", Toast.LENGTH_SHORT).show());
        btnDelete.setOnClickListener(v -> {
            Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void loadMailData() {
        tvSubject.setText(mail.subject != null ? mail.subject : "(No Subject)");
        tvBody.setText(mail.body != null ? mail.body : "(No Content)");
        tvSenderEmail.setText(mail.fromMail != null ? mail.fromMail : "(Unknown Sender)");
        tvDate.setText(formatDate(mail.date));

        String senderName = (mail.firstName != null && mail.lastName != null)
                ? mail.firstName + " " + mail.lastName
                : mail.fromMail != null ? mail.fromMail : "(Unknown)";
        tvSenderName.setText(senderName);

        // הצגת תמונת פרופיל בלבד
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
