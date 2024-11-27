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
import com.example.neighborguard.model.CurrentUserManager;
import com.example.neighborguard.model.User;
import com.example.neighborguard.model.UserRoleEnum;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HomeFragment extends Fragment {
    private UserApi userApi;
    private FragmentHomeBinding binding;
    private CurrentUserManager currentUserManager;


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

        initViews();
    }

    private void initViews() {
        binding.homeBTNIAmOk.setOnClickListener(View -> iAmOkClicked());
    }


    private void iAmOkClicked() {
        User user = currentUserManager.getUserFromExtendedUser();

        // Set the current time in seconds since epoch
        user.setLastOk(System.currentTimeMillis() / 1000L);

        // Show progress bar while updating
        binding.homePBProgressBar.setVisibility(View.VISIBLE);

        // Make API call
        Call<Void> call = userApi.updateUser(user.getUid(), user);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // Hide progress bar
                binding.homePBProgressBar.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    // Update was successful
                    Toast.makeText(getActivity(), "I am OK - Updated", Toast.LENGTH_SHORT).show();

                    // Update the current user in CurrentUserManager
                    currentUserManager.setExtendedUserFromUser(user);
                } else {
                    // Handle error response
                    Toast.makeText(getActivity(), "Failed to update I am OK status", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Hide progress bar
                binding.homePBProgressBar.setVisibility(View.GONE);

                // Handle network/other errors
                Toast.makeText(getActivity(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Retrofit", "Network error or failure: " + t.getMessage());
            }
        });
    }


    private void setUserUIByRole(UserRoleEnum role) {
        if(role == UserRoleEnum.VOLUNTEER){
            binding.homeBTNIAmOk.setVisibility(View.GONE);
        }
        else{//is Recipient
            binding.homeBTNIAmOk.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}