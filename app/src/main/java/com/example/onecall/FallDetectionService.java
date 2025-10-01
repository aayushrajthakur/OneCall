package com.example.onecall;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;

public class FallDetectionService extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static final float ACCELERATION_THRESHOLD = 25.0f;
    private boolean isFallDetected = false;

    @Override
    public void onCreate() {
        super.onCreate();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Log.e("FallDetection", "Accelerometer not available on this device.");
            stopSelf(); // Optional: stop service if sensor is missing
        }

        startForeground(1, createNotification());
    }

    private Notification createNotification() {
        String channelId = "fall_service_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Fall Detection Service",
                    NotificationManager.IMPORTANCE_HIGH // ✅ Ensure visibility
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Fall Detection Active")
                .setContentText("Monitoring for falls in background")
                .setSmallIcon(R.drawable.warning)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float accelerationMagnitude = (float) Math.sqrt(x * x + y * y + z * z);

            if (accelerationMagnitude > ACCELERATION_THRESHOLD && !isFallDetected) {
                isFallDetected = true;
                Log.d("FallDetection", "Hard fall detected! Acceleration: " + accelerationMagnitude);

                // 🔔 Optional: Vibrate for attention
                Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                if (vibrator != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
                }

                // 🚨 Launch FallAlertActivity directly from foreground service
                Intent alertIntent = new Intent(this, FallAlertActivity.class);
                alertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(alertIntent);

                // ⏳ Reset flag after delay (e.g., 5 seconds)
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    isFallDetected = false;
                    Log.d("FallDetection", "Fall detection reset.");
                }, 5000);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
        Log.d("FallDetection", "Service destroyed and sensor unregistered.");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
