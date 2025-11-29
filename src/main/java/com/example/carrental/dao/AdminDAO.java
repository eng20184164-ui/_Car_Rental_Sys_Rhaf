package com.example.carrental.dao;

import com.example.carrental.database.DatabaseConnection;
import com.example.carrental.model.Admin;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminDAO {

    public List<Admin> getAllAdmins() {
        List<Admin> admins = new ArrayList<>();
        String sql = "SELECT * FROM admins ORDER BY admin_id";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                admins.add(extractAdminFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting all admins: " + e.getMessage());
        }
        return admins;
    }

    public Admin getAdminById(int adminId) {
        String sql = "SELECT * FROM admins WHERE admin_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, adminId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractAdminFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting admin by ID: " + e.getMessage());
        }
        return null;
    }

    public Admin getAdminByUsername(String username) {
        String sql = "SELECT * FROM admins WHERE username = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractAdminFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting admin by username: " + e.getMessage());
        }
        return null;
    }

    public boolean validateAdminCredentials(String username, String password) {
        String sql = "SELECT COUNT(*) FROM admins WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;

        } catch (SQLException e) {
            System.err.println("❌ Error validating admin credentials: " + e.getMessage());
            return false;
        }
    }

    public boolean addAdmin(Admin admin) {
        String sql = "INSERT INTO admins (name, phone, username, password) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, admin.getName());
            pstmt.setString(2, admin.getPhone());
            pstmt.setString(3, admin.getUsername());
            pstmt.setString(4, admin.getPassword());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("❌ Error adding admin: " + e.getMessage());
            return false;
        }
    }

    public boolean updateAdmin(Admin admin) {
        String sql = "UPDATE admins SET name = ?, phone = ?, username = ?, password = ? WHERE admin_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, admin.getName());
            pstmt.setString(2, admin.getPhone());
            pstmt.setString(3, admin.getUsername());
            pstmt.setString(4, admin.getPassword());
            pstmt.setInt(5, admin.getAdminId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("❌ Error updating admin: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteAdmin(int adminId) {
        String sql = "DELETE FROM admins WHERE admin_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, adminId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("❌ Error deleting admin: " + e.getMessage());
            return false;
        }
    }

    public boolean adminExists(String username) {
        String sql = "SELECT COUNT(*) FROM admins WHERE username = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            return rs.next() && rs.getInt(1) > 0;

        } catch (SQLException e) {
            System.err.println("❌ Error checking admin existence: " + e.getMessage());
            return false;
        }
    }

    private Admin extractAdminFromResultSet(ResultSet rs) throws SQLException {
        return new Admin(
                rs.getInt("admin_id"),
                rs.getString("name"),
                rs.getString("phone"),
                rs.getString("username"),
                rs.getString("password")
        );
    }
}