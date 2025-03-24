package com.nmims.zepto;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nmims.zepto.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomePageActivity extends AppCompatActivity implements ProductAdapter.OnAddToCartClickListener {
    // NOTE: Using a static list for cartItems is not ideal long-term. Consider a ViewModel.
    public static List<Product> cartItems = new ArrayList<>();
    private final List<Product> productList = new ArrayList<>();  // Full list of products
    private final List<Product> filteredList = new ArrayList<>(); // List for filtered/searched products
    private FloatingActionButton cartView;
    private BadgeDrawable badgeDrawable;
    private ProductAdapter productAdapter;
    private RecyclerView productRecyclerView;
    private static final int CART_REQUEST_CODE = 123;
    private EditText searchBar; // Reference to the search bar
    private Button chipsButton, cokeButton, chocolateButton, allProductsButton;


    @OptIn(markerClass = ExperimentalBadgeUtils.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        productRecyclerView = findViewById(R.id.productRecyclerView);
        cartView = findViewById(R.id.cartView);
        ImageView profileIcon = findViewById(R.id.profileIcon);
        searchBar = findViewById(R.id.searchBar); // Get reference
        chipsButton = findViewById(R.id.chipsButton);
        cokeButton = findViewById(R.id.cokeButton);
        chocolateButton = findViewById(R.id.chocolateButton);
        allProductsButton = findViewById(R.id.allProductsButton);

        if (profileIcon != null) {
            profileIcon.setOnClickListener(view -> {
                Intent intent = new Intent(HomePageActivity.this, ProfileActivity.class);
                startActivity(intent);
            });
        } else {
            Log.e("HomePageActivity", "Profile icon not found!");
        }

        // Initialize the adapter with the *filtered* list.
        productAdapter = new ProductAdapter(filteredList, this); // 'this' for click listener
        //Important: Set Layout manager *before* setting adapter.
        productRecyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns
        productRecyclerView.setAdapter(productAdapter);



        // Set up the cart FAB.
        cartView.setOnClickListener(v -> {
            // Check if user is logged in BEFORE starting CartActivity
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                Log.d("HomePageActivity", "cartView clicked. cartItems size: " + cartItems.size());
                Intent intent = new Intent(HomePageActivity.this, CartActivity.class);
                // ***** CRITICAL: Put the cartItems into the Intent *****
                intent.putParcelableArrayListExtra("cartItems", new ArrayList<>(cartItems));
                startActivityForResult(intent, CART_REQUEST_CODE); // Use startActivityForResult
            } else {
                // User is not logged in, show a message or go to login
                Toast.makeText(HomePageActivity.this, "Please log in to view your cart.", Toast.LENGTH_SHORT).show();
                Intent loginIntent = new Intent(HomePageActivity.this, MainActivity.class); // Or your LoginActivity
                startActivity(loginIntent);
            }
        });

        // Initialize badge. Do this *after* findViewById, but it doesn't need to be in cartView.post
        badgeDrawable = BadgeDrawable.create(this);
        badgeDrawable.setBackgroundColor(getResources().getColor(R.color.black, getTheme())); // Use getTheme() for API < 23
        badgeDrawable.setBadgeTextColor(getResources().getColor(android.R.color.white, getTheme()));
        // Initial badge is hidden. It's updated in updateCartBadge()
        badgeDrawable.setVisible(false);


        // Get a reference to the "products" node in your database.
        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference("products");

        // Fetch products from Firebase.
        productsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                productList.clear(); // Clear previous data.
                filteredList.clear(); // Also clear the filtered list.
                for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                    Product product = productSnapshot.getValue(Product.class);
                    if (product != null) {
                        productList.add(product);
                        filteredList.add(product); // Initially, filtered list is the same.
                    }
                }
                productAdapter.setProducts(filteredList);// Update adapter.
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("HomePageActivity", "Error fetching products: " + databaseError.getMessage());
                Toast.makeText(HomePageActivity.this, "Error fetching products", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up search bar listener
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });


        // Set up category button listeners
        allProductsButton.setOnClickListener(v -> showAllProducts());
        chipsButton.setOnClickListener(v -> filterByCategory("chips"));
        cokeButton.setOnClickListener(v -> filterByCategory("coke"));
        chocolateButton.setOnClickListener(v -> filterByCategory("chocolate"));

    }

    private void filterByCategory(String category) {
        filteredList.clear();
        if (category.equals("all")) {
            filteredList.addAll(productList); // Show all products
        } else {
            for (Product product : productList) {
                if (product.getCategory() != null && product.getCategory().equalsIgnoreCase(category)) {
                    filteredList.add(product);
                }
            }
        }
        productAdapter.setProducts(filteredList); // Update adapter with the correct list
    }
    private void showAllProducts() {
        filteredList.clear();
        filteredList.addAll(productList);
        productAdapter.setProducts(filteredList); // Update adapter
    }

    private void filterProducts(String searchText) {
        filteredList.clear();
        if (searchText.isEmpty()) {
            filteredList.addAll(productList); // If search is empty, show all.
        } else {
            String query = searchText.toLowerCase(Locale.getDefault());
            for (Product product : productList) {
                //Check for null before toLowerCase to prevent crashes.
                if (product.getName() != null && product.getName().toLowerCase(Locale.getDefault()).contains(query)) {
                    filteredList.add(product);
                }
            }
        }
        productAdapter.setProducts(filteredList); // Important: Update the RecyclerView
    }



    @OptIn(markerClass = ExperimentalBadgeUtils.class)
    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge();
    }

    //method to update cart badge
    @OptIn(markerClass = ExperimentalBadgeUtils.class)
    private void updateCartBadge() {
        // Set the number and visibility *before* attaching.
        badgeDrawable.setNumber(cartItems.size());
        badgeDrawable.setVisible(cartItems.size() > 0);

        // --- CRITICAL FIX: Use post() and getApplicationContext() ---
        cartView.post(() -> {
            FrameLayout cartViewContainer = findViewById(R.id.cartViewContainer);
            if (cartViewContainer != null) { // Null check
                try {
                    // Attach the badge *after* layout is complete.
                    BadgeUtils.attachBadgeDrawable(badgeDrawable, cartView, cartViewContainer);
                } catch (Exception e) {
                    Log.e("HomePageActivity", "Error attaching badge: " + e.getMessage());
                }
            } else {
                Log.e("HomePageActivity", "cartViewContainer is NULL");
            }
        });
    }



    // Implement the click listener interface and receive a *Product* object.
    @Override
    public void onAddToCartClick(Product product) {
        Log.d("HomePageActivity", "onAddToCartClick: Product Name = " + (product != null ? product.getName() : "null"));

        if (product != null) { // Check for null product
            boolean found = false;
            for (Product p : cartItems) {
                if (p.getName().equals(product.getName())) {
                    p.setQuantity(p.getQuantity() + 1);
                    found = true;
                    break;
                }
            }
            if (!found) {
                // Add a *copy* of the product with quantity 1.  DO NOT modify the original.
                cartItems.add(new Product(product.getName(), product.getPrice(), product.getImageUrl(), product.getSize(), 1, product.getCategory()));
            }
            updateCartBadge();
            Toast.makeText(this, product.getName() + " added to cart", Toast.LENGTH_SHORT).show();

        } else{
            Log.e("HomePageActivity", "onAddToCartClick: product is NULL");
            Toast.makeText(this, "Error: Could not add product", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CART_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra("updatedCartItems")) {
                ArrayList<Product> updatedCartItems = data.getParcelableArrayListExtra("updatedCartItems");
                if(updatedCartItems != null){
                    cartItems.clear();
                    cartItems.addAll(updatedCartItems);
                    updateCartBadge();
                }
            }

        }
    }
}