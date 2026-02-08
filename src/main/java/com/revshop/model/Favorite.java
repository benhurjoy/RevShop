package com.revshop.model;

import java.time.LocalDateTime;

public class Favorite {
    private int userId;
    private int productId;
    private LocalDateTime addedAt;
    private Product product;

    // Constructors
    public Favorite() {}

    public Favorite(int userId, int productId, LocalDateTime addedAt) {
        this.userId = userId;
        this.productId = productId;
        this.addedAt = addedAt;
    }

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    @Override
    public String toString() {
        return "Favorite{" +
                "userId=" + userId +
                ", productId=" + productId +
                ", addedAt=" + addedAt +
                '}';
    }
}