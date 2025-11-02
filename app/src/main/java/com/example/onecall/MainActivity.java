package com.example.onecall;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.Manifest;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onecall.adaoters.HospitalAdapter;
import com.example.onecall.models.HospitalData;
import com.example.onecall.models.ModelData;
import com.example.onecall.network.ApiService;
import com.example.onecall.network.RetrofitClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
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
    MapView map;
    Button getCurrentlocation;
    MaterialButton sendLocation;
    TextView status;
    double latitude;
    double longitude;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    MaterialButton showHospitals;

    RecyclerView recyclerView;

    private final String api_key = "865ab6d0507c49f1a334dc5d7776e497";

    WebView webView;
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
        locationName = findViewById(R.id.locationName);
        locationCoors = findViewById(R.id.locationCoords);
        status = findViewById(R.id.status);



        getCurrentlocation = findViewById(R.id.getLocation);
        map = findViewById(R.id.map);
        sendLocation = findViewById(R.id.send_location);
        showHospitals = findViewById(R.id.showHospitals);
        webView = findViewById(R.id.webView);


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

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isInternetAvailable(MainActivity.this)) {
                    status.setText("connected ✅");
                }else {
                    status.setText("disconnected 🔴");
                }
            }
        });

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());


        getCurrentlocation.setOnClickListener(view -> checkLocationPermission());


        showHospitals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OkHttpClient client = new OkHttpClient();

                // ✅ Make sure longitude comes first in Geoapify's filter

                String url = "https://api.geoapify.com/v2/places" +
                        "?categories=healthcare.hospital" +
                        "&filter=circle:" + longitude + "," + latitude + ",10000" +  // ✅ Increased radius
                        "&limit=20" +                                                // ✅ Increased limit
                        "&apiKey=" + api_key;

                Log.d("GEOAPIFY_URL", url); // ✅ Log the full URL for testing

                Request request = new Request.Builder()
                        .url(url)
                        .build();

                client.newCall(request).enqueue(new okhttp3.Callback() {
                    @Override
                    public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String jsonData = response.body().string();
                            Log.d("GEOAPIFY_RESPONSE", jsonData); // ✅ Log raw response
                            parseHospitalData(jsonData);
                        }
                    }
                });
            }
        });

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

    private void parseHospitalData(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray features = jsonObject.getJSONArray("features");

            List<HospitalData> hospitals = new ArrayList<>();
            for (int i = 0; i < features.length(); i++) {
                JSONObject feature = features.getJSONObject(i);
                JSONObject properties = feature.getJSONObject("properties");

                String name = properties.optString("name", "Unnamed Hospital");
                String address = properties.optString("address_line1", "") + ", " +
                        properties.optString("address_line2", "");
                double lat = properties.optDouble("lat");
                double lng = properties.optDouble("lon");

                hospitals.add(new HospitalData(name, address, lat, lng));
            }

            runOnUiThread(() -> {
                HospitalAdapter adapter = new HospitalAdapter(hospitals, hospital -> {
                    // Hide MapView and show WebView
                    map.setVisibility(View.GONE);
                    webView.setVisibility(View.VISIBLE);

                    // Load Google Maps directions
                    String mapUrl = "https://www.google.com/maps/dir/?api=1" +
                            "&origin=" + latitude + "," + longitude +
                            "&destination=" + hospital.lat + "," + hospital.lng;

                    webView.loadUrl(mapUrl);
                });

                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                recyclerView.setAdapter(adapter);

                plotHospitalsOnMap(hospitals);
            });


        } catch (JSONException e) {
            e.printStackTrace();
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

        webView.setVisibility(View.GONE);
        map.setVisibility(View.VISIBLE);
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

    private void plotHospitalsOnMap(List<HospitalData> hospitals) {
        map.getOverlays().clear(); // Clear previous overlays

        // Optional: Add user location overlay again if needed
        MyLocationNewOverlay locationOverlay = new MyLocationNewOverlay(
                new GpsMyLocationProvider(this), map);
        locationOverlay.enableMyLocation();
        locationOverlay.enableFollowLocation();
        map.getOverlays().add(locationOverlay);

        for (HospitalData hospital : hospitals) {
            GeoPoint point = new GeoPoint(hospital.lat, hospital.lng);

            Marker marker = new Marker(map);
            marker.setPosition(point);
            marker.setTitle(hospital.name);
            marker.setSubDescription(hospital.address);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

            // Optional: Custom icon
            // marker.setIcon(getResources().getDrawable(R.drawable.hospital_icon));

            // Optional: Click listener
            marker.setOnMarkerClickListener((m, mapView) -> {
                Toast.makeText(this, hospital.name + "\n" + hospital.address, Toast.LENGTH_SHORT).show();
                return true;
            });

            map.getOverlays().add(marker);
        }

        // Center map on first hospital
        if (!hospitals.isEmpty()) {
            map.getController().setCenter(new GeoPoint(hospitals.get(0).lat, hospitals.get(0).lng));
        }

        map.invalidate(); // Refresh map
    }


}