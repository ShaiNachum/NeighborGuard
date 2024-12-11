package com.example.neighborguard.ui.home;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.neighborguard.databinding.FragmentListBinding;
import com.example.neighborguard.interfaces.Callback_recipientClicked;




public class ListFragment extends Fragment {
    private FragmentListBinding binding;

    private Callback_recipientClicked callbackRecipientClicked;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews();
    }

    private void initViews() {
        binding.listBTNSend.setOnClickListener(v -> itemClicked(32.1212, 34.1212));
    }

    private void itemClicked(double lat, double lng){
        if (callbackRecipientClicked != null) {
            callbackRecipientClicked.recipientClicked(lat, lng);
        }
    }

    public void setCallbackRecipientClicked(Callback_recipientClicked callbackRecipientClicked) {
        this.callbackRecipientClicked = callbackRecipientClicked;
    }
}