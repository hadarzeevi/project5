package com.example.project5.ui;

import static java.security.AccessController.getContext;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
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

        // כאן ניתן לטעון Fragments לפי צורך:
        loadFragment(R.id.sidebar, new SidebarFragment());
        // loadFragment(R.id.mailContentContainer, new MailListFragment());

        Button btnCompose = findViewById(R.id.btnComposeFloating);
        btnCompose.setOnClickListener(v -> {
            // כאן את יכולה לפתוח את מסך כתיבת הודעה
            Intent intent = new Intent(MailHomeActivity.this, ComposeActivity.class);
            startActivity(intent);
        });


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
