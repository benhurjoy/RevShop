package com.revshop.util;

import java.util.Scanner;

public class ConsoleUtil {
    private static final Scanner scanner = new Scanner(System.in);

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void printHeader(String title) {
        clearScreen();
        System.out.println("=".repeat(60));
        System.out.println(" ".repeat((60 - title.length()) / 2) + title);
        System.out.println("=".repeat(60));
        System.out.println();
    }

    public static void printMenu(String[] options) {
        for (int i = 0; i < options.length; i++) {
            System.out.printf("%2d. %s%n", i + 1, options[i]);
        }
        System.out.println();
    }

    public static int getIntInput(String prompt, int min, int max) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    System.out.println("Input cannot be empty. Please try again.");
                    continue;
                }

                int value = Integer.parseInt(input);

                if (value < min || value > max) {
                    System.out.printf("Please enter a number between %d and %d.%n", min, max);
                    continue;
                }

                return value;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }

    public static String getStringInput(String prompt, boolean required) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (required && input.isEmpty()) {
                System.out.println("This field is required. Please enter a value.");
                continue;
            }

            return input;
        }
    }

    public static double getDoubleInput(String prompt, double min) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    System.out.println("Input cannot be empty. Please try again.");
                    continue;
                }

                double value = Double.parseDouble(input);

                if (value < min) {
                    System.out.printf("Value must be at least %.2f%n", min);
                    continue;
                }

                return value;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }

    public static boolean getConfirmation(String prompt) {
        while (true) {
            System.out.print(prompt + " (y/n): ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("y") || input.equals("yes")) {
                return true;
            } else if (input.equals("n") || input.equals("no")) {
                return false;
            }

            System.out.println("Please enter 'y' for yes or 'n' for no.");
        }
    }

    public static void pressEnterToContinue() {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }

    public static void printSuccess(String message) {
        System.out.println("✓ " + message);
    }

    public static void printError(String message) {
        System.out.println("✗ ERROR: " + message);
    }

    public static void printInfo(String message) {
        System.out.println("ℹ " + message);
    }
}