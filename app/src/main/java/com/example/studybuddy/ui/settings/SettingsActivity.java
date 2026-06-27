package com.example.studybuddy.ui.settings;

import android.os.Bundle;
import android.widget.Switch;

import com.example.studybuddy.R;
import com.example.studybuddy.ui.BaseActivity;
import com.example.studybuddy.utils.ThemePreferences;
import com.google.android.material.appbar.MaterialToolbar;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.settingsToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        Switch darkModeSwitch = findViewById(R.id.switchDarkMode);
        darkModeSwitch.setChecked(ThemePreferences.isDarkModeEnabled(this));
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                ThemePreferences.setDarkModeEnabled(this, isChecked));
    }
}