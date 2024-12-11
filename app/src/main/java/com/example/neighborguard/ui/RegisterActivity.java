package com.example.neighborguard.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.neighborguard.MainActivity;
import com.example.neighborguard.api.ApiController;
import com.example.neighborguard.api.UserApi;
import com.example.neighborguard.databinding.ActivityRegisterBinding;
import com.example.neighborguard.model.Address;
import com.example.neighborguard.model.CurrentUserManager;
import com.example.neighborguard.model.ExtendedUser;
import com.example.neighborguard.model.LonLat;
import com.example.neighborguard.model.NewUser;
import com.example.neighborguard.model.User;
import com.example.neighborguard.model.UserGenderEnum;
import com.example.neighborguard.model.UserRoleEnum;
import com.example.neighborguard.utils.DialogUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class RegisterActivity extends AppCompatActivity {
    UserApi apiService = ApiController.getRetrofitInstance().create(UserApi.class);
    private ActivityRegisterBinding binding;
    private FirebaseAuth mAuth;

    private UserRoleEnum role;
    private String firstName;
    private String lastName;
    private String phoneNumber;

    private ArrayList<Integer> agesList;
    private int age;

    private ArrayList<String> gendersList;
    private UserGenderEnum gender;

    private ArrayList<String> languages;
    private String[] languagesArray;
    private boolean[] selectedLanguages;
    private ArrayList<Integer> languagesList;
    private ArrayList<String> selectedLanguagesList;

    private ArrayList<String> services;
    private String[] servicesArray;
    private boolean[] selectedServices;
    private ArrayList<Integer> servicesList;
    private ArrayList<String> selectedServicesList;

    private Address address;
    private String city;
    private String street;
    private int houseNumber;
    private int apartmentNumber;
    private String email;
    private String password;

    private LonLat lonLat;

    private final int CAM_REQ = 1000;
    private final int IMG_REQ = 2000;
    private String base64ProfileImage;
    private static final int PERMISSION_REQUEST_CODE = 3000;



    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            User user = new User();
            user.setEmail(currentUser.getEmail());
            switchToLoginActivity(user);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        mAuth = FirebaseAuth.getInstance();

        checkAndRequestPermissions();

        initViews();

    }


    private void initViews() {
        binding.registerBTNRegister.setOnClickListener(View -> registerClicked());
        binding.registerLBLLoginNow.setOnClickListener(View -> loginNowClicked());
        
        binding.registerCRDCamera.setOnClickListener(View -> cameraClicked());
        binding.registerCRDPhotos.setOnClickListener(View -> photosClicked());

        initAges();
        initGenders();
        initLanguages();
        initServices();
    }


    private void photosClicked() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                checkAndRequestPermissions();
                return;
            }
        } else if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            checkAndRequestPermissions();
            return;
        }

        Intent photoIntent = new Intent(Intent.ACTION_PICK);
        photoIntent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(photoIntent, IMG_REQ);
    }


    private void cameraClicked() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            checkAndRequestPermissions();
            return;
        }

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAM_REQ);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && data != null) {  // Changed from requestCode to resultCode
            if(requestCode == CAM_REQ) {
                try {
                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    if(bitmap != null) {
                        binding.registerIMGProfile.setImageBitmap(bitmap);
                        base64ProfileImage = convertBitmapToBase64(bitmap);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show();
                }
            }
            else if(requestCode == IMG_REQ) {
                try {
                    Uri imageUri = data.getData();
                    if(imageUri != null) {
                        binding.registerIMGProfile.setImageURI(imageUri);
                        base64ProfileImage = convertUriToBase64(imageUri);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 and above
            if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_CODE);
            }
        } else {
            // Below Android 13
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        }

        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
        }
    }


    private String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }


    private String convertUriToBase64(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            return convertBitmapToBase64(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private void initAges() {
        agesList = new ArrayList<>();
        for (int i = 0 ; i < 121 ; i++) {
            agesList.add(i);
        }

        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, agesList);
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        binding.registerSPNAge.setAdapter(adapter);
    }


    private void initGenders() {
        gendersList = new ArrayList<>();
        gendersList.add("Male");
        gendersList.add("Female");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, gendersList);
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        binding.registerSPNGender.setAdapter(adapter);
    }


    private void initLanguages() {
        languages = new ArrayList<>();
        languages.add("Hebrew");
        languages.add("English");
        languages.add("French");
        languages.add("Spanish");
        languages.add("German");
        languages.add("Russian");
        languages.add("Arabic");
        languages.add("Portuguese");

        languagesArray = languages.toArray(new String[0]);
        selectedLanguages = new boolean[languagesArray.length];
        languagesList = new ArrayList<>();
        selectedLanguagesList = new ArrayList<>();

        binding.registerLBLSelectLanguages.setOnClickListener(v ->
                DialogUtils.showMultiChoiceDialog(
                        this,
                        "Select Languages",
                        languagesArray,
                        selectedLanguages,
                        languagesList,
                        selectedLanguagesList,
                        binding.registerLBLSelectLanguages,
                        "Select Languages"
                )
        );
    }


    private void initServices() {
        services = new ArrayList<>();
        services.add("Handyman");
        services.add("Dog Walker");
        services.add("Groceries Shopping");

        servicesArray = services.toArray(new String[0]);
        selectedServices = new boolean[servicesArray.length];
        servicesList = new ArrayList<>();
        selectedServicesList = new ArrayList<>();

        binding.registerLBLSelectServices.setOnClickListener(v ->
                DialogUtils.showMultiChoiceDialog(
                        this,
                        "Select Services",
                        servicesArray,
                        selectedServices,
                        servicesList,
                        selectedServicesList,
                        binding.registerLBLSelectServices,
                        "Select Services"
                )
        );
    }


    private void loginNowClicked() {
        Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
        startActivity(intent);
        finish();
    }


    private void registerClicked() {
        binding.registerPBProgressBar.setVisibility(View.VISIBLE);

        NewUser newUser = new NewUser();
        address = new Address();
        lonLat = new LonLat();

        if(binding.registerRDBRecipient.isChecked())
            role = UserRoleEnum.RECIPIENT;
        else if(binding.registerRDBVolunteer.isChecked())
            role = UserRoleEnum.VOLUNTEER;
        newUser.setRole(this.role);

        if(TextUtils.isEmpty(base64ProfileImage)){
            binding.registerPBProgressBar.setVisibility(View.GONE);
            Toast.makeText(RegisterActivity.this, "Please select a profile image", Toast.LENGTH_SHORT).show();
            return;
        }
        newUser.setProfileImage(base64ProfileImage);

        firstName = String.valueOf(binding.registerEDTFirstName.getText());
        if (TextUtils.isEmpty(firstName)) {
            binding.registerPBProgressBar.setVisibility(View.GONE);
            Toast.makeText(RegisterActivity.this, "Enter First Name", Toast.LENGTH_SHORT).show();
            return;
        }
        newUser.setFirstName(this.firstName);

        lastName = String.valueOf(binding.registerEDTLastName.getText());
        if (TextUtils.isEmpty(lastName)) {
            binding.registerPBProgressBar.setVisibility(View.GONE);
            Toast.makeText(RegisterActivity.this, "Enter Last Name", Toast.LENGTH_SHORT).show();
            return;
        }
        newUser.setLastName(this.lastName);

        phoneNumber = String.valueOf(binding.registerEDTPhoneNumber.getText());
        if (TextUtils.isEmpty(String.valueOf(phoneNumber))) {
            binding.registerPBProgressBar.setVisibility(View.GONE);
            Toast.makeText(RegisterActivity.this, "Enter Phone Number", Toast.LENGTH_SHORT).show();
            return;
        }
        newUser.setPhoneNumber(this.phoneNumber);

//        binding.registerSPNAge.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                age = Integer.parseInt(parent.getItemAtPosition(position).toString());
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                // Leave empty or set age to default value
//                age = 0;
//            }
//        });
//        newUser.setAge(this.age);
//
//        binding.registerSPNGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                gender = UserGenderEnum.valueOf(parent.getItemAtPosition(position).toString().toUpperCase());
//
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//            }
//        });
//        newUser.setGender(this.gender);

        // Get age directly
        age = Integer.parseInt(binding.registerSPNAge.getSelectedItem().toString());
        newUser.setAge(this.age);

        // Get gender directly
        String selectedGender = binding.registerSPNGender.getSelectedItem().toString();
        gender = UserGenderEnum.valueOf(selectedGender.toUpperCase());
        newUser.setGender(this.gender);

        if (selectedLanguagesList.isEmpty()) {
            binding.registerPBProgressBar.setVisibility(View.GONE);
            Toast.makeText(RegisterActivity.this, "Please select at least one language", Toast.LENGTH_SHORT).show();
            return;
        }
        newUser.setLanguages(selectedLanguagesList);

        if (selectedServicesList.isEmpty()) {
            binding.registerPBProgressBar.setVisibility(View.GONE);
            Toast.makeText(RegisterActivity.this, "Please select at least one service", Toast.LENGTH_SHORT).show();
            return;
        }
        newUser.setServices(selectedServicesList);

        city = String.valueOf(binding.registerEDTCity.getText());
        if (TextUtils.isEmpty(city)) {
            binding.registerPBProgressBar.setVisibility(View.GONE);
            Toast.makeText(RegisterActivity.this, "Enter City", Toast.LENGTH_SHORT).show();
            return;
        }
        address.setCity(this.city);

        street = String.valueOf(binding.registerEDTStreet.getText());
        if (TextUtils.isEmpty(street)) {
            binding.registerPBProgressBar.setVisibility(View.GONE);
            Toast.makeText(RegisterActivity.this, "Enter Street", Toast.LENGTH_SHORT).show();
            return;
        }
        address.setStreet(this.street);

        houseNumber = Integer.parseInt(String.valueOf(binding.registerEDTHouseNumber.getText()));
        if (TextUtils.isEmpty(String.valueOf(houseNumber))) {
            binding.registerPBProgressBar.setVisibility(View.GONE);
            Toast.makeText(RegisterActivity.this, "Enter House Number", Toast.LENGTH_SHORT).show();
            return;
        }
        address.setHouseNumber(this.houseNumber);

        apartmentNumber = Integer.parseInt(String.valueOf(binding.registerEDTApartmentNumber.getText()));
        if (TextUtils.isEmpty(String.valueOf(apartmentNumber))) {
            binding.registerPBProgressBar.setVisibility(View.GONE);
            Toast.makeText(RegisterActivity.this, "Enter Apartment Number", Toast.LENGTH_SHORT).show();
            return;
        }
        address.setApartmentNumber(this.apartmentNumber);
        address.makeAddressString(city, street, houseNumber);
        newUser.setAddress(address);

        email = String.valueOf(binding.registerEDTEmail.getText());
        if (TextUtils.isEmpty(email)) {
            binding.registerPBProgressBar.setVisibility(View.GONE);
            Toast.makeText(RegisterActivity.this, "Enter Email", Toast.LENGTH_SHORT).show();
            return;
        }
        newUser.setEmail(this.email);

        password = String.valueOf(binding.registerEDTPassword.getText());
        if (TextUtils.isEmpty(password)) {
            binding.registerPBProgressBar.setVisibility(View.GONE);
            Toast.makeText(RegisterActivity.this, "Enter Password", Toast.LENGTH_SHORT).show();
            return;
        }
        newUser.setPassword(this.password);

        if(role == UserRoleEnum.RECIPIENT){
            //less then 1 KM
            lonLat.setLatitude(32.114059);
           lonLat.setLongitude(34.798969);
            //more then 1 KM
//            lonLat.setLatitude(34.886812);
//            lonLat.setLongitude(32.15549);
        }
        else{
            lonLat.setLatitude(32.119885);
            lonLat.setLongitude(34.800303);
        }
        newUser.setLonLat(lonLat);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        binding.registerPBProgressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Account created in firebase.",
                                    Toast.LENGTH_SHORT).show();


                            setNewUserInBackend(newUser);
                        }
                        else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                            // Get the specific error message
                            String errorMessage = task.getException() != null ?
                                    task.getException().getMessage() : "Authentication failed";
                            Log.e("RegisterActivity", "Registration failed: " + errorMessage, task.getException());

                            Toast.makeText(RegisterActivity.this, errorMessage,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void setNewUserInBackend(NewUser newUser){
        Call<User> call = apiService.createUser(newUser);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                User createdNewUser = response.body();
                if (response.isSuccessful()){
                    Toast.makeText(RegisterActivity.this, "User created successfully in backend!", Toast.LENGTH_LONG).show();
                    switchToLoginActivity(createdNewUser);
                } else {
                    Toast.makeText(RegisterActivity.this, "Failed to sign up user", Toast.LENGTH_LONG).show();
                    Log.e("Request failed: ", response.message());

                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }

        });
    }


    private void switchToLoginActivity(User user) {
        Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
        intent.putExtra("email", email);
        intent.putExtra("password", password);
        startActivity(intent);
        this.finish();
    }

}