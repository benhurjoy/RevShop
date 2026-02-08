package com.revshop.ui;

import com.revshop.config.DatabaseConfig;
import com.revshop.model.Product;
import com.revshop.model.User;
import com.revshop.service.*;
import com.revshop.util.ConsoleUtil;

import java.sql.*;
import java.util.Map;

public class SellerMenu {
    private final User user;
    private final ProductService productService;
    private final OrderService orderService;
    private final ReviewService reviewService;
    private final NotificationService notificationService;

    public SellerMenu(User user) {
        this.user = user;
        this.productService = new ProductService();
        this.orderService = new OrderService();
        this.reviewService = new ReviewService();
        this.notificationService = new NotificationService();
    }

    public void show() {
        while (true) {
            ConsoleUtil.printHeader("Seller Dashboard - " + user.getEmail());

            System.out.println("\nMain Menu:");
            String[] options = {
                    "Manage Products",
                    "View Orders",
                    "Update Order Status",
                    "View Product Reviews",
                    "Update Inventory",
                    "View Notifications",
                    "Logout"
            };

            ConsoleUtil.printMenu(options);
            int choice = ConsoleUtil.getIntInput("Select option: ", 1, options.length);

            switch (choice) {
                case 1 -> manageProducts();
                case 2 -> viewOrders();
                case 3 -> updateOrderStatus();
                case 4 -> viewProductReviews();
                case 5 -> updateInventory();
                case 6 -> viewNotifications();
                case 7 -> {
                    ConsoleUtil.printSuccess("Logged out successfully");
                    return;
                }
            }
        }
    }

    private void manageProducts() {
        while (true) {
            ConsoleUtil.printHeader("Manage Products");
            productService.displayProductsBySeller(user.getId());

            System.out.println("\nOptions:");
            System.out.println("1. Add Product");
            System.out.println("2. Update Product");
            System.out.println("3. Delete Product");
            System.out.println("4. View All Products");
            System.out.println("5. Back");

            int choice = ConsoleUtil.getIntInput("Select option: ", 1, 5);

            switch (choice) {
                case 1 -> addProduct();
                case 2 -> updateProduct();
                case 3 -> deleteProduct();
                case 4 -> {
                    productService.displayAllProducts();
                    ConsoleUtil.pressEnterToContinue();
                }
                case 5 -> { return; }
            }
        }
    }

    private void addProduct() {
        ConsoleUtil.printHeader("Add New Product");

        // FIXED: Use direct database query for categories
        displayCategoriesDirectly();

        int categoryId = ConsoleUtil.getIntInput("Category ID: ", 1, Integer.MAX_VALUE);

        String name = ConsoleUtil.getStringInput("Product Name: ", true);
        String description = ConsoleUtil.getStringInput("Description: ", false);
        double mrp = ConsoleUtil.getDoubleInput("MRP: ", 1);
        Double discountPrice = null;

        if (ConsoleUtil.getConfirmation("Add discount price?")) {
            discountPrice = ConsoleUtil.getDoubleInput("Discount Price: ", 1);
            if (discountPrice >= mrp) {
                ConsoleUtil.printError("Discount price must be less than MRP");
                return;
            }
        }

        int stock = ConsoleUtil.getIntInput("Stock Quantity: ", 0, 10000);

        Product product = new Product();
        product.setSellerId(user.getId());
        product.setCategoryId(categoryId);
        product.setName(name);
        product.setDescription(description);
        product.setMrp(mrp);
        product.setDiscountPrice(discountPrice);
        product.setStock(stock);

        productService.addProduct(product);
        ConsoleUtil.pressEnterToContinue();
    }

