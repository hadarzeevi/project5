package com.example.project5.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.project5.R;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Step2Fragment extends Fragment {

    EditText etBirthDate;
    Spinner spinnerGender;
    Button btnNext, btnBack;
    TextView tvError;

    RegisterActivity parentActivity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_step2, container, false);

        parentActivity = (RegisterActivity) getActivity();

        etBirthDate = view.findViewById(R.id.etBirthDate);
        etBirthDate.setFocusable(false);
        etBirthDate.setOnClickListener(v -> showDatePickerDialog());

        spinnerGender = view.findViewById(R.id.spinnerGender);
        btnNext = view.findViewById(R.id.btnNext);
        btnBack = view.findViewById(R.id.btnBack);
        tvError = view.findViewById(R.id.tvError);

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.gender_array));
        spinnerGender.setAdapter(genderAdapter);

        // טעינת ערכים קיימים במידה וחזרנו אחורה
        if (parentActivity.birthDate != null) etBirthDate.setText(parentActivity.birthDate);
        if (parentActivity.gender != null) {
            int spinnerPos = genderAdapter.getPosition(parentActivity.gender);
            spinnerGender.setSelection(spinnerPos);
        }

        btnBack.setOnClickListener(v -> parentActivity.goToPreviousStep());

        btnNext.setOnClickListener(v -> {
            tvError.setVisibility(View.GONE);

            String birthDate = etBirthDate.getText().toString().trim();
            String gender = spinnerGender.getSelectedItem().toString();

            if (birthDate.isEmpty()) {
                showError("Birth date is required");
                return;
            }

            if (!isOver18(birthDate)) {
                showError("You must be at least 18 years old");
                return;
            }

            if (gender.equals("Select Gender")) {
                showError("Gender is required");
                return;
            }

            parentActivity.birthDate = birthDate;
            parentActivity.gender = gender;

            parentActivity.goToNextStep();
        });

        return view;
    }

    private boolean isOver18(String birthDateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date birthDate = sdf.parse(birthDateStr);

            Calendar today = Calendar.getInstance();
            Calendar dob = Calendar.getInstance();
            dob.setTime(birthDate);

            int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
            if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }

            return age >= 18;
        } catch (Exception e) {
            return false;
        }
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePicker = new DatePickerDialog(getContext(),
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    etBirthDate.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        datePicker.show();
    }

    private void showError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }
}