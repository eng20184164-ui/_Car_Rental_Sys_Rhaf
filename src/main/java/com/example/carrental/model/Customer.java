package com.example.carrental.model;

public class Customer extends Person {
    private int customerId;
    private String email;

    public Customer(int customerId, String name, String phone, String email) {
        super(name, phone);
        this.customerId = customerId;
        this.email = email;
    }

    // Getters and Setters
    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getRole() {
        return "Customer";
    }

    @Override
    public String toString() {
        return getName() + " (" + email + ")";
    }
}