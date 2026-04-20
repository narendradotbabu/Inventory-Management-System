package com.retailpos.util;

import java.sql.*;

public class DatabaseHelper {
    private static final String DB_URL = "jdbc:sqlite:retailpos.db";
    private static DatabaseHelper instance;
    private Connection connection;

    private DatabaseHelper() {
        initDatabase();
    }

    public static DatabaseHelper getInstance() {
        if (instance == null) instance = new DatabaseHelper();
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
                connection.createStatement().execute("PRAGMA foreign_keys = ON");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return connection;
    }

    private void initDatabase() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            Statement stmt = connection.createStatement();

            // Users table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    username TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    role TEXT NOT NULL CHECK(role IN ('ADMIN','CASHIER'))
                )
            """);

            // Products table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS products (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    barcode TEXT UNIQUE NOT NULL,
                    name TEXT NOT NULL,
                    category TEXT,
                    price REAL NOT NULL,
                    stock_qty INTEGER NOT NULL DEFAULT 0,
                    tax_rate REAL DEFAULT 0.18
                )
            """);

            // Sales table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS sales (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    cashier_id INTEGER,
                    sale_date TEXT NOT NULL,
                    subtotal REAL,
                    tax_amount REAL,
                    total_amount REAL,
                    payment_method TEXT,
                    payment_status TEXT DEFAULT 'COMPLETED',
                    FOREIGN KEY(cashier_id) REFERENCES users(id)
                )
            """);

            // Sale items table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS sale_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    sale_id INTEGER,
                    product_id INTEGER,
                    quantity INTEGER,
                    unit_price REAL,
                    subtotal REAL,
                    FOREIGN KEY(sale_id) REFERENCES sales(id),
                    FOREIGN KEY(product_id) REFERENCES products(id)
                )
            """);

            // Returns table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS returns (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    sale_id INTEGER,
                    product_id INTEGER,
                    quantity INTEGER,
                    return_type TEXT CHECK(return_type IN ('REFUND','EXCHANGE')),
                    return_date TEXT,
                    refund_amount REAL,
                    status TEXT DEFAULT 'APPROVED',
                    FOREIGN KEY(sale_id) REFERENCES sales(id)
                )
            """);

            // Seed default admin
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO users(name,username,password,role) VALUES('Administrator','admin','admin123','ADMIN')");
                stmt.execute("INSERT INTO users(name,username,password,role) VALUES('John Cashier','cashier','cash123','CASHIER')");
            }

            // Seed sample products
            rs = stmt.executeQuery("SELECT COUNT(*) FROM products");
            if (rs.getInt(1) == 0) {
                String[] products = {
                    "('1001','Apple Juice 1L','Beverages',45.00,100,0.05)",
                    "('1002','Whole Wheat Bread','Bakery',35.00,80,0.00)",
                    "('1003','Full Cream Milk 500ml','Dairy',28.00,150,0.00)",
                    "('1004','Basmati Rice 1kg','Grains',85.00,200,0.05)",
                    "('1005','Sunflower Oil 1L','Cooking Oil',120.00,60,0.12)",
                    "('1006','Dark Chocolate Bar','Snacks',55.00,90,0.12)",
                    "('1007','Green Tea 25 bags','Beverages',75.00,70,0.12)",
                    "('1008','Potato Chips 100g','Snacks',40.00,120,0.12)",
                    "('1009','Cheddar Cheese 200g','Dairy',95.00,45,0.05)",
                    "('1010','Mineral Water 1L','Beverages',20.00,300,0.00)"
                };
                for (String p : products) {
                    stmt.execute("INSERT INTO products(barcode,name,category,price,stock_qty,tax_rate) VALUES" + p);
                }
            }

            stmt.close();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
