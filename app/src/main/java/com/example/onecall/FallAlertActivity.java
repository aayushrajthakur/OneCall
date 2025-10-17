package com.example.onecall;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.onecall.utils.AlertUtils;

public class FallAlertActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Show over lock screen and turn screen on
        setShowWhenLocked(true);
        setTurnScreenOn(true);

        setContentView(R.layout.activity_fall_alert);

        // 🔔 Play alarm and vibrate using AlertUtils
        AlertUtils.playAlarm(this);
        AlertUtils.vibrate(this);

        // ✅ Handle "Yes" button — dismiss alert
        findViewById(R.id.btn_yes).setOnClickListener(v -> {
            AlertUtils.stopAlarm();
            Toast.makeText(this, "Take care of yourself. \n Your safety is our priority", Toast.LENGTH_SHORT).show();
            finish();
        });

        // ✅ Handle "No" button — trigger emergency logic
        findViewById(R.id.btn_no).setOnClickListener(v -> {
            AlertUtils.stopAlarm();
            finish();
        });
    }

    // 🚫 Prevent back button from dismissing the alert
    @Override
    public void onBackPressed() {
        // Optionally block or show a toast
        super.onBackPressed();
        Toast.makeText(this, "Please respond to the alert", Toast.LENGTH_SHORT).show();
    }
}
