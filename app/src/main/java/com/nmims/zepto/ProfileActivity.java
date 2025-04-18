package com.nmims.zepto;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {
    private TextView userNameTextView, userEmailTextView, userPhoneTextView;
    private static final int EDIT_PROFILE_REQUEST_CODE = 1;
    private ImageView profileImageView;
    private DatabaseReference userRef;
    private String currentProfileImageUrl;
    private static final String TAG = "ProfileActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userNameTextView = findViewById(R.id.userNameTextView);
        userEmailTextView = findViewById(R.id.userEmailTextView);
        userPhoneTextView = findViewById(R.id.userPhoneTextView);
        profileImageView = findViewById(R.id.profileImageView);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        String userId = currentUser.getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);

        loadUserInfo(); //it will load the data form the firebase

        TextView editProfile = findViewById(R.id.editProfile);
        TextView recentOrders = findViewById(R.id.recentOrders);
        TextView customerSupport = findViewById(R.id.customerSupport);
        TextView myGiftCard = findViewById(R.id.myGiftCard);
        TextView logout = findViewById(R.id.logout);


        editProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivityForResult(intent, EDIT_PROFILE_REQUEST_CODE);
        });

        recentOrders.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, RecentOrdersActivity.class);
            startActivity(intent);
        });
        customerSupport.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, SupportActivity.class);
            startActivity(intent);
        });
        myGiftCard.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, GiftCardActivity.class);
            startActivity(intent);

        });
        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            SharedPreferences sharedPreferences = getSharedPreferences("user_data", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

    }
    private void loadUserInfo() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String phone = snapshot.child("mobile").getValue(String.class);
                    String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);

                    userNameTextView.setText(name);
                    userEmailTextView.setText(email); //set email
                    userPhoneTextView.setText(phone);

                    // Load the profile image using Glide
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        currentProfileImageUrl = profileImageUrl; // Store the URL
                        Glide.with(ProfileActivity.this)
                                .load(profileImageUrl)
                                .placeholder(R.drawable.ic_profile)
                                .error(R.drawable.lays)
                                .into(profileImageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "error loadi...", error.toException());
                Toast.makeText(ProfileActivity.this, "blah blah", Toast.LENGTH_SHORT).show();
            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_PROFILE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                String updatedName = data.getStringExtra("name");
                String updatedPhone = data.getStringExtra("phone");
                String updatedImageUrl = data.getStringExtra("profileImageUrl");

                if (updatedName != null) {
                    userNameTextView.setText(updatedName);
                }
                if (updatedPhone != null) {
                    userPhoneTextView.setText(updatedPhone);
                }
                if (updatedImageUrl != null) {
                    currentProfileImageUrl = updatedImageUrl;
                    Glide.with(this).load(currentProfileImageUrl).into(profileImageView); // And load it
                }

            }
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
        }
    }
}