package com.revshop.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordHashingTest {

    @Test
    public void testHashAndVerify() {
        String password = "Test@123";
        String hash = PasswordHasher.hashPassword(password);

        assertNotNull(hash);
        assertTrue(hash.contains(":"));
        assertTrue(PasswordHasher.verifyPassword(password, hash));
        assertFalse(PasswordHasher.verifyPassword("WrongPassword", hash));
    }

    @Test
    public void testPasswordStrengthValidation() {
        assertTrue(PasswordHasher.validatePasswordStrength("Strong@123"));
        assertFalse(PasswordHasher.validatePasswordStrength("weak"));
        assertFalse(PasswordHasher.validatePasswordStrength("weakpassword"));
        assertFalse(PasswordHasher.validatePasswordStrength("WEAKPASSWORD"));
        assertFalse(PasswordHasher.validatePasswordStrength("Weak123"));
        assertTrue(PasswordHasher.validatePasswordStrength("Valid@1234"));
    }

    @Test
    public void testDifferentPasswordsProduceDifferentHashes() {
        String password1 = "Password1@";
        String password2 = "Password2@";

        String hash1 = PasswordHasher.hashPassword(password1);
        String hash2 = PasswordHasher.hashPassword(password2);

        assertNotEquals(hash1, hash2);
        assertTrue(PasswordHasher.verifyPassword(password1, hash1));
        assertTrue(PasswordHasher.verifyPassword(password2, hash2));
        assertFalse(PasswordHasher.verifyPassword(password1, hash2));
        assertFalse(PasswordHasher.verifyPassword(password2, hash1));
    }

    @Test
    public void testOTPGeneration() {
        String otp = OTPGenerator.generateOTP();
        assertNotNull(otp);
        assertEquals(6, otp.length());
        assertTrue(otp.matches("\\d{6}"));
    }
}