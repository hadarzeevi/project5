package com.example.project5.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
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

public class MailListFragment extends Fragment {

    private RecyclerView recyclerView;
    private MailAdapter adapter;
    private ProgressBar progressBar;
    private String category = "inbox";
    private String searchTerm = "";
    private final ArrayList<Mail> mailList = new ArrayList<>();
    private final String API_URL = "http://10.0.2.2:4000/api/mails";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_mail_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewMails);
        progressBar = view.findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        Log.d("DEBUG", "hi");
        adapter = new MailAdapter(
        requireContext(), // ← הוספת context

        mailList,
                mail -> {
                    try {
                        Log.d("DEBUG", "clicked mail: " + mail.subject);
                        String jsonString = mail.toJson().toString();  // ← כאן עשויה להתרחש השגיאה
                        Log.d("DEBUG", "converted to JSON: " + jsonString);

                        Intent intent = new Intent(getContext(), MailDetailActivity.class);
                        intent.putExtra("MAIL_JSON", jsonString);
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e("MailAdapter", "שגיאה בפתיחת מייל", e);
                        Toast.makeText(getContext(), "שגיאה בפתיחת מייל", Toast.LENGTH_SHORT).show();
                    }
                },
                mail -> {
                    toggleStar(mail.id, mail.isStarred);
                    mail.isStarred = !mail.isStarred;
                    adapter.notifyDataSetChanged();
                }
        );

        recyclerView.setAdapter(adapter);
        loadMails();

        return view;
    }

    public void setCategory(String newCategory) {
        this.category = newCategory;
        loadMails();
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
        loadMails();
    }

    private void loadMails() {
        progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                String token = prefs.getString("token", null);

                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder json = new StringBuilder();

                String line;
                while ((line = reader.readLine()) != null) {
                    json.append(line);
                }
                reader.close();

                JSONObject responseObject = new JSONObject(json.toString());
                JSONArray jsonArray = responseObject.getJSONArray("mails");
                mailList.clear();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    Mail mail = Mail.fromJson(obj);
                    if (matchesCategory(mail) && matchesSearch(mail)) {
                        mailList.add(mail);
                    }
                }
                requireActivity().runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                });

            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private boolean matchesCategory(Mail mail) {
        switch (category) {
            case "inbox":
            case "sent":
            case "drafts":
            case "spam":
            case "trash":
                return category.equals(mail.category);
            case "starred":
                return mail.isStarred;
            default:
                return true;
        }
    }

    private boolean matchesSearch(Mail mail) {
        if (TextUtils.isEmpty(searchTerm)) return true;
        String lower = searchTerm.toLowerCase();
        return (mail.subject != null && mail.subject.toLowerCase().contains(lower)) ||
                (mail.body != null && mail.body.toLowerCase().contains(lower)) ||
                (mail.fromMail != null && mail.fromMail.toLowerCase().contains(lower));
    }

    private void toggleStar(String mailId, boolean currentStarred) {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:4000/api/mails/" + mailId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PATCH");
                conn.setRequestProperty("Content-Type", "application/json");
                SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                String token = prefs.getString("token", null);
                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }

                conn.setDoOutput(true);
                String payload = "{\"isStarred\": " + (!currentStarred) + "}";
                conn.getOutputStream().write(payload.getBytes());
                conn.getInputStream().close();
            } catch (Exception e) {
                Log.e("toggleStar", "Error: ", e);
            }
        }).start();
    }

    public void moveToCategory(String mailId, String newCategory, String previousCategory) {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:4000/api/mails/" + mailId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PATCH");
                conn.setRequestProperty("Content-Type", "application/json");
                SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                String token = prefs.getString("token", null);
                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }

                conn.setDoOutput(true);
                String payload = "{\"category\": \"" + newCategory + "\", \"previousCategory\": \"" + previousCategory + "\"}";
                conn.getOutputStream().write(payload.getBytes());
                conn.getInputStream().close();
            } catch (Exception e) {
                Log.e("moveToCategory", "Error: ", e);
            }
        }).start();
    }
}
