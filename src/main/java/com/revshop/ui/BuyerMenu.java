package com.revshop.ui;

import com.revshop.model.*;
import com.revshop.service.*;
import com.revshop.util.ConsoleUtil;

import java.util.List;
import java.util.Map;

public class BuyerMenu {
    private final User user;
    private final ProductService productService;
    private final CartService cartService;
    private final OrderService orderService;
    private final ReviewService reviewService;
    private final FavoriteService favoriteService;
    private final NotificationService notificationService;

    public BuyerMenu(User user) {
        this.user = user;
        this.productService = new ProductService();
        this.cartService = new CartService();
        this.orderService = new OrderService();
        this.reviewService = new ReviewService();
        this.favoriteService = new FavoriteService();
        this.notificationService = new NotificationService();
    }

    public void show() {
        while (true) {
            ConsoleUtil.printHeader("Buyer Dashboard - " + user.getEmail());

            // Show cart item count
            int cartCount = cartService.getCartItemCount(user.getId());
            System.out.println("Cart: " + cartCount + " items");

            System.out.println("\nMain Menu:");
            String[] options = {
                    "Browse Products",
                    "Search Products",
                    "View Categories",
                    "View Cart",
                    "View Orders",
                    "View Favorites",
                    "View Reviews",
                    "View Notifications",
                    "Logout"
            };

            ConsoleUtil.printMenu(options);
            int choice = ConsoleUtil.getIntInput("Select option: ", 1, options.length);

            switch (choice) {
                case 1 -> browseProducts();
                case 2 -> searchProducts();
                case 3 -> viewCategories();
                case 4 -> viewCart();
                case 5 -> viewOrders();
                case 6 -> viewFavorites();
                case 7 -> viewReviews();
                case 8 -> viewNotifications();
                case 9 -> {
                    ConsoleUtil.printSuccess("Logged out successfully");
                    return;
                }
            }
        }
    }

    private void browseProducts() {
        while (true) {
            ConsoleUtil.printHeader("Browse Products");
            productService.displayAllProducts();

            System.out.println("\nOptions:");
            System.out.println("1. View Product Details");
            System.out.println("2. Add to Cart");
            System.out.println("3. Add to Favorites");
            System.out.println("4. View Reviews");
            System.out.println("5. Back to Main Menu");

            int choice = ConsoleUtil.getIntInput("Select option: ", 1, 5);

            switch (choice) {
                case 1 -> viewProductDetails();
                case 2 -> addToCart();
                case 3 -> addToFavorites();
                case 4 -> viewProductReviews();
                case 5 -> { return; }
            }
        }
    }

    private void viewProductDetails() {
        int productId = ConsoleUtil.getIntInput("Enter Product ID: ", 1, Integer.MAX_VALUE);
        Product product = productService.getProductById(productId);

        if (product == null) {
            ConsoleUtil.printError("Product not found");
            return;
        }

        ConsoleUtil.printHeader("Product Details");
        System.out.println("Name: " + product.getName());
        System.out.println("Description: " + product.getDescription());
        System.out.println("MRP: ₹" + product.getMrp());
        System.out.println("Discounted Price: ₹" +
                (product.getDiscountPrice() != null ? product.getDiscountPrice() : "N/A"));
        System.out.println("Discount: " + String.format("%.1f", product.getDiscountPercentage()) + "%");
        System.out.println("Stock: " + product.getStock());

        // Show if in favorites
        boolean isFavorite = favoriteService.isFavorite(user.getId(), productId);
        System.out.println("In Favorites: " + (isFavorite ? "✓" : "✗"));

        System.out.println("\nOptions:");
        System.out.println("1. Add to Cart");
        System.out.println("2. " + (isFavorite ? "Remove from" : "Add to") + " Favorites");
        System.out.println("3. View Reviews");
        System.out.println("4. Add Review");
        System.out.println("5. Back");

        int choice = ConsoleUtil.getIntInput("Select option: ", 1, 5);

        switch (choice) {
            case 1 -> {
                int quantity = ConsoleUtil.getIntInput("Quantity: ", 1, product.getStock());
                cartService.addToCart(user.getId(), productId, quantity);
            }
            case 2 -> {
                if (isFavorite) {
                    favoriteService.removeFavorite(user.getId(), productId);
                } else {
                    favoriteService.addFavorite(user.getId(), productId);
                }
            }
            case 3 -> {
                reviewService.displayProductReviews(productId);
                ConsoleUtil.pressEnterToContinue();
            }
            case 4 -> addReview(productId);
            case 5 -> { return; }
        }
    }

