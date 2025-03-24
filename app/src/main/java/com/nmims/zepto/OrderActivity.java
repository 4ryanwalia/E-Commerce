package com.nmims.zepto;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OrderActivity extends AppCompatActivity {
    private static final String TAG = "OrderActivity";

    private TextView availableBalanceTextView, totalPriceTextView, finalPriceTextView;
    private Button orderButton, applyCouponButton, changeAddressButton;
    private TextInputLayout couponInputLayout;
    private TextInputEditText couponEditText;
    private ProgressBar progressBar;
    private double walletBalance = 0.0;
    private double totalPrice = 0.0;
    private double finalPrice = 0.0;
    private DatabaseReference userRef;
    private ArrayList<Product> cartItems;
    private boolean couponApplied = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        // Initialize UI components
        availableBalanceTextView = findViewById(R.id.availableBalanceTextView);
        totalPriceTextView = findViewById(R.id.totalPriceTextView);
        finalPriceTextView = findViewById(R.id.finalPriceTextView);
        orderButton = findViewById(R.id.orderButton);
        applyCouponButton = findViewById(R.id.applyCouponButton);
        changeAddressButton = findViewById(R.id.changeAddressButton);
        progressBar = findViewById(R.id.progressBar);
        couponInputLayout = findViewById(R.id.couponInputLayout);
        couponEditText = findViewById(R.id.couponEditText);

        // Get cart details
        totalPrice = getIntent().getDoubleExtra("totalPrice", 0.0);
        cartItems = getIntent().getParcelableArrayListExtra("updatedCartItems");

        if (cartItems == null || cartItems.isEmpty()) {
            Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        totalPriceTextView.setText("Total: ₹" + String.format("%.2f", totalPrice));
        finalPrice = totalPrice;
        finalPriceTextView.setText("Final Total: ₹" + String.format("%.2f", finalPrice));

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userId = currentUser.getUid();
        userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        // Fetch wallet balance
        fetchWalletBalance();

        // Button actions
        applyCouponButton.setOnClickListener(v -> applyDiscountCoupon());
        orderButton.setOnClickListener(v -> showConfirmationDialog());
        changeAddressButton.setOnClickListener(v -> changeAddress());
    }

    private void fetchWalletBalance() {
        userRef.child("walletBalance").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    walletBalance = dataSnapshot.getValue(Double.class) != null ? dataSnapshot.getValue(Double.class) : 0.0;
                } else {
                    walletBalance = 0.0;
                }
                availableBalanceTextView.setText("Available Balance: ₹" + String.format("%.2f", walletBalance));
                updateOrderButtonState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error fetching wallet balance", databaseError.toException());
                Toast.makeText(OrderActivity.this, "Error fetching wallet balance", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateOrderButtonState() {
        if (walletBalance >= finalPrice) {
            orderButton.setEnabled(true);
            orderButton.setText("Place Order");
        } else {
            orderButton.setEnabled(false);
            orderButton.setText("Insufficient Funds");
        }
    }

    private void applyDiscountCoupon() {
        String couponCode = couponEditText.getText().toString().trim();

        // If no coupon is entered
        if (couponCode.isEmpty()) {
            couponInputLayout.setError("Enter a coupon code");
            return;
        }

        // If a coupon is already applied
        if (couponApplied) {
            Toast.makeText(this, "Coupon already applied!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check for valid coupons
        if ("GET10".equalsIgnoreCase(couponCode)) {
            double discount = totalPrice * 0.10;  // 10% discount
            finalPrice = totalPrice - discount;  // Update final price

            // Update UI
            finalPriceTextView.setText("Final Total: ₹" + String.format("%.2f", finalPrice));
            couponInputLayout.setError(null);  // Clear error if any
            couponEditText.setEnabled(false);  // Disable editing after applying
            applyCouponButton.setEnabled(false);  // Disable button after applying
            couponApplied = true;

            Toast.makeText(this, "10% discount applied!", Toast.LENGTH_SHORT).show();

            Log.d(TAG, "Coupon applied: GET10 | Discount: ₹" + discount + " | Final Price: ₹" + finalPrice);
        } else {
            couponInputLayout.setError("Invalid coupon code");
            Log.d(TAG, "Invalid coupon entered: " + couponCode);
        }

        updateOrderButtonState();  // Ensure button is enabled/disabled correctly
    }
    private void showConfirmationDialog() {
        if (finalPrice > walletBalance) {
            Toast.makeText(this, "Insufficient funds!", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Confirm Order")
                .setMessage("Are you sure you want to place this order?")
                .setPositiveButton("Yes", (dialog, which) -> placeOrder())
                .setNegativeButton("No", null)
                .show();
    }

    private void placeOrder() {
        progressBar.setVisibility(View.VISIBLE);
        orderButton.setEnabled(false);

        double newBalance = walletBalance - finalPrice;
        userRef.child("walletBalance").setValue(newBalance)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Wallet updated");
                    createOrder();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating wallet", e);
                    Toast.makeText(this, "Wallet update failed", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    orderButton.setEnabled(true);
                });
    }

    private void createOrder() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        long timestamp = System.currentTimeMillis();
        Order order = new Order(userId, "pending", timestamp, finalPrice, cartItems, new HashMap<>());
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders");

        ordersRef.push().setValue(order)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Order placed successfully");
                    Toast.makeText(this, "Order Placed!", Toast.LENGTH_SHORT).show();
                    HomePageActivity.cartItems.clear();
                    startActivity(new Intent(this, HomePageActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving order", e);
                    Toast.makeText(this, "Order failed", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    orderButton.setEnabled(true);
                });
    }

    private void changeAddress() {
        Toast.makeText(this, "Feature coming soon!", Toast.LENGTH_SHORT).show();
    }
}
