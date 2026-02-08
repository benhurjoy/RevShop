package com.revshop.dao;

import com.revshop.config.DatabaseConfig;
import com.revshop.model.Review;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewDAO {

    public boolean addReview(Review review) throws SQLException {
        String sql = """
            INSERT INTO reviews (user_id, product_id, rating, comment)
            VALUES (?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE rating = VALUES(rating), comment = VALUES(comment)
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, review.getUserId());
            stmt.setInt(2, review.getProductId());
            stmt.setInt(3, review.getRating());
            stmt.setString(4, review.getComment());
            return stmt.executeUpdate() > 0;
        }
    }

    public List<Map<String, Object>> getReviewsByProduct(int productId) throws SQLException {
        List<Map<String, Object>> reviews = new ArrayList<>();
        String sql = """
            SELECT r.*, u.email as user_email
            FROM reviews r
            JOIN users u ON r.user_id = u.user_id
            WHERE r.product_id = ?
            ORDER BY r.created_at DESC
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> review = new HashMap<>();
                review.put("user_id", rs.getInt("user_id"));
                review.put("user_email", rs.getString("user_email"));
                review.put("rating", rs.getInt("rating"));
                review.put("comment", rs.getString("comment"));
                review.put("created_at", rs.getTimestamp("created_at"));
                reviews.add(review);
            }
        }
        return reviews;
    }

    public List<Map<String, Object>> getReviewsByUser(int userId) throws SQLException {
        List<Map<String, Object>> reviews = new ArrayList<>();
        String sql = """
            SELECT r.*, p.name as product_name
            FROM reviews r
            JOIN products p ON r.product_id = p.product_id
            WHERE r.user_id = ?
            ORDER BY r.created_at DESC
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> review = new HashMap<>();
                review.put("product_id", rs.getInt("product_id"));
                review.put("product_name", rs.getString("product_name"));
                review.put("rating", rs.getInt("rating"));
                review.put("comment", rs.getString("comment"));
                review.put("created_at", rs.getTimestamp("created_at"));
                reviews.add(review);
            }
        }
        return reviews;
    }

    public Map<String, Object> getProductRatingStats(int productId) throws SQLException {
        String sql = """
            SELECT 
                COUNT(*) as total_reviews,
                AVG(rating) as average_rating,
                MIN(rating) as min_rating,
                MAX(rating) as max_rating
            FROM reviews
            WHERE product_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> stats = new HashMap<>();
                stats.put("total_reviews", rs.getInt("total_reviews"));
                stats.put("average_rating", rs.getDouble("average_rating"));
                stats.put("min_rating", rs.getInt("min_rating"));
                stats.put("max_rating", rs.getInt("max_rating"));
                return stats;
            }
        }
        return null;
    }

    public boolean deleteReview(int userId, int productId) throws SQLException {
        String sql = "DELETE FROM reviews WHERE user_id = ? AND product_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            return stmt.executeUpdate() > 0;
        }
    }

    public Review getReview(int userId, int productId) throws SQLException {
        String sql = "SELECT * FROM reviews WHERE user_id = ? AND product_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Review review = new Review();
                review.setUserId(rs.getInt("user_id"));
                review.setProductId(rs.getInt("product_id"));
                review.setRating(rs.getInt("rating"));
                review.setComment(rs.getString("comment"));
                review.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                return review;
            }
            return null;
        }
    }
}