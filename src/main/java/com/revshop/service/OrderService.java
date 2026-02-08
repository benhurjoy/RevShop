package com.revshop.service;

import com.revshop.dao.OrderDAO;
import com.revshop.model.Order;
import com.revshop.util.ConsoleUtil;
import com.revshop.util.TableFormatter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class OrderService {
    private static final Logger logger = LogManager.getLogger(OrderService.class);
    private final OrderDAO orderDAO;

    public OrderService() {
        this.orderDAO = new OrderDAO();
    }

    public int placeOrder(int userId, String paymentMethod) {
        try {
            int orderId = orderDAO.placeOrder(userId, paymentMethod);
            if (orderId > 0) {
                ConsoleUtil.printSuccess("Order placed successfully! Order ID: " + orderId);
                logger.info("Order placed: ID={}, user={}, method={}",
                        orderId, userId, paymentMethod);
                return orderId;
            } else {
                ConsoleUtil.printError("Failed to place order. Check cart and stock availability.");
                return -1;
            }
        } catch (SQLException e) {
            logger.error("Failed to place order: ", e);
            ConsoleUtil.printError("Failed to place order: " + e.getMessage());
            return -1;
        }
    }

    public void displayUserOrders(int userId) {
        try {
            List<Map<String, Object>> orders = orderDAO.getOrdersByUser(userId);
            if (orders.isEmpty()) {
                ConsoleUtil.printInfo("You have no orders");
            } else {
                TableFormatter.printOrderTable(orders);
            }
        } catch (SQLException e) {
            logger.error("Failed to fetch orders: ", e);
            ConsoleUtil.printError("Failed to fetch orders: " + e.getMessage());
        }
    }

    public void displaySellerOrders(int sellerId) {
        try {
            List<Map<String, Object>> orders = orderDAO.getOrdersForSeller(sellerId);
            if (orders.isEmpty()) {
                ConsoleUtil.printInfo("No orders received");
            } else {
                TableFormatter.printOrderTable(orders);
            }
        } catch (SQLException e) {
            logger.error("Failed to fetch seller orders: ", e);
            ConsoleUtil.printError("Failed to fetch orders: " + e.getMessage());
        }
    }

    public void displayOrderDetails(int orderId) {
        try {
            Order order = orderDAO.getOrderById(orderId);
            if (order == null) {
                ConsoleUtil.printError("Order not found");
                return;
            }

            List<Map<String, Object>> details = orderDAO.getOrderDetails(orderId);
            if (details.isEmpty()) {
                ConsoleUtil.printInfo("No items found in order");
                return;
            }

            System.out.println("\nOrder Details - ID: " + orderId);
            System.out.println("Status: " + order.getStatus());
            System.out.println("Total: ₹" + order.getTotalAmount());
            System.out.println("Date: " + order.getCreatedAt());
            System.out.println("=".repeat(80));
            System.out.printf("%-40s %10s %15s %15s%n",
                    "Product", "Quantity", "Price", "Subtotal");
            System.out.println("-".repeat(80));

            double total = 0;
            for (Map<String, Object> item : details) {
                double price = (Double) item.get("price");
                int quantity = (Integer) item.get("quantity");
                double subtotal = price * quantity;
                total += subtotal;

                System.out.printf("%-40s %10d %15.2f %15.2f%n",
                        item.get("product_name"),
                        quantity,
                        price,
                        subtotal);
            }

            System.out.println("-".repeat(80));
            System.out.printf("%67s %15.2f%n", "Total:", total);

        } catch (SQLException e) {
            logger.error("Failed to fetch order details: ", e);
            ConsoleUtil.printError("Failed to fetch order details: " + e.getMessage());
        }
    }

    public boolean updateOrderStatus(int orderId, Order.Status status) {
        try {
            boolean updated = orderDAO.updateOrderStatus(orderId, status);
            if (updated) {
                ConsoleUtil.printSuccess("Order status updated to: " + status);
                logger.info("Order status updated: ID={}, status={}", orderId, status);
                return true;
            } else {
                ConsoleUtil.printError("Failed to update order status");
                return false;
            }
        } catch (SQLException e) {
            logger.error("Failed to update order status: ", e);
            ConsoleUtil.printError("Failed to update order status: " + e.getMessage());
            return false;
        }
    }

    public void displayRecentOrders() {
        try {
            List<Map<String, Object>> orders = orderDAO.getRecentOrders(10);
            if (orders.isEmpty()) {
                ConsoleUtil.printInfo("No recent orders");
            } else {
                ConsoleUtil.printInfo("Recent Orders:");
                TableFormatter.printOrderTable(orders);
            }
        } catch (SQLException e) {
            logger.error("Failed to fetch recent orders: ", e);
            ConsoleUtil.printError("Failed to fetch recent orders: " + e.getMessage());
        }
    }
}