package com.revshop.dao;

import com.revshop.config.DatabaseConfig;
import com.revshop.model.Order;
import com.revshop.model.OrderItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderDAO {

    public int placeOrder(int userId, String paymentMethod) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL place_order_proc(?, ?, ?)}")) {

            stmt.setInt(1, userId);
            stmt.setString(2, paymentMethod);
            stmt.registerOutParameter(3, Types.INTEGER);

            stmt.execute();
            return stmt.getInt(3);
        }
    }

    public List<Map<String, Object>> getOrdersByUser(int userId) throws SQLException {
        List<Map<String, Object>> orders = new ArrayList<>();
        String sql = """
            SELECT o.*, COUNT(oi.product_id) as item_count
            FROM orders o
            LEFT JOIN order_items oi ON o.order_id = oi.order_id
            WHERE o.user_id = ?
            GROUP BY o.order_id
            ORDER BY o.created_at DESC
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> order = new HashMap<>();
                order.put("order_id", rs.getInt("order_id"));
                order.put("total_amount", rs.getDouble("total_amount"));
                order.put("status", rs.getString("status"));
                order.put("created_at", rs.getTimestamp("created_at"));
                order.put("item_count", rs.getInt("item_count"));
                orders.add(order);
            }
        }
        return orders;
    }

    public List<Map<String, Object>> getOrdersForSeller(int sellerId) throws SQLException {
        List<Map<String, Object>> orders = new ArrayList<>();
        String sql = """
            SELECT o.*, u.email as buyer_email, COUNT(oi.product_id) as item_count
            FROM orders o
            JOIN order_items oi ON o.order_id = oi.order_id
            JOIN products p ON oi.product_id = p.product_id
            JOIN users u ON o.user_id = u.user_id
            WHERE p.seller_id = ?
            GROUP BY o.order_id
            ORDER BY o.created_at DESC
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, sellerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> order = new HashMap<>();
                order.put("order_id", rs.getInt("order_id"));
                order.put("total_amount", rs.getDouble("total_amount"));
                order.put("status", rs.getString("status"));
                order.put("created_at", rs.getTimestamp("created_at"));
                order.put("buyer_email", rs.getString("buyer_email"));
                order.put("item_count", rs.getInt("item_count"));
                orders.add(order);
            }
        }
        return orders;
    }

    public List<Map<String, Object>> getOrderDetails(int orderId) throws SQLException {
        List<Map<String, Object>> orderDetails = new ArrayList<>();
        String sql = """
            SELECT oi.*, p.name as product_name, p.discount_price as current_price
            FROM order_items oi
            JOIN products p ON oi.product_id = p.product_id
            WHERE oi.order_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> detail = new HashMap<>();
                detail.put("product_id", rs.getInt("product_id"));
                detail.put("product_name", rs.getString("product_name"));
                detail.put("quantity", rs.getInt("quantity"));
                detail.put("price", rs.getDouble("price"));
                detail.put("current_price", rs.getDouble("current_price"));
                detail.put("subtotal", rs.getDouble("price") * rs.getInt("quantity"));
                orderDetails.add(detail);
            }
        }
        return orderDetails;
    }

    public boolean updateOrderStatus(int orderId, Order.Status status) throws SQLException {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.toString());
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        }
    }

    public Order getOrderById(int orderId) throws SQLException {
        String sql = "SELECT * FROM orders WHERE order_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("order_id"));
                order.setUserId(rs.getInt("user_id"));
                order.setTotalAmount(rs.getDouble("total_amount"));
                order.setStatus(Order.Status.valueOf(rs.getString("status")));
                order.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                return order;
            }
            return null;
        }
    }

    public List<Map<String, Object>> getRecentOrders(int limit) throws SQLException {
        List<Map<String, Object>> orders = new ArrayList<>();
        String sql = """
            SELECT o.*, u.email as buyer_email, COUNT(oi.product_id) as item_count
            FROM orders o
            JOIN users u ON o.user_id = u.user_id
            LEFT JOIN order_items oi ON o.order_id = oi.order_id
            GROUP BY o.order_id
            ORDER BY o.created_at DESC
            LIMIT ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> order = new HashMap<>();
                order.put("order_id", rs.getInt("order_id"));
                order.put("total_amount", rs.getDouble("total_amount"));
                order.put("status", rs.getString("status"));
                order.put("created_at", rs.getTimestamp("created_at"));
                order.put("buyer_email", rs.getString("buyer_email"));
                order.put("item_count", rs.getInt("item_count"));
                orders.add(order);
            }
        }
        return orders;
    }
}