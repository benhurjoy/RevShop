package com.revshop.model;

import java.time.LocalDateTime;

public class Product {
    private int id;
    private int sellerId;
    private int categoryId;
    private String name;
    private String description;
    private double mrp;
    private Double discountPrice;
    private int stock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String sellerEmail;
    private String categoryName;

    // Constructors
    public Product() {}

    public Product(int id, int sellerId, int categoryId, String name, String description,
                   double mrp, Double discountPrice, int stock, LocalDateTime createdAt,
                   LocalDateTime updatedAt) {
        this.id = id;
        this.sellerId = sellerId;
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.mrp = mrp;
        this.discountPrice = discountPrice;
        this.stock = stock;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSellerId() { return sellerId; }
    public void setSellerId(int sellerId) { this.sellerId = sellerId; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getMrp() { return mrp; }
    public void setMrp(double mrp) { this.mrp = mrp; }

    public Double getDiscountPrice() { return discountPrice; }
    public void setDiscountPrice(Double discountPrice) { this.discountPrice = discountPrice; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getSellerEmail() { return sellerEmail; }
    public void setSellerEmail(String sellerEmail) { this.sellerEmail = sellerEmail; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public double getEffectivePrice() {
        return discountPrice != null && discountPrice > 0 ? discountPrice : mrp;
    }

    public double getDiscountPercentage() {
        if (discountPrice != null && discountPrice > 0 && mrp > discountPrice) {
            return ((mrp - discountPrice) / mrp) * 100;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", mrp=" + mrp +
                ", discountPrice=" + discountPrice +
                ", stock=" + stock +
                '}';
    }
}