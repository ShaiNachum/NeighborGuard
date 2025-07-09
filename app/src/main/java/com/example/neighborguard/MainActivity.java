package com.example.neighborguard;

import android.os.Bundle;


import com.example.neighborguard.utils.LocationManager;
import com.example.neighborguard.utils.PermissionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.neighborguard.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private NavController navController;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize LocationManager first thing, before inflating layout
        LocationManager locationManager = LocationManager.getInstance();
        locationManager.init(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupNavigation();
        checkPermissions();
    }

    private void checkPermissions() {
        if (!PermissionManager.hasAllPermissions(this)) {
            if (PermissionManager.shouldShowRationale(this)) {
                // Show explanation dialog
                PermissionManager.showPermissionExplanationDialog(this);
            } else if (PermissionManager.isFirstRequest(this)) {
                // First time, request directly
                PermissionManager.requestPermissions(this);
            } else {
                // Already requested before, suggest settings
                PermissionManager.showSettingsDialog(this);
            }
        } else {
            // Permissions are already granted, check location settings
            // Only call this if locationManager is properly initialized
            if (locationManager != null) {
                locationManager.checkLocationSettings(this);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (PermissionManager.handlePermissionResult(this, requestCode, permissions, grantResults)) {
            // Permissions granted, check location settings
            // Add a null check here to prevent the crash
            if (locationManager != null) {
                locationManager.checkLocationSettings(this);
            } else {
                // If locationManager is still null, initialize it
                locationManager = LocationManager.getInstance();
                locationManager.init(this);
                locationManager.checkLocationSettings(this);
            }
        }
    }

    private void setupNavigation() {
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_meetings, R.id.navigation_home, R.id.navigation_settings)
                .build();

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

}