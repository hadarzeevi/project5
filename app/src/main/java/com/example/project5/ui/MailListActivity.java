package com.example.project5.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project5.R;
import com.example.project5.model.Mail;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MailListActivity extends AppCompatActivity implements MailAdapter.OnMailActionListener {

    private RecyclerView recyclerView;
    private MailAdapter adapter;
    private ProgressBar progressBar;
    private ArrayList<Mail> mailList = new ArrayList<>();
    private final String API_URL = "http://10.0.2.2:4000/api/mails"; // שנה לשרת שלך

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_list);

        recyclerView = findViewById(R.id.recyclerViewMails);
        progressBar = findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MailAdapter(mailList, this);
        recyclerView.setAdapter(adapter);

        loadMails();
    }

    private void loadMails() {
        progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    json.append(line);
                }
                reader.close();

                JSONArray jsonArray = new JSONArray(json.toString());
                mailList.clear();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    Mail mail = Mail.fromJson(obj);
                    mailList.add(mail);
                }

                runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    @Override
    public void onMailClicked(Mail mail) {
        Toast.makeText(this, "Clicked: " + mail.subject, Toast.LENGTH_SHORT).show();
        // כאן תוכלי לפתוח MailDetailActivity בעתיד
    }

    @Override
    public void onStarClicked(Mail mail) {
        mail.isStarred = !mail.isStarred;
        adapter.notifyDataSetChanged();
    }
}
