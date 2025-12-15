package com.example.carrental.model;

public class Admin extends Person {
    private int adminId;
    private String username;
    private String password;

    public Admin(int adminId, String name, String phone, String username, String password) {
        super(name, phone);
        this.adminId = adminId;
        this.username = username;
        this.password = password;
    }

    // Getters and Setters
    public int getAdminId() {
        return adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getRole() {
        return "Admin";
    }

    @Override
    public String toString() {
        return getName() + " (Admin: " + username + ")";
    }
}