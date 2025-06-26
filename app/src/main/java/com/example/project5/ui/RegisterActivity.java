package com.example.project5.ui;


import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.project5.R;

public class RegisterActivity extends AppCompatActivity {

    int step = 1;
    Fragment stepFragment;

    public String firstName, lastName, username;
    public String birthDate, gender;
    public String email, password;
    public Uri profileImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        loadStepFragment();
    }

    public void loadStepFragment() {
        switch (step) {
            case 1:
                stepFragment = new Step1Fragment();
                break;
            case 2:
                stepFragment = new Step2Fragment();
                break;
            case 3:
                stepFragment = new Step3Fragment();
                break;
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frameContainer, stepFragment)
                .commit();
    }

    public void goToNextStep() {
        if (step < 3) {
            step++;
            loadStepFragment();
        }
    }

    public void goToPreviousStep() {
        if (step > 1) {
            step--;
            loadStepFragment();
        }
    }
}