    private void displayCategoriesDirectly() {
        System.out.println("\nAvailable Categories:");
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT category_id, name FROM categories ORDER BY name")) {

            boolean hasCategories = false;
            while (rs.next()) {
                hasCategories = true;
                int categoryId = rs.getInt("category_id");
                String name = rs.getString("name");
                System.out.println(categoryId + ". " + name);
            }

            if (!hasCategories) {
                System.out.println("No categories found in database!");
                System.out.println("Please add categories first.");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching categories: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateProduct() {
        int productId = ConsoleUtil.getIntInput("Enter Product ID to update: ", 1, Integer.MAX_VALUE);
        Product product = productService.getProductById(productId);

        if (product == null || product.getSellerId() != user.getId()) {
            ConsoleUtil.printError("Product not found or you don't own this product");
            return;
        }

        ConsoleUtil.printHeader("Update Product: " + product.getName());

        // FIXED: Use direct database query for categories
        displayCategoriesDirectly();

        int categoryId = ConsoleUtil.getIntInput("New Category ID (" + product.getCategoryId() + "): ",
                1, Integer.MAX_VALUE);

        String name = ConsoleUtil.getStringInput("New Name (" + product.getName() + "): ", true);
        String description = ConsoleUtil.getStringInput("New Description: ", false);
        double mrp = ConsoleUtil.getDoubleInput("New MRP (" + product.getMrp() + "): ", 1);
        Double discountPrice = product.getDiscountPrice();

        if (ConsoleUtil.getConfirmation("Change discount price?")) {
            if (ConsoleUtil.getConfirmation("Remove discount?")) {
                discountPrice = null;
            } else {
                discountPrice = ConsoleUtil.getDoubleInput("New Discount Price: ", 1);
                if (discountPrice >= mrp) {
                    ConsoleUtil.printError("Discount price must be less than MRP");
                    return;
                }
            }
        }

        int stock = ConsoleUtil.getIntInput("New Stock (" + product.getStock() + "): ", 0, 10000);

        product.setCategoryId(categoryId);
        product.setName(name);
        product.setDescription(description);
        product.setMrp(mrp);
        product.setDiscountPrice(discountPrice);
        product.setStock(stock);

        productService.updateProduct(product);
        ConsoleUtil.pressEnterToContinue();
    }

    private void deleteProduct() {
        int productId = ConsoleUtil.getIntInput("Enter Product ID to delete: ", 1, Integer.MAX_VALUE);

        if (ConsoleUtil.getConfirmation("Are you sure you want to delete this product?")) {
            productService.deleteProduct(productId, user.getId());
        }

        ConsoleUtil.pressEnterToContinue();
    }

    private void viewOrders() {
        ConsoleUtil.printHeader("Received Orders");
        orderService.displaySellerOrders(user.getId());
        ConsoleUtil.pressEnterToContinue();
    }

    private void updateOrderStatus() {
        ConsoleUtil.printHeader("Update Order Status");

        orderService.displaySellerOrders(user.getId());
        int orderId = ConsoleUtil.getIntInput("Enter Order ID: ", 1, Integer.MAX_VALUE);

        System.out.println("\nSelect new status:");
        System.out.println("1. CONFIRMED");
        System.out.println("2. SHIPPED");
        System.out.println("3. DELIVERED");
        System.out.println("4. CANCELLED");
        System.out.println("5. Cancel");

        int choice = ConsoleUtil.getIntInput("Select option: ", 1, 5);
        if (choice == 5) return;

        com.revshop.model.Order.Status[] statuses = {
                com.revshop.model.Order.Status.CONFIRMED,
                com.revshop.model.Order.Status.SHIPPED,
                com.revshop.model.Order.Status.DELIVERED,
                com.revshop.model.Order.Status.CANCELLED
        };

        orderService.updateOrderStatus(orderId, statuses[choice - 1]);
        ConsoleUtil.pressEnterToContinue();
    }

    private void viewProductReviews() {
        ConsoleUtil.printHeader("Product Reviews");

        productService.displayProductsBySeller(user.getId());
        int productId = ConsoleUtil.getIntInput("Enter Product ID: ", 1, Integer.MAX_VALUE);

        reviewService.displayProductReviews(productId);
        ConsoleUtil.pressEnterToContinue();
    }

    private void updateInventory() {
        while (true) {
            ConsoleUtil.printHeader("Update Inventory");
            productService.displayProductsBySeller(user.getId());

            System.out.println("\nOptions:");
            System.out.println("1. Add Stock");
            System.out.println("2. Remove Stock");
            System.out.println("3. Back");

            int choice = ConsoleUtil.getIntInput("Select option: ", 1, 3);
            if (choice == 3) return;

            int productId = ConsoleUtil.getIntInput("Enter Product ID: ", 1, Integer.MAX_VALUE);

            if (choice == 1) {
                int quantity = ConsoleUtil.getIntInput("Quantity to add: ", 1, 1000);
                productService.updateStock(productId, quantity, user.getId());
            } else {
                int quantity = ConsoleUtil.getIntInput("Quantity to remove: ", 1, 1000);
                productService.updateStock(productId, -quantity, user.getId());
            }

            ConsoleUtil.pressEnterToContinue();
        }
    }

    private void viewNotifications() {
        ConsoleUtil.printHeader("Notifications");
        notificationService.displayNotifications(user.getId());

        System.out.println("\nOptions:");
        System.out.println("1. Mark All as Read");
        System.out.println("2. Back");

        int choice = ConsoleUtil.getIntInput("Select option: ", 1, 2);
        if (choice == 1) {
            notificationService.markAllAsRead(user.getId());
            ConsoleUtil.printSuccess("All notifications marked as read");
        }

        ConsoleUtil.pressEnterToContinue();
    }
}