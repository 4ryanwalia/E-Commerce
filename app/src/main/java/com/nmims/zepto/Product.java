package com.nmims.zepto;

import android.os.Parcel;
import android.os.Parcelable;

public class Product implements Parcelable {
    private String name;
    private double price; // Use double for price
    private String imageUrl;
    private String size;
    private int quantity;
    private String category; // Add category field

    // Default constructor required for Firebase
    public Product() {}

    public Product(String name, double price, String imageUrl, String size, int quantity, String category) {
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.size = size;
        this.quantity = quantity;
        this.category = category;
    }

    // Getters
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public String getSize() { return size; }
    public int getQuantity() { return quantity; }
    public String getCategory() { return category; } // Getter for category

    // Setters
    public void setName(String name) { this.name = name; }
    public void setPrice(double price) { this.price = price; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setSize(String size) { this.size = size;}
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setCategory(String category) { this.category = category; } // Setter for category


    // Parcelable implementation
    protected Product(Parcel in) {
        name = in.readString();
        price = in.readDouble(); // Read double
        imageUrl = in.readString();
        size = in.readString();
        quantity = in.readInt();
        category = in.readString(); // Read category
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeDouble(price); // Write double
        dest.writeString(imageUrl);
        dest.writeString(size);
        dest.writeInt(quantity);
        dest.writeString(category); // Write category
    }

    @Override
    public int describeContents() {
        return 0;}
    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);}
        @Override
        public Product[] newArray(int size) {
            return new Product[size];}};}