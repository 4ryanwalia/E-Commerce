package com.nmims.zepto;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nmims.zepto.Product; // Ensure correct import

import java.util.ArrayList;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnQuantityChangeListener {

    private RecyclerView cartRecyclerView;
    private CartAdapter cartAdapter;
    private TextView totalPriceTextView;
    private Button checkoutButton;
    private TextView emptyCartTextView;
    private ArrayList<Product> cartItems = new ArrayList<>(); // Local list, correctly initialized
    private double walletBalance = 0.0;
    private FirebaseAuth mAuth;
    private DatabaseReference walletRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        mAuth = FirebaseAuth.getInstance();

        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        totalPriceTextView = findViewById(R.id.cartTotalPrice);
        checkoutButton = findViewById(R.id.checkoutButton);
        emptyCartTextView = findViewById(R.id.emptyCartTextView);

        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        // --- CRITICAL DATA RECEIVING and Adapter Setup---
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("cartItems")) {
            ArrayList<Product> receivedCartItems = intent.getParcelableArrayListExtra("cartItems");
            if (receivedCartItems != null) {
                cartItems.addAll(receivedCartItems); // Copy to the local list
                Log.d("CartActivity", "onCreate: Received cartItems size: " + cartItems.size());
                // Log each product received.  This is ESSENTIAL.
                for (Product p : cartItems) {
                    Log.d("CartActivity", "onCreate: Received Product: " + p.getName() + ", Price: " + p.getPrice() + ", Qty: " + p.getQuantity() + ", ImageURL: " + p.getImageUrl());
                }

            } else {
                Log.w("CartActivity", "onCreate: receivedCartItems is NULL");
            }
        } else {
            Log.w("CartActivity", "onCreate: Intent is NULL or does not have 'cartItems' extra");
        }



        cartAdapter = new CartAdapter(cartItems, totalPriceTextView, this, this);
        cartRecyclerView.setAdapter(cartAdapter);

        // Show/hide the "empty cart" message.
        updateEmptyCartView();
        getUserWalletBalance();

        checkoutButton.setOnClickListener(v -> attemptCheckout());
    }
    private void updateEmptyCartView() {
        if (cartItems.isEmpty()) {
            emptyCartTextView.setVisibility(View.VISIBLE);
            cartRecyclerView.setVisibility(View.GONE);
        } else {
            emptyCartTextView.setVisibility(View.GONE);
            cartRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void attemptCheckout() {
        double totalPrice = cartAdapter.getTotalPriceFromCart();

        // Check if the cart is empty *before* checking wallet balance.
        if (cartItems.isEmpty()) {
            Toast.makeText(CartActivity.this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
            return;
        }


        if (walletBalance < totalPrice) {
            Toast.makeText(CartActivity.this, "Insufficient funds!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(CartActivity.this, OrderActivity.class);
        intent.putExtra("totalPrice", totalPrice);
        intent.putExtra("walletBalance", walletBalance);
        intent.putParcelableArrayListExtra("updatedCartItems", cartItems); // Pass updated cart
        startActivity(intent);
        returnUpdatedCart();
    }


    private void getUserWalletBalance() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Handle not logged in
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity
            return; // IMPORTANT
        }

        String userId = currentUser.getUid();
        walletRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("walletBalance");

        walletRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        walletBalance = snapshot.getValue(Double.class); // Get as Double
                    } catch (Exception e) {
                        Log.e("CartActivity", "Error getting wallet balance", e);
                        walletBalance = 0.0; // Default to 0 on error
                    }
                } else {
                    walletBalance = 0.0;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CartActivity", "Error fetching wallet balance", error.toException());
                Toast.makeText(CartActivity.this, "Error: "+ error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void returnUpdatedCart() {
        Intent resultIntent = new Intent();
        resultIntent.putParcelableArrayListExtra("updatedCartItems", cartItems);
        setResult(RESULT_OK, resultIntent);
        //finish(); // No need to call finish here as onBackPressed will handle it.
    }

    @Override
    public void onBackPressed() {
        returnUpdatedCart();
        super.onBackPressed();
    }

    @Override
    public void onQuantityChanged() {
        Log.d("CartActivity", "onQuantityChanged: called"); // Check if this is called
        cartAdapter.updateTotalPrice();
        updateEmptyCartView(); // Update the empty cart view
    }
}