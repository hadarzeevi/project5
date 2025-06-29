package com.example.project5.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

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

        loadCategories();
        loadLabels();

        return view;
    }

    private void loadCategories() {
        categoriesContainer.removeAllViews();

        String[] categories = {"Inbox", "Starred", "Sent", "Drafts", "Spam", "Trash"};
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
        input.setHint("הכנס שם תווית");

        new android.app.AlertDialog.Builder(getContext())
                .setTitle("תווית חדשה")
                .setView(input)
                .setPositiveButton("הוסף", (dialog, which) -> {
                    String newLabel = input.getText().toString().trim();
                    if (!newLabel.isEmpty() && !userLabels.contains(newLabel)) {
                        userLabels.add(newLabel);
                        addLabelToUI(newLabel);
                    } else {
                        Toast.makeText(getContext(), "תווית קיימת או ריקה", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("ביטול", null)
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
            Toast.makeText(getContext(), label + " selected", Toast.LENGTH_SHORT).show();
        });

        ImageView deleteIcon = new ImageView(getContext());
        deleteIcon.setImageResource(R.drawable.ic_delete);
        deleteIcon.setPadding(16, 16, 16, 16);
        deleteIcon.setOnClickListener(v -> {
            labelsContainer.removeView(labelItem);
            userLabels.remove(label);
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

}
