package com.revshop.service;

import com.revshop.dao.CartDAO;
import com.revshop.dao.ProductDAO;
import com.revshop.model.CartItem;
import com.revshop.util.ConsoleUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

public class CartService {
    private static final Logger logger = LogManager.getLogger(CartService.class);
    private final CartDAO cartDAO;
    private final ProductDAO productDAO;

    public CartService() {
        this.cartDAO = new CartDAO();
        this.productDAO = new ProductDAO();
    }

    public boolean addToCart(int userId, int productId, int quantity) {
        try {
            // Check product availability
            var product = productDAO.getProductById(productId);
            if (product == null) {
                ConsoleUtil.printError("Product not found");
                return false;
            }

            if (product.getStock() < quantity) {
                ConsoleUtil.printError("Insufficient stock. Available: " + product.getStock());
                return false;
            }

            boolean added = cartDAO.addToCart(userId, productId, quantity);
            if (added) {
                ConsoleUtil.printSuccess("Added to cart: " + product.getName());
                logger.info("Product added to cart: user={}, product={}, quantity={}",
                        userId, productId, quantity);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Failed to add to cart: ", e);
            ConsoleUtil.printError("Failed to add to cart: " + e.getMessage());
        }
        return false;
    }

    public boolean updateCartItem(int userId, int productId, int quantity) {
        try {
            boolean updated = cartDAO.updateCartItem(userId, productId, quantity);
            if (updated) {
                if (quantity > 0) {
                    ConsoleUtil.printSuccess("Cart updated");
                } else {
                    ConsoleUtil.printSuccess("Item removed from cart");
                }
                logger.info("Cart updated: user={}, product={}, quantity={}",
                        userId, productId, quantity);
                return true;
            } else {
                ConsoleUtil.printError("Item not found in cart");
                return false;
            }
        } catch (SQLException e) {
            logger.error("Failed to update cart: ", e);
            ConsoleUtil.printError("Failed to update cart: " + e.getMessage());
            return false;
        }
    }

    public boolean removeFromCart(int userId, int productId) {
        try {
            boolean removed = cartDAO.removeFromCart(userId, productId);
            if (removed) {
                ConsoleUtil.printSuccess("Item removed from cart");
                logger.info("Item removed from cart: user={}, product={}", userId, productId);
                return true;
            } else {
                ConsoleUtil.printError("Item not found in cart");
                return false;
            }
        } catch (SQLException e) {
            logger.error("Failed to remove from cart: ", e);
            ConsoleUtil.printError("Failed to remove from cart: " + e.getMessage());
            return false;
        }
    }

    public void displayCart(int userId) {
        try {
            List<CartItem> cartItems = cartDAO.getCartItems(userId);
            if (cartItems.isEmpty()) {
                ConsoleUtil.printInfo("Your cart is empty");
                return;
            }

            System.out.println("\nYour Shopping Cart:");
            System.out.println("=".repeat(80));
            System.out.printf("%-40s %10s %15s %15s%n",
                    "Product", "Quantity", "Price", "Subtotal");
            System.out.println("-".repeat(80));

            double total = 0;
            for (CartItem item : cartItems) {
                double price = item.getProduct().getEffectivePrice();
                double subtotal = item.getSubtotal();
                total += subtotal;

                System.out.printf("%-40s %10d %15.2f %15.2f%n",
                        item.getProduct().getName(),
                        item.getQuantity(),
                        price,
                        subtotal);
            }

            System.out.println("-".repeat(80));
            System.out.printf("%67s %15.2f%n", "Total:", total);
            System.out.println("=".repeat(80));

        } catch (SQLException e) {
            logger.error("Failed to fetch cart: ", e);
            ConsoleUtil.printError("Failed to fetch cart: " + e.getMessage());
        }
    }

    public double getCartTotal(int userId) {
        try {
            return cartDAO.getCartTotal(userId);
        } catch (SQLException e) {
            logger.error("Failed to get cart total: ", e);
            return 0;
        }
    }

    public List<CartItem> getCartItems(int userId) {
        try {
            return cartDAO.getCartItems(userId);
        } catch (SQLException e) {
            logger.error("Failed to get cart items: ", e);
            return List.of();
        }
    }

    public void clearCart(int userId) {
        try {
            cartDAO.clearCart(userId);
            ConsoleUtil.printSuccess("Cart cleared");
            logger.info("Cart cleared for user: {}", userId);
        } catch (SQLException e) {
            logger.error("Failed to clear cart: ", e);
            ConsoleUtil.printError("Failed to clear cart: " + e.getMessage());
        }
    }

    public int getCartItemCount(int userId) {
        try {
            return cartDAO.getCartItemCount(userId);
        } catch (SQLException e) {
            logger.error("Failed to get cart count: ", e);
            return 0;
        }
    }
}