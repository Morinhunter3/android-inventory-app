package com.example.projecttwo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.widget.EditText;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;



public class SmsNotificationActivity extends AppCompatActivity {

    private static final int REQUEST_SMS_PERMISSION = 100;

    private Switch switchEnableSms;
    private Button buttonCheckPermission;
    private Button buttonSendTest;
    private TextView textPermissionStatus;
    private EditText editPhoneNumber;
    private SharedPreferences prefs;

    public static final String PREFS_NAME = "sms_prefs";
    public static final String KEY_SMS_ENABLED = "sms_enabled";
    public static final String KEY_SMS_PHONE = "sms_phone";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_notification);

        switchEnableSms = findViewById(R.id.switchEnableSms);
        buttonCheckPermission = findViewById(R.id.buttonCheckPermission);
        buttonSendTest = findViewById(R.id.buttonSendTest);
        textPermissionStatus = findViewById(R.id.textPermissionStatus);
        editPhoneNumber = findViewById(R.id.editPhoneNumber);
        Button buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> finish());

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        switchEnableSms.setChecked(prefs.getBoolean(KEY_SMS_ENABLED, false));
        editPhoneNumber.setText(prefs.getString(KEY_SMS_PHONE, ""));

        updatePermissionStatus();
        switchEnableSms.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String phone = editPhoneNumber.getText().toString().trim();

            if (isChecked && phone.isEmpty()) {
                Toast.makeText(this,
                        "Enter a phone number before enabling SMS alerts.",
                        Toast.LENGTH_SHORT).show();

                switchEnableSms.setChecked(false);
                return;
            }

            prefs.edit()
                    .putBoolean(KEY_SMS_ENABLED, isChecked)
                    .putString(KEY_SMS_PHONE, phone)
                    .apply();
        });

        editPhoneNumber.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String phone = editPhoneNumber.getText().toString().trim();
                prefs.edit().putString(KEY_SMS_PHONE, phone).apply();
            }
        });

        buttonCheckPermission.setOnClickListener(v -> {
            if (hasSmsPermission()) {
                Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();
                updatePermissionStatus();
            } else {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.SEND_SMS},
                        REQUEST_SMS_PERMISSION
                );
            }
        });


        buttonSendTest.setOnClickListener(v -> {
            if (!switchEnableSms.isChecked()) {
                Toast.makeText(this, "Enable SMS alerts first.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!hasSmsPermission()) {
                Toast.makeText(this, "SMS permission needed before sending.", Toast.LENGTH_SHORT).show();

                return;
            }

            String phone = editPhoneNumber.getText().toString().trim();

            if (phone.isEmpty()) {
                Toast.makeText(this, "Enter a phone number before sending a test.", Toast.LENGTH_SHORT).show();
                return;
            }

            prefs.edit().putString(KEY_SMS_PHONE, phone).apply();

            Toast.makeText(this,
                    "Test SMS notification would be sent here (e.g., low inventory alert).",
                    Toast.LENGTH_LONG).show();
        });
    }

    private boolean hasSmsPermission() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void updatePermissionStatus() {
        if (hasSmsPermission()) {
            textPermissionStatus.setText("Current SMS permission: GRANTED");
        } else {
            textPermissionStatus.setText("Current SMS permission: NOT GRANTED");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_SMS_PERMISSION) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS permission granted.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS permission denied. Notifications will be disabled.", Toast.LENGTH_LONG).show();
            }
            updatePermissionStatus();
        }
    }
}