    private void addToCart() {
        int productId = ConsoleUtil.getIntInput("Enter Product ID: ", 1, Integer.MAX_VALUE);
        Product product = productService.getProductById(productId);

        if (product == null) {
            ConsoleUtil.printError("Product not found");
            return;
        }

        if (product.getStock() <= 0) {
            ConsoleUtil.printError("Product out of stock");
            return;
        }

        int maxQuantity = Math.min(product.getStock(), 10); // Limit to 10 per order
        int quantity = ConsoleUtil.getIntInput(
                "Quantity (1-" + maxQuantity + "): ", 1, maxQuantity);

        cartService.addToCart(user.getId(), productId, quantity);
        ConsoleUtil.pressEnterToContinue();
    }

    private void addToFavorites() {
        int productId = ConsoleUtil.getIntInput("Enter Product ID: ", 1, Integer.MAX_VALUE);
        favoriteService.addFavorite(user.getId(), productId);
        ConsoleUtil.pressEnterToContinue();
    }

    private void viewProductReviews() {
        int productId = ConsoleUtil.getIntInput("Enter Product ID: ", 1, Integer.MAX_VALUE);
        reviewService.displayProductReviews(productId);
        ConsoleUtil.pressEnterToContinue();
    }

    private void addReview(int productId) {
        ConsoleUtil.printHeader("Add Review");

        Review existingReview = reviewService.getReview(user.getId(), productId);
        if (existingReview != null) {
            System.out.println("You have already reviewed this product:");
            System.out.println("Rating: " + existingReview.getRating() + "/5");
            System.out.println("Comment: " + existingReview.getComment());

            if (!ConsoleUtil.getConfirmation("Do you want to update your review?")) {
                return;
            }
        }

        int rating = ConsoleUtil.getIntInput("Rating (1-5): ", 1, 5);
        String comment = ConsoleUtil.getStringInput("Comment (optional): ", false);

        Review review = new Review();
        review.setUserId(user.getId());
        review.setProductId(productId);
        review.setRating(rating);
        review.setComment(comment);

        reviewService.addReview(review);
        ConsoleUtil.pressEnterToContinue();
    }

    private void searchProducts() {
        String keyword = ConsoleUtil.getStringInput("Search: ", true);
        productService.searchProducts(keyword);
        ConsoleUtil.pressEnterToContinue();
    }

    private void viewCategories() {
        while (true) {
            ConsoleUtil.printHeader("Categories");
            productService.displayCategories();

            System.out.println("\nOptions:");
            System.out.println("1. View Category Products");
            System.out.println("2. Back");

            int choice = ConsoleUtil.getIntInput("Select option: ", 1, 2);

            if (choice == 1) {
                int categoryId = ConsoleUtil.getIntInput("Enter Category ID: ", 1, Integer.MAX_VALUE);
                productService.displayProductsByCategory(categoryId);
                ConsoleUtil.pressEnterToContinue();
            } else {
                return;
            }
        }
    }

    private void viewCart() {
        while (true) {
            ConsoleUtil.printHeader("Shopping Cart");
            cartService.displayCart(user.getId());

            System.out.println("\nOptions:");
            System.out.println("1. Update Quantity");
            System.out.println("2. Remove Item");
            System.out.println("3. Checkout");
            System.out.println("4. Clear Cart");
            System.out.println("5. Back");

            int choice = ConsoleUtil.getIntInput("Select option: ", 1, 5);

            switch (choice) {
                case 1 -> updateCartQuantity();
                case 2 -> removeFromCart();
                case 3 -> checkout();
                case 4 -> {
                    if (ConsoleUtil.getConfirmation("Clear all items from cart?")) {
                        cartService.clearCart(user.getId());
                    }
                }
                case 5 -> { return; }
            }
        }
    }

