package com.revshop.dao;

import com.revshop.config.DatabaseConfig;
import com.revshop.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductDAO {

    public int addProduct(Product product) throws SQLException {
        String sql = "INSERT INTO products (seller_id, category_id, name, description, mrp, discount_price, stock) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, product.getSellerId());
            stmt.setInt(2, product.getCategoryId());
            stmt.setString(3, product.getName());
            stmt.setString(4, product.getDescription());
            stmt.setDouble(5, product.getMrp());
            stmt.setDouble(6, product.getDiscountPrice());
            stmt.setInt(7, product.getStock());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return -1;
        }
    }

    public boolean updateProduct(Product product) throws SQLException {
        String sql = "UPDATE products SET name = ?, description = ?, mrp = ?, " +
                "discount_price = ?, stock = ?, category_id = ? WHERE product_id = ? AND seller_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, product.getName());
            stmt.setString(2, product.getDescription());
            stmt.setDouble(3, product.getMrp());
            stmt.setDouble(4, product.getDiscountPrice());
            stmt.setInt(5, product.getStock());
            stmt.setInt(6, product.getCategoryId());
            stmt.setInt(7, product.getId());
            stmt.setInt(8, product.getSellerId());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteProduct(int productId, int sellerId) throws SQLException {
        String sql = "DELETE FROM products WHERE product_id = ? AND seller_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            stmt.setInt(2, sellerId);
            return stmt.executeUpdate() > 0;
        }
    }

    public List<Map<String, Object>> getAllProducts() throws SQLException {
        List<Map<String, Object>> products = new ArrayList<>();
        String sql = """
            SELECT p.*, c.name as category_name, u.email as seller_email 
            FROM products p
            JOIN categories c ON p.category_id = c.category_id
            JOIN users u ON p.seller_id = u.user_id
            WHERE p.stock > 0
            ORDER BY p.category_id 
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> product = new HashMap<>();
                product.put("product_id", rs.getInt("product_id"));
                product.put("name", rs.getString("name"));
                product.put("description", rs.getString("description"));
                product.put("mrp", rs.getDouble("mrp"));
                product.put("discount_price", rs.getDouble("discount_price"));
                product.put("stock", rs.getInt("stock"));
                product.put("category_name", rs.getString("category_name"));
                product.put("seller_email", rs.getString("seller_email"));
                product.put("created_at", rs.getTimestamp("created_at"));
                products.add(product);
            }
        }
        return products;
    }

    public List<Map<String, Object>> getProductsBySeller(int sellerId) throws SQLException {
        List<Map<String, Object>> products = new ArrayList<>();
        String sql = """
            SELECT p.*, c.name as category_name 
            FROM products p
            JOIN categories c ON p.category_id = c.category_id
            WHERE p.seller_id = ?
            ORDER BY p.created_at DESC
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, sellerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> product = new HashMap<>();
                product.put("product_id", rs.getInt("product_id"));
                product.put("name", rs.getString("name"));
                product.put("description", rs.getString("description"));
                product.put("mrp", rs.getDouble("mrp"));
                product.put("discount_price", rs.getDouble("discount_price"));
                product.put("stock", rs.getInt("stock"));
                product.put("category_name", rs.getString("category_name"));
                product.put("created_at", rs.getTimestamp("created_at"));
                products.add(product);
            }
        }
        return products;
    }

    public Product getProductById(int productId) throws SQLException {
        String sql = "SELECT * FROM products WHERE product_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Product product = new Product();
                product.setId(rs.getInt("product_id"));
                product.setSellerId(rs.getInt("seller_id"));
                product.setCategoryId(rs.getInt("category_id"));
                product.setName(rs.getString("name"));
                product.setDescription(rs.getString("description"));
                product.setMrp(rs.getDouble("mrp"));
                product.setDiscountPrice(rs.getDouble("discount_price"));
                product.setStock(rs.getInt("stock"));
                product.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                product.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                return product;
            }
            return null;
        }
    }

    public List<Map<String, Object>> searchProducts(String keyword) throws SQLException {
        List<Map<String, Object>> products = new ArrayList<>();
        String sql = """
            SELECT p.*, c.name as category_name, u.email as seller_email 
            FROM products p
            JOIN categories c ON p.category_id = c.category_id
            JOIN users u ON p.seller_id = u.user_id
            WHERE (p.name LIKE ? OR p.description LIKE ? OR c.name LIKE ?)
            AND p.stock > 0
            ORDER BY p.category_id 
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchTerm = "%" + keyword + "%";
            stmt.setString(1, searchTerm);
            stmt.setString(2, searchTerm);
            stmt.setString(3, searchTerm);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> product = new HashMap<>();
                product.put("product_id", rs.getInt("product_id"));
                product.put("name", rs.getString("name"));
                product.put("description", rs.getString("description"));
                product.put("mrp", rs.getDouble("mrp"));
                product.put("discount_price", rs.getDouble("discount_price"));
                product.put("stock", rs.getInt("stock"));
                product.put("category_name", rs.getString("category_name"));
                product.put("seller_email", rs.getString("seller_email"));
                product.put("created_at", rs.getTimestamp("created_at"));
                products.add(product);
            }
        }
        return products;
    }

    public List<Map<String, Object>> getProductsByCategory(int categoryId) throws SQLException {
        List<Map<String, Object>> products = new ArrayList<>();
        String sql = """
            SELECT p.*, c.name as category_name, u.email as seller_email 
            FROM products p
            JOIN categories c ON p.category_id = c.category_id
            JOIN users u ON p.seller_id = u.user_id
            WHERE p.category_id = ? AND p.stock > 0
            ORDER BY p.category_id 
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> product = new HashMap<>();
                product.put("product_id", rs.getInt("product_id"));
                product.put("name", rs.getString("name"));
                product.put("description", rs.getString("description"));
                product.put("mrp", rs.getDouble("mrp"));
                product.put("discount_price", rs.getDouble("discount_price"));
                product.put("stock", rs.getInt("stock"));
                product.put("category_name", rs.getString("category_name"));
                product.put("seller_email", rs.getString("seller_email"));
                product.put("created_at", rs.getTimestamp("created_at"));
                products.add(product);
            }
        }
        return products;
    }

    public List<Map<String, Object>> getAllCategories() throws SQLException {
        List<Map<String, Object>> categories = new ArrayList<>();
        String sql = "SELECT category_id, name FROM categories ORDER BY category_id";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> category = new HashMap<>();
                category.put("category_id", rs.getInt("category_id"));
                category.put("name", rs.getString("name"));
                categories.add(category);
            }
        }
        return categories;
    }

    public boolean updateStock(int productId, int quantityChange) throws SQLException {
        String sql = "UPDATE products SET stock = stock + ? WHERE product_id = ? AND stock + ? >= 0";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, quantityChange);
            stmt.setInt(2, productId);
            stmt.setInt(3, quantityChange);
            return stmt.executeUpdate() > 0;
        }
    }
}