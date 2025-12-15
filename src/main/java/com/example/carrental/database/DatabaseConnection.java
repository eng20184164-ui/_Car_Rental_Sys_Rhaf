package com.example.carrental.database;

import java.sql.*;

public class DatabaseConnection {
    private static final String URL = "jdbc:sqlite:car_rental.db";
    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        try {
            connection = DriverManager.getConnection(URL);
            createTables();
            initializeSampleData();
            System.out.println("✅ Database connected successfully!");
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting database connection: " + e.getMessage());
        }
        return connection;
    }

    private void createTables() {
        String[] createTablesSQL = {
                // جدول العملاء
                "CREATE TABLE IF NOT EXISTS customers (" +
                        "customer_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "phone TEXT, " +
                        "email TEXT UNIQUE NOT NULL, " +
                        "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",

                // جدول المديرين
                "CREATE TABLE IF NOT EXISTS admins (" +
                        "admin_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "phone TEXT, " +
                        "username TEXT UNIQUE NOT NULL, " +
                        "password TEXT NOT NULL, " +
                        "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",

                // جدول السيارات
                "CREATE TABLE IF NOT EXISTS cars (" +
                        "car_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "brand TEXT NOT NULL, " +
                        "model TEXT NOT NULL, " +
                        "year INTEGER NOT NULL, " +
                        "color TEXT NOT NULL, " +
                        "price_per_day REAL NOT NULL, " +
                        "available BOOLEAN DEFAULT 1, " +
                        "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",

                // جدول الحجوزات
                "CREATE TABLE IF NOT EXISTS reservations (" +
                        "reservation_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "customer_id INTEGER NOT NULL, " +
                        "car_id INTEGER NOT NULL, " +
                        "start_date DATE NOT NULL, " +
                        "end_date DATE NOT NULL, " +
                        "total_price REAL NOT NULL, " +
                        "status TEXT DEFAULT 'Confirmed', " +
                        "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY (customer_id) REFERENCES customers(customer_id), " +
                        "FOREIGN KEY (car_id) REFERENCES cars(car_id))",

                //جدول الاحصائيات
                "CREATE TABLE IF NOT EXISTS statistics_cache (" +
                        "stat_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "stat_key TEXT UNIQUE NOT NULL, " +
                        "stat_value TEXT NOT NULL, " +
                        "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",


        };

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            for (String sql : createTablesSQL) {
                stmt.execute(sql);
            }
            System.out.println("✅ Database tables created successfully!");
        } catch (SQLException e) {
            System.err.println("❌ Error creating tables: " + e.getMessage());
        }
    }

    private void initializeSampleData() {
        if (!isDataInitialized()) {
            insertSampleData();
        }
    }

    private boolean isDataInitialized() {
        String sql = "SELECT COUNT(*) FROM cars";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    private void insertSampleData() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // إدخال بيانات المديرين
            stmt.execute("INSERT OR IGNORE INTO admins (name, phone, username, password) VALUES " +
                    "('System Admin', '555-0101', 'admin', 'admin123'), " +
                    "('Manager', '555-0102', 'manager', 'manager123')");

            // إدخال بيانات العملاء
            stmt.execute("INSERT OR IGNORE INTO customers (name, phone, email) VALUES " +
                    "('Rahaf Saeid', '123-456-7890', 'rahaf@example.com'), " +
                    "('Ahmed Mohamed', '987-654-3210', 'ahmed@example.com')");

            // إدخال بيانات السيارات
            stmt.execute("INSERT OR IGNORE INTO cars (brand, model, year, color, price_per_day, available) VALUES " +
                    "('Toyota', 'Camry', 2022, 'White', 50.0, 1), " +
                    "('Honda', 'Civic', 2023, 'Black', 45.0, 1), " +
                    "('BMW', 'X5', 2021, 'Blue', 100.0, 1), " +
                    "('Mercedes', 'C-Class', 2023, 'Silver', 120.0, 1), " +
                    "('Hyundai', 'Elantra', 2022, 'Red', 40.0, 1)");

            stmt.execute(
                    "INSERT OR REPLACE INTO statistics_cache (stat_key, stat_value) VALUES "+
                           "('last_dashboard_update', datetime('now'))," +
                    "('total_revenue_trend', 'up')," +
                    "('popular_brand', 'Toyota')"
            );

            System.out.println("✅ Sample data inserted successfully!");
        } catch (SQLException e) {
            System.err.println("❌ Error inserting sample data: " + e.getMessage());
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✅ Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error closing database connection: " + e.getMessage());
        }
    }
}