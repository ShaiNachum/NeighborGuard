package com.example.neighborguard.ui.meetings;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.neighborguard.adapters.MeetingAdapter;
import com.example.neighborguard.api.ApiController;
import com.example.neighborguard.api.MeetingApi;
import com.example.neighborguard.api.UserApi;
import com.example.neighborguard.databinding.FragmentMeetingsBinding;
import com.example.neighborguard.enums.MeetingAssistanceStatusEnum;
import com.example.neighborguard.enums.MeetingStatusEnum;
import com.example.neighborguard.enums.UserRoleEnum;
import com.example.neighborguard.interfaces.Callback_recipient;
import com.example.neighborguard.model.CurrentUserManager;
import com.example.neighborguard.model.Meeting;
import com.example.neighborguard.model.SearchMeetingsResponseSchema;
import com.example.neighborguard.ui.home.ListFragment;
import com.example.neighborguard.ui.home.MapFragment;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MeetingsFragment extends Fragment{
    private FragmentMeetingsBinding binding;
    private CurrentUserManager currentUserManager;
    private MeetingApi meetingApi;

    private MeetingAdapter nextMeetingsAdapter;
    private MeetingAdapter lastMeetingsAdapter;

    private ArrayList<Meeting> nextMeetings;
    private ArrayList<Meeting> lastMeetings;

    private String userID;
    private UserRoleEnum userRole;




    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentMeetingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        currentUserManager = CurrentUserManager.getInstance();
        meetingApi = ApiController.getRetrofitInstance().create(MeetingApi.class);
        userID = currentUserManager.getUser().getUid();
        userRole = currentUserManager.getUser().getRole();

        setUserUIByRole(userRole);
        initViews(userRole);
    }

    private void initViews(UserRoleEnum role) {
        if(role == UserRoleEnum.VOLUNTEER) {
            // Initialize lists
            nextMeetings = new ArrayList<>();
            lastMeetings = new ArrayList<>();

            // Create adapters first
            nextMeetingsAdapter = new MeetingAdapter(getContext(), nextMeetings);
            lastMeetingsAdapter = new MeetingAdapter(getContext(), lastMeetings);

            // Set up RecyclerViews
            binding.meetingsVolunteerRVNextMeetingsList.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.meetingsVolunteerRVLastMeetingsList.setLayoutManager(new LinearLayoutManager(getContext()));

            binding.meetingsVolunteerRVNextMeetingsList.setAdapter(nextMeetingsAdapter);
            binding.meetingsVolunteerRVLastMeetingsList.setAdapter(lastMeetingsAdapter);

            // Now fetch data after everything is set up
            fetchMeetings(MeetingStatusEnum.IS_PICKED, nextMeetings, nextMeetingsAdapter, true);
            fetchMeetings(MeetingStatusEnum.DONE, lastMeetings, lastMeetingsAdapter, false);

        } else {  // Recipient
            // Initialize list
            lastMeetings = new ArrayList<>();

            // Create adapter
            lastMeetingsAdapter = new MeetingAdapter(getContext(), lastMeetings);

            // Set up RecyclerView
            binding.meetingsRecipientRVNextMeetingsList.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.meetingsRecipientRVNextMeetingsList.setAdapter(lastMeetingsAdapter);

            // Fetch data
            fetchMeetings(MeetingStatusEnum.DONE, lastMeetings, lastMeetingsAdapter, false);
        }
    }

    private void fetchMeetings(MeetingStatusEnum meetingStatus, ArrayList<Meeting> meetings, MeetingAdapter meetingAdapter, boolean isNextMeetings) {
        String noMeetingsText = isNextMeetings ? "No upcoming meetings" : "No past meetings";
        String failToLoadText = isNextMeetings ? "Failed to load upcoming meetings" : "Failed to load past meetings";

        Call<SearchMeetingsResponseSchema> call = meetingApi.getMeetings(
                userID,
                meetingStatus.toString()
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
                        Toast.makeText(getContext(), noMeetingsText, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), failToLoadText, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SearchMeetingsResponseSchema> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setUserUIByRole(UserRoleEnum role) {
        if(role == UserRoleEnum.VOLUNTEER){
            binding.meetingsLLRecipient.setVisibility(View.GONE);
            binding.meetingsLLVolunteer.setVisibility(View.VISIBLE);
        }
        else{//is Recipient
            binding.meetingsLLVolunteer.setVisibility(View.GONE);
            binding.meetingsLLRecipient.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh meetings lists whenever fragment becomes visible
        if (userRole == UserRoleEnum.VOLUNTEER) {
            fetchMeetings(MeetingStatusEnum.IS_PICKED, nextMeetings, nextMeetingsAdapter, true);
            fetchMeetings(MeetingStatusEnum.DONE, lastMeetings, lastMeetingsAdapter, false);
        } else{
            fetchMeetings(MeetingStatusEnum.DONE, lastMeetings, lastMeetingsAdapter, false);
        }
    }
}