package com.example.neighborguard.ui.settings;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.neighborguard.R;
import com.example.neighborguard.api.ApiController;
import com.example.neighborguard.api.UserApi;
import com.example.neighborguard.databinding.FragmentSettingsBinding;
import com.example.neighborguard.enums.MeetingAssistanceStatusEnum;
import com.example.neighborguard.model.Address;
import com.example.neighborguard.model.CurrentUserManager;
import com.example.neighborguard.model.LonLat;
import com.example.neighborguard.model.User;
import com.example.neighborguard.enums.UserRoleEnum;
import com.example.neighborguard.ui.LogInActivity;
import com.example.neighborguard.utils.DialogUtils;
import com.example.neighborguard.utils.LocationManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsFragment extends Fragment{
    private UserApi userApi;
    private FragmentSettingsBinding binding;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private CurrentUserManager currentUserManager;

    private User user;

    private String firstName;
    private String lastName;
    private String phoneNumber;

    private ArrayList<String> languages;
    private String[] languagesArray;
    private boolean[] selectedLanguages;
    private ArrayList<Integer> languagesList;
    private ArrayList<String> selectedLanguagesList;

    private ArrayList<String> services;
    private String[] servicesArray;
    private boolean[] selectedServices;
    private ArrayList<Integer> servicesList;
    private HashMap<String, MeetingAssistanceStatusEnum> selectedServicesList;

    private Address address;
    private String city;
    private String street;
    private int houseNumber;
    private int apartmentNumber;

    private LonLat lonLat;

    private final int CAM_REQ = 1000;
    private final int IMG_REQ = 2000;
    private String base64ProfileImage;
    private static final int PERMISSION_REQUEST_CODE = 3000;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUserManager = CurrentUserManager.getInstance();
        userApi = ApiController.getRetrofitInstance().create(UserApi.class);
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        if(firebaseUser == null){
            switchToLoginActivity();
            return;
        }

        checkAndRequestPermissions();

        initViews();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    private void initViews() {
        binding.settingsBTNLogout.setOnClickListener(View -> logoutClicked());
        binding.settingsBTNSave.setOnClickListener(View -> saveClicked());

        binding.settingsCRDCamera.setOnClickListener(View -> cameraClicked());
        binding.settingsCRDPhotos.setOnClickListener(View -> photosClicked());

        initHintsFromCurrentUser();
        initLanguages();
        initServices();
    }


    private void photosClicked() {
        if (getActivity() == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (getActivity().checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                checkAndRequestPermissions();
                return;
            }
        } else if (getActivity().checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            checkAndRequestPermissions();
            return;
        }

        Intent photoIntent = new Intent(Intent.ACTION_PICK);
        photoIntent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(photoIntent, IMG_REQ);
    }


    private void cameraClicked() {
        if (getActivity() == null) return;

        if (getActivity().checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            checkAndRequestPermissions();
            return;
        }

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAM_REQ);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(getActivity() == null) return;

        if(resultCode == Activity.RESULT_OK && data != null) {
            if(requestCode == CAM_REQ) {
                try {
                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    if(bitmap != null) {
                        base64ProfileImage = convertBitmapToBase64(bitmap);
                        Glide.with(requireContext())
                                .load(bitmap)
                                .circleCrop()
                                .into(binding.settingIMGProfile);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Failed to capture image", Toast.LENGTH_SHORT).show();
                }
            }
            else if(requestCode == IMG_REQ) {
                try {
                    Uri imageUri = data.getData();
                    if(imageUri != null) {
                        base64ProfileImage = convertUriToBase64(imageUri);
                        Glide.with(requireContext())
                                .load(imageUri)
                                .circleCrop()
                                .into(binding.settingIMGProfile);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private void checkAndRequestPermissions() {
        if (getActivity() == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 and above
            if (getActivity().checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_CODE);
            }
        } else {
            // Below Android 13
            if (getActivity().checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        }

        if (getActivity().checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
        }
    }


    private String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }


    private String convertUriToBase64(Uri imageUri) {
        try {
            if (getActivity() == null) return null;
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
            return convertBitmapToBase64(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private void initHintsFromCurrentUser() {
        base64ProfileImage = currentUserManager.getUser().getProfileImage();

        if (base64ProfileImage != null && !base64ProfileImage.isEmpty()) {
            Glide.with(requireContext())
                    .load(Base64.decode(base64ProfileImage, Base64.DEFAULT))
                    .placeholder(R.drawable.ic_profile_image_24)
                    .error(R.drawable.ic_profile_image_24)
                    .circleCrop()
                    .into(binding.settingIMGProfile);
        }

        binding.settingsEDTFirstName.setText(currentUserManager.getUser().getFirstName());
        binding.settingsEDTLastName.setText(String.valueOf(currentUserManager.getUser().getLastName()));
        binding.settingsEDTPhoneNumber.setText(String.valueOf(currentUserManager.getUser().getPhoneNumber()));
        binding.settingsLBLSelectLanguages.setText(String.valueOf(currentUserManager.getUser().getLanguages()));

        // Handle services display
        if (currentUserManager.getUser().getRole() == UserRoleEnum.RECIPIENT) {
            binding.settingsLBLServices.setVisibility(View.GONE);
            binding.settingsLBLSelectServices.setVisibility(View.GONE);
        } else {
            // For volunteers, show currently provided services
            StringBuilder servicesText = new StringBuilder();
            HashMap<String, MeetingAssistanceStatusEnum> userServices =
                    currentUserManager.getUser().getServices();

            // We'll only show services that are PROVIDE, but skip showing General Check
            boolean isFirst = true;  // Track if this is the first service to handle commas
            for (Map.Entry<String, MeetingAssistanceStatusEnum> entry : userServices.entrySet()) {
                if (!entry.getKey().equals("General Check") &&
                        entry.getValue() == MeetingAssistanceStatusEnum.PROVIDE) {
                    if (!isFirst) {
                        servicesText.append(", ");
                    }
                    servicesText.append(entry.getKey());
                    isFirst = false;
                }
            }

            binding.settingsLBLSelectServices.setText(
                    servicesText.length() > 0 ? servicesText.toString() : "Select Services"
            );
        }

        binding.settingsEDTCity.setText(currentUserManager.getUser().getAddress().getCity());
        binding.settingsEDTStreet.setText(currentUserManager.getUser().getAddress().getStreet());
        binding.settingsEDTHouseNumber.setText(String.valueOf(currentUserManager.getUser().getAddress().getHouseNumber()));
        binding.settingsEDTApartmentNumber.setText(String.valueOf(currentUserManager.getUser().getAddress().getApartmentNumber()));
    }


    private void initLanguages() {
        languages = new ArrayList<>();
        languages.add("Hebrew");
        languages.add("English");
        languages.add("French");
        languages.add("Spanish");
        languages.add("German");
        languages.add("Russian");
        languages.add("Arabic");
        languages.add("Portuguese");

        languagesArray = languages.toArray(new String[0]);
        selectedLanguages = new boolean[languagesArray.length];
        languagesList = new ArrayList<>();
        selectedLanguagesList = new ArrayList<>();

        binding.settingsLBLSelectLanguages.setOnClickListener(v ->
                DialogUtils.showMultiChoiceDialog(
                        getContext(),
                        "Select Languages",
                        languagesArray,
                        selectedLanguages,
                        languagesList,
                        selectedLanguagesList,
                        binding.settingsLBLSelectLanguages,
                        "Select Languages"
                )
        );
    }


    private void initServices() {
        // Only initialize services for volunteers
        if (currentUserManager.getUser().getRole() != UserRoleEnum.VOLUNTEER) {
            return;
        }

        services = new ArrayList<>();
        services.add("Handyman");
        services.add("Dog Walker");
        services.add("Groceries Shopping");

        servicesArray = services.toArray(new String[0]);
        selectedServices = new boolean[servicesArray.length];
        servicesList = new ArrayList<>();
        selectedServicesList = new HashMap<>();

        // Get current services from user
        HashMap<String, MeetingAssistanceStatusEnum> currentServices =
                currentUserManager.getUser().getServices();

        // Initialize selection state from current services
        for (int i = 0; i < services.size(); i++) {
            String service = services.get(i);
            if (currentServices.containsKey(service) &&
                    currentServices.get(service) == MeetingAssistanceStatusEnum.PROVIDE) {
                selectedServices[i] = true;
                servicesList.add(i);
            }
        }

        // Initialize selectedServicesList with current values
        selectedServicesList.putAll(currentServices);

        binding.settingsLBLSelectServices.setOnClickListener(v ->
                DialogUtils.showServicesMultiChoiceDialog(
                        getContext(),
                        "Select Services",
                        servicesArray,
                        selectedServices,
                        servicesList,
                        selectedServicesList,
                        binding.settingsLBLSelectServices,
                        "Select Services",
                        UserRoleEnum.VOLUNTEER,
                        currentServices
                )
        );
    }


    private void saveClicked() {
        user = currentUserManager.getUser();
        address = user.getAddress();
        lonLat = new LonLat();
        boolean isAddressChanged = false;
        boolean isChanged = false;

        // Profile Image
        if (base64ProfileImage != null && !base64ProfileImage.equals(user.getProfileImage())) {
            user.setProfileImage(base64ProfileImage);
            isChanged = true;
            Toast.makeText(getActivity(), "Profile image updated", Toast.LENGTH_SHORT).show();
        }

        // First Name
        firstName = String.valueOf(binding.settingsEDTFirstName.getText());
        if (!TextUtils.isEmpty(firstName) && !firstName.equals(user.getFirstName())) {
            user.setFirstName(this.firstName);
            isChanged = true;
            Toast.makeText(getActivity(), "First Name updated", Toast.LENGTH_SHORT).show();
        }

        // Last Name
        lastName = String.valueOf(binding.settingsEDTLastName.getText());
        if (!TextUtils.isEmpty(lastName) && !lastName.equals(user.getLastName())) {
            user.setLastName(this.lastName);
            isChanged = true;
            Toast.makeText(getActivity(), "Last Name updated", Toast.LENGTH_SHORT).show();
        }

        // Phone Number
        phoneNumber = String.valueOf(binding.settingsEDTPhoneNumber.getText());
        if (!TextUtils.isEmpty(phoneNumber) && !phoneNumber.equals(user.getPhoneNumber())) {
            user.setPhoneNumber(this.phoneNumber);
            isChanged = true;
            Toast.makeText(getActivity(), "Phone number updated", Toast.LENGTH_SHORT).show();
        }

        // Languages
        if (!selectedLanguagesList.isEmpty() && !selectedLanguagesList.equals(user.getLanguages())) {
            user.setLanguages(selectedLanguagesList);
            isChanged = true;
            Toast.makeText(getActivity(), "Languages updated", Toast.LENGTH_SHORT).show();
        }

        // Services
        if (currentUserManager.getUser().getRole() == UserRoleEnum.VOLUNTEER) {
            HashMap<String, MeetingAssistanceStatusEnum> currentServices = user.getServices();
            HashMap<String, MeetingAssistanceStatusEnum> updatedServices = new HashMap<>();

            // Always ensure General Check is PROVIDE for volunteers
            updatedServices.put("General Check", MeetingAssistanceStatusEnum.PROVIDE);

            // Update other services based on selection
            for (String service : services) {
                if (!service.equals("General Check")) {
                    boolean isSelected = selectedServices[services.indexOf(service)];
                    updatedServices.put(service,
                            isSelected ? MeetingAssistanceStatusEnum.PROVIDE :
                                    MeetingAssistanceStatusEnum.DO_NOT_PROVIDE);
                }
            }

            // Check if services have actually changed
            if (!areServicesEqual(currentServices, updatedServices)) {
                user.setServices(updatedServices);
                isChanged = true;
                Toast.makeText(getActivity(), "Services updated", Toast.LENGTH_SHORT).show();
            }
        }

        // City
        city = String.valueOf(binding.settingsEDTCity.getText());
        if (!TextUtils.isEmpty(city) && !city.equals(address.getCity())) {
            address.setCity(this.city);
            isAddressChanged = true;
            isChanged = true;
            Toast.makeText(getActivity(), "City updated", Toast.LENGTH_SHORT).show();
        }

        // Street
        street = String.valueOf(binding.settingsEDTStreet.getText());
        if (!TextUtils.isEmpty(street) && !street.equals(address.getStreet())) {
            address.setStreet(this.street);
            isAddressChanged = true;
            isChanged = true;
            Toast.makeText(getActivity(), "Street updated", Toast.LENGTH_SHORT).show();
        }

        // House Number
        String houseNumberStr = String.valueOf(binding.settingsEDTHouseNumber.getText());
        if (!TextUtils.isEmpty(houseNumberStr)) {
            houseNumber = Integer.parseInt(houseNumberStr);
            if (houseNumber != address.getHouseNumber()) {
                address.setHouseNumber(this.houseNumber);
                isAddressChanged = true;
                isChanged = true;
                Toast.makeText(getActivity(), "House Number updated", Toast.LENGTH_SHORT).show();
            }
        }

        // Apartment Number
        String apartmentNumberStr = String.valueOf(binding.settingsEDTApartmentNumber.getText());
        if (!TextUtils.isEmpty(apartmentNumberStr)) {
            apartmentNumber = Integer.parseInt(apartmentNumberStr);
            if (apartmentNumber != address.getApartmentNumber()) {
                address.setApartmentNumber(this.apartmentNumber);
                isAddressChanged = true;
                isChanged = true;
                Toast.makeText(getActivity(), "Apartment Number updated", Toast.LENGTH_SHORT).show();
            }
        }

        if(isAddressChanged){
            address.makeAddressString(city, street, houseNumber);
            user.setAddress(address);
        }

        // Call updateUserInBackend if any changes were made
        if (isChanged) {
            updateUserInBackend(user);
        } else {
            Toast.makeText(getActivity(), "No changes made", Toast.LENGTH_SHORT).show();
        }
    }


    private void updateUserInBackend(User user) {
        // Show progress bar while updating
        binding.settingsPBProgressBar.setVisibility(View.VISIBLE);

        // Use the User's uid directly.
        String uid = user.getUid();
        int i = 0;

        // Make API call
        Call<Void> call = userApi.updateUser(uid, user);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // Hide progress bar
                binding.settingsPBProgressBar.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    // Update was successful
                    Toast.makeText(getActivity(), "Profile updated successfully", Toast.LENGTH_SHORT).show();

                    // Update the current user in CurrentUserManager
                    currentUserManager.setUser(user);
                } else {
                    // Handle error response
                    Toast.makeText(getActivity(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Hide progress bar
                binding.settingsPBProgressBar.setVisibility(View.GONE);

                // Handle network/other errors
                Toast.makeText(getActivity(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Retrofit", "Network error or failure: " + t.getMessage());
            }
        });

    }


    private void logoutClicked() {
        LocationManager locationManager = LocationManager.getInstance();
        locationManager.setLocationUpdateCallback(null);
        locationManager.stopLocationUpdates();

        FirebaseAuth.getInstance().signOut();
        switchToLoginActivity();
    }


    private void switchToLoginActivity() {
        Intent intent = new Intent(this.getActivity() , LogInActivity.class);
        startActivity(intent);

        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private boolean areServicesEqual(HashMap<String, MeetingAssistanceStatusEnum> map1,
                                     HashMap<String, MeetingAssistanceStatusEnum> map2) {
        if (map1.size() != map2.size()) {
            return false;
        }

        for (Map.Entry<String, MeetingAssistanceStatusEnum> entry : map1.entrySet()) {
            String key = entry.getKey();
            if (!map2.containsKey(key) || map2.get(key) != entry.getValue()) {
                return false;
            }
        }

        return true;
    }
}
