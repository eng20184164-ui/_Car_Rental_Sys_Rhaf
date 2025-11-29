package com.example.carrental.service;

import com.example.carrental.dao.CustomerDAO;
import com.example.carrental.model.Customer;
import com.example.carrental.exception.ReservationException;
import java.util.List;

public class CustomerService {
    private CustomerDAO customerDAO;

    public CustomerService() {
        this.customerDAO = new CustomerDAO();
        // تم إزالة initializeSampleCustomers() لأن البيانات الآن تأتي من قاعدة البيانات
    }

    public Customer registerCustomer(String name, String phone, String email) throws ReservationException {
        // التحقق من البيانات
        if (name == null || name.trim().isEmpty()) {
            throw new ReservationException("Customer name is required");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new ReservationException("Customer email is required");
        }

        // التحقق من صحة البريد الإلكتروني
        if (!isValidEmail(email)) {
            throw new ReservationException("Please enter a valid email address");
        }

        // التحقق من عدم وجود بريد إلكتروني مكرر
        if (customerDAO.customerExists(email)) {
            throw new ReservationException("Customer with this email already exists");
        }

        Customer customer = new Customer(0, name, phone, email);
        boolean success = customerDAO.addCustomer(customer);

        if (!success) {
            throw new ReservationException("Failed to register customer in database");
        }

        // الحصول على العميل مع ID الذي تم إنشاؤه
        Customer registeredCustomer = customerDAO.getCustomerByEmail(email);
        if (registeredCustomer == null) {
            throw new ReservationException("Failed to retrieve registered customer");
        }

        return registeredCustomer;
    }

    public Customer findCustomerById(int customerId) {
        return customerDAO.getCustomerById(customerId);
    }

    public Customer findCustomerByEmail(String email) {
        return customerDAO.getCustomerByEmail(email);
    }

    public List<Customer> getAllCustomers() {
        return customerDAO.getAllCustomers();
    }

    public boolean updateCustomer(Customer updatedCustomer) throws ReservationException {
        if (updatedCustomer == null) {
            throw new ReservationException("Customer cannot be null");
        }

        Customer existingCustomer = findCustomerById(updatedCustomer.getCustomerId());
        if (existingCustomer == null) {
            throw new ReservationException("Customer not found");
        }

        // التحقق من أن البريد الإلكتروني الجديد غير مكرر (إذا تم تغييره)
        if (!existingCustomer.getEmail().equals(updatedCustomer.getEmail()) &&
                customerDAO.customerExists(updatedCustomer.getEmail())) {
            throw new ReservationException("Another customer with this email already exists");
        }

        return customerDAO.updateCustomer(updatedCustomer);
    }

    public boolean deleteCustomer(int customerId) throws ReservationException {
        Customer customer = findCustomerById(customerId);
        if (customer == null) {
            throw new ReservationException("Customer not found with ID: " + customerId);
        }

        return customerDAO.deleteCustomer(customerId);
    }

    private boolean isValidEmail(String email) {
        if (email == null) return false;
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }
}