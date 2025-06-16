package com.nestsecure.zeroless;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "ZerolessPrefs";
    private static final String PREF_STORAGE = "storage";
    private static final String PREF_THEME = "theme";

    private TextInputEditText commonKeyInput, personalKeyInput;
    private SwitchMaterial storageSwitch, themeSwitch;
    private boolean isSdCardAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme from preferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isDarkTheme = prefs.getBoolean(PREF_THEME, false);
        setTheme(isDarkTheme ? R.style.Theme_Zeroless_Dark : R.style.Theme_Zeroless_Light);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Prevent screenshots
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        // Initialize views
        commonKeyInput = findViewById(R.id.common_key_settings_input);
        personalKeyInput = findViewById(R.id.personal_key_settings_input);
        storageSwitch = findViewById(R.id.storage_switch);
        themeSwitch = findViewById(R.id.theme_switch);

        // Check SD card availability
        isSdCardAvailable = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        storageSwitch.setEnabled(isSdCardAvailable);

        // Load preferences
        boolean useSdCard = prefs.getBoolean(PREF_STORAGE, false);
        storageSwitch.setChecked(useSdCard);

        themeSwitch.setChecked(isDarkTheme);

        // Storage switch listener
        storageSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !prefs.getBoolean("warning_shown", false)) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.warning_internal_storage)
                        .setMessage(R.string.warning_internal_storage)
                        .setPositiveButton(R.string.ok, (dialog, which) -> {
                            prefs.edit().putBoolean(PREF_STORAGE, true).apply();
                            prefs.edit().putBoolean("warning_shown", true).apply();
                        })
                        .setNegativeButton(R.string.cancel, (dialog, which) -> storageSwitch.setChecked(false))
                        .show();
            } else {
                prefs.edit().putBoolean(PREF_STORAGE, isChecked).apply();
            }
        });

        // Theme switch listener
        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(PREF_THEME, isChecked).apply();
            recreate(); // Restart activity to apply theme
        });

        // Save key button
        MaterialButton saveKeyButton = findViewById(R.id.save_key_button);
        saveKeyButton.setOnClickListener(v -> {
            String commonKey = commonKeyInput.getText().toString();
            String personalKey = personalKeyInput.getText().toString();
            if (commonKey.isEmpty() || personalKey.isEmpty()) {
                Toast.makeText(this, "Both keys are required", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                String encryptedKey = CryptoUtils.encryptKey(commonKey, personalKey);
                CryptoUtils.saveEncryptedKey(this, encryptedKey, storageSwitch.isChecked());
                Toast.makeText(this, "Key saved successfully", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Error saving key: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Restore key button
        MaterialButton restoreKeyButton = findViewById(R.id.restore_key_button);
        restoreKeyButton.setOnClickListener(v -> {
            String personalKey = personalKeyInput.getText().toString();
            if (personalKey.isEmpty()) {
                Toast.makeText(this, "Personal key is required", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                String encryptedKey = CryptoUtils.readEncryptedKey(this, storageSwitch.isChecked());
                String commonKey = CryptoUtils.decryptKey(encryptedKey, personalKey);
                commonKeyInput.setText(commonKey);
                Toast.makeText(this, "Key restored successfully", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Error restoring key: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
