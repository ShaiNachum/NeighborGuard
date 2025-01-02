package com.example.neighborguard.ui.home;

import android.os.Bundle;
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
import com.example.neighborguard.databinding.FragmentHomeBinding;
import com.example.neighborguard.interfaces.Callback_recipient;
import com.example.neighborguard.model.CurrentUserManager;
import com.example.neighborguard.model.User;
import com.example.neighborguard.enums.UserAssistanceStatusEnum;
import com.example.neighborguard.enums.UserRoleEnum;
import com.example.neighborguard.utils.DialogUtils;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HomeFragment extends Fragment {
    private UserApi userApi;
    private FragmentHomeBinding binding;
    private CurrentUserManager currentUserManager;

    private ListFragment listFragment;

    private ArrayList<String> services;
    private String[] servicesArray;
    private boolean[] selectedServices;
    private ArrayList<Integer> servicesList;
    private ArrayList<String> selectedServicesList;

    private MapFragment mapFragment;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        currentUserManager = CurrentUserManager.getInstance();
        userApi = ApiController.getRetrofitInstance().create(UserApi.class);

        setUserUIByRole(currentUserManager.getUser().getRole());

        initViews(currentUserManager.getUser().getRole());
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
                    mapFragment.zoop(lat,lng);
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

            binding.homeRecipientBTNPost.setOnClickListener(View -> postClicked());
            initServices();
        }
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

        binding.homeRecipientLBLSelectServices.setOnClickListener(v ->
                DialogUtils.showMultiChoiceDialog(
                        getContext(),
                        "Select Services",
                        servicesArray,
                        selectedServices,
                        servicesList,
                        selectedServicesList,
                        binding.homeRecipientLBLSelectServices,
                        "Select Services"
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

        user.setServices(selectedServicesList);

        if (selectedServicesList.isEmpty() && user.getAssistanceStatus() == UserAssistanceStatusEnum.DO_NOT_NEED_ASSISTANCE) {
            user.setAssistanceStatus(UserAssistanceStatusEnum.DO_NOT_NEED_ASSISTANCE);
        } else {
            user.setAssistanceStatus(UserAssistanceStatusEnum.NEED_ASSISTANCE);
        }

        updateUserInBackend(user, binding.homeRecipientPBProgressBar, selectedServicesList.isEmpty() ? "Services cleared successfully" : "Services updated successfully");
    }


    private void iAmOkClicked() {
        User user = currentUserManager.getUser();

        // Set the current time in seconds since epoch
        user.setLastOK(System.currentTimeMillis() / 1000L);

        // Only set DO_NOT_NEED_ASSISTANCE if there are no active services
        if (user.getServices().isEmpty()) {
            user.setAssistanceStatus(UserAssistanceStatusEnum.DO_NOT_NEED_ASSISTANCE);
        }

        updateUserInBackend(user, binding.homePBProgressBar, "I am OK - Updated");
    }


    private void setUserUIByRole(UserRoleEnum role) {
        if(role == UserRoleEnum.VOLUNTEER){
            binding.homeRecipientFLUpperPart.setVisibility(View.GONE);
            binding.homeRecipientBTNIAmOk.setVisibility(View.GONE);
            binding.homeRecipientLBLNextMeetings.setVisibility(View.GONE);
            binding.homeRecipientFRAMENextMeetingsList.setVisibility(View.GONE);
            binding.homeRecipientLLServices.setVisibility(View.GONE);
            binding.homeRecipientBTNPost.setVisibility(View.GONE);
            binding.homeVolunteerFRAMEList.setVisibility(View.VISIBLE);
            binding.homeVolunteerFRAMEMap.setVisibility(View.VISIBLE);
        }
        else{//is Recipient
            binding.homeRecipientFLUpperPart.setVisibility(View.VISIBLE);
            binding.homeRecipientBTNIAmOk.setVisibility(View.VISIBLE);
            binding.homeRecipientLBLNextMeetings.setVisibility(View.VISIBLE);
            binding.homeRecipientFRAMENextMeetingsList.setVisibility(View.VISIBLE);
            binding.homeRecipientLLServices.setVisibility(View.VISIBLE);
            if(currentUserManager.getUser().getServices().isEmpty())
                binding.homeRecipientLBLSelectServices.setText("Select Services");
            else
                binding.homeRecipientLBLSelectServices.setText(String.valueOf(currentUserManager.getUser().getServices()));
            binding.homeRecipientBTNPost.setVisibility(View.VISIBLE);
            binding.homeVolunteerFRAMEList.setVisibility(View.GONE);
            binding.homeVolunteerFRAMEMap.setVisibility(View.GONE);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}