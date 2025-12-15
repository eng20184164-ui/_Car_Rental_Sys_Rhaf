package com.example.carrental.dao;

import com.example.carrental.database.DatabaseConnection;
import com.example.carrental.model.Car;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CarDAO {

    public List<Car> getAllCars() {
        List<Car> cars = new ArrayList<>();
        String sql = "SELECT * FROM cars ORDER BY car_id";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                cars.add(extractCarFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting all cars: " + e.getMessage());
        }
        return cars;
    }

    public List<Car> getAvailableCars() {
        List<Car> cars = new ArrayList<>();
        String sql = "SELECT * FROM cars WHERE available = 1 ORDER BY car_id";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                cars.add(extractCarFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting available cars: " + e.getMessage());
        }
        return cars;
    }

    public Car getCarById(int carId) {
        String sql = "SELECT * FROM cars WHERE car_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, carId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractCarFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting car by ID: " + e.getMessage());
        }
        return null;
    }

    public boolean addCar(Car car) {
        String sql = "INSERT INTO cars (brand, model, year, color, price_per_day, available) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, car.getBrand());
            pstmt.setString(2, car.getModel());
            pstmt.setInt(3, car.getYear());
            pstmt.setString(4, car.getColor());
            pstmt.setDouble(5, car.getPricePerDay());
            pstmt.setBoolean(6, car.isAvailable());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("❌ Error adding car: " + e.getMessage());
            return false;
        }
    }

    public boolean updateCar(Car car) {
        String sql = "UPDATE cars SET brand = ?, model = ?, year = ?, color = ?, price_per_day = ?, available = ? WHERE car_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, car.getBrand());
            pstmt.setString(2, car.getModel());
            pstmt.setInt(3, car.getYear());
            pstmt.setString(4, car.getColor());
            pstmt.setDouble(5, car.getPricePerDay());
            pstmt.setBoolean(6, car.isAvailable());
            pstmt.setInt(7, car.getCarId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("❌ Error updating car: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteCar(int carId) {
        String sql = "DELETE FROM cars WHERE car_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, carId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("❌ Error deleting car: " + e.getMessage());
            return false;
        }
    }

    public boolean updateCarAvailability(int carId, boolean available) {
        String sql = "UPDATE cars SET available = ? WHERE car_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, available);
            pstmt.setInt(2, carId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("❌ Error updating car availability: " + e.getMessage());
            return false;
        }
    }

    public List<Car> searchCars(String brand, String model, Double maxPrice) {
        List<Car> cars = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM cars WHERE available = 1");
        List<Object> parameters = new ArrayList<>();

        if (brand != null && !brand.isEmpty()) {
            sql.append(" AND LOWER(brand) LIKE ?");
            parameters.add("%" + brand.toLowerCase() + "%");
        }

        if (model != null && !model.isEmpty()) {
            sql.append(" AND LOWER(model) LIKE ?");
            parameters.add("%" + model.toLowerCase() + "%");
        }

        if (maxPrice != null) {
            sql.append(" AND price_per_day <= ?");
            parameters.add(maxPrice);
        }

        sql.append(" ORDER BY car_id");

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                pstmt.setObject(i + 1, parameters.get(i));
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                cars.add(extractCarFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error searching cars: " + e.getMessage());
        }
        return cars;
    }

    private Car extractCarFromResultSet(ResultSet rs) throws SQLException {
        return new Car(
                rs.getInt("car_id"),
                rs.getString("brand"),
                rs.getString("model"),
                rs.getInt("year"),
                rs.getString("color"),
                rs.getDouble("price_per_day"),
                rs.getBoolean("available")
        );
    }
}