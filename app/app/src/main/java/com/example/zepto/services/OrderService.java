package com.example.zepto.services;

import com.example.zepto.models.Order;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class OrderService {
    private FirebaseFirestore firestore;

    public OrderService() {
        firestore = FirebaseFirestore.getInstance();
    }

    public void saveOrder(Order order, OnCompleteListener<Void> listener) {
        firestore.collection("orders")
                .document(order.getId())
                .set(order)
                .addOnCompleteListener(listener);
    }
} 