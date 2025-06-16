package com.nestsecure.zeroless;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "ZerolessPrefs";
    private static final String PREF_THEME = "theme";

    private TextInputEditText textInput, commonKeyInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme from preferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isDarkTheme = prefs.getBoolean(PREF_THEME, false);
        setTheme(isDarkTheme ? R.style.Theme_Zeroless_Dark : R.style.Theme_Zeroless_Light);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Prevent screenshots
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        // Initialize views
        textInput = findViewById(R.id.text_input);
        commonKeyInput = findViewById(R.id.common_key_input);
        commonKeyInput.setTransformationMethod(new PartialMaskTransformation());

        // Set up toolbar
        setSupportActionBar(findViewById(R.id.toolbar));

        // Encrypt button
        MaterialButton encryptButton = findViewById(R.id.encrypt_button);
        encryptButton.setOnClickListener(v -> {
            String text = textInput.getText().toString();
            String key = commonKeyInput.getText().toString();
            if (text.isEmpty() || key.isEmpty()) {
                Toast.makeText(this, "Text and key are required", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                String encrypted = CryptoUtils.encrypt(text, key);
                textInput.setText(encrypted);
            } catch (Exception e) {
                Toast.makeText(this, "Encryption error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Decrypt button
        MaterialButton decryptButton = findViewById(R.id.decrypt_button);
        decryptButton.setOnClickListener(v -> {
            String text = textInput.getText().toString();
            String key = commonKeyInput.getText().toString();
            if (text.isEmpty() || key.isEmpty()) {
                Toast.makeText(this, "Text and key are required", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                String decrypted = CryptoUtils.decrypt(text, key);
                textInput.setText(decrypted);
            } catch (Exception e) {
                Toast.makeText(this, "Decryption error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Copy button
        MaterialButton copyButton = findViewById(R.id.copy_button);
        copyButton.setOnClickListener(v -> {
            String text = textInput.getText().toString();
            if (text.isEmpty()) {
                Toast.makeText(this, "Nothing to copy", Toast.LENGTH_SHORT).show();
                return;
            }
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Zeroless", text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
        });

        // Paste button
        MaterialButton pasteButton = findViewById(R.id.paste_button);
        pasteButton.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard.hasPrimaryClip() && clipboard.getPrimaryClip().getItemCount() > 0) {
                String pastedText = clipboard.getPrimaryClip().getItemAt(0).getText().toString();
                textInput.setText(pastedText);
            } else {
                Toast.makeText(this, "Clipboard is empty", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_clear_all) {
            textInput.setText("");
            commonKeyInput.setText("");
            Toast.makeText(this, "Cleared all fields", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_exit) {
            // Clear clipboard with numbers 1-45
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            StringBuilder dummyData = new StringBuilder();
            for (int i = 1; i <= 45; i++) {
                dummyData.append(i).append(" ");
            }
            ClipData clip = ClipData.newPlainText("Zeroless", dummyData.toString());
            clipboard.setPrimaryClip(clip);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
