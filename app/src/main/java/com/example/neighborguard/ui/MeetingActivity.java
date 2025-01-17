package com.example.neighborguard.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.neighborguard.R;
import com.example.neighborguard.api.ApiController;
import com.example.neighborguard.api.MeetingApi;
import com.example.neighborguard.api.UserApi;
import com.example.neighborguard.databinding.ActivityMeetingBinding;
import com.example.neighborguard.enums.MeetingAssistanceStatusEnum;
import com.example.neighborguard.enums.MeetingStatusEnum;
import com.example.neighborguard.enums.UserRoleEnum;
import com.example.neighborguard.model.CurrentUserManager;
import com.example.neighborguard.model.Meeting;
import com.example.neighborguard.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MeetingActivity extends AppCompatActivity {
    private ActivityMeetingBinding binding;
    private UserApi userApiService;
    private MeetingApi meetingApiService;
    private CurrentUserManager currentUserManager;
    private Meeting meeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMeetingBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        userApiService = ApiController.getRetrofitInstance().create(UserApi.class);
        meetingApiService = ApiController.getRetrofitInstance().create(MeetingApi.class);
        currentUserManager = CurrentUserManager.getInstance();
        meeting = (Meeting) getIntent().getSerializableExtra("meeting");

        initViews();
        initButtons();
    }

    private void initButtons() {
        binding.meetingBTNBack.setOnClickListener(v -> backClicked());

        if(meeting.getStatus() == MeetingStatusEnum.DONE){
            binding.meetingBTNCancel.setVisibility(View.GONE);
            binding.meetingBTNDone.setVisibility(View.GONE);
        } else{
            if(currentUserManager.getUser().getRole() == UserRoleEnum.RECIPIENT){
                binding.meetingBTNCancel.setOnClickListener(v -> recipientClickedCancel());
                binding.meetingBTNDone.setOnClickListener(v -> recipientClickedDone());

            }else {
                binding.meetingBTNCancel.setOnClickListener(v -> volunteerClickedCancel());
                binding.meetingBTNDone.setVisibility(View.GONE);;
            }
        }
    }

    private void volunteerClickedCancel() {
        disableButtons();

        new AlertDialog.Builder(this)
                .setTitle("Cancel Meeting")
                .setMessage("Are you sure you want to cancel this meeting?")
                .setPositiveButton("Yes", (dialog, which) -> volunteerPerformCancellation())
                .setNegativeButton("No", (dialog, which) -> enableButtons())  // Re-enable if user clicks No
                .setOnCancelListener(dialog -> enableButtons())  // Re-enable if dialog is dismissed
                .show();
    }

    private void recipientClickedDone() {
        disableButtons();

        new AlertDialog.Builder(this)
                .setTitle("Meeting Complete")
                .setMessage("Do you still need assistance with these services?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // If they still need assistance, pass the appropriate status
                    recipientCompleteMeeting(MeetingStatusEnum.DONE, MeetingAssistanceStatusEnum.NEED_ASSISTANCE);
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // If they don't need assistance anymore, mark services as completed
                    recipientCompleteMeeting(MeetingStatusEnum.DONE, MeetingAssistanceStatusEnum.DO_NOT_NEED_ASSISTANCE);
                })
                .setOnCancelListener(dialog -> enableButtons())
                .show();
    }

    private void recipientClickedCancel() {
        disableButtons();

        new AlertDialog.Builder(this)
                .setTitle("Cancel Meeting")
                .setMessage("Are you sure you want to cancel this meeting?")
                .setPositiveButton("Yes", (dialog, which) -> recipientPerformCancellation())
                .setNegativeButton("No", (dialog, which) -> enableButtons())  // Re-enable if user clicks No
                .setOnCancelListener(dialog -> enableButtons())  // Re-enable if dialog is dismissed
                .show();
    }

    private void disableButtons() {
        binding.meetingBTNCancel.setEnabled(false);
        binding.meetingBTNDone.setEnabled(false);
    }

    private void enableButtons() {
        binding.meetingBTNCancel.setEnabled(true);
        binding.meetingBTNDone.setEnabled(true);
    }

    private void volunteerPerformCancellation() {
        meetingApiService.cancelMeeting(meeting.getUid(), currentUserManager.getUser().getUid())
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            // Prepare updated services map for the recipient
                            HashMap<String, MeetingAssistanceStatusEnum> updatedServices =
                                    new HashMap<>(meeting.getRecipient().getServices());

                            // Set services to NEED_ASSISTANCE
                            for (String service : meeting.getServices()) {
                                updatedServices.put(service, MeetingAssistanceStatusEnum.NEED_ASSISTANCE);
                            }

                            // Update recipient's services using our unified function
                            updateUserServices(updatedServices, "Meeting cancelled successfully", meeting.getRecipient());
                        } else {
                            Toast.makeText(MeetingActivity.this,
                                    "Failed to cancel meeting", Toast.LENGTH_SHORT).show();
                            enableButtons();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(MeetingActivity.this,
                                "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        enableButtons();
                    }
                });
    }

    private void recipientCompleteMeeting(MeetingStatusEnum meetingStatus, MeetingAssistanceStatusEnum serviceStatus) {
        meetingApiService.updateMeetingStatus(meeting.getUid(), meetingStatus.toString())
                .enqueue(new Callback<Meeting>() {
                    @Override
                    public void onResponse(Call<Meeting> call, Response<Meeting> response) {
                        if (response.isSuccessful()) {
                            // Prepare updated services map
                            HashMap<String, MeetingAssistanceStatusEnum> updatedServices =
                                    new HashMap<>(currentUserManager.getUser().getServices());

                            // Update services based on the passed status
                            for (String service : meeting.getServices()) {
                                updatedServices.put(service, serviceStatus);
                            }

                            // Create appropriate success message based on the status
                            String successMessage = serviceStatus == MeetingAssistanceStatusEnum.NEED_ASSISTANCE ?
                                    "Meeting completed. Services marked for further assistance." :
                                    "Meeting completed successfully";

                            // Let updateUserServices handle the General Check logic
                            updateUserServices(updatedServices, successMessage, null); // null means update current user
                        } else {
                            Toast.makeText(MeetingActivity.this,
                                    "Failed to complete meeting", Toast.LENGTH_SHORT).show();
                            enableButtons();
                        }
                    }

                    @Override
                    public void onFailure(Call<Meeting> call, Throwable t) {
                        Toast.makeText(MeetingActivity.this,
                                "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        enableButtons();
                    }
                });
    }

    private void recipientPerformCancellation() {
        meetingApiService.cancelMeeting(meeting.getUid(), currentUserManager.getUser().getUid())
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            // Prepare updated services map
                            HashMap<String, MeetingAssistanceStatusEnum> updatedServices =
                                    new HashMap<>(currentUserManager.getUser().getServices());

                            // Set services to NEED_ASSISTANCE
                            for (String service : meeting.getServices()) {
                                updatedServices.put(service, MeetingAssistanceStatusEnum.NEED_ASSISTANCE);
                            }

                            updateUserServices(updatedServices, "Meeting cancelled successfully", null);// null means update current user
                        } else {
                            Toast.makeText(MeetingActivity.this,
                                    "Failed to cancel meeting", Toast.LENGTH_SHORT).show();
                            enableButtons();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(MeetingActivity.this,
                                "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        enableButtons();
                    }
                });
    }

