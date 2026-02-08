package com.revshop.dao;

import com.revshop.config.DatabaseConfig;
import com.revshop.model.Favorite;
import com.revshop.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavoriteDAO {

    public boolean addFavorite(int userId, int productId) throws SQLException {
        String sql = "INSERT IGNORE INTO favorites (user_id, product_id) VALUES (?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean removeFavorite(int userId, int productId) throws SQLException {
        String sql = "DELETE FROM favorites WHERE user_id = ? AND product_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            return stmt.executeUpdate() > 0;
        }
    }

    public List<Map<String, Object>> getFavoritesByUser(int userId) throws SQLException {
        List<Map<String, Object>> favorites = new ArrayList<>();
        String sql = """
            SELECT f.*, p.name, p.mrp, p.discount_price, p.stock, 
                   c.name as category_name, u.email as seller_email
            FROM favorites f
            JOIN products p ON f.product_id = p.product_id
            JOIN categories c ON p.category_id = c.category_id
            JOIN users u ON p.seller_id = u.user_id
            WHERE f.user_id = ?
            ORDER BY f.added_at DESC
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> favorite = new HashMap<>();
                favorite.put("product_id", rs.getInt("product_id"));
                favorite.put("name", rs.getString("name"));
                favorite.put("mrp", rs.getDouble("mrp"));
                favorite.put("discount_price", rs.getDouble("discount_price"));
                favorite.put("stock", rs.getInt("stock"));
                favorite.put("category_name", rs.getString("category_name"));
                favorite.put("seller_email", rs.getString("seller_email"));
                favorite.put("added_at", rs.getTimestamp("added_at"));
                favorites.add(favorite);
            }
        }
        return favorites;
    }

    public boolean isFavorite(int userId, int productId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM favorites WHERE user_id = ? AND product_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public int getFavoriteCount(int productId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM favorites WHERE product_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}