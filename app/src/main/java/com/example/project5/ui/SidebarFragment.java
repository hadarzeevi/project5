package com.example.project5.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.project5.R;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SidebarFragment extends Fragment {

    private LinearLayout categoriesContainer;
    private LinearLayout labelsContainer;
    private ImageButton btnAddLabel;
    private OnCategorySelectedListener listener;

    private final List<String> userLabels = new ArrayList<>();
    private final Map<String, String> labelIdMap = new HashMap<>();
    private View selectedCategoryView = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sidebar, container, false);

        categoriesContainer = view.findViewById(R.id.categoryContainer);
        labelsContainer = view.findViewById(R.id.labelContainer);
        btnAddLabel = view.findViewById(R.id.btnAddLabel);
        btnAddLabel.setOnClickListener(v -> promptAddLabel());

        fetchLabelsFromServer();
        loadCategories();

        return view;
    }

    private void loadCategories() {
        categoriesContainer.removeAllViews();

        String[] categories = {"Inbox", "Starred", "Sent", "Drafts", "Spam", "Trash"};
        Map<String, String> categoryMap = new HashMap<>();
        categoryMap.put("Inbox", "inbox");
        categoryMap.put("Starred", "starred");
        categoryMap.put("Sent", "sent");
        categoryMap.put("Drafts", "drafts");
        categoryMap.put("Spam", "spam");
        categoryMap.put("Trash", "trash");

        int[] icons = {R.drawable.ic_inbox, R.drawable.ic_star, R.drawable.ic_send, R.drawable.ic_draft, R.drawable.ic_report, R.drawable.ic_delete};

        for (int i = 0; i < categories.length; i++) {
            String category = categories[i];

            TextView tv = new TextView(getContext());
            tv.setText(category);
            tv.setPadding(40, 30, 40, 30);
            tv.setTextSize(16);
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.grayDark));
            tv.setCompoundDrawablesWithIntrinsicBounds(icons[i], 0, 0, 0);
            tv.setCompoundDrawablePadding(24);
            tv.setBackgroundResource(R.drawable.selector_category_background);

            tv.setOnClickListener(v -> {
                if (selectedCategoryView != null) {
                    selectedCategoryView.setBackgroundResource(R.drawable.selector_category_background);
                    ((TextView) selectedCategoryView).setTypeface(null, Typeface.NORMAL);
                }
                v.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.selectedCategory));
                ((TextView) v).setTypeface(null, Typeface.BOLD);
                selectedCategoryView = v;
                MailHomeActivity activity = (MailHomeActivity) requireActivity();
                MailListFragment listFragment = activity.getMailListFragment();
                if (listFragment != null) {
                    String mappedCategory = categoryMap.getOrDefault(category, "inbox");
                    listFragment.setCategory(mappedCategory);
                    Log.d("MailListFragment", "Loading category: " + category);

                }
                Toast.makeText(getContext(), category + " selected", Toast.LENGTH_SHORT).show();

                if (listener != null) {
                    listener.onCategorySelected(category.toLowerCase());
                }
            });

            categoriesContainer.addView(tv);
        }
    }

    private void loadLabels() {
        labelsContainer.removeAllViews();

        for (String label : userLabels) {
            addLabelToUI(label);
        }
    }

    private void promptAddLabel() {
        EditText input = new EditText(getContext());
        input.setHint("Enter label name");

        new android.app.AlertDialog.Builder(getContext())
                .setTitle("new label")
                .setView(input)
                .setPositiveButton("add", (dialog, which) -> {
                    String newLabel = input.getText().toString().trim();
                    if (!newLabel.isEmpty() && !userLabels.contains(newLabel)) {
                        userLabels.add(newLabel);
                        saveLabelsToPrefs();
                        addLabelToUI(newLabel);
                        sendLabelToServer(newLabel);
                    } else {
                        Toast.makeText(getContext(), "תווית קיימת או ריקה", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("cancel", null)
                .show();
    }

    private void addLabelToUI(String label) {
        LinearLayout labelItem = new LinearLayout(getContext());
        labelItem.setOrientation(LinearLayout.HORIZONTAL);
        labelItem.setPadding(24, 24, 24, 24);

        TextView tv = new TextView(getContext());
        tv.setText(label);
        tv.setTextSize(16);
        tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.grayDark));
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        tv.setBackgroundResource(R.drawable.selector_category_background);

        tv.setOnClickListener(v -> {
            if (selectedCategoryView != null) {
                selectedCategoryView.setBackgroundResource(R.drawable.selector_category_background);
                ((TextView) selectedCategoryView).setTypeface(null, Typeface.NORMAL);
            }
            v.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.selectedCategory));
            ((TextView) v).setTypeface(null, Typeface.BOLD);
            selectedCategoryView = v;

            MailHomeActivity activity = (MailHomeActivity) requireActivity();
            MailListFragment listFragment = activity.getMailListFragment();
            if (listFragment != null) {
                listFragment.setLabel(label);
            }

            Toast.makeText(getContext(), "Label: " + label + " selected", Toast.LENGTH_SHORT).show();
        });

        ImageView deleteIcon = new ImageView(getContext());
        deleteIcon.setImageResource(R.drawable.ic_delete);
        deleteIcon.setPadding(16, 16, 16, 16);
        deleteIcon.setOnClickListener(v -> {
            labelsContainer.removeView(labelItem);
            userLabels.remove(label);
            saveLabelsToPrefs();
            Log.d("DEBUG", "מנסה למחוק תווית מהשרת: " + label);

            String labelId = labelIdMap.get(label);
            if (labelId != null) {
                deleteLabelFromServerById(labelId);
            } else {
                Log.e("DEBUG", "לא נמצא ID עבור התווית: " + label);
            }

            Toast.makeText(getContext(), "נמחקה תווית: " + label, Toast.LENGTH_SHORT).show();
        });

        labelItem.addView(tv);
        labelItem.addView(deleteIcon);
        labelsContainer.addView(labelItem);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnCategorySelectedListener) {
            listener = (OnCategorySelectedListener) context;
        }
    }

    private void saveLabelsToPrefs() {
        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("userLabels", new Gson().toJson(userLabels));
        editor.apply();
    }

    private void fetchLabelsFromServer() {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:4000/api/labels");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                String token = prefs.getString("token", null);
                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                JSONArray jsonArray = new JSONArray(sb.toString());
                userLabels.clear();
                labelIdMap.clear();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    String label = obj.getString("name");
                    String id = obj.getString("_id");
                    userLabels.add(label);
                    labelIdMap.put(label, id);
                }

                requireActivity().runOnUiThread(this::loadLabels);

            } catch (Exception e) {
                Log.e("Labels", "שגיאה ב-fetchLabelsFromServer: " + e.getMessage());
            }
        }).start();
    }

    private void sendLabelToServer(String labelName) {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:4000/api/labels");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");

                SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                String token = prefs.getString("token", null);
                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }

                conn.setDoOutput(true);
                JSONObject payload = new JSONObject();
                payload.put("name", labelName);

                OutputStream os = conn.getOutputStream();
                os.write(payload.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == 200) {
                    Log.d("Label", "תווית נשמרה בשרת");
                } else {
                    Log.e("Label", "שגיאה בשמירת תווית לשרת: " + responseCode);
                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Label", "שגיאה ב-sendLabelToServer: " + e.getMessage());
            }
        }).start();
    }

    private void deleteLabelFromServerById(String labelId) {
        Log.d("Label", "מנסה למחוק מהשרת labelId: " + labelId);

        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:4000/api/labels/" + labelId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");

                SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                String token = prefs.getString("token", null);
                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                    Log.d("Label", "התווית נמחקה מהשרת בהצלחה");
                } else {
                    Log.e("Label", "שגיאה במחיקת תווית מהשרת: " + responseCode);
                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Label", "שגיאה ב-deleteLabelFromServerById: " + e.getMessage());
            }
        }).start();
    }
}
