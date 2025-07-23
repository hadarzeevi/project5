package com.example.project5.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.project5.ui.UserMenuBottomSheet;


import com.bumptech.glide.Glide;
import com.example.project5.R;

public class MailHomeActivity extends AppCompatActivity {

    private boolean isDarkMode = false;
    private MailListFragment mailListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_home);

        // load mail list
        mailListFragment = new MailListFragment();
        loadFragment(R.id.mailContentContainer, mailListFragment);

        // compose btn
        Button btnCompose = findViewById(R.id.btnComposeFloating);
        btnCompose.setOnClickListener(v -> {
            Intent intent = new Intent(MailHomeActivity.this, ComposeActivity.class);
            startActivity(intent);
        });

        // load profile pics
        ImageView ivAvatar = findViewById(R.id.ivAvatar);
        EditText etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mailListFragment != null) {
                    mailListFragment.setSearchQuery(s.toString());
                }
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        loadUserProfileImage(ivAvatar);

        // open profile menu
        ivAvatar.setOnClickListener(v -> {
            String username = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("username", "User");
            String email = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("mail", "user@example.com");
            String profileImage = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("profileImage", null);
            String url = profileImage != null ? "http://10.0.2.2:4000/" + profileImage.replace("\\", "/") : null;

            UserMenuBottomSheet bottomSheet = UserMenuBottomSheet.newInstance(username, email, url);
            bottomSheet.show(getSupportFragmentManager(), "userMenu");
        });

        ImageView ivMenu = findViewById(R.id.ivMenu);
        FrameLayout sidebar = findViewById(R.id.sidebar);
        View sidebarOverlay = findViewById(R.id.sidebarOverlay);

        ivMenu.setOnClickListener(v -> toggleSidebar(sidebar, sidebarOverlay));
        sidebarOverlay.setOnClickListener(v -> closeSidebar(sidebar, sidebarOverlay));
    }

    private void loadUserProfileImage(ImageView ivAvatar) {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String userProfileImage = prefs.getString("profileImage", null);

        Log.d("Avatar", "Loaded profile image from prefs: " + userProfileImage);

        if (userProfileImage != null && !userProfileImage.isEmpty()) {
            String url = userProfileImage.startsWith("http")
                    ? userProfileImage
                    : "http://10.0.2.2:4000/" + userProfileImage.replace("\\", "/");
            Log.d("Avatar", "Loading image from URL: " + url);

            Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.ic_avatar)
                    .error(R.drawable.ic_avatar)
                    .circleCrop()
                    .into(ivAvatar);
        } else {
            Log.d("Avatar", "No profile image found, showing default.");
            ivAvatar.setImageResource(R.drawable.ic_avatar);
        }
    }

    private void loadFragment(int containerId, Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(containerId, fragment)
                .commit();
    }

    private void toggleSidebar(FrameLayout sidebar, View sidebarOverlay) {
        if (sidebar.getVisibility() == View.GONE) {
            sidebar.setVisibility(View.VISIBLE);
            sidebarOverlay.setVisibility(View.VISIBLE);
            loadFragment(R.id.sidebar, new SidebarFragment());
        } else {
            closeSidebar(sidebar, sidebarOverlay);
        }
    }

    private void closeSidebar(FrameLayout sidebar, View sidebarOverlay) {
        sidebar.setVisibility(View.GONE);
        sidebarOverlay.setVisibility(View.GONE);
    }

    private void toggleDarkMode() {
        isDarkMode = !isDarkMode;
        recreate();
    }
    public MailListFragment getMailListFragment() {
        return mailListFragment;
    }

    @Override
    protected void onResume() {
        super.onResume();
        ImageView ivAvatar = findViewById(R.id.ivAvatar);
        loadUserProfileImage(ivAvatar);
    }

}
