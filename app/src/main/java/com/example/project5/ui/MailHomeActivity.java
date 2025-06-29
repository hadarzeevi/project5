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

import com.example.project5.R;

public class MailHomeActivity extends AppCompatActivity {
    private boolean isDarkMode = false;
    AppCompatActivity view = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_home);

        Log.d("DEBUG", "inside");
        loadFragment(R.id.mailContentContainer, new MailListFragment());

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
}
