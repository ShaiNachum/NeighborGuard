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
import com.example.neighborguard.model.ExtendedUser;
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
    private ArrayList<ExtendedUser> recipients = new ArrayList<>();


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
            public void recipientPicked(ExtendedUser extendedUser, int position) {
                // Handle when a recipient is picked
                // You can implement your logic here
            }

            @Override
            public void recipientCanceled(ExtendedUser extendedUser, int position) {
                // Handle when a recipient is canceled
                // You can implement your logic here
            }
        });

        //binding.listBTNSend.setOnClickListener(v -> itemClicked(32.1212, 34.1212));
    }

    private void fetchRecipients() {
        Call<SearchUsersResponseSchema> call = userApi.findUser(
                null,           // email
                true,          // toExtendMeeting
                "RECIPIENT",   // role - get only recipients
                null,          // filterByLat
                null,          // filterByLon
                true          // isRequiredAssistance - get recipients who need help
        );

        call.enqueue(new Callback<SearchUsersResponseSchema>() {
            @Override
            public void onResponse(Call<SearchUsersResponseSchema> call, Response<SearchUsersResponseSchema> response) {
                if (response.isSuccessful() && response.body() != null) {
                    recipients.clear();
                    ArrayList<ExtendedUser> users = response.body().getUsers();
                    if (users != null) {
                        recipients.addAll(users);
                        recipientAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "No recipients found", Toast.LENGTH_SHORT).show();
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