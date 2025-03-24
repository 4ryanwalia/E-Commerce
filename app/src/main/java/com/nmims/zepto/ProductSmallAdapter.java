package com.nmims.zepto;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class ProductSmallAdapter extends RecyclerView.Adapter<ProductSmallAdapter.ProductViewHolder> {

    private List<Product> productList;
    private Context context;

    public ProductSmallAdapter(List<Product> productList, Context context) {
        this.productList = productList;
        this.context = context;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_item_small, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.productName.setText(product.getName());
        holder.productQuantity.setText("Qty: " + product.getQuantity());
        holder.productPrice.setText("₹" + String.format("%.2f", product.getPrice()));

        // Use Glide to load the image
        Glide.with(context)
                .load(product.getImageUrl())
                .placeholder(R.drawable.lays) // Placeholder image
                .error(R.drawable.lays) // Error image
                .into(holder.productImage);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productQuantity, productPrice;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImageSmall);
            productName = itemView.findViewById(R.id.productNameSmall);
            productQuantity = itemView.findViewById(R.id.productQuantitySmall);
            productPrice = itemView.findViewById(R.id.productPriceSmall);
        }
    }
}