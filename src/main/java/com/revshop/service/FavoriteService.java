package com.revshop.service;

import com.revshop.dao.FavoriteDAO;
import com.revshop.util.ConsoleUtil;
import com.revshop.util.TableFormatter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class FavoriteService {
    private static final Logger logger = LogManager.getLogger(FavoriteService.class);
    private final FavoriteDAO favoriteDAO;

    public FavoriteService() {
        this.favoriteDAO = new FavoriteDAO();
    }

    public boolean addFavorite(int userId, int productId) {
        try {
            boolean added = favoriteDAO.addFavorite(userId, productId);
            if (added) {
                ConsoleUtil.printSuccess("Added to favorites");
                logger.info("Product added to favorites: user={}, product={}", userId, productId);
                return true;
            } else {
                ConsoleUtil.printInfo("Product already in favorites");
                return false;
            }
        } catch (SQLException e) {
            logger.error("Failed to add favorite: ", e);
            ConsoleUtil.printError("Failed to add to favorites: " + e.getMessage());
            return false;
        }
    }

    public boolean removeFavorite(int userId, int productId) {
        try {
            boolean removed = favoriteDAO.removeFavorite(userId, productId);
            if (removed) {
                ConsoleUtil.printSuccess("Removed from favorites");
                logger.info("Product removed from favorites: user={}, product={}", userId, productId);
                return true;
            } else {
                ConsoleUtil.printError("Product not found in favorites");
                return false;
            }
        } catch (SQLException e) {
            logger.error("Failed to remove favorite: ", e);
            ConsoleUtil.printError("Failed to remove from favorites: " + e.getMessage());
            return false;
        }
    }

    public void displayFavorites(int userId) {
        try {
            List<Map<String, Object>> favorites = favoriteDAO.getFavoritesByUser(userId);
            if (favorites.isEmpty()) {
                ConsoleUtil.printInfo("You have no favorite products");
                return;
            }

            System.out.println("\nYour Favorite Products:");
            System.out.println("=".repeat(80));
            System.out.printf("%-40s %15s %15s %10s%n",
                    "Product", "Category", "Price", "Stock");
            System.out.println("-".repeat(80));

            for (Map<String, Object> favorite : favorites) {
                double price = (Double) favorite.get("discount_price") > 0 ?
                        (Double) favorite.get("discount_price") : (Double) favorite.get("mrp");

                System.out.printf("%-40s %15s %15.2f %10d%n",
                        favorite.get("name"),
                        favorite.get("category_name"),
                        price,
                        favorite.get("stock"));
            }

            System.out.println("=".repeat(80));

        } catch (SQLException e) {
            logger.error("Failed to fetch favorites: ", e);
            ConsoleUtil.printError("Failed to fetch favorites: " + e.getMessage());
        }
    }

    public boolean isFavorite(int userId, int productId) {
        try {
            return favoriteDAO.isFavorite(userId, productId);
        } catch (SQLException e) {
            logger.error("Failed to check favorite: ", e);
            return false;
        }
    }
}