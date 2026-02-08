package com.revshop.service;

import com.revshop.dao.UserDAO;
import com.revshop.model.User;
import com.revshop.security.EmailService;
import com.revshop.security.OTPGenerator;
import com.revshop.security.PasswordHasher;
import com.revshop.util.ConsoleUtil;
import com.revshop.util.InputValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Random;

public class UserService {
    private static final Logger logger = LogManager.getLogger(UserService.class);
    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    public User registerBuyer(String email, String password) {
        try {
            // Validate email
            if (!InputValidator.isValidEmail(email)) {
                ConsoleUtil.printError("Invalid email format");
                return null;
            }

            // Check if email exists
            if (userDAO.emailExists(email)) {
                ConsoleUtil.printError("Email already registered");
                return null;
            }

            // Validate password strength
            if (!PasswordHasher.validatePasswordStrength(password)) {
                ConsoleUtil.printError("Password must be at least 8 characters with uppercase, lowercase, digit, and special character");
                return null;
            }

            // Register user
            int userId = userDAO.registerUser(email, password, User.Role.BUYER);
            if (userId > 0) {
                // Generate and send OTP
                String otp = OTPGenerator.generateOTP();
                LocalDateTime expiryTime = OTPGenerator.generateExpiryTime();
                userDAO.saveOTP(userId, otp, expiryTime);

                // Send email
                if (EmailService.sendOTPEmail(email, otp)) {
                    ConsoleUtil.printSuccess("Registration successful! OTP sent to your email.");
                    logger.info("Buyer registered successfully: {}", email);

                    User user = new User();
                    user.setId(userId);
                    user.setEmail(email);
                    user.setRole(User.Role.BUYER);
                    user.setVerified(false);
                    return user;
                } else {
                    ConsoleUtil.printError("Failed to send OTP email. Please try again.");
                    return null;
                }
            }
        } catch (SQLException e) {
            logger.error("Registration failed: ", e);
            ConsoleUtil.printError("Registration failed: " + e.getMessage());
        }
        return null;
    }

    public User registerSeller(String email, String password) {
        try {
            // Validate email
            if (!InputValidator.isValidEmail(email)) {
                ConsoleUtil.printError("Invalid email format");
                return null;
            }

            // Check if email exists
            if (userDAO.emailExists(email)) {
                ConsoleUtil.printError("Email already registered");
                return null;
            }

            // Validate password strength
            if (!PasswordHasher.validatePasswordStrength(password)) {
                ConsoleUtil.printError("Password must be at least 8 characters with uppercase, lowercase, digit, and special character");
                return null;
            }

            // Register user (sellers are auto-verified)
            int userId = userDAO.registerUser(email, password, User.Role.SELLER);
            if (userId > 0) {
                ConsoleUtil.printSuccess("Seller registration successful!");
                logger.info("Seller registered successfully: {}", email);

                User user = new User();
                user.setId(userId);
                user.setEmail(email);
                user.setRole(User.Role.SELLER);
                user.setVerified(true);
                return user;
            }
        } catch (SQLException e) {
            logger.error("Seller registration failed: ", e);
            ConsoleUtil.printError("Registration failed: " + e.getMessage());
        }
        return null;
    }

    public User login(String email, String password) {
        try {
            User user = userDAO.login(email, password);
            if (user != null) {
                if (user.getRole() == User.Role.BUYER && !user.isVerified()) {
                    ConsoleUtil.printError("Please verify your email first");
                    return null;
                }
                ConsoleUtil.printSuccess("Login successful!");
                logger.info("User logged in: {}", email);
                return user;
            } else {
                ConsoleUtil.printError("Invalid email or password");
                return null;
            }
        } catch (SQLException e) {
            logger.error("Login failed: ", e);
            ConsoleUtil.printError("Login failed: " + e.getMessage());
            return null;
        }
    }

    public boolean verifyEmail(int userId, String otp) {
        try {
            boolean verified = userDAO.verifyOTP(userId, otp);
            if (verified) {
                userDAO.verifyUser(userId);
                ConsoleUtil.printSuccess("Email verified successfully!");
                logger.info("Email verified for user ID: {}", userId);
                return true;
            } else {
                ConsoleUtil.printError("Invalid or expired OTP");
                return false;
            }
        } catch (SQLException e) {
            logger.error("Email verification failed: ", e);
            ConsoleUtil.printError("Verification failed: " + e.getMessage());
            return false;
        }
    }

