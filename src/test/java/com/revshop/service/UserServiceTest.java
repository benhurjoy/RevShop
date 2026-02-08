package com.revshop.service;

import com.revshop.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {
    private UserService userService;

    @BeforeEach
    public void setUp() {
        userService = new UserService();
    }

    @Test
    public void testPasswordStrengthValidation() {
        // Note: These tests check the validation logic
        // Actual registration would require database connection

        String weakPassword = "weak";
        String strongPassword = "Strong@123";

        // Test through the service's validation logic
        assertFalse(com.revshop.security.PasswordHasher.validatePasswordStrength(weakPassword));
        assertTrue(com.revshop.security.PasswordHasher.validatePasswordStrength(strongPassword));
    }

    @Test
    public void testEmailValidation() {
        String validEmail = "test@example.com";
        String invalidEmail = "invalid-email";

        // Test email validation logic
        assertTrue(com.revshop.util.InputValidator.isValidEmail(validEmail));
        assertFalse(com.revshop.util.InputValidator.isValidEmail(invalidEmail));
    }

    @Test
    public void testUserRoleEnum() {
        User.Role buyerRole = User.Role.BUYER;
        User.Role sellerRole = User.Role.SELLER;

        assertEquals("BUYER", buyerRole.toString());
        assertEquals("SELLER", sellerRole.toString());
        assertNotEquals(buyerRole, sellerRole);
    }
}