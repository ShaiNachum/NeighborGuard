package com.example.neighborguard.ui.settings;

import android.content.Intent;
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

import com.example.neighborguard.api.ApiController;
import com.example.neighborguard.api.UserApi;
import com.example.neighborguard.databinding.FragmentSettingsBinding;
import com.example.neighborguard.model.Address;
import com.example.neighborguard.model.CurrentUserManager;
import com.example.neighborguard.model.LonLat;
import com.example.neighborguard.model.User;
import com.example.neighborguard.ui.LogInActivity;
import com.example.neighborguard.utils.DialogUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

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
    private int phoneNumber;

    private ArrayList<String> languages;
    private String[] languagesArray;
    private boolean[] selectedLanguages;
    private ArrayList<Integer> languagesList;
    private ArrayList<String> selectedLanguagesList;

    private ArrayList<String> services;
    private String[] servicesArray;
    private boolean[] selectedServices;
    private ArrayList<Integer> servicesList;
    private ArrayList<String> selectedServicesList;

    private Address address;
    private String city;
    private String street;
    private int houseNumber;
    private int apartmentNumber;

    private LonLat lonLat;


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

        initHintsFromCurrentUser();
        initLanguages();
        initServices();
    }


    private void initHintsFromCurrentUser() {
        binding.settingsEDTFirstName.setText(currentUserManager.getUser().getFirstName());
        binding.settingsEDTLastName.setText(String.valueOf(currentUserManager.getUser().getLastName()));
        binding.settingsEDTPhoneNumber.setText(String.valueOf(currentUserManager.getUser().getPhoneNumber()));
        binding.settingsLBLSelectLanguages.setText(String.valueOf(currentUserManager.getUser().getLanguages()));
        binding.settingsLBLSelectServices.setText(String.valueOf(currentUserManager.getUser().getServices()));
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
        services = new ArrayList<>();
        services.add("Handyman");
        services.add("Dog Walker");
        services.add("Groceries Shopping");

        servicesArray = services.toArray(new String[0]);
        selectedServices = new boolean[servicesArray.length];
        servicesList = new ArrayList<>();
        selectedServicesList = new ArrayList<>();

        binding.settingsLBLSelectServices.setOnClickListener(v ->
                DialogUtils.showMultiChoiceDialog(
                        getContext(),
                        "Select Services",
                        servicesArray,
                        selectedServices,
                        servicesList,
                        selectedServicesList,
                        binding.settingsLBLSelectServices,
                        "Select Services"
                )
        );
    }


    private void saveClicked() {
        user = currentUserManager.getUserFromExtendedUser();
        address = user.getAddress();
        lonLat = new LonLat();
        boolean isAddressChanged = false;

        firstName = String.valueOf(binding.settingsEDTFirstName.getText());
        if (!TextUtils.isEmpty(firstName)) {
            binding.settingsPBProgressBar.setVisibility(View.GONE);

            user.setFirstName(this.firstName);
            Toast.makeText(getActivity(), "First Name updated", Toast.LENGTH_SHORT).show();
        }
        user.setFirstName(this.firstName);

        lastName = String.valueOf(binding.settingsEDTLastName.getText());
        if (!TextUtils.isEmpty(lastName)) {
            binding.settingsPBProgressBar.setVisibility(View.GONE);

            user.setLastName(this.lastName);
            Toast.makeText(getActivity(), "Last Name updated", Toast.LENGTH_SHORT).show();
        }
        user.setLastName(this.lastName);

        phoneNumber = Integer.parseInt(String.valueOf(binding.settingsEDTPhoneNumber.getText()));
        if (!TextUtils.isEmpty(String.valueOf(phoneNumber))) {
            binding.settingsPBProgressBar.setVisibility(View.GONE);

            user.setPhoneNumber(this.phoneNumber);
            Toast.makeText(getActivity(), "Phone number updated", Toast.LENGTH_SHORT).show();
        }

        if (!selectedLanguagesList.isEmpty()) {
            binding.settingsPBProgressBar.setVisibility(View.GONE);

            user.setLanguages(selectedLanguagesList);
            Toast.makeText(getActivity(), "Languages updated", Toast.LENGTH_SHORT).show();
        }

        if (!selectedServicesList.isEmpty()) {
            binding.settingsPBProgressBar.setVisibility(View.GONE);

            user.setServices(selectedServicesList);
            Toast.makeText(getActivity(), "Service updated", Toast.LENGTH_SHORT).show();
        }


        city = String.valueOf(binding.settingsEDTCity.getText());
        if (!TextUtils.isEmpty(city)) {
            binding.settingsPBProgressBar.setVisibility(View.GONE);

            address.setCity(this.city);
            isAddressChanged = true;
            Toast.makeText(getActivity(), "City updated", Toast.LENGTH_SHORT).show();
        }


        street = String.valueOf(binding.settingsEDTStreet.getText());
        if (!TextUtils.isEmpty(street)) {
            binding.settingsPBProgressBar.setVisibility(View.GONE);

            address.setStreet(this.street);
            isAddressChanged = true;
            Toast.makeText(getActivity(), "Street updated", Toast.LENGTH_SHORT).show();
        }


        houseNumber = Integer.parseInt(String.valueOf(binding.settingsEDTHouseNumber.getText()));
        if (!TextUtils.isEmpty(String.valueOf(houseNumber))) {
            binding.settingsPBProgressBar.setVisibility(View.GONE);

            address.setHouseNumber(this.houseNumber);
            isAddressChanged = true;
            Toast.makeText(getActivity(), "Enter House Number", Toast.LENGTH_SHORT).show();
        }


        apartmentNumber = Integer.parseInt(String.valueOf(binding.settingsEDTApartmentNumber.getText()));
        if (!TextUtils.isEmpty(String.valueOf(apartmentNumber))) {
            binding.settingsPBProgressBar.setVisibility(View.GONE);

            address.setApartmentNumber(this.apartmentNumber);
            isAddressChanged = true;
            Toast.makeText(getActivity(), "Enter Apartment Number", Toast.LENGTH_SHORT).show();
        }

        if(isAddressChanged){
            address.makeAddressString(city, street, houseNumber);
            user.setAddress(address);
        }


        updateUserInBackend(user);
    }


    private void updateUserInBackend(User user) {
        // Show progress bar while updating
        binding.settingsPBProgressBar.setVisibility(View.VISIBLE);

        // Use the ExtendedUser's uid directly
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
                    currentUserManager.setExtendedUserFromUser(user);
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
}
