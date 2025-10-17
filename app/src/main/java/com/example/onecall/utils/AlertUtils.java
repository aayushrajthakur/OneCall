package com.example.onecall.utils;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Toast;

public class AlertUtils {

    private static Ringtone ringtone;

    public static void playAlarm(Context context) {
        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        ringtone = RingtoneManager.getRingtone(context, alert);
        if (ringtone != null) {
            ringtone.play();
        } else {
            Toast.makeText(context, "Ringtone is Null", Toast.LENGTH_SHORT).show();
        }
    }

    public static void stopAlarm() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
    }

    public static void vibrate(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            Toast.makeText(context, "Vibrator is not found", Toast.LENGTH_SHORT).show();
        }
    }
}
