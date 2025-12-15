package com.example.carrental.dao;

import com.example.carrental.database.DatabaseConnection;
import java.sql.*;

public class StatisticsDAO {

    public void updateStatistic(String key, String value) {
        String sql = "INSERT OR REPLACE INTO statistics_cache (stat_key, stat_value) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, key);
            pstmt.setString(2, value);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("❌ Error updating statistic: " + e.getMessage());
        }
    }

    public String getStatistic(String key) {
        String sql = "SELECT stat_value FROM statistics_cache WHERE stat_key = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, key);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("stat_value");
            }

        } catch (SQLException e) {
            System.err.println("❌ Error getting statistic: " + e.getMessage());
        }

        return null;
    }
}