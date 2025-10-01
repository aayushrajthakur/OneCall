package com.example.onecall;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

public class FallAlertActivity extends AppCompatActivity {

    private Ringtone ringtone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Show over lock screen and turn screen on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            );
        }

        setContentView(R.layout.activity_fall_alert);

        // 🔊 Play system alarm sound safely
        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        ringtone = RingtoneManager.getRingtone(getApplicationContext(), alert);
        if (ringtone != null) {
            ringtone.play();
        }

        // 🔔 Optional: Vibrate for attention
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
        }

        // ✅ Handle "Yes" button — dismiss alert
        findViewById(R.id.btn_yes).setOnClickListener(v -> {
            if (ringtone != null && ringtone.isPlaying()) ringtone.stop();
            finish();
        });

        // ✅ Handle "No" button — trigger emergency logic
        findViewById(R.id.btn_no).setOnClickListener(v -> {
            if (ringtone != null && ringtone.isPlaying()) ringtone.stop();
            // TODO: Trigger emergency SMS, location sharing, or call
            finish();
        });
    }

    // 🚫 Prevent back button from dismissing the alert
    @Override
    public void onBackPressed() {
        // Optionally show a toast or do nothing
        super.onBackPressed();
    }
}
