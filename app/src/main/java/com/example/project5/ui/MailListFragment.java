package com.example.project5.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project5.R;
import com.example.project5.model.Mail;
import com.example.project5.utils.SpamUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MailListFragment extends Fragment {
    private String searchQuery = "";

    private RecyclerView recyclerView;
    private MailAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvCategoryTitle;
    private final ArrayList<Mail> allMails = new ArrayList<>();
    private final ArrayList<Mail> mailList = new ArrayList<>();
    private final String API_URL = "http://10.0.2.2:4000/api/mails";
    private final String CHECK_SPAM_URL = "http://10.0.2.2:4000/api/blacklist/check";
    private String currentCategory = "inbox";
    private String selectedLabel = null;
    private ActivityResultLauncher<Intent> mailDetailLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mail_list, container, false);
        tvCategoryTitle = view.findViewById(R.id.tvCategoryTitle);

        recyclerView = view.findViewById(R.id.recyclerViewMails);
        progressBar = view.findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new MailAdapter(
                mailList,
                mail -> {
                    try {
                        if ("drafts".equalsIgnoreCase(mail.category)) {
                            Intent intent = new Intent(getContext(), ComposeActivity.class);
                            intent.putExtra("replyTo", mail.sendTo);
                            intent.putExtra("replySubject", mail.subject);
                            intent.putExtra("replyBody", mail.body);
                            intent.putExtra("draftId", mail.id);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(getContext(), MailDetailActivity.class);
                            JSONObject json = mail.toJson();
                            intent.putExtra("MAIL_JSON", json.toString());
                            if (selectedLabel != null) {
                                intent.putExtra("SELECTED_LABEL", selectedLabel);
                            }
                            mailDetailLauncher.launch(intent);
                        }
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

        loadMails(currentCategory);

        mailDetailLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            if (data.hasExtra("deletedMailId")) {
                                String deletedId = data.getStringExtra("deletedMailId");
                                removeMailFromList(deletedId);
                            }
                            // טיפול במייל שהועבר מספאם או אליו
                            if (data.hasExtra("mailMovedFromSpam")) {
                                String movedMailId = data.getStringExtra("mailMovedFromSpam");
                                handleMailMovedFromSpam(movedMailId);
                            }
                            if (data.hasExtra("mailMovedToSpam")) {
                                String movedMailId = data.getStringExtra("mailMovedToSpam");
                                handleMailMovedToSpam(movedMailId);
                            }
                        }
                    }
                }
        );

        return view;
    }

    private void loadMails(String category) {
        progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                String urlStr = API_URL;
                if (category != null && !category.isEmpty()) {
                    urlStr += "?category=" + category.toLowerCase();
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

                    // בדיקת ספאם - גם בדיקה רגילה וגם בדיקת קישורים
                    boolean isSpam = checkIfSpam(mail.subject, mail.body) ||
                            SpamUtils.containsBlacklistedLinks(getContext(), mail.subject, mail.body);

                    if ("inbox".equalsIgnoreCase(mail.category) && isSpam) {
                        mail.category = "spam";
                        // אם המייל נכנס לספאם בגלל קישורים, נוסיף את הקישורים לblacklist
                        if (SpamUtils.containsBlacklistedLinks(getContext(), mail.subject, mail.body)) {
                            List<String> links = SpamUtils.extractLinksFromText(mail.subject + " " + mail.body);
                            SpamUtils.addLinksToBlacklist(getContext(), links);
                        }
                    }

                    allMails.add(mail);

                    if (mail.from != null && !mail.from.isEmpty()) {
                        fetchSenderDetails(mail.from, mail);
                    }
                }

                filterAndDisplayMails();

                requireActivity().runOnUiThread(() -> {
                    tvCategoryTitle.setText(getCategoryDisplayName(currentCategory));
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

    private boolean checkIfSpam(String subject, String body) {
        try {
            URL url = new URL(CHECK_SPAM_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
            String token = prefs.getString("token", null);
            if (token != null) {
                conn.setRequestProperty("Authorization", "Bearer " + token);
            }

            conn.setDoOutput(true);
            JSONObject bodyJson = new JSONObject();
            bodyJson.put("subject", subject);
            bodyJson.put("body", body);

            conn.getOutputStream().write(bodyJson.toString().getBytes("UTF-8"));

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
            reader.close();

            JSONObject response = new JSONObject(json.toString());
            return response.optBoolean("spam", false);

        } catch (Exception e) {
            Log.e("checkIfSpam", "Error checking spam: " + e.getMessage());
            return false;
        }
    }

    /**
     * טיפול במייל שהועבר מספאם - הסרת הקישורים מהblacklist
     */
    private void handleMailMovedFromSpam(String mailId) {
        Mail mail = findMailById(mailId);
        if (mail != null) {
            List<String> links = SpamUtils.extractLinksFromText(mail.subject + " " + mail.body);
            if (!links.isEmpty()) {
                SpamUtils.removeLinksFromBlacklist(getContext(), links);
                Log.d("MailListFragment", "Removed " + links.size() + " links from blacklist for mail: " + mailId);
            }
        }
    }

    /**
     * טיפול במייל שהועבר לספאם - הוספת הקישורים לblacklist
     */
    private void handleMailMovedToSpam(String mailId) {
        Mail mail = findMailById(mailId);
        if (mail != null) {
            List<String> links = SpamUtils.extractLinksFromText(mail.subject + " " + mail.body);
            if (!links.isEmpty()) {
                SpamUtils.addLinksToBlacklist(getContext(), links);
                Log.d("MailListFragment", "Added " + links.size() + " links to blacklist for mail: " + mailId);
            }
        }
    }

    /**
     * מציאת מייל לפי ID
     */
    private Mail findMailById(String mailId) {
        for (Mail mail : allMails) {
            if (mail.id.equals(mailId)) {
                return mail;
            }
        }
        return null;
    }

    private void filterAndDisplayMails() {
        mailList.clear();

        for (Mail mail : allMails) {
            String cat = currentCategory != null ? currentCategory.toLowerCase() : null;
            String query = searchQuery != null ? searchQuery.toLowerCase().trim() : null;

            boolean matchesCategory = false;
            boolean matchesLabel = false;
            boolean matchesSearch = false;

            if ("starred".equals(cat)) {
                matchesCategory = mail.isStarred;
            } else if (cat != null) {
                matchesCategory = mail.category != null && mail.category.equalsIgnoreCase(cat);
            } else {
                matchesCategory = true;
            }

            if (selectedLabel != null) {
                matchesLabel = mail.label != null && mail.label.contains(selectedLabel);
            } else {
                matchesLabel = true;
            }

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

    private void fetchSenderDetails(String userId, Mail mail) {
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

                requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());

            } catch (Exception e) {
                Log.e("FetchSenderDetails", "Error fetching sender details: " + e.getMessage());
            }
        }).start();
    }

    public void setCategory(String category) {
        this.currentCategory = category;
        this.selectedLabel = null;
        loadMails(category);
    }

    public void setLabel(String label) {
        this.selectedLabel = label;
        this.currentCategory = null;
        loadMails(null);
    }

    public void setSearchQuery(String query) {
        this.searchQuery = query.toLowerCase();
        adapter.setSearchQuery(query);
        filterAndDisplayMails();
    }

    private void removeMailFromList(String mailId) {
        Iterator<Mail> iterator = mailList.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().id.equals(mailId)) {
                iterator.remove();
                break;
            }
        }
        adapter.notifyDataSetChanged();
    }

    private String getCategoryDisplayName(String key) {
        if (key == null) return "";
        switch (key.toLowerCase()) {
            case "inbox": return "Inbox";
            case "sent": return "Sent";
            case "drafts": return "Drafts";
            case "trash": return "Trash";
            case "spam": return "Spam";
            case "starred": return "Starred";
            default: return key;
        }
    }
}