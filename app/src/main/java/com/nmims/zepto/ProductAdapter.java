//ProductAdapter.java (Updated)
package com.nmims.zepto;

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

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    private OnAddToCartClickListener listener;

    public interface OnAddToCartClickListener {
        // Change parameter to Product object
        void onAddToCartClick(Product product);
    }

    public ProductAdapter(List<Product> productList, OnAddToCartClickListener listener) {
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);  // Make sure this layout exists!
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.productNameTextView.setText(product.getName());
        // Format the price before setting it
        holder.productPriceTextView.setText(formatPrice(product.getPrice()));

        // Load image using Glide with placeholder and error handling
        Glide.with(holder.itemView.getContext())
                .load(product.getImageUrl())
                .apply(new RequestOptions()
                        .placeholder(R.drawable.logo) // Use your placeholder
                        .error(R.drawable.maxlays))   // Use your error image
                .into(holder.productImageView);


        holder.addToCartButton.setOnClickListener(v -> {
            if (listener != null) {
                // Pass the Product object, not the position
                listener.onAddToCartClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImageView;
        TextView productNameTextView;
        TextView productPriceTextView;
        Button addToCartButton;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImageView = itemView.findViewById(R.id.productImageView);
            productNameTextView = itemView.findViewById(R.id.productNameTextView);
            productPriceTextView = itemView.findViewById(R.id.productPriceTextView);
            addToCartButton = itemView.findViewById(R.id.addToCartButton);
        }
    }

    // Helper method to format the price
    private String formatPrice(double price) {
        return NumberFormat.getCurrencyInstance(new Locale("en", "IN")).format(price);
    }

    // Method to update the product list (useful for filtering/searching)
    public void setProducts(List<Product> products) {
        this.productList = products;
        notifyDataSetChanged();
    }
}