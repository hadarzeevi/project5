package com.example.project5.ui;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.example.project5.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

public class EditProfileBottomSheet extends BottomSheetDialogFragment {

    private ImageView ivProfile;
    private EditText etFirstName, etLastName, etUsername, etBirthDate;
    private Spinner spGender;
    private ProgressBar progressBar;
    private Button btnSave, btnCancel;
    private String selectedGender = "";

    public static EditProfileBottomSheet newInstance() {
        return new EditProfileBottomSheet();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        ivProfile = view.findViewById(R.id.ivProfile);
        etFirstName = view.findViewById(R.id.etFirstName);
        etLastName = view.findViewById(R.id.etLastName);
        etUsername = view.findViewById(R.id.etUsername);
        etBirthDate = view.findViewById(R.id.etBirthDate);
        spGender = view.findViewById(R.id.spGender);
        progressBar = view.findViewById(R.id.progressBar);
        btnSave = view.findViewById(R.id.btnSave);
        btnCancel = view.findViewById(R.id.btnCancel);

        // הגדר spinner מגדר
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.gender_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGender.setAdapter(adapter);

        spGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGender = parent.getItemAtPosition(position).toString();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        etBirthDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(getContext(),
                    (view1, year, month, dayOfMonth) ->
                            etBirthDate.setText(year + "-" + String.format("%02d", month + 1) + "-" + String.format("%02d", dayOfMonth)),
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)).show();
        });

        // טעינת פרטי המשתמש מהזיכרון להצגה
        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String profileImage = prefs.getString("profileImage", null);
        if (!TextUtils.isEmpty(profileImage)) {
            String url = "http://10.0.2.2:4000/" + profileImage.replace("\\", "/");
            Glide.with(requireContext()).load(url).circleCrop().into(ivProfile);
        } else {
            ivProfile.setImageResource(R.drawable.ic_avatar);
        }

        etFirstName.setText(prefs.getString("first_name", ""));
        etLastName.setText(prefs.getString("last_name", ""));
        etUsername.setText(prefs.getString("username", ""));
        etBirthDate.setText(prefs.getString("birth_date", ""));

        String genderSaved = prefs.getString("gender", "");
        if (!TextUtils.isEmpty(genderSaved)) {
            int spinnerPosition = adapter.getPosition(genderSaved);
            if (spinnerPosition >= 0) {
                spGender.setSelection(spinnerPosition);
            }
        }

        btnSave.setOnClickListener(v -> saveProfile());
        btnCancel.setOnClickListener(v -> dismiss());

        return view;
    }

    private void saveProfile() {
        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        new Thread(() -> {
            try {
                SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                String token = prefs.getString("token", "");
                String userId = prefs.getString("userId", "");

                URL url = new URL("http://10.0.2.2:4000/api/users/" + userId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");

                JSONObject body = new JSONObject();
                body.put("first_name", etFirstName.getText().toString().trim());
                body.put("last_name", etLastName.getText().toString().trim());
                body.put("username", etUsername.getText().toString().trim());
                body.put("birth_date", etBirthDate.getText().toString().trim());
                body.put("gender", selectedGender);

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes());
                os.flush();
                os.close();

                if (conn.getResponseCode() == 200) {
                    requireActivity().runOnUiThread(() -> {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("first_name", etFirstName.getText().toString().trim());
                        editor.putString("last_name", etLastName.getText().toString().trim());
                        editor.putString("username", etUsername.getText().toString().trim());
                        editor.putString("birth_date", etBirthDate.getText().toString().trim());
                        editor.putString("gender", selectedGender);
                        editor.apply();

                        Toast.makeText(getContext(), "Profile updated!", Toast.LENGTH_SHORT).show();
                        dismiss();
                    });
                } else {
                    requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error updating profile", Toast.LENGTH_SHORT).show());
                }

                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                });

            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}
