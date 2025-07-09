package com.example.neighborguard.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.neighborguard.MainActivity;
import com.example.neighborguard.api.ApiController;
import com.example.neighborguard.api.UserApi;
import com.example.neighborguard.databinding.ActivityLogInBinding;
import com.example.neighborguard.model.CurrentUserManager;
import com.example.neighborguard.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LogInActivity extends AppCompatActivity {

    private ActivityLogInBinding binding;
    private FirebaseAuth mAuth;
    UserApi apiService = ApiController.getRetrofitInstance().create(UserApi.class);

    private String email;
    private String password;



    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            email = currentUser.getEmail();
            getUserByEmail(email);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLogInBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        this.email = intent.getStringExtra("email");
        this.password = intent.getStringExtra("password");

        if (email != null && password != null)
            getUserByEmail(email);

        initViews();
    }


    private void initViews() {
        binding.loginBTNLogin.setOnClickListener(View -> loginClicked());
        binding.loginLBLRegisterNow.setOnClickListener(View -> registerNowClicked());
    }


    private void registerNowClicked() {
        Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(intent);
        finish();
    }


    private void loginClicked() {
        binding.loginPBProgressBar.setVisibility(View.VISIBLE);

        String email = String.valueOf(binding.loginEDTEmail.getText());
        String password = String.valueOf(binding.loginEDTPassword.getText());

        if (TextUtils.isEmpty(email)) {
            binding.loginPBProgressBar.setVisibility(View.GONE);
            Toast.makeText(LogInActivity.this, "Enter Email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.loginPBProgressBar.setVisibility(View.GONE);
            Toast.makeText(LogInActivity.this, "Enter Password", Toast.LENGTH_SHORT).show();
            return;
        }

        doLogin(email, password);
    }


    private void doLogin(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        binding.loginPBProgressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(LogInActivity.this, "Login Successful.", Toast.LENGTH_SHORT).show();
                            getUserByEmail(email);
                        } else {
                            // If sign in fails, display a message to the user.

                            Toast.makeText(LogInActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void getUserByEmail(String email) {
        Call<User> call = apiService.getUserByEmail(email);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    Toast.makeText(LogInActivity.this, "Hello " + user.getEmail(), Toast.LENGTH_LONG).show();
                    switchToMainActivity(user);
                } else {
                    Toast.makeText(LogInActivity.this, "Failed to log in ", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("Retrofit", "Network error or failure: " + t.getMessage());
                Toast.makeText(LogInActivity.this, "SHIT HAPPENS ;)", Toast.LENGTH_LONG).show();
            }
        });
    }


    private void switchToMainActivity(User user) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        CurrentUserManager manager = CurrentUserManager.getInstance();
        manager.setUser(user);
        startActivity(intent);
        this.finish();
    }
}