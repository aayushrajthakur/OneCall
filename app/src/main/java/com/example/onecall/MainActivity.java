package com.example.onecall;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.Manifest;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.onecall.models.ModelData;
import com.example.onecall.network.ApiService;
import com.example.onecall.network.RetrofitClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.config.Configuration;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity{

    private final BroadcastReceiver fallReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // ✅ Launch the alert activity over lock screen
            Intent alertIntent = new Intent(context, FallAlertActivity.class);
            alertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(alertIntent);
        }
    };

    FirebaseAuth mAuth;
    FirebaseUser user;
    TextView greet;
    TextView locationName;
    TextView locationCoors;
    Button logOut;
    MaterialButton callButton;
    MapView map;
    Button getCurrentlocation;
    MaterialButton sendLocation;
    TextView status;
    ImageView refreshButton;
    double latitude;
    double longitude;

    TextInputEditText target_id;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static final float ACCELERATION_THRESHOLD = 25.0f; // Adjust based on testing
    private boolean isFallDetected = false;

    private Ringtone ringtone;

    // 🔔 Receiver for fall detection





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());


        Configuration.getInstance().load(getApplicationContext(),
                androidx.preference.PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        checkPermissions();         // 🔐 Your sensor/location permission logic
        checkBatteryOptimization(); // 🔋 Prompt user early
        startFallDetectionService();

        Intent serviceIntent = new Intent(this, FallDetectionService.class);
        ContextCompat.startForegroundService(this, serviceIntent);


        greet = findViewById(R.id.greetings);
        logOut = findViewById(R.id.logout);
        target_id = findViewById(R.id.targetId);
        callButton = findViewById(R.id.callButton);
        locationName = findViewById(R.id.locationName);
        locationCoors = findViewById(R.id.locationCoords);
        status = findViewById(R.id.status);
        refreshButton = findViewById(R.id.refresh_Button);



        getCurrentlocation = findViewById(R.id.getLocation);
        map = findViewById(R.id.map);
        sendLocation = findViewById(R.id.send_location);


        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();






        if (user != null) {
            user.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    String username = user.getDisplayName();
                    greet.setText("Hello \n" + (username != null ? username : "User"));
                    String mail = user.getEmail();
                    String userid = mail.replace("@gmail.com", "");
                    //removed zego cloud calls from here.
                }
            });
        } else {
            greet.setText("Hello User");
        }

        if (isInternetAvailable(this)) {
            status.setText("connected ✅");
        }else {
            status.setText("disconnected 🔴");
        }


        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isInternetAvailable(MainActivity.this)) {
                    status.setText("connected ✅");
                }else {
                    status.setText("disconnected 🔴");
                }
            }
        });


        getCurrentlocation.setOnClickListener(view -> checkLocationPermission());



        sendLocation.setOnClickListener(v -> {
            getCurrentLocation(); // request location

            new android.os.Handler().postDelayed(() -> {
                // Delay slightly to allow getLastLocation() to return
                if (latitude != 0.0 && longitude != 0.0) {
                    ApiService service = RetrofitClient.getClient().create(ApiService.class);
                    ModelData location = new ModelData(latitude, longitude, user.getDisplayName());
                    String userId = user.getUid(); // or user.getDisplayName() or any unique ID
                    Call<Void> call = service.sendLocation(userId, location);

                    call.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(MainActivity.this, "Location sent successfully", Toast.LENGTH_SHORT).show();
                                Log.e("API", "Location sent");
                            } else {
                                Log.e("Network", "Error: " + response.code());
                                Toast.makeText(MainActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Log.e("API", "Network error: " + t.getMessage());
                            Toast.makeText(MainActivity.this, "Network failed", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    Toast.makeText(MainActivity.this, "Location not yet available", Toast.LENGTH_SHORT).show();
                }
            }, 1000); // wait 1 second before sending
        });



        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void checkBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(getPackageName())) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
    }

    private void startFallDetectionService() {
        Intent serviceIntent = new Intent(this, FallDetectionService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }






    public void getCurrentLocation(){
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();

                        } else {
                            Toast.makeText(MainActivity.this, "Location is null", Toast.LENGTH_SHORT).show();
                        }
                    });

        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void fetchAddressFromCoordinates(double lat, double lon) {
        String url = "https://nominatim.openstreetmap.org/reverse?format=json&lat=" + lat + "&lon=" + lon;

        new Thread(() -> {
            try {
                java.net.URL apiUrl = new java.net.URL(url);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) apiUrl.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "OneCallApp/1.0"); // Required by Nominatim

                java.io.BufferedReader in = new java.io.BufferedReader(
                        new java.io.InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                org.json.JSONObject json = new org.json.JSONObject(response.toString());
                String displayName = json.optString("display_name", "Address not found");

                runOnUiThread(() -> locationName.setText(displayName));

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> locationCoors.setText("Error fetching address"));
            }
        }).start();
    }

    public static boolean isInternetAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        Network network = cm.getActiveNetwork();
        if (network == null) return false;

        NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
        return capabilities != null &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, 1);
        } else {
            // Permissions already granted — proceed
            initializeMapAndLocation();
        }
    }

    private void initializeMapAndLocation() {
        getCurrentLocation();

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        MapController mapController = (MapController) map.getController();
        mapController.setZoom(15.0);

        MyLocationNewOverlay locationOverlay = new MyLocationNewOverlay(
                new GpsMyLocationProvider(this), map);
        locationOverlay.enableMyLocation();
        locationOverlay.setDrawAccuracyEnabled(false);
        locationOverlay.enableFollowLocation();
        map.getOverlays().add(locationOverlay);

        locationCoors.setText("Lat: " + latitude + ",\nLng: " + longitude);
        fetchAddressFromCoordinates(latitude, longitude);
    }











    private void checkPermissions() {
        List<String> permissions = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED)
            permissions.add(Manifest.permission.BODY_SENSORS);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED)
            permissions.add(Manifest.permission.ACTIVITY_RECOGNITION);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), 100);
        }
    }


}