    public boolean resendOTP(int userId, String email) {
        try {
            User user = userDAO.getUserById(userId);
            if (user == null || !user.getEmail().equals(email)) {
                return false;
            }

            String otp = OTPGenerator.generateOTP();
            LocalDateTime expiryTime = OTPGenerator.generateExpiryTime();
            userDAO.saveOTP(userId, otp, expiryTime);

            if (EmailService.sendOTPEmail(email, otp)) {
                ConsoleUtil.printSuccess("OTP resent to your email");
                logger.info("OTP resent to: {}", email);
                return true;
            }
            return false;
        } catch (SQLException e) {
            logger.error("Failed to resend OTP: ", e);
            return false;
        }
    }

    public User getUserById(int userId) {
        try {
            return userDAO.getUserById(userId);
        } catch (SQLException e) {
            logger.error("Failed to get user: ", e);
            return null;
        }
    }

    // NEW METHODS FOR PASSWORD RESET

    public boolean initiatePasswordReset(String email) {
        try {
            User user = userDAO.getUserByEmail(email);
            if (user == null) {
                ConsoleUtil.printError("No account found with this email address.");
                return false;
            }

            ConsoleUtil.printHeader("Password Reset Options");
            System.out.println("Choose reset method:");
            System.out.println("1. Email OTP");
            System.out.println("2. Security Question");
            System.out.println("3. Cancel");

            int choice = ConsoleUtil.getIntInput("Select option: ", 1, 3);

            switch (choice) {
                case 1 -> {
                    return sendPasswordResetOTP(user);
                }
                case 2 -> {
                    if (!userDAO.hasSecurityQuestion(user.getId())) {
                        ConsoleUtil.printError("No security question set for this account.");
                        return false;
                    }
                    return verifySecurityQuestion(user);
                }
                case 3 -> {
                    return false;
                }
            }
            return false;
        } catch (SQLException e) {
            logger.error("Error initiating password reset: ", e);
            ConsoleUtil.printError("Error initiating password reset: " + e.getMessage());
            return false;
        }
    }

    private boolean sendPasswordResetOTP(User user) {
        try {
            String otp = generateResetOTP();
            LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(10);

            userDAO.saveOTP(user.getId(), otp, expiryTime);

            String subject = "RevShop - Password Reset OTP";
            String body = createResetOTPEmailBody(user.getEmail(), otp);

            boolean emailSent = EmailService.sendResetEmail(user.getEmail(), subject, body);

            if (emailSent) {
                ConsoleUtil.printSuccess("Password reset OTP sent to your email.");
                return verifyResetOTPAndResetPassword(user);
            } else {
                ConsoleUtil.printError("Failed to send OTP email. Please try again.");
                return false;
            }
        } catch (Exception e) {
            logger.error("Error sending password reset OTP: ", e);
            ConsoleUtil.printError("Error sending OTP: " + e.getMessage());
            return false;
        }
    }

    private boolean verifySecurityQuestion(User user) {
        try {
            String question = user.getSecurityQuestion();
            if (question == null || question.trim().isEmpty()) {
                ConsoleUtil.printError("No security question set for this account.");
                return false;
            }

            ConsoleUtil.printHeader("Security Question");
            System.out.println("Question: " + question);

            String answer = ConsoleUtil.getStringInput("Your answer: ", true);

            if (userDAO.verifySecurityAnswer(user.getId(), answer)) {
                ConsoleUtil.printSuccess("Security answer verified successfully!");
                return resetPassword(user);
            } else {
                ConsoleUtil.printError("Incorrect answer.");
                return false;
            }
        } catch (SQLException e) {
            logger.error("Error verifying security answer: ", e);
            ConsoleUtil.printError("Error verifying security answer: " + e.getMessage());
            return false;
        }
    }

    private boolean verifyResetOTPAndResetPassword(User user) {
        ConsoleUtil.printHeader("Password Reset - OTP Verification");
        ConsoleUtil.printInfo("Enter the OTP sent to your email to reset your password.");

        int attempts = 3;
        while (attempts > 0) {
            String otp = ConsoleUtil.getStringInput("Enter OTP (or 'resend' to resend): ", true);

            if (otp.equalsIgnoreCase("resend")) {
                sendPasswordResetOTP(user);
                continue;
            }

            try {
                if (userDAO.verifyOTP(user.getId(), otp)) {
                    return resetPassword(user);
                } else {
                    attempts--;
                    if (attempts > 0) {
                        ConsoleUtil.printError("Invalid OTP. " + attempts + " attempts remaining.");
                    }
                }
            } catch (SQLException e) {
                logger.error("Error verifying OTP: ", e);
                ConsoleUtil.printError("Error verifying OTP: " + e.getMessage());
                return false;
            }
        }

        ConsoleUtil.printError("Too many failed attempts. Please try again later.");
        return false;
    }

