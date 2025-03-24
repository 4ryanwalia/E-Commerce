package com.nmims.zepto;
import java.util.List; // Make sure this import is present
import java.util.Map;
public class Order {
    public String userId;
    public String status; // e.g., "pending", "processing", "shipped", "delivered", "cancelled"
    public long timestamp;
    public double totalAmount;
    public List<Product> products; // List of products in the order
    public Map<String, Object> address; // Store the address as a Map
    // Required empty constructor for Firebase
    public Order() {}
    // Constructor (include address)
    public Order(String userId, String status, long timestamp, double totalAmount, List<Product> products, Map<String,Object> address) {
        this.userId = userId;
        this.status = status;
        this.timestamp = timestamp;
        this.totalAmount = totalAmount;
        this.products = products;
        this.address = address;
    }
    // Getters (important for Firebase to read the data)
    public String getUserId() {
        return userId;
    }
    public String getStatus() {
        return status;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public double getTotalAmount() {
        return totalAmount;
    }
    public List<Product> getProducts(){
        return products;
    }
    public Map<String, Object> getAddress() { return address; }
    // Setters
    public void setUserId(String userId){
        this.userId = userId;
    }
    public void setStatus(String status){
        this.status = status;
    }
    public void setTimestamp(long timestamp){
        this.timestamp = timestamp;
    }
    public void setTotalAmount(double totalAmount){
        this.totalAmount = totalAmount;
    }
    public void setProducts(List<Product> products){
        this.products = products;
    }
    public void setAddress(Map<String, Object> address){
        this.address = address;
    }
}