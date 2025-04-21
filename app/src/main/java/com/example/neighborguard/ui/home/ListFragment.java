package com.example.neighborguard.ui.home;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.neighborguard.adapters.RecipientAdapter;
import com.example.neighborguard.api.ApiController;
import com.example.neighborguard.api.UserApi;
import com.example.neighborguard.databinding.FragmentListBinding;
import com.example.neighborguard.interfaces.Callback_recipient;
import com.example.neighborguard.model.CurrentUserManager;
import com.example.neighborguard.model.User;
import com.example.neighborguard.model.SearchUsersResponseSchema;

import android.widget.Toast;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;


public class ListFragment extends Fragment {
    private FragmentListBinding binding;
    private Callback_recipient callbackRecipient;
    private RecipientAdapter recipientAdapter;
    private UserApi userApi;
    private ArrayList<User> recipients = new ArrayList<>();


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userApi = ApiController.getRetrofitInstance().create(UserApi.class);
        initViews();
        fetchRecipients();
    }

    private void initViews() {
        // Initialize RecyclerView with vertical layout
        recipientAdapter = new RecipientAdapter(getContext(), recipients);
        binding.listLSTRecipients.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.listLSTRecipients.setAdapter(recipientAdapter);

        // Set up adapter callback
        recipientAdapter.setCallbackRecipient(new Callback_recipient() {
            @Override
            public void recipientClicked(double lat, double lng) {
                if (callbackRecipient != null) {
                    callbackRecipient.recipientClicked(lat, lng);
                }
            }

            @Override
            public void updateMapRecipients(ArrayList<User> recipients) {
                // You can leave this method empty if the adapter doesn't need
                // to update the map directly, or forward it to the parent callback
                if (callbackRecipient != null) {
                    callbackRecipient.updateMapRecipients(recipients);
                }
            }
        });

        //binding.listBTNSend.setOnClickListener(v -> itemClicked(32.1212, 34.1212));
    }

    private void fetchRecipients() {
        String volunteerUID = CurrentUserManager.getInstance().getUser().getUid();
        Double volunteerLat = CurrentUserManager.getInstance().getUser().getLonLat().getLatitude();
        Double volunteerLon = CurrentUserManager.getInstance().getUser().getLonLat().getLongitude();

        Call<SearchUsersResponseSchema> call = userApi.getNearbyRecipients(
                volunteerUID,     // current volunteer's UID
                volunteerLat,    // filterByLat
                volunteerLon     // filterByLon
        );

        call.enqueue(new Callback<SearchUsersResponseSchema>() {
            @Override
            public void onResponse(
                    Call<SearchUsersResponseSchema> call,
                    Response<SearchUsersResponseSchema> response)
            {
                if (response.isSuccessful() && response.body() != null) {
                    recipients.clear();
                    ArrayList<User> users = response.body().getUsers();
                    if (users != null && !users.isEmpty()) {
                        recipients.addAll(users);
                        recipientAdapter.notifyDataSetChanged();

                        // Send recipients to the map
                        if (callbackRecipient != null) {
                            callbackRecipient.updateMapRecipients(new ArrayList<>(recipients));
                        }

                    } else {
                        Toast.makeText(getContext(), "No recipients need assistance nearby", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to load recipients", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SearchUsersResponseSchema> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void itemClicked(double lat, double lng){
        if (callbackRecipient != null) {
            callbackRecipient.recipientClicked(lat, lng);
        }
    }

    public void setCallbackRecipientClicked(Callback_recipient callbackRecipientClicked) {
        this.callbackRecipient = callbackRecipientClicked;
    }
}