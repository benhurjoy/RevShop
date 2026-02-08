package com.revshop.service;

import com.revshop.dao.ReviewDAO;
import com.revshop.model.Review;
import com.revshop.util.ConsoleUtil;
import com.revshop.util.InputValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ReviewService {
    private static final Logger logger = LogManager.getLogger(ReviewService.class);
    private final ReviewDAO reviewDAO;

    public ReviewService() {
        this.reviewDAO = new ReviewDAO();
    }

    public boolean addReview(Review review) {
        try {
            // Validate rating
            if (!InputValidator.isValidRating(review.getRating())) {
                ConsoleUtil.printError("Rating must be between 1 and 5");
                return false;
            }

            // Validate comment length
            if (review.getComment() != null && review.getComment().length() > 500) {
                ConsoleUtil.printError("Comment too long (max 500 characters)");
                return false;
            }

            boolean added = reviewDAO.addReview(review);
            if (added) {
                ConsoleUtil.printSuccess("Review submitted successfully!");
                logger.info("Review added: user={}, product={}, rating={}",
                        review.getUserId(), review.getProductId(), review.getRating());
                return true;
            }
        } catch (SQLException e) {
            logger.error("Failed to add review: ", e);
            ConsoleUtil.printError("Failed to submit review: " + e.getMessage());
        }
        return false;
    }

    public void displayProductReviews(int productId) {
        try {
            List<Map<String, Object>> reviews = reviewDAO.getReviewsByProduct(productId);
            Map<String, Object> stats = reviewDAO.getProductRatingStats(productId);

            if (stats != null) {
                double avgRating = (Double) stats.get("average_rating");
                int totalReviews = (Integer) stats.get("total_reviews");

                System.out.println("\nProduct Reviews:");
                System.out.println("Average Rating: " + String.format("%.1f", avgRating) + " ★");
                System.out.println("Total Reviews: " + totalReviews);
                System.out.println("-".repeat(50));
            }

            if (reviews.isEmpty()) {
                ConsoleUtil.printInfo("No reviews yet");
                return;
            }

            for (Map<String, Object> review : reviews) {
                System.out.println("User: " + review.get("user_email"));
                System.out.println("Rating: " + "★".repeat((Integer) review.get("rating")) +
                        " (" + review.get("rating") + "/5)");
                if (review.get("comment") != null && !((String) review.get("comment")).isEmpty()) {
                    System.out.println("Comment: " + review.get("comment"));
                }
                System.out.println("Date: " + review.get("created_at"));
                System.out.println("-".repeat(50));
            }

        } catch (SQLException e) {
            logger.error("Failed to fetch reviews: ", e);
            ConsoleUtil.printError("Failed to fetch reviews: " + e.getMessage());
        }
    }

    public void displayUserReviews(int userId) {
        try {
            List<Map<String, Object>> reviews = reviewDAO.getReviewsByUser(userId);
            if (reviews.isEmpty()) {
                ConsoleUtil.printInfo("You have not reviewed any products");
                return;
            }

            System.out.println("\nYour Reviews:");
            System.out.println("=".repeat(80));
            System.out.printf("%-40s %10s %30s%n", "Product", "Rating", "Date");
            System.out.println("-".repeat(80));

            for (Map<String, Object> review : reviews) {
                System.out.printf("%-40s %10s %30s%n",
                        review.get("product_name"),
                        "★".repeat((Integer) review.get("rating")),
                        review.get("created_at"));
            }

            System.out.println("=".repeat(80));

        } catch (SQLException e) {
            logger.error("Failed to fetch user reviews: ", e);
            ConsoleUtil.printError("Failed to fetch your reviews: " + e.getMessage());
        }
    }

    public boolean deleteReview(int userId, int productId) {
        try {
            boolean deleted = reviewDAO.deleteReview(userId, productId);
            if (deleted) {
                ConsoleUtil.printSuccess("Review deleted successfully!");
                logger.info("Review deleted: user={}, product={}", userId, productId);
                return true;
            } else {
                ConsoleUtil.printError("Review not found");
                return false;
            }
        } catch (SQLException e) {
            logger.error("Failed to delete review: ", e);
            ConsoleUtil.printError("Failed to delete review: " + e.getMessage());
            return false;
        }
    }

    public Review getReview(int userId, int productId) {
        try {
            return reviewDAO.getReview(userId, productId);
        } catch (SQLException e) {
            logger.error("Failed to get review: ", e);
            return null;
        }
    }
}