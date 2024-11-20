package com.example.neighborguard.ui.meetings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.neighborguard.databinding.FragmentMeetingsBinding;



public class MeetingsFragment extends Fragment{

    private FragmentMeetingsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentMeetingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.textMeetings.setText("This is the Meetings Fragment");
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}