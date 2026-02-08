package com.revshop.util;

import java.util.regex.Pattern;

public class InputValidator {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[a-zA-Z0-9\\s\\-.,'&()]{2,100}$");

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidProductName(String name) {
        if (name == null || name.trim().length() < 2 || name.trim().length() > 200) {
            return false;
        }
        return NAME_PATTERN.matcher(name).matches();
    }

    public static boolean isValidPrice(double price) {
        return price > 0 && price <= 10000000; // Max 10 million
    }

    public static boolean isValidStock(int stock) {
        return stock >= 0 && stock <= 10000; // Max 10,000 units
    }

    public static boolean isValidRating(int rating) {
        return rating >= 1 && rating <= 5;
    }

    public static boolean isValidDescription(String description) {
        return description != null && description.length() <= 1000;
    }
}