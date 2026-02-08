package com.revshop.model;

import java.time.LocalDateTime;

public class CartItem {
    private int userId;
    private int productId;
    private int quantity;
    private LocalDateTime addedAt;
    private Product product;

    // Constructors
    public CartItem() {}

    public CartItem(int userId, int productId, int quantity, LocalDateTime addedAt) {
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.addedAt = addedAt;
    }

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public double getSubtotal() {
        if (product != null) {
            return product.getEffectivePrice() * quantity;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "productId=" + productId +
                ", quantity=" + quantity +
                ", addedAt=" + addedAt +
                '}';
    }
}