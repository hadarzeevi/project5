package com.example.project5.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
    private String searchQuery = "";

    private RecyclerView recyclerView;
    private MailAdapter adapter;
    private ProgressBar progressBar;
    private final ArrayList<Mail> allMails = new ArrayList<>();   // כל המיילים שהתקבלו

    private final ArrayList<Mail> mailList = new ArrayList<>();
    private final String API_URL = "http://10.0.2.2:4000/api/mails"; // שנה לכתובת השרת שלך
    private String currentCategory = "inbox"; // ברירת מחדל
    private String selectedLabel = null;  // אם תרצי בהמשך

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_mail_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewMails);
        progressBar = view.findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new MailAdapter(
                mailList,
                mail -> {
                    try {
                        Intent intent = new Intent(getContext(), MailDetailActivity.class);
                        JSONObject json = mail.toJson();  // ודאי שקיימת פונקציה כזו במודל שלך
                        intent.putExtra("MAIL_JSON", json.toString());
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "שגיאה בפתיחת המייל", Toast.LENGTH_SHORT).show();
                    }
                },
                mail -> {
                    mail.isStarred = !mail.isStarred;
                    adapter.notifyDataSetChanged();
                }
        );

        recyclerView.setAdapter(adapter);
        loadMails(currentCategory); // ← נטען את אינבוקס כברירת מחדל

        return view;
    }


    private void loadMails(String category) {
        progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                String urlStr = "http://10.0.2.2:4000/api/mails";
                if (category != null && !category.isEmpty()) {
                    urlStr += "?category=" + category.toLowerCase();
                    Log.d("MailListFragment", "Full API URL: " + urlStr);
                }

                URL url = new URL(urlStr);
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
                allMails.clear();
                mailList.clear();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    Mail mail = Mail.fromJson(obj);
                    allMails.add(mail);

                    // טעינת פרטי השולח
                    if (mail.from != null && !mail.from.isEmpty()) {
                        fetchSenderDetails(mail.from, mail);
                    }
                }

// סינון מקומי לפי קטגוריה נבחרת
                filterAndDisplayMails();


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
    private void filterAndDisplayMails() {
        mailList.clear();

        for (Mail mail : allMails) {
            String cat = currentCategory != null ? currentCategory.toLowerCase() : null;
            String query = searchQuery != null ? searchQuery.toLowerCase().trim() : null;

            boolean matchesCategory = false;
            boolean matchesLabel = false;
            boolean matchesSearch = false;

            // סינון לפי קטגוריה
            if ("starred".equals(cat)) {
                matchesCategory = mail.isStarred;
            } else if (cat != null) {
                matchesCategory = mail.category != null && mail.category.equalsIgnoreCase(cat);
            } else {
                matchesCategory = true;
            }

            // סינון לפי תווית
            if (selectedLabel != null) {
                matchesLabel = mail.label != null && mail.label.contains(selectedLabel);
            } else {
                matchesLabel = true;
            }

            // סינון לפי טקסט חיפוש
            if (query != null && !query.isEmpty()) {
                String combinedText = (mail.subject + " " + mail.body + " " + mail.fromMail).toLowerCase();
                matchesSearch = combinedText.contains(query);
            } else {
                matchesSearch = true;
            }

            if (matchesCategory && matchesLabel && matchesSearch) {
                mailList.add(mail);
            }
        }

        requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
    }

    // שליפת פרטי השולח ועדכון ה־Mail
    private void fetchSenderDetails(String userId, Mail mail) {
        Log.d("FetchSenderDetails", "Fetching for userId: " + userId);
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:4000/api/users/" + userId);
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

                JSONObject userObj = new JSONObject(json.toString());

                mail.username = userObj.optString("username", "");
                mail.firstName = userObj.optString("first_name", "");
                mail.lastName = userObj.optString("last_name", "");
                mail.profileImage = userObj.optString("profileImage", "");
                Log.d("FetchSenderDetails", "username: " + mail.username);


                requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());

            } catch (Exception e) {
                Log.e("FetchSenderDetails", "Error fetching sender details: " + e.getMessage());
            }
        }).start();
    }
    public void setCategory(String category) {
        this.currentCategory = category;
        this.selectedLabel = null;
        filterAndDisplayMails();
    }

    public void setLabel(String label) {
        this.selectedLabel = label;
        this.currentCategory = null;
        filterAndDisplayMails();
    }

    public void setSearchQuery(String query) {
        this.searchQuery = query.toLowerCase();
        adapter.setSearchQuery(query);  // כאן את מעדכנת את האדפטר כדי שידע להדגיש
        filterAndDisplayMails();     }


}
