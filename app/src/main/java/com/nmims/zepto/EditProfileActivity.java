package com.nmims.zepto;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";

    private EditText editTextName, editTextPhone;
    private ProgressBar progressBar;
    private ImageView profileImageView;
    private Button saveButton;
    private Uri imageUri = null; // Store the selected image URI
    private DatabaseReference userRef; // Keep this as a class-level variable
    private boolean isCloudinaryInitialized = false;

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            imageUri = result.getData().getData();
                            Glide.with(this).load(imageUri).into(profileImageView);
                        }
                    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        editTextName = findViewById(R.id.nameEditText);
        editTextPhone = findViewById(R.id.mobileEditText);
        profileImageView = findViewById(R.id.profileImageView);
        saveButton = findViewById(R.id.saveButton);
        progressBar = findViewById(R.id.progressBar);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String userId = currentUser.getUid();
        // Initialize userRef ONLY ONCE here in onCreate
        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);

        loadExistingUserInfo();

        profileImageView.setOnClickListener(v -> openImageChooser());
        saveButton.setOnClickListener(v -> saveProfileDetails());

        initCloudinary();
    }
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    private void initCloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dudr0rkup");
        config.put("api_key", "584465432458657");
        config.put("api_secret", "rfXmwyUj5ryEzHJg2maa7dMypwg");
        try {
            MediaManager.init(this, config);
            isCloudinaryInitialized = true;
            Log.d(TAG, "Cloudinary initialized successfully.");
        } catch (Exception e) {
            Log.e(TAG, "Cloudinary init failed: " + e.getMessage());
            Toast.makeText(this, "Cloudinary initialization failed", Toast.LENGTH_SHORT).show();
            saveButton.setEnabled(false); // Disable image upload
        }
    }

    private void loadExistingUserInfo() {
        // Use the existing userRef
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String mobile = snapshot.child("mobile").getValue(String.class);
                    String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);

                    editTextName.setText(name);
                    editTextPhone.setText(mobile);

                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(EditProfileActivity.this)
                                .load(profileImageUrl)
                                .placeholder(R.drawable.ic_profile)
                                .error(R.drawable.lays)
                                .into(profileImageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading user info", error.toException());
                Toast.makeText(EditProfileActivity.this, "Error loading user info", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfileDetails() {
        String name = editTextName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (phone.length() != 10) {
            Toast.makeText(this, "Please enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        if (imageUri != null && isCloudinaryInitialized) {
            uploadToCloudinary(imageUri, name, phone);
        } else {
            updateUserData(name, phone, null); // No new image
        }
    }

    private void uploadToCloudinary(Uri imageUri, final String name, final String phone) {
        MediaManager.get().upload(imageUri)
                .option("folder", "profile_images/")
                .option("resource_type", "image")
                .option("upload_preset", "profile_image_upload") // Correct preset name
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d(TAG, "onStart: Uploading image...");
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        Double progress = (double) bytes / totalBytes;
                        Log.d(TAG, "onProgress: Upload progress: " + (progress * 100) + "%");
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = (String) resultData.get("secure_url");
                        Log.d(TAG, "onSuccess: Image uploaded: " + imageUrl);
                        progressBar.setVisibility(View.GONE);
                        updateUserData(name, phone, imageUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Log.e(TAG, "onError: Upload error: " + error.getDescription());
                        Toast.makeText(EditProfileActivity.this, "Upload error: " + error.getDescription(), Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        Log.d(TAG, "onReschedule: " + error.getDescription());
                    }
                })
                .dispatch();
    }

    private void updateUserData(String name, String phone, String profileImageUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("mobile", phone);
        if (profileImageUrl != null) {
            updates.put("profileImageUrl", profileImageUrl); // Save URL to Firebase
        }

        // Use the consistently initialized userRef
        userRef.updateChildren(updates)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE); // Hide progress bar
                    if (task.isSuccessful()) {
                        // Prepare result to be sent back to ProfileActivity
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("name", name);
                        resultIntent.putExtra("phone", phone);
                        if (profileImageUrl != null) {
                            resultIntent.putExtra("profileImageUrl", profileImageUrl);
                        }
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();


                    } else {
                        Log.e(TAG, "Failed to update user data", task.getException());
                        Toast.makeText(EditProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}