    private boolean resetPassword(User user) {
        ConsoleUtil.printHeader("Set New Password");

        String newPassword = ConsoleUtil.getStringInput("New Password: ", true);
        String confirmPassword = ConsoleUtil.getStringInput("Confirm New Password: ", true);

        if (!newPassword.equals(confirmPassword)) {
            ConsoleUtil.printError("Passwords do not match.");
            return false;
        }

        if (!PasswordHasher.validatePasswordStrength(newPassword)) {
            ConsoleUtil.printError("Password must be at least 8 characters with uppercase, lowercase, digit, and special character");
            return false;
        }

        try {
            if (userDAO.updatePassword(user.getId(), newPassword)) {
                ConsoleUtil.printSuccess("Password reset successfully!");
                logger.info("Password reset for user: {}", user.getEmail());

                // Send confirmation email
                String subject = "RevShop - Password Reset Confirmation";
                String body = createPasswordResetConfirmationEmailBody(user.getEmail());

                EmailService.sendResetEmail(user.getEmail(), subject, body);
                return true;
            } else {
                ConsoleUtil.printError("Failed to reset password.");
                return false;
            }
        } catch (SQLException e) {
            logger.error("Error resetting password: ", e);
            ConsoleUtil.printError("Error resetting password: " + e.getMessage());
            return false;
        }
    }

    private String generateResetOTP() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(999999));
    }

    private String createResetOTPEmailBody(String email, String otp) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; padding: 20px;">
                    <div style="max-width: 600px; margin: 0 auto; border: 1px solid #ddd; border-radius: 10px; padding: 30px;">
                        <h2 style="color: #333; text-align: center;">RevShop Password Reset</h2>
                        <p style="font-size: 16px; color: #555;">
                            Dear %s,<br><br>
                            You requested to reset your password. Please use the following OTP:
                        </p>
                        <div style="text-align: center; margin: 30px 0;">
                            <span style="font-size: 32px; font-weight: bold; letter-spacing: 10px; color: #2c3e50; background: #f8f9fa; padding: 15px 30px; border-radius: 5px; border: 2px dashed #e74c3c;">
                                %s
                            </span>
                        </div>
                        <p style="font-size: 14px; color: #777;">
                            This OTP is valid for 10 minutes. If you didn't request this, please ignore this email.
                        </p>
                        <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                        <p style="font-size: 12px; color: #999; text-align: center;">
                            © 2024 RevShop Electronics Marketplace. All rights reserved.
                        </p>
                    </div>
                </body>
                </html>
                """.formatted(email, otp);
    }

    private String createPasswordResetConfirmationEmailBody(String email) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; padding: 20px;">
                    <div style="max-width: 600px; margin: 0 auto; border: 1px solid #ddd; border-radius: 10px; padding: 30px;">
                        <h2 style="color: #333; text-align: center;">Password Reset Successful</h2>
                        <p style="font-size: 16px; color: #555;">
                            Dear %s,<br><br>
                            Your password has been successfully reset.
                        </p>
                        <div style="text-align: center; margin: 30px 0;">
                            <div style="background: #d4edda; color: #155724; padding: 15px; border-radius: 5px; border: 1px solid #c3e6cb;">
                                <strong>Password Reset Complete</strong>
                            </div>
                        </div>
                        <p style="font-size: 14px; color: #777;">
                            If you did not perform this action, please contact our support team immediately.
                        </p>
                        <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                        <p style="font-size: 12px; color: #999; text-align: center;">
                            © 2024 RevShop Electronics Marketplace. All rights reserved.
                        </p>
                    </div>
                </body>
                </html>
                """.formatted(email);
    }

    public boolean setupSecurityQuestion(int userId, String question, String answer) {
        try {
            if (question == null || question.trim().isEmpty() || answer == null || answer.trim().isEmpty()) {
                ConsoleUtil.printError("Question and answer cannot be empty.");
                return false;
            }

            boolean success = userDAO.setSecurityQuestion(userId, question, answer);
            if (success) {
                logger.info("Security question set for user ID: {}", userId);
            }
            return success;
        } catch (SQLException e) {
            logger.error("Error setting up security question: ", e);
            ConsoleUtil.printError("Error setting up security question: " + e.getMessage());
            return false;
        }
    }

    public boolean updateSecurityQuestion(int userId, String currentPassword, String newQuestion, String newAnswer) {
        try {
            User user = userDAO.getUserById(userId);
            if (user == null) {
                return false;
            }

            // Verify current password
            if (!PasswordHasher.verifyPassword(currentPassword, user.getPasswordHash())) {
                ConsoleUtil.printError("Current password is incorrect.");
                return false;
            }

            return setupSecurityQuestion(userId, newQuestion, newAnswer);
        } catch (SQLException e) {
            logger.error("Error updating security question: ", e);
            return false;
        }
    }
}