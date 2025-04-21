package com.example.neighborguard.ui.home;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.neighborguard.R;
import com.example.neighborguard.databinding.FragmentMapBinding;
import com.example.neighborguard.model.User;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "MapFragment";
    private FragmentMapBinding binding;
    private GoogleMap myMap;
    private LatLng currentLocation;
    private Marker currentLocationMarker;
    private Map<String, Marker> recipientMarkers = new HashMap<>();
    private boolean isMapReady = false;

    // Queue to store recipients that need to be added before map is ready
    private List<User> pendingRecipients = new ArrayList<>();

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);

        // If the map fragment isn't already added, we need to add it
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.map, mapFragment)
                    .commit();
        }

        mapFragment.getMapAsync(this);
    }

    public void zoom(double lat, double lon) {
        if (myMap != null) {
            LatLng location = new LatLng(lat, lon);
            myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f));
        }
    }

    /**
     * Update the current user's location marker
     */
    public void updateMyLocation(double lat, double lon) {
        if (!isAdded()) return;

        currentLocation = new LatLng(lat, lon);

        if (myMap != null) {
            if (currentLocationMarker == null) {
                // Create new marker for current location
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(currentLocation)
                        .title("My Location")
                        .icon(getBitmapDescriptor(R.drawable.ic_my_location));

                currentLocationMarker = myMap.addMarker(markerOptions);

                // Initial zoom to current location
                myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 13f));
            } else {
                // Just update existing marker position
                currentLocationMarker.setPosition(currentLocation);
            }
        } else {
            Log.d(TAG, "Map not ready yet, location update queued");
        }
    }

    /**
     * Add a recipient to the map
     */
    public void addRecipient(User recipient) {
        if (!isAdded()) return;

        if (myMap != null && isMapReady) {
            addRecipientMarker(recipient);
        } else {
            // Queue recipient to be added when map is ready
            pendingRecipients.add(recipient);
            Log.d(TAG, "Map not ready yet, recipient queued");
        }
    }

    /**
     * Add recipients to the map
     */
    public void updateRecipients(List<User> recipients) {
        if (!isAdded()) return;

        // Clear existing markers
        for (Marker marker : recipientMarkers.values()) {
            marker.remove();
        }
        recipientMarkers.clear();

        // Add new markers
        if (myMap != null && isMapReady) {
            for (User recipient : recipients) {
                addRecipientMarker(recipient);
            }
        } else {
            // Queue recipients to be added when map is ready
            pendingRecipients.clear();
            pendingRecipients.addAll(recipients);
            Log.d(TAG, "Map not ready yet, recipients queued");
        }
    }

    /**
     * Helper method to add a recipient marker
     */
    private void addRecipientMarker(User recipient) {
        if (recipient.getLonLat() == null) return;

        LatLng position = new LatLng(
                recipient.getLonLat().getLatitude(),
                recipient.getLonLat().getLongitude()
        );

        // Create marker options
        MarkerOptions markerOptions = new MarkerOptions()
                .position(position)
                .title(recipient.getFirstName() + " " + recipient.getLastName())
                .snippet(getRecipientSnippet(recipient))
                .icon(getBitmapDescriptor(R.drawable.ic_recipient_location));

        // Add marker to map
        Marker marker = myMap.addMarker(markerOptions);

        if (marker != null) {
            // Store in map for future reference
            recipientMarkers.put(recipient.getUid(), marker);
        }
    }

    /**
     * Generate snippet text for recipient marker
     */
    private String getRecipientSnippet(User recipient) {
        StringBuilder snippet = new StringBuilder();

        // Add services that need assistance
        snippet.append("Needs help with: ");
        boolean first = true;

        if (recipient.getServices() != null) {
            for (Map.Entry<String, com.example.neighborguard.enums.MeetingAssistanceStatusEnum> entry :
                    recipient.getServices().entrySet()) {
                if (entry.getValue() == com.example.neighborguard.enums.MeetingAssistanceStatusEnum.NEED_ASSISTANCE) {
                    if (!first) snippet.append(", ");
                    snippet.append(entry.getKey());
                    first = false;
                }
            }
        }

        return snippet.toString();
    }

    /**
     * Convert drawable resource to BitmapDescriptor for map markers
     */
    private BitmapDescriptor getBitmapDescriptor(int resourceId) {
        if (!isAdded()) return BitmapDescriptorFactory.defaultMarker();

        Drawable drawable = ContextCompat.getDrawable(requireContext(), resourceId);
        if (drawable == null) {
            return BitmapDescriptorFactory.defaultMarker();
        }

        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;
        isMapReady = true;

        // Configure map settings
        myMap.getUiSettings().setZoomControlsEnabled(true);
        myMap.getUiSettings().setMyLocationButtonEnabled(true);

        // If we have a current location, update the marker
        if (currentLocation != null) {
            updateMyLocation(currentLocation.latitude, currentLocation.longitude);
        } else {
            // Default location (Tel Aviv) if we don't have user's location yet
            updateMyLocation(32.0853, 34.7818);
        }

        // Process any pending recipients
        if (!pendingRecipients.isEmpty()) {
            for (User recipient : pendingRecipients) {
                addRecipientMarker(recipient);
            }
            pendingRecipients.clear();
        }
    }
}