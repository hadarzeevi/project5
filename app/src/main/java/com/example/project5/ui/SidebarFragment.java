package com.example.project5.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import java.lang.reflect.Type;
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
    private View selectedCategoryView = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sidebar, container, false);

        categoriesContainer = view.findViewById(R.id.categoryContainer);
        labelsContainer = view.findViewById(R.id.labelContainer);
        btnAddLabel = view.findViewById(R.id.btnAddLabel);
        btnAddLabel.setOnClickListener(v -> promptAddLabel());
        loadLabelsFromPrefs();   // ← קודם נטען מהאחסון המקומי

        loadCategories();
        loadLabels();

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
                        saveLabelsToPrefs(); // ← כאן
                        addLabelToUI(newLabel);
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

            // עדכון המסך הראשי לסינון לפי תווית
            MailHomeActivity activity = (MailHomeActivity) requireActivity();
            MailListFragment listFragment = activity.getMailListFragment();
            if (listFragment != null) {
                listFragment.setLabel(label); // ← הנה הקריאה החשובה!
            }

            Toast.makeText(getContext(), "Label: " + label + " selected", Toast.LENGTH_SHORT).show();
        });


        ImageView deleteIcon = new ImageView(getContext());
        deleteIcon.setImageResource(R.drawable.ic_delete);
        deleteIcon.setPadding(16, 16, 16, 16);
        deleteIcon.setOnClickListener(v -> {
            labelsContainer.removeView(labelItem);
            userLabels.remove(label);
            saveLabelsToPrefs(); // ← כאן

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

    private void loadLabelsFromPrefs() {
        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String json = prefs.getString("userLabels", "[]");
        Type type = new TypeToken<List<String>>(){}.getType();
        List<String> savedLabels = new Gson().fromJson(json, type);
        userLabels.clear();
        userLabels.addAll(savedLabels);
    }

}
