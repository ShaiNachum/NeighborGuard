package com.example.neighborguard.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.neighborguard.model.LonLat;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;


public class LocationManager {
    private static final String TAG = "LocationManager";
    private static final int REQUEST_CHECK_SETTINGS = 1001;

    // Singleton instance
    private static LocationManager instance;

    // Location components
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    // Location data
    private Location lastLocation;
    private boolean isTracking = false;

    // Callback interface for location updates
    public interface LocationUpdateCallback {
        void onLocationUpdate(LonLat location);
    }

    private LocationUpdateCallback callback;

    // Private constructor for singleton pattern
    private LocationManager() {
    }

    // Get singleton instance
    public static LocationManager getInstance() {
        if (instance == null) {
            instance = new LocationManager();
        }
        return instance;
    }

    // Initialize the location manager
    public void init(Context context) {
        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

            // Configure location request settings for accuracy and frequency
            locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                    .setWaitForAccurateLocation(false)
                    .setMinUpdateIntervalMillis(5000)
                    .setMaxUpdateDelayMillis(15000)
                    .build();

            // Define location callback to handle received locations
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    for (Location location : locationResult.getLocations()) {
                        lastLocation = location;

                        if (callback != null) {
                            LonLat lonLat = new LonLat();
                            lonLat.setLongitude(location.getLongitude());
                            lonLat.setLatitude(location.getLatitude());
                            callback.onLocationUpdate(lonLat);
                        }
                    }
                }
            };
        }
    }

    // Check if location permissions are granted
    public boolean hasLocationPermissions(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED;
    }

    // Request location settings to be enabled
    public void checkLocationSettings(Activity activity) {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(activity);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(activity, locationSettingsResponse -> {
            // Location settings are satisfied, start location updates
            startLocationUpdates(activity);
        });

        task.addOnFailureListener(activity, e -> {
            if (e instanceof ResolvableApiException) {
                // Location settings are not satisfied, show the user a dialog
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult()
                    resolvable.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignore the error.
                    Log.e(TAG, "Error showing location settings dialog", sendEx);
                }
            }
        });
    }

    // Get the last known location
    public void getLastLocation(Context context, LocationUpdateCallback callback) {
        // Initialize if not already done
        if (fusedLocationClient == null) {
            init(context);
        }

        if (!hasLocationPermissions(context)) {
            Log.e(TAG, "Location permissions not granted");
            return;
        }

        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    lastLocation = location;

                    LonLat lonLat = new LonLat();
                    lonLat.setLongitude(location.getLongitude());
                    lonLat.setLatitude(location.getLatitude());
                    callback.onLocationUpdate(lonLat);
                } else {
                    Log.d(TAG, "Last location is null, requesting updates");
                    this.callback = callback;
                    startLocationUpdates(context);
                }
            });
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception when getting location", e);
        }
    }

    // Start receiving location updates
    public void startLocationUpdates(Context context) {
        if (!hasLocationPermissions(context)) {
            Log.e(TAG, "Location permissions not granted");
            return;
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
            );
            isTracking = true;
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception when requesting location updates", e);
        }
    }

    // Stop receiving location updates
    public void stopLocationUpdates() {
        if (fusedLocationClient != null && isTracking) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            isTracking = false;
        }
    }

    // Set callback for location updates
    public void setLocationUpdateCallback(LocationUpdateCallback callback) {
        this.callback = callback;
    }

    // Convert Location to LonLat model
    public LonLat locationToLonLat(Location location) {
        if (location == null) return null;

        LonLat lonLat = new LonLat();
        lonLat.setLongitude(location.getLongitude());
        lonLat.setLatitude(location.getLatitude());
        return lonLat;
    }

    // Get current location as LonLat
    public LonLat getCurrentLonLat() {
        if (lastLocation == null) return null;
        return locationToLonLat(lastLocation);
    }
}