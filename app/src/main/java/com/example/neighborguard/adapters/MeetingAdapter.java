package com.example.neighborguard.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.neighborguard.R;
import com.example.neighborguard.enums.UserRoleEnum;
import com.example.neighborguard.model.CurrentUserManager;
import com.example.neighborguard.model.Meeting;
import com.example.neighborguard.ui.MeetingActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;



public class MeetingAdapter extends RecyclerView.Adapter<MeetingAdapter.MeetingViewHolder>{
    private Context context;
    private ArrayList<Meeting> meetings;
    private UserRoleEnum currentRole;


    public MeetingAdapter(Context context, ArrayList<Meeting> meetings) {
        this.context = context;
        this.meetings = meetings;
        this.currentRole = CurrentUserManager.getInstance().getUser().getRole();
    }

    @NonNull
    @Override
    public MeetingAdapter.MeetingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.horizontal_meeting_item, parent, false);
        return new MeetingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MeetingAdapter.MeetingViewHolder holder, int position) {
        String volunteerFullName = meetings.get(position).getVolunteer().getFirstName() + " " + meetings.get(position).getVolunteer().getLastName();
        String recipientFullName = meetings.get(position).getRecipient().getFirstName() + " " + meetings.get(position).getRecipient().getLastName();

        // For RECIPIENT users, hide recipient labels
        if (currentRole == UserRoleEnum.RECIPIENT) {
            holder.meeting_RL_recipient.setVisibility(View.GONE);
            holder.meeting_LBL_recipientName.setVisibility(View.GONE);
            holder.meeting_LBL_recipientTheName.setVisibility(View.GONE);
            holder.meeting_LBL_volunteerTheName.setText(volunteerFullName);
        }

        // For VOLUNTEER users, hide volunteer labels
        if (currentRole == UserRoleEnum.VOLUNTEER) {
            holder.meeting_RL_volunteer.setVisibility(View.GONE);
            holder.meeting_LBL_volunteerName.setVisibility(View.GONE);
            holder.meeting_LBL_volunteerTheName.setVisibility(View.GONE);
            holder.meeting_LBL_recipientTheName.setText(recipientFullName);

        }

        long timestamp = meetings.get(position).getDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm , MMM dd, yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(new Date(timestamp * 1000L));
        holder.recipient_LBL_pickDate.setText("Pick Date: " + formattedDate);
    }

    @Override
    public int getItemCount() {
        return meetings == null ? 0 : meetings.size();
    }

    public Meeting getMeeting(int position) {
        return meetings.get(position);
    }


    public class MeetingViewHolder extends RecyclerView.ViewHolder{
        private LinearLayoutCompat meeting_RL_volunteer;
        private MaterialTextView meeting_LBL_volunteerName;
        private MaterialTextView meeting_LBL_volunteerTheName;
        private LinearLayoutCompat meeting_RL_recipient;
        private MaterialTextView meeting_LBL_recipientName;
        private MaterialTextView meeting_LBL_recipientTheName;
        private MaterialTextView recipient_LBL_pickDate;
        private MaterialButton meeting_BTN_viewDetails;

        public MeetingViewHolder(@NonNull View itemView) {
            super(itemView);
            initViews(itemView);
            setupButtonListeners();
        }

        private void initViews(View itemView) {
            meeting_RL_volunteer = itemView.findViewById(R.id.meeting_RL_volunteer);
            meeting_LBL_volunteerName = itemView.findViewById(R.id.meeting_LBL_volunteerName);
            meeting_LBL_volunteerTheName = itemView.findViewById(R.id.meeting_LBL_volunteerTheName);
            meeting_RL_recipient = itemView.findViewById(R.id.meeting_RL_recipient);
            meeting_LBL_recipientName = itemView.findViewById(R.id.meeting_LBL_recipientName);
            meeting_LBL_recipientTheName = itemView.findViewById(R.id.meeting_LBL_recipientTheName);
            recipient_LBL_pickDate = itemView.findViewById(R.id.recipient_LBL_pickDate);
            meeting_BTN_viewDetails = itemView.findViewById(R.id.meeting_BTN_viewDetails);
        }

        private void setupButtonListeners() {
            meeting_BTN_viewDetails.setOnClickListener(v -> viewDetailsClicked());
        }

        private void viewDetailsClicked() {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Intent intent = new Intent(context, MeetingActivity.class);
                intent.putExtra("meeting", meetings.get(position));
                context.startActivity(intent);
            }
        }
    }
}
