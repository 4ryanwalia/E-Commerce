package com.nmims.zepto;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.collection.LLRBNode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orderList;
    private Context context;

    public OrderAdapter(List<Order> orderList, Context context) {
        this.orderList = orderList;
        this.context = context;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.orderIdTextView.setText("Order #" + order.getTimestamp()); // Use timestamp as a simple ID
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String formattedDate = sdf.format(new Date(order.getTimestamp()));
        holder.orderDateTextView.setText(formattedDate);
        holder.orderTotalTextView.setText("Total: ₹" + String.format("%.2f", order.getTotalAmount()));
        holder.orderStatusTextView.setText("Status: "+ order.getStatus());

        // Set up the inner RecyclerView for products
        ProductSmallAdapter productAdapter = new ProductSmallAdapter(order.getProducts(), context);
        holder.orderItemsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        holder.orderItemsRecyclerView.setAdapter(productAdapter);

    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdTextView, orderDateTextView, orderTotalTextView, orderStatusTextView;
        RecyclerView orderItemsRecyclerView; // RecyclerView for products within the order

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdTextView = itemView.findViewById(R.id.orderIdTextView);
            orderDateTextView = itemView.findViewById(R.id.orderDateTextView);
            orderTotalTextView = itemView.findViewById(R.id.orderTotalTextView);
            orderStatusTextView = itemView.findViewById(R.id.orderStatusTextView);
            orderItemsRecyclerView = itemView.findViewById(R.id.orderItemsRecyclerView); // Initialize RecyclerView
        }
    }
    public void setOrders(List<Order> orders) {
        this.orderList = orders;
        notifyDataSetChanged();
    }
}