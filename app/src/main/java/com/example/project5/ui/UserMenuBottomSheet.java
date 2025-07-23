package com.example.project5.ui;
import com.example.project5.utils.ProfileImageUtils;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.project5.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class UserMenuBottomSheet extends BottomSheetDialogFragment {

    private String username;
    private String email;
    private String profileImageUrl;

    private LinearLayout layoutProfileView;
    private ScrollView layoutEditProfile;

    private TextView tvUsername, tvEmail;
    private ImageView ivProfile;

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

        layoutProfileView = view.findViewById(R.id.layoutProfileView);
        layoutEditProfile = view.findViewById(R.id.layoutEditProfile);
        ivProfile = view.findViewById(R.id.ivProfile);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvEmail = view.findViewById(R.id.tvEmail);

        Button btnEditProfile = view.findViewById(R.id.btnEditProfile);
        Button btnLogout = view.findViewById(R.id.btnLogout);
        Button btnSaveEdit = view.findViewById(R.id.btnSaveEdit);
        Button btnCancelEdit = view.findViewById(R.id.btnCancelEdit);

        fetchUserDetailsFromServer();

        btnEditProfile.setOnClickListener(v -> {
            layoutProfileView.setVisibility(View.GONE);
            layoutEditProfile.setVisibility(View.VISIBLE);

            clearEditFields(view); // איפוס שדות תמידי

            SharedPreferences prefs = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
//            ((EditText) view.findViewById(R.id.etFirstName)).setText(prefs.getString("first_name", ""));
//            ((EditText) view.findViewById(R.id.etLastName)).setText(prefs.getString("last_name", ""));
//            ((EditText) view.findViewById(R.id.etUsername)).setText(prefs.getString("username", ""));
            EditText etBirthDate = view.findViewById(R.id.etBirthDate);
            etBirthDate.setFocusable(false);
            etBirthDate.setOnClickListener(v2 -> showDatePickerDialog(etBirthDate));

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

        btnLogout.setOnClickListener(v -> {
            requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                    .edit().remove("token").apply();
            startActivity(new Intent(requireContext(), EntryActivity.class));
            requireActivity().finish();
        });

        btnSaveEdit.setOnClickListener(v -> {
            String firstName = ((EditText) view.findViewById(R.id.etFirstName)).getText().toString();
            String lastName = ((EditText) view.findViewById(R.id.etLastName)).getText().toString();
            String usernameNew = ((EditText) view.findViewById(R.id.etUsername)).getText().toString();
            String birthDate = ((EditText) view.findViewById(R.id.etBirthDate)).getText().toString();
            String gender = ((Spinner) view.findViewById(R.id.spGender)).getSelectedItem().toString();

            if (!isOver18(birthDate)) {
                Toast.makeText(requireContext(), "You must be at least 18 years old", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences.Editor editor = requireActivity()
                    .getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE).edit();
            editor.putString("first_name", firstName);
            editor.putString("last_name", lastName);
            editor.putString("username", usernameNew);
            editor.putString("birth_date", birthDate);
            editor.putString("gender", gender);
            editor.commit();

            updateUserOnServer(firstName, lastName, usernameNew, birthDate, gender);

            tvUsername.setText(usernameNew.isEmpty() ? "User" : usernameNew);

            layoutEditProfile.setVisibility(View.GONE);
            layoutProfileView.setVisibility(View.VISIBLE);
        });

        btnCancelEdit.setOnClickListener(v -> {
            layoutEditProfile.setVisibility(View.GONE);
            layoutProfileView.setVisibility(View.VISIBLE);
            clearEditFields(view);
        });

        return view;
    }

    private void clearEditFields(View view) {
        ((EditText) view.findViewById(R.id.etFirstName)).setText("");
        ((EditText) view.findViewById(R.id.etLastName)).setText("");
        ((EditText) view.findViewById(R.id.etUsername)).setText("");
        ((EditText) view.findViewById(R.id.etBirthDate)).setText("");
        Spinner spGender = view.findViewById(R.id.spGender);
        if (spGender.getAdapter() != null) spGender.setSelection(0);
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

    private void showDatePickerDialog(EditText targetEditText) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePicker = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    targetEditText.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        datePicker.show();
    }
    private void fetchUserDetailsFromServer() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", "");
        String userId = prefs.getString("userId", null);
        String cachedUsername = prefs.getString("username", "User");
        String cachedEmail = prefs.getString("email", "user@example.com");
        String cachedImage = prefs.getString("profileImage", null);

        Log.d("UserMenu", "Loading from SharedPreferences:");
        Log.d("UserMenu", "username: " + cachedUsername);
        Log.d("UserMenu", "email: " + cachedEmail);
        Log.d("UserMenu", "profileImage: " + cachedImage);

        // טען פרטי משתמש מקומיים
        tvUsername.setText(cachedUsername);
        tvEmail.setText(cachedEmail);

        if (cachedImage != null && !cachedImage.isEmpty()) {
            String fullUrl = cachedImage.startsWith("http")
                    ? cachedImage
                    : "http://10.0.2.2:4000/" + cachedImage.replace("\\", "/");

            Glide.with(requireContext())
                    .load(fullUrl)
                    .placeholder(R.drawable.ic_avatar)
                    .circleCrop()
                    .into(ivProfile);
            Log.d("UserMenu", "Loaded image from cache.");
        } else {
            ivProfile.setImageResource(R.drawable.ic_avatar);
            Log.d("UserMenu", "No cached image found. Loaded default avatar.");
        }

        if (userId == null) {
            Log.e("UserMenu", "User ID not found in SharedPreferences");
            return;
        }

        Log.d("UserMenu", "Fetching from server: /api/users/" + userId);

        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:4000/api/users/" + userId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + token);

                int responseCode = conn.getResponseCode();
                Log.d("UserMenu", "GET response code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) response.append(line);
                    in.close();

                    JSONObject user = new JSONObject(response.toString());

                    String fetchedUsername = user.optString("username", cachedUsername);
                    String fetchedEmail = user.optString("email", cachedEmail);
                    String fetchedImageUrl = user.optString("profileImage", cachedImage);

                    requireActivity().runOnUiThread(() -> {
                        tvUsername.setText(fetchedUsername);
                        tvEmail.setText(fetchedEmail);

                        if (fetchedImageUrl != null && !fetchedImageUrl.isEmpty()) {
                            String fullImageUrl = fetchedImageUrl.startsWith("http")
                                    ? fetchedImageUrl
                                    : "http://10.0.2.2:4000/" + fetchedImageUrl.replace("\\", "/");

                            Glide.with(requireContext())
                                    .load(fullImageUrl)
                                    .placeholder(R.drawable.ic_avatar)
                                    .circleCrop()
                                    .into(ivProfile);
                            Log.d("UserMenu", "Loaded image from server");

                            prefs.edit().putString("profileImage", fetchedImageUrl).apply();
                        }
                    });

                } else {
                    BufferedReader err = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    StringBuilder errorMsg = new StringBuilder();
                    String line;
                    while ((line = err.readLine()) != null) errorMsg.append(line);
                    err.close();
                    Log.e("UserMenu", "Error body: " + errorMsg);
                }
            } catch (Exception e) {
                Log.e("UserMenu", "Exception while fetching user data", e);
            }
        }).start();
    }


    private void updateUserOnServer(String firstName, String lastName, String username, String birthDate, String gender) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", "");
        String userId = prefs.getString("userId", null);

        if (userId == null) {
            Log.e("UserMenu", "Missing userId in SharedPreferences");
            return;
        }

        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:4000/api/users/" + userId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("first_name", firstName);
                json.put("last_name", lastName);
                json.put("username", username);
                json.put("birth_date", birthDate);
                json.put("gender", gender);

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                Log.d("UserMenu", "PATCH response code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    requireActivity().runOnUiThread(() -> {
                        Log.d("UserMenu", "Successfully updated user. Refreshing...");
                        fetchUserDetailsFromServer();
                    });
                } else {
                    BufferedReader errorStream = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    StringBuilder errorMsg = new StringBuilder();
                    String line;
                    while ((line = errorStream.readLine()) != null) errorMsg.append(line);
                    errorStream.close();
                    Log.e("UserMenu", "PATCH Error: " + errorMsg.toString());
                }

            } catch (Exception e) {
                Log.e("UserMenu", "Exception in PATCH: ", e);
            }
        }).start();
    }

}