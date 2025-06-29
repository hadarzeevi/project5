package com.example.project5.ui;

import android.content.Context;
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

    private RecyclerView recyclerView;
    private MailAdapter adapter;
    private ProgressBar progressBar;
    private final ArrayList<Mail> mailList = new ArrayList<>();
    private final String API_URL = "http://10.0.2.2:4000/api/mails"; // שנה לכתובת השרת שלך

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
                    Toast.makeText(getContext(), "Clicked: " + mail.subject, Toast.LENGTH_SHORT).show();
                },
                mail -> {
                    mail.isStarred = !mail.isStarred;
                    adapter.notifyDataSetChanged();
                }
        );

        recyclerView.setAdapter(adapter);
        loadMails();

        return view;
    }


    private void loadMails() {
        progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                Log.d("DEBUG", "get");

                SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                String token = prefs.getString("token", null);
                Log.d("DEBUG", "token" + token);

                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                Log.d("DEBUG", "reader" + reader);

                StringBuilder json = new StringBuilder();
                Log.d("DEBUG", "json" + json);

                String line;
                while ((line = reader.readLine()) != null) {
                    Log.d("DEBUG", "loop");

                    json.append(line);
                }
                Log.d("DEBUG", "close");
                reader.close();
                Log.d("DEBUG", json.toString());
                JSONObject responseObject = new JSONObject(json.toString());
                Log.d("DEBUG", "responseObject");
                JSONArray jsonArray = responseObject.getJSONArray("mails");
                Log.d("DEBUG", "jsonArray");

                mailList.clear();
                Log.d("DEBUG", "clear");

                for (int i = 0; i < jsonArray.length(); i++) {
                    Log.d("DEBUG", "insidee");

                    JSONObject obj = jsonArray.getJSONObject(i);
                    Log.d("DEBUG", "1");

                    Mail mail = Mail.fromJson(obj);
                    Log.d("DEBUG", "2");

                    mailList.add(mail);
                    Log.d("DEBUG", "3");

                }
                Log.d("DEBUG", "HELLO");

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
}
