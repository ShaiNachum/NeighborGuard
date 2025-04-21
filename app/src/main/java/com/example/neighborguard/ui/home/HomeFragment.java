package com.example.neighborguard.ui.home;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.neighborguard.adapters.MeetingAdapter;
import com.example.neighborguard.api.ApiController;
import com.example.neighborguard.api.MeetingApi;
import com.example.neighborguard.api.UserApi;
import com.example.neighborguard.databinding.FragmentHomeBinding;
import com.example.neighborguard.enums.MeetingAssistanceStatusEnum;
import com.example.neighborguard.enums.MeetingStatusEnum;
import com.example.neighborguard.interfaces.Callback_recipient;
import com.example.neighborguard.model.CurrentUserManager;
import com.example.neighborguard.model.Meeting;
import com.example.neighborguard.model.SearchMeetingsResponseSchema;
import com.example.neighborguard.model.User;
import com.example.neighborguard.enums.UserRoleEnum;
import com.example.neighborguard.utils.DialogUtils;
import com.example.neighborguard.utils.LocationManager;
import com.example.neighborguard.utils.PermissionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HomeFragment extends Fragment {
    private UserApi userApi;
    private MeetingApi meetingApi;
    private FragmentHomeBinding binding;
    private CurrentUserManager currentUserManager;

    private ListFragment listFragment;
    private MapFragment mapFragment;

    private ArrayList<String> services;
    private String[] servicesArray;
    private boolean[] selectedServices;
    private ArrayList<Integer> servicesList;
    private HashMap<String, MeetingAssistanceStatusEnum> selectedServicesList;

    private MeetingAdapter meetingAdapter;
    private ArrayList<Meeting> meetings;

    private LocationManager locationManager;
    private static final String TAG = "HomeFragment";



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        currentUserManager = CurrentUserManager.getInstance();

        // Initialize location manager
        locationManager = LocationManager.getInstance();

        meetings = new ArrayList<>();

        userApi = ApiController.getRetrofitInstance().create(UserApi.class);
        meetingApi = ApiController.getRetrofitInstance().create(MeetingApi.class);

        setUserUIByRole(currentUserManager.getUser().getRole());

        initViews(currentUserManager.getUser().getRole());

        // Only update volunteer location if permissions are granted
        if (currentUserManager.getUser().getRole() == UserRoleEnum.VOLUNTEER) {
            if (PermissionManager.hasAllPermissions(getActivity())) {
                // Initialize location manager first
                locationManager.init(getActivity());
                // Then update location
                updateVolunteerLocation();
            } else {
                // Don't try to update location yet - wait for permission grant
                Log.d(TAG, "Location permissions not granted yet");
            }
        }
    }

    private void updateVolunteerLocation() {
        if (!isAdded() || getActivity() == null) return;

        // Only update location for volunteers
        if (currentUserManager.getUser().getRole() != UserRoleEnum.VOLUNTEER) return;

        Log.d(TAG, "Updating volunteer location");

        // Make sure locationManager is not null
        if (locationManager == null) {
            locationManager = LocationManager.getInstance();
            locationManager.init(getActivity());
        }

        // Set location update callback only if locationManager is initialized
        if (locationManager != null) {
            locationManager.setLocationUpdateCallback(location -> {
                if (location == null) return;

                // Update current user's location
                User user = currentUserManager.getUser();
                user.setLonLat(location);

                // Update user in backend
                updateUserInBackend(user, binding.homePBProgressBar, "Location updated");

                // Update map with current location
                if (mapFragment != null) {
                    mapFragment.updateMyLocation(location.getLatitude(), location.getLongitude());
                }
            });

            // Get last location and start updates - add null checks
            locationManager.getLastLocation(getActivity(), location -> {
                if (location == null) return;

                Log.d(TAG, "Got last location: " + location.getLatitude() + ", " + location.getLongitude());

                // Update current user's location
                User user = currentUserManager.getUser();
                user.setLonLat(location);

                // Update user in backend
                updateUserInBackend(user, binding.homePBProgressBar, "Location updated");

                // Update map with current location
                if (mapFragment != null) {
                    mapFragment.updateMyLocation(location.getLatitude(), location.getLongitude());
                }
            });
        }
    }


    private void initViews(UserRoleEnum role) {
        if(role == UserRoleEnum.VOLUNTEER){
            //list:
            listFragment = new ListFragment();
            getParentFragmentManager().beginTransaction()
                    .add(binding.homeVolunteerFRAMEList.getId(), listFragment)
                    .commit();

            listFragment.setCallbackRecipientClicked(new Callback_recipient() {
                @Override
                public void recipientClicked(double lat, double lng) {
                    mapFragment.zoom(lat,lng);
                }

                @Override
                public void updateMapRecipients(ArrayList<User> recipients) {
                    // Update the map with the recipients
                    if (mapFragment != null) {
                        mapFragment.updateRecipients(recipients);
                    }
                }
            });


            //map:
            mapFragment = new MapFragment();
            getParentFragmentManager().beginTransaction()
                    .add(binding.homeVolunteerFRAMEMap.getId(), mapFragment)
                    .commit();

        }
        else {//is Recipient
            binding.homeRecipientBTNIAmOk.setOnClickListener(View -> iAmOkClicked());

            fetchMeetings();
            meetingAdapter = new MeetingAdapter(getContext(), meetings);
            binding.homeRecipientFRAMENextMeetingsList.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.homeRecipientFRAMENextMeetingsList.setAdapter(meetingAdapter);

            binding.homeRecipientBTNPost.setOnClickListener(View -> postClicked());
            initServices();
        }
    }

    private void fetchMeetings() {
        String userId = currentUserManager.getUser().getUid();
        MeetingStatusEnum status = MeetingStatusEnum.IS_PICKED;

        Call<SearchMeetingsResponseSchema> call = meetingApi.getMeetings(
                userId,
                status.toString()
        );

        call.enqueue(new Callback<SearchMeetingsResponseSchema>() {
            @Override
            public void onResponse(
                    Call<SearchMeetingsResponseSchema> call,
                    Response<SearchMeetingsResponseSchema> response)
            {
                // Clear the meetings list first
                meetings.clear();
                meetingAdapter.notifyDataSetChanged();  // Tell adapter the data changed

                if (response.isSuccessful() && response.body() != null) {
                    meetings.clear();
                    ArrayList<Meeting> receivedMeetings  = response.body().getMeetings();
                    if (receivedMeetings  != null && !receivedMeetings .isEmpty()) {
                        meetings.addAll(receivedMeetings);
                        meetingAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "No meetings found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to load meetings", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SearchMeetingsResponseSchema> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initServices() {
    // Initialize service collections
    services = new ArrayList<>();
    // Don't add General Check since recipients shouldn't see it
    services.add("Handyman");
    services.add("Dog Walker");
    services.add("Groceries Shopping");

    servicesArray = services.toArray(new String[0]);
    selectedServices = new boolean[servicesArray.length];
    servicesList = new ArrayList<>();
    selectedServicesList = new HashMap<>();

    // Initialize services map with current user's services if they exist
    HashMap<String, MeetingAssistanceStatusEnum> currentServices =
            currentUserManager.getUser().getServices();

    // Pre-select services that are currently marked as NEED_ASSISTANCE
    for (int i = 0; i < services.size(); i++) {
        String service = services.get(i);
        if (currentServices != null &&
                currentServices.containsKey(service) &&
                currentServices.get(service) == MeetingAssistanceStatusEnum.NEED_ASSISTANCE) {
            selectedServices[i] = true;
            servicesList.add(i);
        }
    }

    // Set up dialog click listener
    binding.homeRecipientLBLSelectServices.setOnClickListener(v ->
            DialogUtils.showServicesMultiChoiceDialog(
                    getContext(),
                    "Select Services",
                    servicesArray,
                    selectedServices,
                    servicesList,
                    selectedServicesList,
                    binding.homeRecipientLBLSelectServices,
                    "Select Services",
                    UserRoleEnum.RECIPIENT,
                    currentServices  // Pass current services for IN_PROGRESS checking
            )
    );
}

    private void updateUserInBackend(User user, View progressBar, String successMessage) {
        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);

        Call<Void> call = userApi.updateUser(user.getUid(), user);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // Hide progress bar
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    Toast.makeText(getActivity(), successMessage, Toast.LENGTH_SHORT).show();
                    currentUserManager.setUser(user);
                } else {
                    Toast.makeText(getActivity(), "Failed to update user", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getActivity(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Retrofit", "Network error or failure: " + t.getMessage());
            }
        });
    }

    private void postClicked() {
        User user = currentUserManager.getUser();
        HashMap<String, MeetingAssistanceStatusEnum> currentServices = user.getServices();
        HashMap<String, MeetingAssistanceStatusEnum> updatedServices = new HashMap<>();

        // Step 1: Keep General Check's current status
        if (currentServices.containsKey("General Check")) {
            updatedServices.put("General Check", currentServices.get("General Check"));
        }

        // Step 2: Go through each service (except General Check) and set its status
        for (String service : services) {
            // Skip General Check as we already handled it
            if (service.equals("General Check")) {
                continue;
            }

            // If service is IN_PROGRESS, keep it that way
            if (currentServices.containsKey(service) &&
                    currentServices.get(service) == MeetingAssistanceStatusEnum.IN_PROGRESS) {
                updatedServices.put(service, MeetingAssistanceStatusEnum.IN_PROGRESS);
            }
            // If service was selected in dialog, set to NEED_ASSISTANCE
            else if (selectedServicesList.containsKey(service) &&
                    selectedServicesList.get(service) == MeetingAssistanceStatusEnum.NEED_ASSISTANCE) {
                updatedServices.put(service, MeetingAssistanceStatusEnum.NEED_ASSISTANCE);
            }
            // Otherwise, set to DO_NOT_NEED_ASSISTANCE
            else {
                updatedServices.put(service, MeetingAssistanceStatusEnum.DO_NOT_NEED_ASSISTANCE);
            }
        }

        user.setServices(updatedServices);
        updateUserInBackend(user, binding.homeRecipientPBProgressBar, "Services updated successfully");
    }

    private void iAmOkClicked() {
        User user = currentUserManager.getUser();
        HashMap<String, MeetingAssistanceStatusEnum> userServices = user.getServices();

        // Set the current time in seconds since epoch
        user.setLastOK(System.currentTimeMillis() / 1000L);

        userServices.put("General Check", MeetingAssistanceStatusEnum.DO_NOT_NEED_ASSISTANCE);

        // Always update backend when I am OK is clicked
        updateUserInBackend(user, binding.homePBProgressBar, "I am OK - Updated");
    }

    private String buildServiceDisplayText(HashMap<String, MeetingAssistanceStatusEnum> services) {
        // Create a string of active services (IN_PROGRESS or NEED_ASSISTANCE), excluding General Check
        StringBuilder text = new StringBuilder();
        boolean isFirst = true;

        for (Map.Entry<String, MeetingAssistanceStatusEnum> entry : services.entrySet()) {
            String service = entry.getKey();
            MeetingAssistanceStatusEnum status = entry.getValue();

            // Skip General Check and only include services that are active
            if (!service.equals("General Check") &&
                    (status == MeetingAssistanceStatusEnum.IN_PROGRESS ||
                            status == MeetingAssistanceStatusEnum.NEED_ASSISTANCE)) {

                if (!isFirst) {
                    text.append(", ");
                }
                text.append(service);
                isFirst = false;
            }
        }

        return text.length() > 0 ? text.toString() : "Select Services";
    }

    private void setUserUIByRole(UserRoleEnum role) {
        if(role == UserRoleEnum.VOLUNTEER){
            binding.homeRecipientFLUpperPart.setVisibility(View.GONE);
            binding.homeRecipientBTNIAmOk.setVisibility(View.GONE);
            binding.homeRecipientLBLNextMeetings.setVisibility(View.GONE);
            binding.homeRecipientFLMiddleSection.setVisibility(View.GONE);
            binding.homeRecipientLLServices.setVisibility(View.GONE);
            binding.homeRecipientBTNPost.setVisibility(View.GONE);
            binding.homeVolunteerFRAMEList.setVisibility(View.VISIBLE);
            binding.homeVolunteerLISTContainer.setVisibility(View.VISIBLE); // Show the container too
            binding.homeVolunteerFRAMEMap.setVisibility(View.VISIBLE);
        }
        else{//is Recipient
            binding.homeRecipientFLUpperPart.setVisibility(View.VISIBLE);
            binding.homeRecipientBTNIAmOk.setVisibility(View.VISIBLE);
            binding.homeRecipientLBLNextMeetings.setVisibility(View.VISIBLE);
            binding.homeRecipientFLMiddleSection.setVisibility(View.VISIBLE);
            binding.homeRecipientLLServices.setVisibility(View.VISIBLE);
            binding.homeRecipientBTNPost.setVisibility(View.VISIBLE);
            binding.homeVolunteerFRAMEList.setVisibility(View.GONE);
            binding.homeVolunteerLISTContainer.setVisibility(View.GONE); // Hide the container too
            binding.homeVolunteerFRAMEMap.setVisibility(View.GONE);
            // Configure the services text display
            HashMap<String, MeetingAssistanceStatusEnum> currentServices =
                    currentUserManager.getUser().getServices();

            // Set the services text, handling empty services case
            String servicesText = currentServices.isEmpty() ?
                    "Select Services" :
                    buildServiceDisplayText(currentServices);

            binding.homeRecipientLBLSelectServices.setText(servicesText);

            // Ensure text stays on one line with ellipsis if needed
            binding.homeRecipientLBLSelectServices.setSingleLine(true);
            binding.homeRecipientLBLSelectServices.setEllipsize(TextUtils.TruncateAt.END);
        }
    }


    @Override
    public void onDestroyView() {
        // Make sure to stop location updates
        if (locationManager != null) {
            // Remove the callback and stop updates
            locationManager.setLocationUpdateCallback(null);
            locationManager.stopLocationUpdates();
        }
        super.onDestroyView();
        binding = null; // Clear binding
    }

    @Override
    public void onDestroy() {
        // Also good to stop here in case onDestroyView isn't called
        if (locationManager != null) {
            locationManager.setLocationUpdateCallback(null);
            locationManager.stopLocationUpdates();
        }
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh meetings list whenever fragment becomes visible
        if (currentUserManager.getUser().getRole() == UserRoleEnum.RECIPIENT) {
            fetchMeetings();
        } else{
            // For volunteers, update location when resuming
            updateVolunteerLocation();
        }
    }
}