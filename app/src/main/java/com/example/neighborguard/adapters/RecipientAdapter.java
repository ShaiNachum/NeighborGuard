package com.example.neighborguard.adapters;

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
import com.example.neighborguard.interfaces.Callback_recipient;
import com.example.neighborguard.model.CurrentUserManager;
import com.example.neighborguard.model.ExtendedUser;
import com.example.neighborguard.model.LonLat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;


public class RecipientAdapter extends RecyclerView.Adapter<RecipientAdapter.RecipientViewHolder> {

    private Context context;
    private Callback_recipient callbackRecipient;
    private ArrayList<ExtendedUser> recipients;
    private CurrentUserManager currentUserManager;

    private SparseBooleanArray expandedItems = new SparseBooleanArray();


    public RecipientAdapter(Context context, ArrayList<ExtendedUser> recipients) {
        this.context = context;
        this.recipients = recipients;
        this.currentUserManager = CurrentUserManager.getInstance();
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
        ExtendedUser recipient = recipients.get(position);
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

        // Set profile image if exists
//        if (recipient.getProfileImage() != null && !recipient.getProfileImage().isEmpty()) {
//            try {
//                byte[] decodedString = Base64.decode(recipient.getProfileImage(), Base64.DEFAULT);
//                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
//                holder.recipient_IMG_image.setImageBitmap(bitmap);
//            } catch (Exception e) {
//                e.printStackTrace();
//                holder.recipient_IMG_image.setImageResource(R.drawable.ic_profile_image_24);
//            }
//        }
        if (recipient.getProfileImage() != null && !recipient.getProfileImage().isEmpty()) {
            Glide.with(context)
                    .load(Base64.decode(recipient.getProfileImage(), Base64.DEFAULT))
                    .placeholder(R.drawable.ic_profile_image_24)
                    .error(R.drawable.ic_profile_image_24)
                    .circleCrop()
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


    private String getServicesText(ExtendedUser recipient) {
        // Check if services list exists and is not empty
        if (recipient.getServices() == null || recipient.getServices().isEmpty()) {
            return "Services: none";
        }

        // Join all services with comma and space
        String servicesString = String.join(", ", recipient.getServices());

        return "Services: " + servicesString;
    }


    private String getDistanceText(ExtendedUser recipient) {
        // Check if current user and recipient have valid location data
        ExtendedUser currentUser = currentUserManager.getUser();
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

    public ExtendedUser getRecipient(int position) {
        return recipients.get(position);
    }


    public class RecipientViewHolder extends RecyclerView.ViewHolder {
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
        private CardView recipient_CARD_image;  // Add this line
        private CardView recipient_CARD_data;
        private RelativeLayout dataRelativeLayout;



        public RecipientViewHolder(@NonNull View itemView) {
            super(itemView);
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
             recipient_BTN_pick.setOnClickListener(v ->{
                 if (callbackRecipient != null) {
                     callbackRecipient.recipientPicked(getRecipient(getAdapterPosition()), getAdapterPosition());
                 }
             });

             recipient_BTN_cancel = itemView.findViewById(R.id.recipient_BTN_cancel);
             recipient_BTN_cancel.setOnClickListener(v -> {
                if (callbackRecipient != null) {
                    callbackRecipient.recipientCanceled(getRecipient(getAdapterPosition()), getAdapterPosition());
                }
            });
        }
    }
}