//    private void updateUserServices(HashMap<String, MeetingAssistanceStatusEnum> updatedServices, String successMessage) {
//        User currentUser = currentUserManager.getUser();
//        currentUser.setServices(updatedServices);
//
//        // Check if this is a completed General Check service
//        boolean isGeneralCheckCompleted = meeting.getServices().contains("General Check") &&
//                updatedServices.get("General Check") == MeetingAssistanceStatusEnum.DO_NOT_NEED_ASSISTANCE;
//
//        if (isGeneralCheckCompleted) {
//            // Update lastOK timestamp when General Check is successfully completed
//            // This indicates the recipient has been checked on and is doing well
//            currentUser.setLastOK(System.currentTimeMillis() / 1000L);
//        }
//
//        // Update user in backend
//        userApiService.updateUser(currentUser.getUid(), currentUser)
//                .enqueue(new Callback<Void>() {
//                    @Override
//                    public void onResponse(Call<Void> call, Response<Void> response) {
//                        if (response.isSuccessful()) {
//                            // Update local user instance
//                            currentUserManager.setUser(currentUser);
//                            Toast.makeText(MeetingActivity.this, successMessage, Toast.LENGTH_SHORT).show();
//                            finish(); // Close the activity
//                        } else {
//                            Toast.makeText(MeetingActivity.this,
//                                    "Failed to update services", Toast.LENGTH_SHORT).show();
//                        }
//                        enableButtons();
//                    }
//
//                    @Override
//                    public void onFailure(Call<Void> call, Throwable t) {
//                        Toast.makeText(MeetingActivity.this,
//                                "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//                        enableButtons();
//                    }
//                });
//    }

    private void updateUserServices(HashMap<String, MeetingAssistanceStatusEnum> updatedServices, String successMessage, User userToUpdate) {
        // If no specific user is provided, use the current user (for recipient's own actions)
        User targetUser = userToUpdate != null ? userToUpdate : currentUserManager.getUser();

        targetUser.setServices(updatedServices);

        // Check if this is a completed General Check service
        boolean isGeneralCheckCompleted = meeting.getServices().contains("General Check") &&
                updatedServices.get("General Check") == MeetingAssistanceStatusEnum.DO_NOT_NEED_ASSISTANCE;

        if (isGeneralCheckCompleted) {
            // Update lastOK timestamp when General Check is successfully completed
            // This indicates the recipient has been checked on and is doing well
            targetUser.setLastOK(System.currentTimeMillis() / 1000L);
        }

        // Update user in backend
        userApiService.updateUser(targetUser.getUid(), targetUser)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            // Only update CurrentUserManager if we're updating the current user
                            if (userToUpdate == null) {
                                currentUserManager.setUser(targetUser);
                            }
                            Toast.makeText(MeetingActivity.this, successMessage, Toast.LENGTH_SHORT).show();
                            finish(); // Close the activity
                        } else {
                            Toast.makeText(MeetingActivity.this,
                                    "Failed to update services", Toast.LENGTH_SHORT).show();
                        }
                        enableButtons();
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(MeetingActivity.this,
                                "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        enableButtons();
                    }
                });
    }

    private void backClicked() {
        this.finish();
    }

    private void initViews() {
        long timestamp = meeting.getDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm , MMM dd, yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(new Date(timestamp * 1000L));
        binding.meetingLBLAt.setText(formattedDate);

        ArrayList<String> meetingServices = meeting.getServices();
        if (meetingServices != null && !meetingServices.isEmpty()) {
            String servicesText = String.join(", ", meetingServices);
            binding.meetingLBLTheServiceProvided.setText(servicesText);
        }

        // Set recipient data
        String recipientFulName = meeting.getRecipient().getFirstName() + " " + meeting.getRecipient().getLastName();
        binding.meetingLBLRecipientTheName.setText(recipientFulName);

        String recipientPhone = String.valueOf(meeting.getRecipient().getPhoneNumber());
        binding.meetingLBLRecipientPhone.setText("Phone: " + recipientPhone);

        binding.meetingLBLRecipientTheAddress.setText(meeting.getRecipient().getAddress().getAddressString());

        String recipientAge = String.valueOf(meeting.getRecipient().getAge());
        binding.meetingLBLRecipientAge.setText("Age: " + recipientAge);

        binding.meetingLBLRecipientGender.setText("Gender: " + meeting.getRecipient().getGender());

        binding.meetingLBLRecipientTheLanguages.setText(String.valueOf(meeting.getRecipient().getLanguages()));

        long currentTime = System.currentTimeMillis() / 1000L;
        long lastOkTime = meeting.getRecipient().getLastOK();
        long timeSinceLastOk = currentTime - lastOkTime;
        String timeAgoText = formatTimeAgo(timeSinceLastOk);
        binding.meetingLBLRecipientLastOk.setText("Last OK: " + timeAgoText);

        String base64RecipientProfileImage = meeting.getRecipient().getProfileImage();
        if (base64RecipientProfileImage != null && !base64RecipientProfileImage.isEmpty()) {
            Glide.with(this)
                    .load(Base64.decode(base64RecipientProfileImage, Base64.DEFAULT))
                    .placeholder(R.drawable.ic_profile_image_24)
                    .error(R.drawable.ic_profile_image_24)
                    .centerCrop()
                    .into(binding.meetingIMGRecipientImage);
        }

        // Set volunteer data
        String volunteerFulName = meeting.getVolunteer().getFirstName() + " " + meeting.getVolunteer().getLastName();
        binding.meetingLBLVolunteerTheName.setText(volunteerFulName);

        String volunteerPhone = String.valueOf(meeting.getVolunteer().getPhoneNumber());
        binding.meetingLBLVolunteerPhone.setText("Phone: " + volunteerPhone);

        binding.meetingLBLVolunteerTheAddress.setText(meeting.getVolunteer().getAddress().getAddressString());

        String volunteerAge = String.valueOf(meeting.getVolunteer().getAge());
        binding.meetingLBLVolunteerAge.setText("Age: " + volunteerAge);

        binding.meetingLBLVolunteerGender.setText("Gender: " + meeting.getVolunteer().getGender());

        binding.meetingLBLVolunteerTheLanguages.setText(String.valueOf(meeting.getVolunteer().getLanguages()));

        String base64VolunteerProfileImage = meeting.getVolunteer().getProfileImage();
        if (base64VolunteerProfileImage != null && !base64VolunteerProfileImage.isEmpty()) {
            Glide.with(this)
                    .load(Base64.decode(base64VolunteerProfileImage, Base64.DEFAULT))
                    .placeholder(R.drawable.ic_profile_image_24)
                    .error(R.drawable.ic_profile_image_24)
                    .centerCrop()
                    .into(binding.meetingIMGVolunteerImage);
        }
    }

    private String formatTimeAgo(long seconds) {
        if (seconds < 0) {
            return "Just now";
        }

        // Calculate days, hours, and minutes
        long days = seconds / 86400;
        long remainingSeconds = seconds % 86400;
        long hours = remainingSeconds / 3600;
        long minutes = (remainingSeconds % 3600) / 60;

        // Build the time ago string
        StringBuilder timeAgoBuilder = new StringBuilder();

        if (days > 0) {
            // If days exist, include full days, hours, and minutes with commas
            timeAgoBuilder.append(days).append(days > 1 ? " days" : " day");
            if (hours > 0 || minutes > 0) {
                timeAgoBuilder.append(", ");
            }
            if (hours > 0) {
                timeAgoBuilder.append(hours).append(hours > 1 ? " hours" : " hour");
                if (minutes > 0) {
                    timeAgoBuilder.append(", ");
                }
            }
            if (minutes > 0) {
                timeAgoBuilder.append(minutes).append(minutes > 1 ? " minutes" : " minute");
            }
        } else if (hours > 0) {
            // If no days but hours exist, include hours and minutes with a comma
            timeAgoBuilder.append(hours).append(hours > 1 ? " hours" : " hour");
            if (minutes > 0) {
                timeAgoBuilder.append(", ").append(minutes).append(minutes > 1 ? " minutes" : " minute");
            }
        } else if (minutes > 0) {
            // If only minutes exist
            timeAgoBuilder.append(minutes).append(minutes > 1 ? " minutes" : " minute");
        } else {
            // Less than a minute
            return "Just now";
        }

        timeAgoBuilder.append(" ago");
        return timeAgoBuilder.toString();
    }









}

