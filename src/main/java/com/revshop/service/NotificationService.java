package com.revshop.service;

import com.revshop.config.DatabaseConfig;
import com.revshop.util.ConsoleUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NotificationService {
    private static final Logger logger = LogManager.getLogger(NotificationService.class);

    public List<Map<String, Object>> getNotifications(int userId) {
        List<Map<String, Object>> notifications = new ArrayList<>();
        String sql = """
            SELECT notification_id, message, is_read, created_at
            FROM notifications
            WHERE user_id = ?
            ORDER BY created_at DESC
            LIMIT 20
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                notifications.add(Map.of(
                        "id", rs.getInt("notification_id"),  // Changed from "id" to "notification_id"
                        "message", rs.getString("message"),
                        "is_read", rs.getBoolean("is_read"),
                        "created_at", rs.getTimestamp("created_at")
                ));
            }
        } catch (SQLException e) {
            logger.error("Failed to fetch notifications: ", e);
        }
        return notifications;
    }

    public void markAsRead(int notificationId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE notification_id = ?";  // Changed from "id" to "notification_id"

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, notificationId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to mark notification as read: ", e);
        }
    }

    public void markAllAsRead(int userId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to mark all notifications as read: ", e);
        }
    }

    public void displayNotifications(int userId) {
        List<Map<String, Object>> notifications = getNotifications(userId);
        if (notifications.isEmpty()) {
            ConsoleUtil.printInfo("No notifications");
            return;
        }

        System.out.println("\nNotifications:");
        System.out.println("=".repeat(80));

        int unreadCount = 0;
        for (Map<String, Object> notification : notifications) {
            boolean isRead = (Boolean) notification.get("is_read");
            String prefix = isRead ? "  " : "● ";

            if (!isRead) unreadCount++;

            System.out.println(prefix + notification.get("message"));
            System.out.println("  " + notification.get("created_at"));
            System.out.println("-".repeat(80));
        }

        if (unreadCount > 0) {
            System.out.println("You have " + unreadCount + " unread notifications");
        }
    }
}