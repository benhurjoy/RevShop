package com.revshop.util;

import java.util.List;
import java.util.Map;

public class TableFormatter {
    public static void printTable(List<String> headers, List<List<String>> rows) {
        if (headers == null || headers.isEmpty() || rows == null) {
            return;
        }

        // Calculate column widths
        int[] columnWidths = new int[headers.size()];
        for (int i = 0; i < headers.size(); i++) {
            columnWidths[i] = Math.max(columnWidths[i], headers.get(i).length());
        }

        for (List<String> row : rows) {
            for (int i = 0; i < row.size() && i < headers.size(); i++) {
                String cell = row.get(i) != null ? row.get(i) : "";
                columnWidths[i] = Math.max(columnWidths[i], cell.length());
            }
        }

        // Add padding
        for (int i = 0; i < columnWidths.length; i++) {
            columnWidths[i] += 2;
        }

        // Print top border
        printBorder(columnWidths);

        // Print headers
        printRow(headers, columnWidths);

        // Print separator
        printSeparator(columnWidths);

        // Print rows
        for (List<String> row : rows) {
            printRow(row, columnWidths);
        }

        // Print bottom border
        printBorder(columnWidths);
    }

    private static void printBorder(int[] widths) {
        System.out.print("+");
        for (int width : widths) {
            System.out.print("-".repeat(width) + "+");
        }
        System.out.println();
    }

    private static void printSeparator(int[] widths) {
        System.out.print("|");
        for (int width : widths) {
            System.out.print("-".repeat(width) + "+");
        }
        System.out.println();
    }

    private static void printRow(List<String> cells, int[] widths) {
        System.out.print("|");
        for (int i = 0; i < widths.length; i++) {
            String cell = i < cells.size() && cells.get(i) != null ? cells.get(i) : "";
            System.out.printf(" %-" + (widths[i] - 2) + "s |", cell);
        }
        System.out.println();
    }

    public static void printProductTable(List<Map<String, Object>> products) {
        if (products == null || products.isEmpty()) {
            System.out.println("No products found.");
            return;
        }

        List<String> headers = List.of("ID", "Name", "Category", "MRP", "Discounted", "Stock", "SellerId");
        List<List<String>> rows = new java.util.ArrayList<>();

        for (Map<String, Object> product : products) {
            List<String> row = new java.util.ArrayList<>();
            row.add(String.valueOf(product.get("order_id")));
            row.add((String) product.get("name"));
            row.add((String) product.get("category_name"));
            row.add(String.format("₹%.2f", (Double) product.get("mrp")));

            Double discountPrice = (Double) product.get("discount_price");
            if (discountPrice != null && discountPrice > 0) {
                row.add(String.format("₹%.2f", discountPrice));
            } else {
                row.add("N/A");
            }

            row.add(String.valueOf(product.get("stock")));
            row.add((String) product.get("seller_id"));
            rows.add(row);
        }

        printTable(headers, rows);
    }

    public static void printOrderTable(List<Map<String, Object>> orders) {
        if (orders == null || orders.isEmpty()) {
            System.out.println("No orders found.");
            return;
        }

        List<String> headers = List.of("Order ID", "Date", "Total", "Status", "Items");
        List<List<String>> rows = new java.util.ArrayList<>();

        for (Map<String, Object> order : orders) {
            List<String> row = new java.util.ArrayList<>();
            row.add(String.valueOf(order.get("order_id")));
            row.add(((java.sql.Timestamp) order.get("created_at")).toLocalDateTime().toString());
            row.add(String.format("₹%.2f", (Double) order.get("total_amount")));
            row.add((String) order.get("status"));
            row.add(String.valueOf(order.get("item_count")));
            rows.add(row);
        }

        printTable(headers, rows);
    }
}