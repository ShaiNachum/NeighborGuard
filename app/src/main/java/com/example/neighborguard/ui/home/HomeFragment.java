package com.example.neighborguard.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.neighborguard.databinding.FragmentHomeBinding;
import com.example.neighborguard.model.CurrentUserManager;


public class HomeFragment extends Fragment {

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

        binding.textHome.setText("Hello" + currentUserManager.getUser().getEmail());
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}