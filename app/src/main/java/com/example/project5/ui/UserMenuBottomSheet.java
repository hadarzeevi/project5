package com.example.project5.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.project5.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class UserMenuBottomSheet extends BottomSheetDialogFragment {

    private String username;
    private String email;
    private String profileImageUrl;

    private LinearLayout layoutProfileView;
    private ScrollView layoutEditProfile;

    @Override
    public void onStart() {
        super.onStart();
        View view = getView();
        if (view != null) {
            View parent = (View) view.getParent();
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(parent);
            behavior.setPeekHeight((int) (getResources().getDisplayMetrics().heightPixels * 0.7));
            parent.setBackgroundColor(Color.TRANSPARENT);
            parent.post(() -> {
                ViewGroup.LayoutParams params = parent.getLayoutParams();
                params.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.7);
                parent.setLayoutParams(params);
            });
        }
    }

    public static UserMenuBottomSheet newInstance(String username, String email, String profileImageUrl) {
        UserMenuBottomSheet fragment = new UserMenuBottomSheet();
        Bundle args = new Bundle();
        args.putString("username", username);
        args.putString("email", email);
        args.putString("profileImageUrl", profileImageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.bottom_sheet_user_menu, container, false);

        if (getArguments() != null) {
            username = getArguments().getString("username", "User");
            email = getArguments().getString("email", "user@example.com");
            profileImageUrl = getArguments().getString("profileImageUrl", null);
        }

        layoutProfileView = view.findViewById(R.id.layoutProfileView);
        layoutEditProfile = view.findViewById(R.id.layoutEditProfile);

        // show profile
        ImageView ivProfile = view.findViewById(R.id.ivProfile);
        TextView tvUsername = view.findViewById(R.id.tvUsername);
        TextView tvEmail = view.findViewById(R.id.tvEmail);
        Button btnEditProfile = view.findViewById(R.id.btnEditProfile);
        Button btnLogout = view.findViewById(R.id.btnLogout);

        tvUsername.setText(username);
        tvEmail.setText(email);

        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            Glide.with(requireContext())
                    .load(profileImageUrl)
                    .placeholder(R.drawable.ic_avatar)
                    .circleCrop()
                    .into(ivProfile);
        } else {
            ivProfile.setImageResource(R.drawable.ic_avatar);
        }

        // edit profile
        btnEditProfile.setOnClickListener(v -> {
            layoutProfileView.setVisibility(View.GONE);
            layoutEditProfile.setVisibility(View.VISIBLE);

            // load details
            SharedPreferences prefs = requireActivity().getSharedPreferences("MyAppPrefs", getContext().MODE_PRIVATE);

            ((EditText) view.findViewById(R.id.etFirstName)).setText(prefs.getString("first_name", ""));
            ((EditText) view.findViewById(R.id.etLastName)).setText(prefs.getString("last_name", ""));
            ((EditText) view.findViewById(R.id.etUsername)).setText(prefs.getString("username", ""));
            ((EditText) view.findViewById(R.id.etBirthDate)).setText(prefs.getString("birth_date", ""));

            Spinner spGender = view.findViewById(R.id.spGender);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                    R.array.gender_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spGender.setAdapter(adapter);

            String savedGender = prefs.getString("gender", "");
            if (!savedGender.isEmpty()) {
                int spinnerPosition = adapter.getPosition(savedGender);
                if (spinnerPosition >= 0) {
                    spGender.setSelection(spinnerPosition);
                }
            }
        });

        // logout
        btnLogout.setOnClickListener(v -> {
            requireActivity().getSharedPreferences("MyAppPrefs", getContext().MODE_PRIVATE)
                    .edit()
                    .remove("token")
                    .apply();
            startActivity(new Intent(requireContext(), EntryActivity.class));
            requireActivity().finish();
        });

        // save
        Button btnSaveEdit = view.findViewById(R.id.btnSaveEdit);
        btnSaveEdit.setOnClickListener(v -> {
            SharedPreferences.Editor editor = requireActivity()
                    .getSharedPreferences("MyAppPrefs", getContext().MODE_PRIVATE)
                    .edit();

            String firstName = ((EditText) view.findViewById(R.id.etFirstName)).getText().toString();
            String lastName = ((EditText) view.findViewById(R.id.etLastName)).getText().toString();
            String usernameNew = ((EditText) view.findViewById(R.id.etUsername)).getText().toString();
            String birthDate = ((EditText) view.findViewById(R.id.etBirthDate)).getText().toString();
            String gender = ((Spinner) view.findViewById(R.id.spGender)).getSelectedItem().toString();

            editor.putString("first_name", firstName);
            editor.putString("last_name", lastName);
            editor.putString("username", usernameNew);
            editor.putString("birth_date", birthDate);
            editor.putString("gender", gender);
            editor.apply();

            tvUsername.setText(usernameNew.isEmpty() ? "User" : usernameNew);

            layoutEditProfile.setVisibility(View.GONE);
            layoutProfileView.setVisibility(View.VISIBLE);
        });

        // cancel
        Button btnCancelEdit = view.findViewById(R.id.btnCancelEdit);
        btnCancelEdit.setOnClickListener(v -> {
            layoutEditProfile.setVisibility(View.GONE);
            layoutProfileView.setVisibility(View.VISIBLE);
        });

        return view;
    }
}
