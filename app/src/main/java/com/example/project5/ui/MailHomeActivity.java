package com.example.project5.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

        Log.d("DEBUG", "MailHomeActivity started");

        // טען את רשימת המיילים למסך הראשי
        mailListFragment = new MailListFragment();
        loadFragment(R.id.mailContentContainer, mailListFragment);

        // כפתור כתיבת מייל
        Button btnCompose = findViewById(R.id.btnComposeFloating);
        btnCompose.setOnClickListener(v -> {
            Intent intent = new Intent(MailHomeActivity.this, ComposeActivity.class);
            startActivity(intent);
        });

        // טעינת תמונת פרופיל
        ImageView ivAvatar = findViewById(R.id.ivAvatar);
        loadUserProfileImage(ivAvatar);

        // פתיחת תפריט פרופיל בלחיצה על תמונת הפרופיל
        ivAvatar.setOnClickListener(v -> {
            String username = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("username", "User");
            String email = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("mail", "user@example.com");
            String profileImage = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("profileImage", null);
            String url = profileImage != null ? "http://10.0.2.2:4000/" + profileImage.replace("\\", "/") : null;

            UserMenuBottomSheet bottomSheet = UserMenuBottomSheet.newInstance(username, email, url);
            bottomSheet.show(getSupportFragmentManager(), "userMenu");
        });

        // תפריט צד (המבורגר)
        ImageView ivMenu = findViewById(R.id.ivMenu);
        FrameLayout sidebar = findViewById(R.id.sidebar);
        View sidebarOverlay = findViewById(R.id.sidebarOverlay);

        ivMenu.setOnClickListener(v -> toggleSidebar(sidebar, sidebarOverlay));
        sidebarOverlay.setOnClickListener(v -> closeSidebar(sidebar, sidebarOverlay));

        Log.d("DEBUG", "MailHomeActivity ready");
    }

    /**
     * טוען את תמונת הפרופיל מה-SharedPreferences או תמונת ברירת מחדל
     */
    private void loadUserProfileImage(ImageView ivAvatar) {
        String userProfileImage = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                .getString("profileImage", null);

        if (userProfileImage != null && !userProfileImage.isEmpty()) {
            String url = "http://10.0.2.2:4000/" + userProfileImage.replace("\\", "/");
            Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.ic_avatar)
                    .error(R.drawable.ic_avatar)
                    .circleCrop()
                    .into(ivAvatar);
        } else {
            ivAvatar.setImageResource(R.drawable.ic_avatar);
        }
    }

    /**
     * טוען פרגמנט למיקום שנבחר במסך
     */
    private void loadFragment(int containerId, Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(containerId, fragment)
                .commit();
    }

    /**
     * פתיחה/סגירת תפריט צד
     */
    private void toggleSidebar(FrameLayout sidebar, View sidebarOverlay) {
        if (sidebar.getVisibility() == View.GONE) {
            sidebar.setVisibility(View.VISIBLE);
            sidebarOverlay.setVisibility(View.VISIBLE);
            loadFragment(R.id.sidebar, new SidebarFragment());
        } else {
            closeSidebar(sidebar, sidebarOverlay);
        }
    }

    /**
     * סגירת תפריט צד
     */
    private void closeSidebar(FrameLayout sidebar, View sidebarOverlay) {
        sidebar.setVisibility(View.GONE);
        sidebarOverlay.setVisibility(View.GONE);
    }

    /**
     * החלפת מצב כהה (לשימוש עתידי)
     */
    private void toggleDarkMode() {
        isDarkMode = !isDarkMode;
        recreate();
    }
    public MailListFragment getMailListFragment() {
        return mailListFragment;
    }

}
