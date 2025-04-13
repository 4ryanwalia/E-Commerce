package com.nmims.zepto;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
public class GiftCardActivity extends AppCompatActivity {
    private EditText addMoneyInput;
    private TextView walletBalanceText;
    private Button addMoneyButton;
    private double walletBalance = 0.0;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gift_card);
        // Initialize FirebaseAuth and Realtime Database
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            userId = user.getUid();
        } else {
            Toast.makeText(this, "without login you cant come here..k", Toast.LENGTH_SHORT).show();
            finish();
        }
        // Initialize views
        addMoneyInput = findViewById(R.id.addMoneyInput);
        walletBalanceText = findViewById(R.id.walletBalanceText);
        addMoneyButton = findViewById(R.id.addMoneyButton);
        // Display current wallet balance
        getWalletBalanceFromRealtimeDatabase();
        // Button to add money to the wallet
        addMoneyButton.setOnClickListener(v -> {
            String input = addMoneyInput.getText().toString().trim();

            if (TextUtils.isEmpty(input)) {
                Toast.makeText(this, "(add500 or add1000)", Toast.LENGTH_SHORT).show();
                return;
            }
            if ("add500".equalsIgnoreCase(input)) {
                walletBalance += 500;
                updateWalletBalanceInRealtimeDatabase();
                Toast.makeText(this, "Added 500 to wallet", Toast.LENGTH_SHORT).show();
            } else if ("add1000".equalsIgnoreCase(input)) {
                walletBalance += 1000;
                updateWalletBalanceInRealtimeDatabase();
                Toast.makeText(this, "Added 1000 to wallet", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Invalid command. Use 'add500' or 'add1000'", Toast.LENGTH_SHORT).show();
            }

            updateWalletBalance();
        });
    }
    private void updateWalletBalance() {
        walletBalanceText.setText("Wallet Balance: " + walletBalance);
    }
    private void getWalletBalanceFromRealtimeDatabase() {
        usersRef.child(userId).child("walletBalance").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                walletBalance = task.getResult().getValue(Double.class);
                updateWalletBalance();
            } else {
                walletBalance = 0.0;
                updateWalletBalance();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error fetching balance: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
    private void updateWalletBalanceInRealtimeDatabase() {
        usersRef.child(userId).child("walletBalance").setValue(walletBalance)
                .addOnSuccessListener(aVoid -> {
                    // Successfully updated balance
                    Toast.makeText(this, "Wallet balance updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    } }
