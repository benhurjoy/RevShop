package com.revshop.dao;

import com.revshop.config.DatabaseConfig;
import com.revshop.model.User;
import com.revshop.security.PasswordHasher;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public int registerUser(String email, String password, User.Role role) throws SQLException {
        String hashedPassword = PasswordHasher.hashPassword(password);

        try (Connection conn = DatabaseConfig.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL register_user_proc(?, ?, ?)}")) {

            stmt.setString(1, email);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, role.toString());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("user_id");
            }
            return -1;
        }
    }

    public User login(String email, String password) throws SQLException {
        String sql = "SELECT user_id, email, password_hash, role, is_verified, created_at, " +
                "security_question, security_answer_hash " +
                "FROM users WHERE email = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                if (PasswordHasher.verifyPassword(password, storedHash)) {
                    User user = new User();
                    user.setId(rs.getInt("user_id"));
                    user.setEmail(rs.getString("email"));
                    user.setPasswordHash(storedHash);
                    user.setRole(User.Role.valueOf(rs.getString("role")));
                    user.setVerified(rs.getBoolean("is_verified"));
                    user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    user.setSecurityQuestion(rs.getString("security_question"));
                    user.setSecurityAnswerHash(rs.getString("security_answer_hash"));
                    return user;
                }
            }
            return null;
        }
    }

    public User getUserById(int userId) throws SQLException {
        String sql = "SELECT user_id, email, role, is_verified, created_at, " +
                "security_question, security_answer_hash " +
                "FROM users WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("user_id"));
                user.setEmail(rs.getString("email"));
                user.setRole(User.Role.valueOf(rs.getString("role")));
                user.setVerified(rs.getBoolean("is_verified"));
                user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                user.setSecurityQuestion(rs.getString("security_question"));
                user.setSecurityAnswerHash(rs.getString("security_answer_hash"));
                return user;
            }
            return null;
        }
    }

    public User getUserByEmail(String email) throws SQLException {
        String sql = "SELECT user_id, email, role, is_verified, created_at, " +
                "security_question, security_answer_hash " +
                "FROM users WHERE email = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("user_id"));
                user.setEmail(rs.getString("email"));
                user.setRole(User.Role.valueOf(rs.getString("role")));
                user.setVerified(rs.getBoolean("is_verified"));
                user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                user.setSecurityQuestion(rs.getString("security_question"));
                user.setSecurityAnswerHash(rs.getString("security_answer_hash"));
                return user;
            }
            return null;
        }
    }

    public boolean verifyUser(int userId) throws SQLException {
        String sql = "UPDATE users SET is_verified = TRUE WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public void saveOTP(int userId, String otp, LocalDateTime expiryTime) throws SQLException {
        String sql = "INSERT INTO otp_verification (user_id, otp, expires_at) " +
                "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE " +
                "otp = VALUES(otp), expires_at = VALUES(expires_at), attempts = 0";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, otp);
            stmt.setTimestamp(3, Timestamp.valueOf(expiryTime));
            stmt.executeUpdate();
        }
    }

    public boolean verifyOTP(int userId, String otp) throws SQLException {
        String sql = "SELECT otp, expires_at, attempts FROM otp_verification WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                LocalDateTime expiresAt = rs.getTimestamp("expires_at").toLocalDateTime();
                int attempts = rs.getInt("attempts");

                if (LocalDateTime.now().isAfter(expiresAt)) {
                    incrementOTPAttempts(userId);
                    return false;
                }

                if (attempts >= 3) {
                    return false;
                }

                String storedOTP = rs.getString("otp");
                if (storedOTP.equals(otp)) {
                    clearOTP(userId);
                    return true;
                } else {
                    incrementOTPAttempts(userId);
                    return false;
                }
            }
            return false;
        }
    }

    private void incrementOTPAttempts(int userId) throws SQLException {
        String sql = "UPDATE otp_verification SET attempts = attempts + 1 WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    private void clearOTP(int userId) throws SQLException {
        String sql = "DELETE FROM otp_verification WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    public boolean updatePassword(int userId, String newPassword) throws SQLException {
        String hashedPassword = PasswordHasher.hashPassword(newPassword);
        String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, hashedPassword);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean setSecurityQuestion(int userId, String question, String answer) throws SQLException {
        String hashedAnswer = PasswordHasher.hashPassword(answer.toLowerCase().trim());
        String sql = "UPDATE users SET security_question = ?, security_answer_hash = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, question);
            stmt.setString(2, hashedAnswer);
            stmt.setInt(3, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean verifySecurityAnswer(int userId, String answer) throws SQLException {
        String sql = "SELECT security_answer_hash FROM users WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("security_answer_hash");
                if (storedHash == null || storedHash.trim().isEmpty()) {
                    return false;
                }
                return PasswordHasher.verifyPassword(answer.toLowerCase().trim(), storedHash);
            }
            return false;
        }
    }

    public boolean hasSecurityQuestion(int userId) throws SQLException {
        String sql = "SELECT security_question FROM users WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String question = rs.getString("security_question");
                return question != null && !question.trim().isEmpty();
            }
            return false;
        }
    }

    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, email, role, is_verified, created_at, " +
                "security_question FROM users ORDER BY created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("user_id"));
                user.setEmail(rs.getString("email"));
                user.setRole(User.Role.valueOf(rs.getString("role")));
                user.setVerified(rs.getBoolean("is_verified"));
                user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                user.setSecurityQuestion(rs.getString("security_question"));
                users.add(user);
            }
        }
        return users;
    }
}