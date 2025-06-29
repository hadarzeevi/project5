package com.example.project5.ui;

import static java.security.AccessController.getContext;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.project5.R;

public class MailHomeActivity extends AppCompatActivity implements OnCategorySelectedListener{
    private boolean isDarkMode = false;
    private MailListFragment mailListFragment;

    AppCompatActivity view = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_home);

        Log.d("DEBUG", "inside");
        mailListFragment = new MailListFragment();
        loadFragment(R.id.mailContentContainer, mailListFragment);

        // כפתור כתיבת מייל
        Button btnCompose = findViewById(R.id.btnComposeFloating);
        btnCompose.setOnClickListener(v -> {
            Intent intent = new Intent(MailHomeActivity.this, ComposeActivity.class);
            startActivity(intent);
        });

        // טעינת תפריט צד ברירת מחדל (רק אם רוצים שיופיע מיד)
        // loadFragment(R.id.sidebar, new SidebarFragment());

        // כפתור תפריט
        ImageView ivMenu = findViewById(R.id.ivMenu);
        ImageView ivAvatar = findViewById(R.id.ivAvatar);

// דוגמה לשליפת תמונת הפרופיל מה-SharedPreferences או מהמשתמש
        String userProfileImage = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                .getString("profileImage", null);

        if (userProfileImage != null && !userProfileImage.isEmpty()) {
            String url = "http://10.0.2.2:4000/" + userProfileImage.replace("\\", "/");
            Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.ic_avatar)
                    .error(R.drawable.ic_avatar)
                    .into(ivAvatar);
        } else {
            ivAvatar.setImageResource(R.drawable.ic_avatar);
        }

        FrameLayout sidebar = findViewById(R.id.sidebar);
        View sidebarOverlay = findViewById(R.id.sidebarOverlay);

        ivMenu.setOnClickListener(v -> {
            if (sidebar.getVisibility() == View.GONE) {
                sidebar.setVisibility(View.VISIBLE);
                sidebarOverlay.setVisibility(View.VISIBLE);
                loadFragment(R.id.sidebar, new SidebarFragment());
            } else {
                sidebar.setVisibility(View.GONE);
                sidebarOverlay.setVisibility(View.GONE);
            }
        });

        // סגירה בלחיצה מחוץ לתפריט
        sidebarOverlay.setOnClickListener(v -> {
            sidebar.setVisibility(View.GONE);
            sidebarOverlay.setVisibility(View.GONE);
        });

        Log.d("DEBUG", "end");
    }

    private void toggleDarkMode() {
        isDarkMode = !isDarkMode;
        // ניתן לשמור במשתף העדפות ולהחיל עיצוב כהה
        recreate(); // או עדכון של צבעים ידנית
    }

    private void loadFragment(int containerId, Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(containerId, fragment)
                .commit();
    }
    @Override
    public void onCategorySelected(String category) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.mailContentContainer);
        if (currentFragment instanceof MailListFragment) {
            ((MailListFragment) currentFragment).setCategory(category);
        }
    }
}
