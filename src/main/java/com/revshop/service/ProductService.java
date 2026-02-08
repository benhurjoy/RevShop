package com.revshop.service;

import com.revshop.dao.ProductDAO;
import com.revshop.model.Product;
import com.revshop.util.ConsoleUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ProductService {
    private final ProductDAO productDAO;

    public ProductService() {
        this.productDAO = new ProductDAO();
    }

    public void addProduct(Product product) {
        try {
            int productId = productDAO.addProduct(product);
            if (productId > 0) {
                ConsoleUtil.printSuccess("Product added successfully! Product ID: " + productId);
            } else {
                ConsoleUtil.printError("Failed to add product");
            }
        } catch (SQLException e) {
            ConsoleUtil.printError("Failed to add product: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateProduct(Product product) {
        try {
            if (productDAO.updateProduct(product)) {
                ConsoleUtil.printSuccess("Product updated successfully!");
            } else {
                ConsoleUtil.printError("Failed to update product");
            }
        } catch (SQLException e) {
            ConsoleUtil.printError("Failed to update product: " + e.getMessage());
        }
    }

    public void deleteProduct(int productId, int sellerId) {
        try {
            if (productDAO.deleteProduct(productId, sellerId)) {
                ConsoleUtil.printSuccess("Product deleted successfully!");
            } else {
                ConsoleUtil.printError("Failed to delete product");
            }
        } catch (SQLException e) {
            ConsoleUtil.printError("Failed to delete product: " + e.getMessage());
        }
    }

    public void displayAllProducts() {
        try {
            List<Map<String, Object>> products = productDAO.getAllProducts();
            if (products.isEmpty()) {
                System.out.println("No products available.");
                return;
            }

            System.out.println("\n=== All Products ===");
            for (Map<String, Object> product : products) {
                printProductDetails(product);
            }
        } catch (SQLException e) {
            ConsoleUtil.printError("Failed to fetch products: " + e.getMessage());
        }
    }

    public void displayProductsBySeller(int sellerId) {
        try {
            List<Map<String, Object>> products = productDAO.getProductsBySeller(sellerId);
            if (products.isEmpty()) {
                System.out.println("No products found.");
                return;
            }

            System.out.println("\n=== Your Products ===");
            for (Map<String, Object> product : products) {
                printProductDetails(product);
            }
        } catch (SQLException e) {
            ConsoleUtil.printError("Failed to fetch products: " + e.getMessage());
        }
    }

    public Product getProductById(int productId) {
        try {
            return productDAO.getProductById(productId);
        } catch (SQLException e) {
            ConsoleUtil.printError("Failed to fetch product: " + e.getMessage());
            return null;
        }
    }

    public void searchProducts(String keyword) {
        try {
            List<Map<String, Object>> products = productDAO.searchProducts(keyword);
            if (products.isEmpty()) {
                System.out.println("No products found matching: " + keyword);
                return;
            }

            System.out.println("\n=== Search Results ===");
            for (Map<String, Object> product : products) {
                printProductDetails(product);
            }
        } catch (SQLException e) {
            ConsoleUtil.printError("Failed to search products: " + e.getMessage());
        }
    }

    public void displayProductsByCategory(int categoryId) {
        try {
            List<Map<String, Object>> products = productDAO.getProductsByCategory(categoryId);
            if (products.isEmpty()) {
                System.out.println("No products found in this category.");
                return;
            }

            System.out.println("\n=== Products in Category ===");
            for (Map<String, Object> product : products) {
                printProductDetails(product);
            }
        } catch (SQLException e) {
            ConsoleUtil.printError("Failed to fetch products: " + e.getMessage());
        }
    }

    public void displayCategories() {
        try {
            List<Map<String, Object>> categories = productDAO.getAllCategories();
            if (categories.isEmpty()) {
                System.out.println("No categories available.");
                return;
            }

            System.out.println("\nAvailable Categories:");
            for (Map<String, Object> category : categories) {
                // Handle potential null values
                Object idObj = category.get("category_id");
                Object nameObj = category.get("name");

                String idStr = (idObj != null) ? idObj.toString() : "null";
                String nameStr = (nameObj != null) ? nameObj.toString() : "null";

                System.out.println(idStr + ". " + nameStr);
            }
        } catch (SQLException e) {
            ConsoleUtil.printError("Failed to fetch categories: " + e.getMessage());
        }
    }

    public void updateStock(int productId, int quantityChange, int sellerId) {
        try {
            // First verify the product belongs to the seller
            Product product = productDAO.getProductById(productId);
            if (product == null || product.getSellerId() != sellerId) {
                ConsoleUtil.printError("Product not found or you don't own this product");
                return;
            }

            if (productDAO.updateStock(productId, quantityChange)) {
                String action = quantityChange > 0 ? "added to" : "removed from";
                ConsoleUtil.printSuccess(Math.abs(quantityChange) + " units " + action + " stock successfully!");
            } else {
                ConsoleUtil.printError("Failed to update stock. Insufficient stock available.");
            }
        } catch (SQLException e) {
            ConsoleUtil.printError("Failed to update stock: " + e.getMessage());
        }
    }

    private void printProductDetails(Map<String, Object> product) {
        System.out.println("\nProduct ID: " + product.get("product_id"));
        System.out.println("Name: " + product.get("name"));
        System.out.println("Description: " + product.get("description"));

        Double mrp = (Double) product.get("mrp");
        Double discountPrice = (Double) product.get("discount_price");

        if (discountPrice != null && discountPrice > 0) {
            System.out.printf("Price: ₹%.2f (MRP: ₹%.2f) - %.0f%% off\n",
                    discountPrice, mrp, ((mrp - discountPrice) / mrp) * 100);
        } else {
            System.out.printf("Price: ₹%.2f\n", mrp);
        }

        System.out.println("Stock: " + product.get("stock"));
        System.out.println("Category: " + product.get("category_name"));
        System.out.println("Seller: " + product.get("seller_email"));
        System.out.println("Added: " + product.get("created_at"));
        System.out.println("-".repeat(50));
    }
}