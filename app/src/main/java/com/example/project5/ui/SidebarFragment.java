package com.example.project5.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
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
import androidx.appcompat.app.AlertDialog;
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
    private final Map<String, LabelInfo> labelInfoMap = new HashMap<>();
    private View selectedCategoryView = null;
    private static class LabelInfo {
        String id;
        String color;
        LabelInfo(String id, String color) {
            this.id = id;
            this.color = color;
        }
    }

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

        for (Map.Entry<String, LabelInfo> entry : labelInfoMap.entrySet()) {
            String label = entry.getKey();
            String color = entry.getValue().color;
            addLabelToUI(label, color);
        }
    }

    private void promptAddLabel() {
        Context context = getContext();

        EditText labelInput = new EditText(context);
        labelInput.setHint("Enter label name");

        EditText colorInput = new EditText(context);
        colorInput.setHint("Enter color (e.g. red, blue)");
        colorInput.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 10);
        layout.addView(labelInput);
        layout.addView(colorInput);

        new AlertDialog.Builder(context)
                .setTitle("New Label")
                .setView(layout)
                .setPositiveButton("Add", (dialog, which) -> {
                    String labelName = labelInput.getText().toString().trim();
                    String colorName = colorInput.getText().toString().trim().toLowerCase();

                    if (labelName.isEmpty()) {
                        Toast.makeText(context, "שם התווית לא יכול להיות ריק", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (userLabels.contains(labelName)) {
                        Toast.makeText(context, "תווית כבר קיימת", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        Color.parseColor(colorName); // validate
                    } catch (IllegalArgumentException e) {
                        Toast.makeText(context, "שם צבע לא תקף", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    userLabels.add(labelName);
                    saveLabelsToPrefs();
                    addLabelToUI(labelName, colorName); // ← נעביר גם את הצבע
                    sendLabelToServer(labelName, colorName);
                })
                .setNegativeButton("Cancel", null)
                .show();
        }
    private void addLabelToUI(String label, String colorName) {
        Context context = getContext();

        // מכולה של כל השורה (כולל טקסט + אייקון)
        LinearLayout labelItem = new LinearLayout(context);
        labelItem.setOrientation(LinearLayout.HORIZONTAL);
        labelItem.setPadding(16, 16, 16, 16);
        labelItem.setGravity(Gravity.CENTER_VERTICAL);
        labelItem.setBackground(createRoundedBackground(Color.TRANSPARENT)); // ברירת מחדל – שקוף

        // טקסט של התווית
        TextView tv = new TextView(context);
        tv.setText(label);
        tv.setTextSize(15);
        tv.setTypeface(null, Typeface.NORMAL);
        tv.setPadding(36, 20, 36, 20);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        // פח
        ImageView deleteIcon = new ImageView(context);
        deleteIcon.setImageResource(R.drawable.ic_delete); // ודאי שזה אייקון קטן עם רקע שקוף
        deleteIcon.setPadding(20, 20, 20, 20);
        deleteIcon.setVisibility(View.GONE);

        try {
            int baseColor = Color.parseColor(colorName);
            int lightColor = lightenColor(baseColor, 0.8f);

            // שמירה בתור תגיות לשימוש מאוחר
            labelItem.setTag(R.id.label_bg_color, lightColor);
            labelItem.setTag(R.id.label_text_color, baseColor);

        } catch (Exception e) {
            labelItem.setBackground(createRoundedBackground(Color.LTGRAY));
            tv.setTextColor(Color.BLACK);
        }

        // בעת לחיצה על תווית
        tv.setOnClickListener(v -> {
            // אפס תווית קודמת
            if (selectedCategoryView != null && selectedCategoryView instanceof LinearLayout) {
                LinearLayout prevItem = (LinearLayout) selectedCategoryView;
                prevItem.setBackground(createRoundedBackground(Color.TRANSPARENT));

                TextView prevTv = (TextView) prevItem.getChildAt(0);
                prevTv.setTypeface(null, Typeface.NORMAL);
                prevTv.setTextColor(Color.parseColor("#333333"));

                ImageView prevTrash = (ImageView) prevItem.getChildAt(1);
                prevTrash.setVisibility(View.GONE);
            }

            // עדכן עיצוב חדש
            Integer bgColor = (Integer) labelItem.getTag(R.id.label_bg_color);
            if (bgColor != null) {
                labelItem.setBackground(createRoundedBackground(bgColor));
            }

            Integer textColor = (Integer) labelItem.getTag(R.id.label_text_color);
            if (textColor != null) {
                tv.setTextColor(textColor);
                deleteIcon.setColorFilter(textColor, PorterDuff.Mode.SRC_IN); // ← זה משנה את צבע הפח

            }

            tv.setTypeface(null, Typeface.BOLD);
            deleteIcon.setVisibility(View.VISIBLE);
            selectedCategoryView = labelItem;

            // שליחה לרשימת מיילים
            MailHomeActivity activity = (MailHomeActivity) requireActivity();
            MailListFragment listFragment = activity.getMailListFragment();
            if (listFragment != null) {
                listFragment.setLabel(label);
            }

            Toast.makeText(context, "Label: " + label + " selected", Toast.LENGTH_SHORT).show();
        });

        // כשלוחצים על פח
        deleteIcon.setOnClickListener(v -> {
            labelsContainer.removeView(labelItem);
            userLabels.remove(label);
            saveLabelsToPrefs();

            LabelInfo info = labelInfoMap.get(label);
            String labelId = info != null ? info.id : null;
            if (labelId != null) {
                deleteLabelFromServerById(labelId);
            }

            Toast.makeText(context, "נמחקה תווית: " + label, Toast.LENGTH_SHORT).show();
        });

        labelItem.addView(tv);
        labelItem.addView(deleteIcon);
        labelsContainer.addView(labelItem);
    }
    private Drawable createRoundedBackground(int color) {
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(0); // בלי עיגול בכלל
        bg.setColor(color);
        return bg;
    }

//    private void addLabelToUI(String label, String colorName) {
//        Context context = getContext();
//        LinearLayout labelItem = new LinearLayout(context);
//        labelItem.setOrientation(LinearLayout.HORIZONTAL);
//        labelItem.setPadding(24, 24, 24, 24);
//
//        ImageView deleteIcon = new ImageView(context);
//        deleteIcon.setImageResource(R.drawable.ic_delete);
//        deleteIcon.setPadding(16, 16, 16, 16);
//        deleteIcon.setVisibility(View.GONE);
//
//        TextView tv = new TextView(context);
//        tv.setText(label);
//        tv.setTextSize(16);
//        tv.setTextColor(ContextCompat.getColor(context, R.color.grayDark));
//        tv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
//        tv.setBackgroundColor(Color.TRANSPARENT);
//
//        tv.setOnClickListener(v -> {
//            // איפוס תווית קודמת
//            if (selectedCategoryView != null) {
//                TextView prev = (TextView) selectedCategoryView;
//                prev.setTypeface(null, Typeface.NORMAL);
//                prev.setBackgroundColor(Color.TRANSPARENT);
//
//                View parent = (View) selectedCategoryView.getParent();
//                if (parent instanceof LinearLayout) {
//                    ImageView prevDeleteIcon = (ImageView) ((LinearLayout) parent).getChildAt(1);
//                    if (prevDeleteIcon != null) prevDeleteIcon.setVisibility(View.GONE);
//                }
//            }
//
//            // סימון נוכחי
//            int brightColor = lightenColor(Color.parseColor(colorName), 0.6f);
//            tv.setBackgroundColor(brightColor);
//            tv.setTypeface(null, Typeface.BOLD);
//            selectedCategoryView = tv;
//            deleteIcon.setVisibility(View.VISIBLE);
//
//            MailHomeActivity activity = (MailHomeActivity) requireActivity();
//            MailListFragment listFragment = activity.getMailListFragment();
//            if (listFragment != null) {
//                listFragment.setLabel(label);
//            }
//
//            Toast.makeText(context, "Label: " + label + " selected", Toast.LENGTH_SHORT).show();
//        });
//
//        deleteIcon.setOnClickListener(v -> {
//            labelsContainer.removeView(labelItem);
//            userLabels.remove(label);
//            saveLabelsToPrefs();
//
//            LabelInfo info = labelInfoMap.get(label);
//            String labelId = info != null ? info.id : null;
//            if (labelId != null) {
//                deleteLabelFromServerById(labelId);
//            }
//
//            Toast.makeText(context, "נמחקה תווית: " + label, Toast.LENGTH_SHORT).show();
//        });
//
//        labelItem.addView(tv);
//        labelItem.addView(deleteIcon);
//        labelsContainer.addView(labelItem);
//    }

    private int lightenColor(int color, float factor) {
        int r = Math.min(255, (int) (Color.red(color) + (255 - Color.red(color)) * factor));
        int g = Math.min(255, (int) (Color.green(color) + (255 - Color.green(color)) * factor));
        int b = Math.min(255, (int) (Color.blue(color) + (255 - Color.blue(color)) * factor));
        return Color.rgb(r, g, b);
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
                labelInfoMap.clear();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    String label = obj.getString("name");
                    String id = obj.getString("_id");
                    String color = obj.optString("color", "#D3D3D3"); // אפור בהיר כברירת מחדל
                    userLabels.add(label);
                    labelInfoMap.put(label, new LabelInfo(id, color));

                }

                requireActivity().runOnUiThread(this::loadLabels);

            } catch (Exception e) {
                Log.e("Labels", "שגיאה ב-fetchLabelsFromServer: " + e.getMessage());
            }
        }).start();
    }

    private void sendLabelToServer(String labelName, String color) {
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
                payload.put("color", color);
                OutputStream os = conn.getOutputStream
                        ();
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
