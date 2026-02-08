package com.revshop.ui;

import com.revshop.model.User;
import com.revshop.service.UserService;
import com.revshop.util.ConsoleUtil;

public class AuthMenu {
    private final UserService userService;

    public AuthMenu() {
        this.userService = new UserService();
    }

    public void show() {
        while (true) {
            ConsoleUtil.printHeader("RevShop - Welcome");
            System.out.println("1. Buyer Login");
            System.out.println("2. Buyer Register");
            System.out.println("3. Seller Login");
            System.out.println("4. Seller Register");
            System.out.println("5. Forgot Password");
            System.out.println("6. Exit");

            int choice = ConsoleUtil.getIntInput("Select option: ", 1, 6);

            switch (choice) {
                case 1 -> buyerLogin();
                case 2 -> buyerRegister();
                case 3 -> sellerLogin();
                case 4 -> sellerRegister();
                case 5 -> forgotPassword();
                case 6 -> {
                    ConsoleUtil.printInfo("Thank you for using RevShop!");
                    System.exit(0);
                }
            }
        }
    }

    private void buyerLogin() {
        ConsoleUtil.printHeader("Buyer Login");

        String email = ConsoleUtil.getStringInput("Email: ", true);
        String password = ConsoleUtil.getStringInput("Password: ", true);

        User user = userService.login(email, password);
        if (user != null && user.getRole() == User.Role.BUYER) {
            if (!user.isVerified()) {
                verifyEmailFlow(user);
            } else {
                BuyerMenu buyerMenu = new BuyerMenu(user);
                buyerMenu.show();
            }
        }
    }

    private void buyerRegister() {
        ConsoleUtil.printHeader("Buyer Registration");

        String email = ConsoleUtil.getStringInput("Email: ", true);
        String password = ConsoleUtil.getStringInput("Password: ", true);
        String confirmPassword = ConsoleUtil.getStringInput("Confirm Password: ", true);

        if (!password.equals(confirmPassword)) {
            ConsoleUtil.printError("Passwords do not match");
            ConsoleUtil.pressEnterToContinue();
            return;
        }

        User user = userService.registerBuyer(email, password);
        if (user != null) {
            verifyEmailFlow(user);
            askToSetupSecurityQuestion(user);
        } else {
            ConsoleUtil.pressEnterToContinue();
        }
    }

    private void sellerLogin() {
        ConsoleUtil.printHeader("Seller Login");

        String email = ConsoleUtil.getStringInput("Email: ", true);
        String password = ConsoleUtil.getStringInput("Password: ", true);

        User user = userService.login(email, password);
        if (user != null && user.getRole() == User.Role.SELLER) {
            SellerMenu sellerMenu = new SellerMenu(user);
            sellerMenu.show();
        }
    }

    private void sellerRegister() {
        ConsoleUtil.printHeader("Seller Registration");

        String email = ConsoleUtil.getStringInput("Email: ", true);
        String password = ConsoleUtil.getStringInput("Password: ", true);
        String confirmPassword = ConsoleUtil.getStringInput("Confirm Password: ", true);

        if (!password.equals(confirmPassword)) {
            ConsoleUtil.printError("Passwords do not match");
            ConsoleUtil.pressEnterToContinue();
            return;
        }

        User user = userService.registerSeller(email, password);
        if (user != null) {
            askToSetupSecurityQuestion(user);
            SellerMenu sellerMenu = new SellerMenu(user);
            sellerMenu.show();
        } else {
            ConsoleUtil.pressEnterToContinue();
        }
    }

    private void forgotPassword() {
        ConsoleUtil.printHeader("Forgot Password");

        String email = ConsoleUtil.getStringInput("Enter your email address: ", true);

        boolean success = userService.initiatePasswordReset(email);

        if (success) {
            ConsoleUtil.printSuccess("Password reset completed successfully!");
        } else {
            ConsoleUtil.printError("Password reset failed. Please try again.");
        }

        ConsoleUtil.pressEnterToContinue();
    }

    private void askToSetupSecurityQuestion(User user) {
        ConsoleUtil.printHeader("Security Question Setup");
        ConsoleUtil.printInfo("Setting up a security question will help you reset your password if you forget it.");
        System.out.println("Would you like to set up a security question now?");
        System.out.println("1. Yes, set up security question");
        System.out.println("2. No, maybe later");

        int choice = ConsoleUtil.getIntInput("Select option: ", 1, 2);

        if (choice == 1) {
            setupSecurityQuestion(user);
        } else {
            ConsoleUtil.printInfo("You can set up a security question later from your account settings.");
            ConsoleUtil.pressEnterToContinue();
        }
    }

    private void setupSecurityQuestion(User user) {
        ConsoleUtil.printHeader("Set Security Question");

        String question = ConsoleUtil.getStringInput("Enter your security question: ", true);
        String answer = ConsoleUtil.getStringInput("Enter your answer: ", true);
        String confirmAnswer = ConsoleUtil.getStringInput("Confirm your answer: ", true);

        if (!answer.equals(confirmAnswer)) {
            ConsoleUtil.printError("Answers do not match.");
            ConsoleUtil.pressEnterToContinue();
            return;
        }

        if (userService.setupSecurityQuestion(user.getId(), question, answer)) {
            ConsoleUtil.printSuccess("Security question set up successfully!");
        } else {
            ConsoleUtil.printError("Failed to set up security question.");
        }

        ConsoleUtil.pressEnterToContinue();
    }

    private void verifyEmailFlow(User user) {
        ConsoleUtil.printHeader("Email Verification");
        ConsoleUtil.printInfo("An OTP has been sent to your email. Please enter it below.");

        int attempts = 3;
        while (attempts > 0) {
            String otp = ConsoleUtil.getStringInput("Enter OTP (or 'resend' to resend): ", true);

            if (otp.equalsIgnoreCase("resend")) {
                if (userService.resendOTP(user.getId(), user.getEmail())) {
                    ConsoleUtil.printSuccess("OTP resent successfully");
                    continue;
                } else {
                    ConsoleUtil.printError("Failed to resend OTP");
                    break;
                }
            }

            if (userService.verifyEmail(user.getId(), otp)) {
                ConsoleUtil.printSuccess("Email verified successfully!");
                BuyerMenu buyerMenu = new BuyerMenu(userService.getUserById(user.getId()));
                buyerMenu.show();
                return;
            } else {
                attempts--;
                if (attempts > 0) {
                    ConsoleUtil.printError("Invalid OTP. " + attempts + " attempts remaining.");
                }
            }
        }

        ConsoleUtil.printError("Too many failed attempts. Please try again later.");
        ConsoleUtil.pressEnterToContinue();
    }
}