    private void updateCartQuantity() {
        int productId = ConsoleUtil.getIntInput("Enter Product ID: ", 1, Integer.MAX_VALUE);
        int quantity = ConsoleUtil.getIntInput("New Quantity: ", 0, 100);
        cartService.updateCartItem(user.getId(), productId, quantity);
        ConsoleUtil.pressEnterToContinue();
    }

    private void removeFromCart() {
        int productId = ConsoleUtil.getIntInput("Enter Product ID: ", 1, Integer.MAX_VALUE);
        cartService.removeFromCart(user.getId(), productId);
        ConsoleUtil.pressEnterToContinue();
    }

    private void checkout() {
        ConsoleUtil.printHeader("Checkout");

        List<CartItem> cartItems = cartService.getCartItems(user.getId());
        if (cartItems.isEmpty()) {
            ConsoleUtil.printError("Your cart is empty");
            return;
        }

        double total = cartService.getCartTotal(user.getId());
        System.out.println("Total Amount: ₹" + total);
        System.out.println("\nPayment Methods:");
        System.out.println("1. Credit Card");
        System.out.println("2. Debit Card");
        System.out.println("3. UPI");
        System.out.println("4. Net Banking");
        System.out.println("5. Cancel");

        int paymentChoice = ConsoleUtil.getIntInput("Select payment method: ", 1, 5);
        if (paymentChoice == 5) {
            return;
        }

        String[] methods = {"CREDIT_CARD", "DEBIT_CARD", "UPI", "NET_BANKING"};
        String paymentMethod = methods[paymentChoice - 1];

        if (ConsoleUtil.getConfirmation("Confirm order for ₹" + total + "?")) {
            int orderId = orderService.placeOrder(user.getId(), paymentMethod);
            if (orderId > 0) {
                ConsoleUtil.printSuccess("Order placed successfully! Order ID: " + orderId);
            }
        }

        ConsoleUtil.pressEnterToContinue();
    }

    private void viewOrders() {
        while (true) {
            ConsoleUtil.printHeader("My Orders");
            orderService.displayUserOrders(user.getId());

            System.out.println("\nOptions:");
            System.out.println("1. View Order Details");
            System.out.println("2. Back");

            int choice = ConsoleUtil.getIntInput("Select option: ", 1, 2);

            if (choice == 1) {
                int orderId = ConsoleUtil.getIntInput("Enter Order ID: ", 1, Integer.MAX_VALUE);
                orderService.displayOrderDetails(orderId);
                ConsoleUtil.pressEnterToContinue();
            } else {
                return;
            }
        }
    }

    private void viewFavorites() {
        while (true) {
            ConsoleUtil.printHeader("My Favorites");
            favoriteService.displayFavorites(user.getId());

            System.out.println("\nOptions:");
            System.out.println("1. Remove from Favorites");
            System.out.println("2. View Product Details");
            System.out.println("3. Back");

            int choice = ConsoleUtil.getIntInput("Select option: ", 1, 3);

            switch (choice) {
                case 1 -> {
                    int productId = ConsoleUtil.getIntInput("Enter Product ID: ", 1, Integer.MAX_VALUE);
                    favoriteService.removeFavorite(user.getId(), productId);
                }
                case 2 -> {
                    int productId = ConsoleUtil.getIntInput("Enter Product ID: ", 1, Integer.MAX_VALUE);
                    viewProductDetails();
                }
                case 3 -> { return; }
            }
        }
    }

    private void viewReviews() {
        while (true) {
            ConsoleUtil.printHeader("My Reviews");
            reviewService.displayUserReviews(user.getId());

            System.out.println("\nOptions:");
            System.out.println("1. Update Review");
            System.out.println("2. Delete Review");
            System.out.println("3. Back");

            int choice = ConsoleUtil.getIntInput("Select option: ", 1, 3);

            switch (choice) {
                case 1 -> {
                    int productId = ConsoleUtil.getIntInput("Enter Product ID: ", 1, Integer.MAX_VALUE);
                    addReview(productId);
                }
                case 2 -> {
                    int productId = ConsoleUtil.getIntInput("Enter Product ID: ", 1, Integer.MAX_VALUE);
                    reviewService.deleteReview(user.getId(), productId);
                    ConsoleUtil.pressEnterToContinue();
                }
                case 3 -> { return; }
            }
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