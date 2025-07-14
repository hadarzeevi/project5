package com.example.project5.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.project5.R;

public class Step1Fragment extends Fragment {

    EditText etFirstName, etLastName, etUsername;
    Button btnNext;
    TextView tvError;

    RegisterActivity parentActivity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_step1, container, false);

        parentActivity = (RegisterActivity) getActivity();

        etFirstName = view.findViewById(R.id.etFirstName);
        etLastName = view.findViewById(R.id.etLastName);
        etUsername = view.findViewById(R.id.etUsername);
        btnNext = view.findViewById(R.id.btnNext);
        tvError = view.findViewById(R.id.tvError);

        if (parentActivity.firstName != null) etFirstName.setText(parentActivity.firstName);
        if (parentActivity.lastName != null) etLastName.setText(parentActivity.lastName);
        if (parentActivity.username != null) etUsername.setText(parentActivity.username);

        btnNext.setOnClickListener(v -> {
            tvError.setVisibility(View.GONE);

            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String username = etUsername.getText().toString().trim();

            if (firstName.length() < 2) {
                showError("First name must be at least 2 characters");
                return;
            }
            if (lastName.length() < 2) {
                showError("Last name must be at least 2 characters");
                return;
            }
            if (username.length() < 3) {
                showError("Username must be at least 3 characters");
                return;
            }

            parentActivity.firstName = firstName;
            parentActivity.lastName = lastName;
            parentActivity.username = username;

            parentActivity.goToNextStep();
        });

        return view;
    }

    private void showError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }
}