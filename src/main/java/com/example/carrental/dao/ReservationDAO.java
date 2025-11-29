package com.example.carrental.dao;

import com.example.carrental.database.DatabaseConnection;
import com.example.carrental.model.Customer;
import com.example.carrental.model.Car;
import com.example.carrental.model.Reservation;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {
    private CarDAO carDAO;
    private CustomerDAO customerDAO;

    public ReservationDAO() {
        this.carDAO = new CarDAO();
        this.customerDAO = new CustomerDAO();
    }

    public List<Reservation> getAllReservations() {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT r.*, c.name as customer_name, c.phone as customer_phone, c.email as customer_email, " +
                "car.brand, car.model, car.year, car.color, car.price_per_day, car.available " +
                "FROM reservations r " +
                "JOIN customers c ON r.customer_id = c.customer_id " +
                "JOIN cars car ON r.car_id = car.car_id " +
                "ORDER BY r.reservation_id DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                reservations.add(extractReservationFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting all reservations: " + e.getMessage());
        }
        return reservations;
    }

    public List<Reservation> getReservationsByCustomerId(int customerId) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT r.*, c.name as customer_name, c.phone as customer_phone, c.email as customer_email, " +
                "car.brand, car.model, car.year, car.color, car.price_per_day, car.available " +
                "FROM reservations r " +
                "JOIN customers c ON r.customer_id = c.customer_id " +
                "JOIN cars car ON r.car_id = car.car_id " +
                "WHERE r.customer_id = ? " +
                "ORDER BY r.reservation_id DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                reservations.add(extractReservationFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting customer reservations: " + e.getMessage());
        }
        return reservations;
    }

    public Reservation getReservationById(int reservationId) {
        String sql = "SELECT r.*, c.name as customer_name, c.phone as customer_phone, c.email as customer_email, " +
                "car.brand, car.model, car.year, car.color, car.price_per_day, car.available " +
                "FROM reservations r " +
                "JOIN customers c ON r.customer_id = c.customer_id " +
                "JOIN cars car ON r.car_id = car.car_id " +
                "WHERE r.reservation_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, reservationId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractReservationFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting reservation by ID: " + e.getMessage());
        }
        return null;
    }

    public int addReservation(Reservation reservation) {
        String sql = "INSERT INTO reservations (customer_id, car_id, start_date, end_date, total_price, status) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, reservation.getCustomer().getCustomerId());
            pstmt.setInt(2, reservation.getCar().getCarId());
            pstmt.setDate(3, Date.valueOf(reservation.getStartDate()));
            pstmt.setDate(4, Date.valueOf(reservation.getEndDate()));
            pstmt.setDouble(5, reservation.getTotalPrice());
            pstmt.setString(6, reservation.getStatus());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
            return -1;

        } catch (SQLException e) {
            System.err.println("❌ Error adding reservation: " + e.getMessage());
            return -1;
        }
    }

    public boolean updateReservation(Reservation reservation) {
        String sql = "UPDATE reservations SET customer_id = ?, car_id = ?, start_date = ?, end_date = ?, total_price = ?, status = ? WHERE reservation_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, reservation.getCustomer().getCustomerId());
            pstmt.setInt(2, reservation.getCar().getCarId());
            pstmt.setDate(3, Date.valueOf(reservation.getStartDate()));
            pstmt.setDate(4, Date.valueOf(reservation.getEndDate()));
            pstmt.setDouble(5, reservation.getTotalPrice());
            pstmt.setString(6, reservation.getStatus());
            pstmt.setInt(7, reservation.getReservationId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("❌ Error updating reservation: " + e.getMessage());
            return false;
        }
    }

    public boolean cancelReservation(int reservationId) {
        String sql = "UPDATE reservations SET status = 'Cancelled' WHERE reservation_id = ? AND status = 'Confirmed'";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, reservationId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("❌ Error cancelling reservation: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteReservation(int reservationId) {
        String sql = "DELETE FROM reservations WHERE reservation_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, reservationId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("❌ Error deleting reservation: " + e.getMessage());
            return false;
        }
    }

    public boolean isCarAvailableForDates(int carId, LocalDate startDate, LocalDate endDate, int excludeReservationId) {
        String sql = "SELECT COUNT(*) FROM reservations " +
                "WHERE car_id = ? AND status = 'Confirmed' " +
                "AND ((start_date BETWEEN ? AND ?) OR (end_date BETWEEN ? AND ?) " +
                "OR (? BETWEEN start_date AND end_date) OR (? BETWEEN start_date AND end_date))";

        if (excludeReservationId > 0) {
            sql += " AND reservation_id != ?";
        }

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, carId);
            pstmt.setDate(2, Date.valueOf(startDate));
            pstmt.setDate(3, Date.valueOf(endDate));
            pstmt.setDate(4, Date.valueOf(startDate));
            pstmt.setDate(5, Date.valueOf(endDate));
            pstmt.setDate(6, Date.valueOf(startDate));
            pstmt.setDate(7, Date.valueOf(endDate));

            if (excludeReservationId > 0) {
                pstmt.setInt(8, excludeReservationId);
            }

            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) == 0;

        } catch (SQLException e) {
            System.err.println("❌ Error checking car availability: " + e.getMessage());
            return false;
        }
    }

    public int getReservationCount() {
        String sql = "SELECT COUNT(*) FROM reservations";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            System.err.println("❌ Error getting reservation count: " + e.getMessage());
            return 0;
        }
    }

    private Reservation extractReservationFromResultSet(ResultSet rs) throws SQLException {
        // إنشاء كائن Customer
        Customer customer = new Customer(
                rs.getInt("customer_id"),
                rs.getString("customer_name"),
                rs.getString("customer_phone"),
                rs.getString("customer_email")
        );

        // إنشاء كائن Car
        Car car = new Car(
                rs.getInt("car_id"),
                rs.getString("brand"),
                rs.getString("model"),
                rs.getInt("year"),
                rs.getString("color"),
                rs.getDouble("price_per_day"),
                rs.getBoolean("available")
        );

        // إنشاء كائن Reservation
        Reservation reservation = new Reservation(
                rs.getInt("reservation_id"),
                customer,
                car,
                rs.getDate("start_date").toLocalDate(),
                rs.getDate("end_date").toLocalDate()
        );

        reservation.setTotalPrice(rs.getDouble("total_price"));
        reservation.setStatus(rs.getString("status"));

        return reservation;
    }
}