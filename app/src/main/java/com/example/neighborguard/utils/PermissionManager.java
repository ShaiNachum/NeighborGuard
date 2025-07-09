package com.example.neighborguard.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.neighborguard.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class to handle permission requests and related UI
 */
public class PermissionManager {
    private static final String TAG = "PermissionManager";

    // Permission request codes
    public static final int PERMISSIONS_REQUEST_CODE = 100;

    // SharedPreferences key for tracking first-time permission requests
    private static final String PREFS_NAME = "PermissionPrefs";
    private static final String KEY_PERMISSION_REQUESTED = "permissionRequested";

    // Required permissions (updated based on Android version)
    private static String[] getRequiredPermissions() {
        List<String> permissions = new ArrayList<>();

        // Location permissions (always needed)
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        // Camera permission
        permissions.add(Manifest.permission.CAMERA);

        // Storage permissions based on Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        return permissions.toArray(new String[0]);
    }

    /**
     * Check if all required permissions are granted
     */
    public static boolean hasAllPermissions(Context context) {
        for (String permission : getRequiredPermissions()) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Request all required permissions
     */
    public static void requestPermissions(Activity activity) {
        String[] permissions = getMissingPermissions(activity);

        if (permissions.length > 0) {
            // Mark that we've requested permissions
            setPermissionRequested(activity, true);

            // Request the permissions
            ActivityCompat.requestPermissions(activity, permissions, PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Get list of permissions that haven't been granted yet
     */
    public static String[] getMissingPermissions(Context context) {
        List<String> missingPermissions = new ArrayList<>();

        for (String permission : getRequiredPermissions()) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }

        return missingPermissions.toArray(new String[0]);
    }

    /**
     * Show explanation dialog for permissions
     */
    public static void showPermissionExplanationDialog(Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.permission_required_title)
                .setMessage(R.string.permission_required_message)
                .setPositiveButton(R.string.grant_permission, (dialog, which) -> {
                    requestPermissions(activity);
                })
                .setNegativeButton(R.string.not_now, null)
                .create()
                .show();
    }

    /**
     * Show settings dialog when permissions are denied
     */
    public static void showSettingsDialog(Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.permission_denied_title)
                .setMessage(R.string.permission_denied_message)
                .setPositiveButton(R.string.go_to_settings, (dialog, which) -> {
                    // Open app settings
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                    intent.setData(uri);
                    activity.startActivity(intent);
                })
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show();
    }

    /**
     * Check if permission rationale should be shown
     */
    public static boolean shouldShowRationale(Activity activity) {
        for (String permission : getMissingPermissions(activity)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if we've already requested permissions before
     */
    public static boolean isFirstRequest(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return !prefs.getBoolean(KEY_PERMISSION_REQUESTED, false);
    }

    /**
     * Mark that we've requested permissions
     */
    private static void setPermissionRequested(Context context, boolean requested) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_PERMISSION_REQUESTED, requested);
        editor.apply();
    }

    /**
     * Handle permission results
     */
    public static boolean handlePermissionResult(Activity activity, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            // Check if all permissions are granted
            boolean allGranted = true;

            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (!allGranted) {
                // If permissions denied but we can show rationale, show explanation dialog
                if (shouldShowRationale(activity)) {
                    showPermissionExplanationDialog(activity);
                } else if (!isFirstRequest(activity)) {
                    // If permissions permanently denied, suggest settings
                    showSettingsDialog(activity);
                }
            }

            return allGranted;
        }

        return false;
    }
}