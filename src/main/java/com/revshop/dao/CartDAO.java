package com.revshop.dao;

import com.revshop.config.DatabaseConfig;
import com.revshop.model.CartItem;
import com.revshop.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartDAO {

    public boolean addToCart(int userId, int productId, int quantity) throws SQLException {
        String sql = """
            INSERT INTO cart (user_id, product_id, quantity)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            stmt.setInt(3, quantity);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updateCartItem(int userId, int productId, int quantity) throws SQLException {
        if (quantity <= 0) {
            return removeFromCart(userId, productId);
        }

        String sql = "UPDATE cart SET quantity = ? WHERE user_id = ? AND product_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, quantity);
            stmt.setInt(2, userId);
            stmt.setInt(3, productId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean removeFromCart(int userId, int productId) throws SQLException {
        String sql = "DELETE FROM cart WHERE user_id = ? AND product_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            return stmt.executeUpdate() > 0;
        }
    }

    public void clearCart(int userId) throws SQLException {
        String sql = "DELETE FROM cart WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    public List<CartItem> getCartItems(int userId) throws SQLException {
        List<CartItem> cartItems = new ArrayList<>();
        String sql = """
            SELECT c.*, p.name, p.mrp, p.discount_price, p.stock
            FROM cart c
            JOIN products p ON c.product_id = p.product_id
            WHERE c.user_id = ?
            ORDER BY c.added_at DESC
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                CartItem item = new CartItem();
                item.setUserId(rs.getInt("user_id"));
                item.setProductId(rs.getInt("product_id"));
                item.setQuantity(rs.getInt("quantity"));
                item.setAddedAt(rs.getTimestamp("added_at").toLocalDateTime());

                Product product = new Product();
                product.setId(rs.getInt("product_id"));
                product.setName(rs.getString("name"));
                product.setMrp(rs.getDouble("mrp"));
                product.setDiscountPrice(rs.getDouble("discount_price"));
                product.setStock(rs.getInt("stock"));
                item.setProduct(product);

                cartItems.add(item);
            }
        }
        return cartItems;
    }

    public double getCartTotal(int userId) throws SQLException {
        String sql = """
            SELECT SUM(CASE 
                WHEN p.discount_price IS NOT NULL AND p.discount_price > 0 
                THEN p.discount_price * c.quantity
                ELSE p.mrp * c.quantity
            END) as total
            FROM cart c
            JOIN products p ON c.product_id = p.product_id
            WHERE c.user_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("total");
            }
            return 0;
        }
    }

    public boolean isProductInCart(int userId, int productId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM cart WHERE user_id = ? AND product_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public int getCartItemCount(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM cart WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}