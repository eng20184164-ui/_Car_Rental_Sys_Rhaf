package com.example.carrental.service;

import com.example.carrental.dao.AdminDAO;
import com.example.carrental.model.Admin;
import com.example.carrental.model.Customer;
import com.example.carrental.exception.ReservationException;

public class AuthenticationService {
    private AdminDAO adminDAO;
    private CustomerService customerService;

    public AuthenticationService(CustomerService customerService) {
        this.adminDAO = new AdminDAO();
        this.customerService = customerService;
        // تم إزالة initializeSampleAdmins() لأن البيانات الآن تأتي من قاعدة البيانات
    }

    public Customer loginCustomer(String email) throws ReservationException {
        if (email == null || email.trim().isEmpty()) {
            throw new ReservationException("Email is required");
        }

        Customer customer = customerService.findCustomerByEmail(email);
        if (customer == null) {
            throw new ReservationException("Customer not found with email: " + email);
        }

        return customer;
    }

    public Admin loginAdmin(String username, String password) throws ReservationException {
        if (username == null || username.trim().isEmpty()) {
            throw new ReservationException("Username is required");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new ReservationException("Password is required");
        }

        // التحقق من بيانات الاعتماد
        boolean isValid = adminDAO.validateAdminCredentials(username, password);
        if (!isValid) {
            throw new ReservationException("Invalid username or password");
        }

        // الحصول على بيانات المدير
        Admin admin = adminDAO.getAdminByUsername(username);
        if (admin == null) {
            throw new ReservationException("Failed to retrieve admin information");
        }

        return admin;
    }

    public Customer registerCustomer(String name, String phone, String email) throws ReservationException {
        return customerService.registerCustomer(name, phone, email);
    }

    public boolean adminExists(String username) {
        return adminDAO.adminExists(username);
    }
}