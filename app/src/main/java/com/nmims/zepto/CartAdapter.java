package com.nmims.zepto;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.nmims.zepto.Product; // Ensure correct import

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<Product> productList;
    private TextView totalPriceTextView;
    private Context context;
    private OnQuantityChangeListener quantityChangeListener;

    // Interface for quantity changes
    public interface OnQuantityChangeListener {
        void onQuantityChanged();
    }

    public CartAdapter(List<Product> productList, TextView totalPriceTextView, Context context, OnQuantityChangeListener listener) {
        this.productList = productList;
        this.totalPriceTextView = totalPriceTextView;
        this.context = context;
        this.quantityChangeListener = listener;
        updateTotalPrice(); // Initialize total price
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        Log.d("CartAdapter", "onCreateViewHolder: called"); // Check if this is called
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        if (position < 0 || position >= productList.size()) {
            Log.e("CartAdapter", "onBindViewHolder: Invalid position: " + position);
            return; // Prevent IndexOutOfBoundsException
        }

        Product product = productList.get(position);
        Log.d("CartAdapter", "onBindViewHolder: position=" + position + ", product=" + (product != null ? product.getName() : "null"));

        if (product != null) { // Check for null product
            holder.productNameTextView.setText(product.getName());
            holder.productPriceTextView.setText(formatPrice(product.getPrice()));

            // Load image using Glide (use the stored context)
            Glide.with(context)
                    .load(product.getImageUrl())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.logo) // Use your placeholder
                            .error(R.drawable.lays)) // Use your error image
                    .into(holder.productImageView);

            holder.quantityTextView.setText(String.valueOf(product.getQuantity()));

            holder.minusButton.setOnClickListener(v -> {
                int quantity = product.getQuantity();
                if (quantity > 1) {
                    product.setQuantity(quantity - 1);
                } else {
                    productList.remove(position);  // Remove the item if quantity is 1
                }
                notifyDataSetChanged(); // CRITICAL: Update the RecyclerView
                if (quantityChangeListener != null) {
                    quantityChangeListener.onQuantityChanged();
                }
            });

            holder.plusButton.setOnClickListener(v -> {
                product.setQuantity(product.getQuantity() + 1);
                notifyDataSetChanged(); // CRITICAL: Update the RecyclerView
                if (quantityChangeListener != null) {
                    quantityChangeListener.onQuantityChanged();
                }
            });

        } else {
            Log.e("CartAdapter", "onBindViewHolder: product is NULL at position: " + position);
        }
    }
    @Override
    public int getItemCount() {
        int count = productList.size();
        Log.d("CartAdapter", "getItemCount: returning " + count); // Check the returned count
        return count;
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView productImageView;
        TextView productNameTextView, productPriceTextView, quantityTextView;
        Button minusButton, plusButton;

        public CartViewHolder(View itemView) {
            super(itemView);
            productImageView = itemView.findViewById(R.id.productImageView);
            productNameTextView = itemView.findViewById(R.id.productNameTextView);
            productPriceTextView = itemView.findViewById(R.id.productPriceTextView);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);
            minusButton = itemView.findViewById(R.id.minusButton);
            plusButton = itemView.findViewById(R.id.plusButton);

            // Add null checks here, and log if any view is null
            if (productImageView == null) Log.e("CartViewHolder", "productImageView is NULL");
            if (productNameTextView == null) Log.e("CartViewHolder", "productNameTextView is NULL");
            if (productPriceTextView == null) Log.e("CartViewHolder", "productPriceTextView is NULL");
            if (quantityTextView == null) Log.e("CartViewHolder", "quantityTextView is NULL");
            if (minusButton == null) Log.e("CartViewHolder", "minusButton is NULL");
            if (plusButton == null) Log.e("CartViewHolder", "plusButton is NULL");

        }
    }

    private String formatPrice(double price) {
        return NumberFormat.getCurrencyInstance(new Locale("en", "IN")).format(price);
    }

    public void updateTotalPrice() {
        double total = 0;
        for (Product product : productList) {
            total += product.getPrice() * product.getQuantity();
        }
        Log.d("CartAdapter","updateTotalPrice :"+total); //log total price.
        totalPriceTextView.setText("Total: " + formatPrice(total));
    }

    public double getTotalPriceFromCart() {
        double total = 0;
        for (Product product : productList) {
            total += product.getPrice() * product.getQuantity();
        }
        return total;
    }
}