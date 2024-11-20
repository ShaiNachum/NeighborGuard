package com.example.neighborguard.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.neighborguard.MainActivity;
import com.example.neighborguard.databinding.FragmentSettingsBinding;
import com.example.neighborguard.model.CurrentUserManager;
import com.example.neighborguard.model.User;
import com.example.neighborguard.ui.LogInActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsFragment extends Fragment{

    private FragmentSettingsBinding binding;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        if(firebaseUser == null){
            switchToLoginActivity();
            return;
        }

        binding.textSettings.setText("This is Settings Fragment");
        initViews();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    private void initViews() {
        binding.settingsBTNLogout.setOnClickListener(View -> logoutClicked());
    }

    private void logoutClicked() {
        FirebaseAuth.getInstance().signOut();
        switchToLoginActivity();
    }

    private void switchToLoginActivity() {
        Intent intent = new Intent(this.getActivity() , LogInActivity.class);
        startActivity(intent);

        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}
