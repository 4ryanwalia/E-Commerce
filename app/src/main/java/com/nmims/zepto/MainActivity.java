package com.nmims.zepto;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        TextView textViewRegister = findViewById(R.id.textViewRegister);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");

        // Check if the user is already logged in
        SharedPreferences sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            // User is already logged in, navigate to HomePageActivity
            startActivity(new Intent(MainActivity.this, HomePageActivity.class));
            finish(); // Close MainActivity
        }

        buttonLogin.setOnClickListener(v -> {
            String email = editTextUsername.getText().toString();
            String password = editTextPassword.getText().toString();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(MainActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            try {
                                // Check if email is verified
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null && user.isEmailVerified()) {
                                    // Store login status in SharedPreferences
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putBoolean("isLoggedIn", true);
                                    editor.apply();

                                    // Check if walletBalance exists for the user in Realtime Database
                                    String userId = user.getUid();
                                    usersRef.child(userId).child("walletBalance").get().addOnCompleteListener(walletTask -> {
                                        if (walletTask.isSuccessful()) {
                                            if (walletTask.getResult().getValue() == null) {
                                                // If walletBalance does not exist, set it to 0.0
                                                usersRef.child(userId).child("walletBalance").setValue(0.0)
                                                        .addOnSuccessListener(aVoid -> {
                                                            Toast.makeText(MainActivity.this, "Wallet balance initialized to 0.0", Toast.LENGTH_SHORT).show();
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Toast.makeText(MainActivity.this, "Error initializing wallet balance: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        });
                                            }
                                        } else {
                                            Toast.makeText(MainActivity.this, "Error fetching wallet balance: " + walletTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                    Toast.makeText(MainActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(MainActivity.this, HomePageActivity.class));
                                    finish(); // Close MainActivity after successful login
                                } else if (user != null) {
                                    Toast.makeText(MainActivity.this, "Please verify your email address.", Toast.LENGTH_LONG).show();
                                }
                            } catch (Exception e) {
                                Log.e("MainActivity", "Error after successful login", e);
                                Toast.makeText(MainActivity.this, "An error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        textViewRegister.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, RegisterActivity.class)));
    }
}
