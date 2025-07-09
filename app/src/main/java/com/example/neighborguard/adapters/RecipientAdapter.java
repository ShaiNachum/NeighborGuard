package com.example.neighborguard.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.neighborguard.R;
import com.example.neighborguard.api.ApiController;
import com.example.neighborguard.api.MeetingApi;
import com.example.neighborguard.enums.MeetingAssistanceStatusEnum;
import com.example.neighborguard.enums.MeetingStatusEnum;
import com.example.neighborguard.interfaces.Callback_recipient;
import com.example.neighborguard.model.CurrentUserManager;
import com.example.neighborguard.model.Meeting;
import com.example.neighborguard.model.NewMeeting;
import com.example.neighborguard.model.User;
import com.example.neighborguard.model.LonLat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import android.util.Base64;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class RecipientAdapter extends RecyclerView.Adapter<RecipientAdapter.RecipientViewHolder> {

    private Context context;
    private Callback_recipient callbackRecipient;
    private ArrayList<User> recipients;
    private CurrentUserManager currentUserManager;
    private SparseBooleanArray expandedItems = new SparseBooleanArray();
    private MeetingApi meetingApi;



    public RecipientAdapter(Context context, ArrayList<User> recipients) {
        this.context = context;
        this.recipients = recipients;
        this.currentUserManager = CurrentUserManager.getInstance();
        this.meetingApi = ApiController.getRetrofitInstance().create(MeetingApi.class);
    }

    public void setCallbackRecipient(Callback_recipient callbackRecipient) {
        this.callbackRecipient = callbackRecipient;
    }

    @NonNull
    @Override
    public RecipientAdapter.RecipientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.horizontal_recipient_item, parent, false);
        return new RecipientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipientAdapter.RecipientViewHolder holder, int position) {
        User recipient = recipients.get(position);
        boolean isExpanded = expandedItems.get(position, false);

        setExpandableContent(holder, isExpanded);

        // Always show name and last OK
        holder.recipient_LBL_name.setText("Name: " + recipient.getFirstName() + " " + recipient.getLastName());

        long currentTime = System.currentTimeMillis() / 1000L;
        long lastOkTime = recipient.getLastOK();
        long timeSinceLastOk = currentTime - lastOkTime;
        String timeAgoText = formatTimeAgo(timeSinceLastOk);
        holder.recipient_LBL_lastOk.setText("Last OK: " + timeAgoText);

        holder.recipient_LBL_service.setText(getServicesText(recipient));

        if (recipient.getProfileImage() != null && !recipient.getProfileImage().isEmpty()) {
            Glide.with(context)
                    .load(Base64.decode(recipient.getProfileImage(), Base64.DEFAULT))
                    .placeholder(R.drawable.ic_profile_image_24)
                    .error(R.drawable.ic_profile_image_24)
                    .centerCrop()
                    .into(holder.recipient_IMG_image);
        } else {
            holder.recipient_IMG_image.setImageResource(R.drawable.ic_profile_image_24);
        }

        if (isExpanded) {
            holder.recipient_LBL_phone.setText("Phone: " + recipient.getPhoneNumber());
            holder.recipient_LBL_address.setText("Address: " + recipient.getAddress().getAddressString());
            holder.recipient_LBL_distance.setText(getDistanceText(recipient));
        }

        // Set click listener for expansion
        holder.itemView.setOnClickListener(v -> {
            expandedItems.put(position, !isExpanded);
            notifyItemChanged(position);
        });
    }


    private void setExpandableContent(RecipientViewHolder holder, boolean isExpanded) {
        // Update image card size
        ViewGroup.LayoutParams imageParams = holder.recipient_CARD_image.getLayoutParams();
        imageParams.width = context.getResources().getDimensionPixelSize(
                isExpanded ? R.dimen.recipient_image_card_width_expanded : R.dimen.recipient_image_card_width_collapsed);
        imageParams.height = context.getResources().getDimensionPixelSize(
                isExpanded ? R.dimen.recipient_image_card_height_expanded : R.dimen.recipient_image_card_height_collapsed);
        holder.recipient_CARD_image.setLayoutParams(imageParams);

        // Update margins for data card
        ViewGroup.MarginLayoutParams dataParams = (ViewGroup.MarginLayoutParams) holder.recipient_CARD_data.getLayoutParams();
        dataParams.setMarginStart(context.getResources().getDimensionPixelSize(
                isExpanded ? R.dimen.recipient_data_card_margin_start_expanded : R.dimen.recipient_data_card_margin_start_collapsed));
        holder.recipient_CARD_data.setLayoutParams(dataParams);

        // Update RelativeLayout margins
        ViewGroup.MarginLayoutParams relativeParams = (ViewGroup.MarginLayoutParams) holder.dataRelativeLayout.getLayoutParams();
        relativeParams.setMarginStart(context.getResources().getDimensionPixelSize(
                isExpanded ? R.dimen.recipient_data_content_margin_start_expanded : R.dimen.recipient_data_content_margin_start_collapsed));
        holder.dataRelativeLayout.setLayoutParams(relativeParams);

        // Update check image size
        ViewGroup.LayoutParams checkParams = holder.recipient_IMG_check.getLayoutParams();
        int checkSize = context.getResources().getDimensionPixelSize(
                isExpanded ? R.dimen.recipient_check_icon_size_expanded : R.dimen.recipient_check_icon_size_collapsed);
        checkParams.width = checkSize;
        checkParams.height = checkSize;
        holder.recipient_IMG_check.setLayoutParams(checkParams);

        // Set expandable content visibility
        holder.expandableContent.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
    }


    private String getServicesText(User recipient) {
        // Check if services map exists and is not empty
        if (recipient.getServices() == null || recipient.getServices().isEmpty()) {
            return "Services: none";
        }

        StringBuilder servicesText = new StringBuilder("Services: ");
        boolean isFirst = true;

        // Always check if General Check needs assistance
        if (recipient.getServices().containsKey("General Check") &&
                recipient.getServices().get("General Check") == MeetingAssistanceStatusEnum.NEED_ASSISTANCE) {
            servicesText.append("General Check");
            isFirst = false;
        }

        // Add other services that need assistance
        for (Map.Entry<String, MeetingAssistanceStatusEnum> entry : recipient.getServices().entrySet()) {
            if (!entry.getKey().equals("General Check") &&
                    entry.getValue() == MeetingAssistanceStatusEnum.NEED_ASSISTANCE) {
                if (!isFirst) {
                    servicesText.append(", ");
                }
                servicesText.append(entry.getKey());
                isFirst = false;
            }
        }

        return servicesText.toString();
    }


    private String getDistanceText(User recipient) {
        // Check if current user and recipient have valid location data
        User currentUser = currentUserManager.getUser();
        if (currentUser == null || currentUser.getLonLat() == null
                || recipient == null || recipient.getLonLat() == null) {
            return "Distance: N/A";
        }

        // Calculate distance
        LonLat location1 = currentUser.getLonLat();
        LonLat location2 = recipient.getLonLat();

        double distance = calculateDistance(
                location1.getLatitude(), location1.getLongitude(),
                location2.getLatitude(), location2.getLongitude()
        );

        // Format distance text
        if (distance >= 1) {
            return String.format("Distance: %.1f km", distance);
        } else {
            return String.format("Distance: %d m", (int)(distance * 1000));
        }
    }


    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Radius of the Earth in kilometers
        final double R = 6371.0;

        // Convert latitude and longitude to radians
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // Differences in coordinates
        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;

        // Haversine formula
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Calculate the distance
        return R * c;
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


    @Override
    public int getItemCount() {
        return recipients == null ? 0 : recipients.size();
    }


    public User getRecipient(int position) {
        return recipients.get(position);
    }


    public class RecipientViewHolder extends RecyclerView.ViewHolder {
        // UI Components
        private ShapeableImageView recipient_IMG_image;
        private MaterialTextView recipient_LBL_name;
        private MaterialTextView recipient_LBL_phone;
        private MaterialTextView recipient_LBL_address;
        private MaterialTextView recipient_LBL_distance;
        private MaterialTextView recipient_LBL_lastOk;
        private MaterialTextView recipient_LBL_service;
        private ShapeableImageView recipient_IMG_check;
        private MaterialButton recipient_BTN_pick;
        private MaterialButton recipient_BTN_cancel;

        private LinearLayout expandableContent;
        private CardView recipient_CARD_image;
        private CardView recipient_CARD_data;
        private RelativeLayout dataRelativeLayout;

        // State tracking variables
        private boolean isPickedByCurrentVolunteer = false;
        private String meetingID;  // We still need this for API calls

        public RecipientViewHolder(@NonNull View itemView) {
            super(itemView);
            initViews(itemView);
            setupButtonListeners();
        }

        private void initViews(View itemView) {
            // Initialize all view references
            expandableContent = itemView.findViewById(R.id.expandableContent);
            recipient_CARD_image = itemView.findViewById(R.id.recipient_CARD_image);
            recipient_CARD_data = itemView.findViewById(R.id.recipient_CARD_data);
            dataRelativeLayout = recipient_CARD_data.findViewById(R.id.data_relative_layout);

            recipient_IMG_image = itemView.findViewById(R.id.recipient_IMG_image);
            recipient_LBL_name = itemView.findViewById(R.id.recipient_LBL_name);
            recipient_LBL_phone = itemView.findViewById(R.id.recipient_LBL_phone);
            recipient_LBL_address = itemView.findViewById(R.id.recipient_LBL_address);
            recipient_LBL_distance = itemView.findViewById(R.id.recipient_LBL_distance);
            recipient_LBL_lastOk = itemView.findViewById(R.id.recipient_LBL_lastOk);
            recipient_LBL_service = itemView.findViewById(R.id.recipient_LBL_service);
            recipient_IMG_check = itemView.findViewById(R.id.recipient_IMG_check);
            recipient_BTN_pick = itemView.findViewById(R.id.recipient_BTN_pick);
            recipient_BTN_pick.setVisibility(View.VISIBLE);
            recipient_BTN_cancel = itemView.findViewById(R.id.recipient_BTN_cancel);
            recipient_BTN_cancel.setVisibility(View.GONE);
        }

        private void setupButtonListeners() {
            // Handle pick button clicks
            recipient_BTN_pick.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    if (!isPickedByCurrentVolunteer) {
                        pickButtonClicked(position);
                    } else {
                        Toast.makeText(context, "You have already picked this recipient", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // Handle cancel button clicks with parallel logic
            recipient_BTN_cancel.setOnClickListener(v -> {
                if (isPickedByCurrentVolunteer) {
                    cancelButtonClicked();
                } else {
                    Toast.makeText(context, "You haven't picked this recipient yet", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void pickButtonClicked(int position) {
            User recipient = recipients.get(position);
            User volunteer = currentUserManager.getUser();

            // Create list of services that need assistance
            ArrayList<String> neededServices = new ArrayList<>();

            // Add all services that need assistance
            for (Map.Entry<String, MeetingAssistanceStatusEnum> entry : recipient.getServices().entrySet()) {
                if (entry.getValue() == MeetingAssistanceStatusEnum.NEED_ASSISTANCE) {
                    neededServices.add(entry.getKey());
                }
            }

            // Create the meeting request
            NewMeeting newMeeting = new NewMeeting();
            newMeeting.setRecipient(recipient);
            newMeeting.setVolunteer(volunteer);
            newMeeting.setDate(System.currentTimeMillis() / 1000L);
            newMeeting.setStatus(MeetingStatusEnum.IS_PICKED);
            newMeeting.setServices(neededServices);

            // Make the API call
            meetingApi.createMeeting(newMeeting).enqueue(new Callback<Meeting>() {
                @Override
                public void onResponse(Call<Meeting> call, Response<Meeting> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        // Store meeting ID and update state
                        meetingID = response.body().getUid();
                        Toast.makeText(context, "Meeting created successfully!", Toast.LENGTH_SHORT).show();

                        // Update UI on main thread
                        recipient_IMG_check.post(() -> {
                            recipient_IMG_check.setImageResource(R.drawable.ic_green_check);
                            isPickedByCurrentVolunteer = true;
                            updateButtonsVisibility();
                        });
                    } else {
                        if (response.code() == 409) {
                            Toast.makeText(context, "This recipient has already been picked by another volunteer", Toast.LENGTH_LONG).show();
                            // Remove from list if picked by another volunteer
                            recipients.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, recipients.size());
                        } else {
                            Toast.makeText(context, "Failed to create meeting", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<Meeting> call, Throwable t) {
                    Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void cancelButtonClicked() {
            // Show confirmation dialog before cancelling
            new AlertDialog.Builder(context)
                    .setTitle("Cancel Meeting")
                    .setMessage("Are you sure you want to cancel this meeting?")
                    .setPositiveButton("Yes", (dialog, which) -> performCancellation())
                    .setNegativeButton("No", null)
                    .show();
        }

        private void performCancellation() {
            String volunteerUID = currentUserManager.getUser().getUid();

            meetingApi.cancelMeeting(meetingID, volunteerUID).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(context, "Meeting cancelled successfully", Toast.LENGTH_SHORT).show();

                        // Reset state on main thread
                        recipient_IMG_check.post(() -> {
                            resetState();
                        });
                    } else {
                        Toast.makeText(context, "Failed to cancel meeting", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void resetState() {
            // Reset all state variables
            isPickedByCurrentVolunteer = false;
            meetingID = null;

            // Reset UI elements
            recipient_IMG_check.setImageResource(R.drawable.ic_black_check);
            updateButtonsVisibility();
        }

        private void updateButtonsVisibility() {
            // Update buttons based on picked state
            recipient_BTN_pick.setVisibility(isPickedByCurrentVolunteer ? View.GONE : View.VISIBLE);
            recipient_BTN_cancel.setVisibility(isPickedByCurrentVolunteer ? View.VISIBLE : View.GONE);
        }
    }
}
