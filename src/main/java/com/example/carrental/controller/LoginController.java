package com.example.carrental.controller;

import com.example.carrental.model.Admin;
import com.example.carrental.model.Customer;
import com.example.carrental.service.AuthenticationService;
import com.example.carrental.service.CustomerService;
import com.example.carrental.exception.ReservationException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

public class LoginController {
    @FXML private TextField customerEmailField;
    @FXML private TextField adminUsernameField;
    @FXML private PasswordField adminPasswordField;
    @FXML private Label messageLabel;

    private AuthenticationService authService;
    private CustomerService customerService;

    public LoginController() {
        this.customerService = new CustomerService();
        this.authService = new AuthenticationService(customerService);
    }

    @FXML
    private void initialize() {
        // إعداد القيم الافتراضية للتسهيل (يمكن إزالتها لاحقاً)
        customerEmailField.setText("rahaf@example.com");
        adminUsernameField.setText("admin");
        adminPasswordField.setText("admin123");
    }

    @FXML
    private void handleCustomerLogin() {
        try {
            String email = customerEmailField.getText().trim();
            if (email.isEmpty()) {
                showError("Input Error", "Please enter your email address");
                return;
            }

            Customer customer = authService.loginCustomer(email);

            // افتح واجهة العميل
            openCustomerDashboard(customer);

        } catch (ReservationException e) {
            showError("Login Error", e.getMessage());
        } catch (Exception e) {
            showError("System Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handleAdminLogin() {
        try {
            String username = adminUsernameField.getText().trim();
            String password = adminPasswordField.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                showError("Input Error", "Please enter both username and password");
                return;
            }

            Admin admin = authService.loginAdmin(username, password);

            // افتح واجهة المدير
            openAdminDashboard(admin);

        } catch (ReservationException e) {
            showError("Login Error", e.getMessage());
        } catch (Exception e) {
            showError("System Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handleCustomerRegistration() {
        try {
            // نافذة تسجيل جديدة
            TextInputDialog emailDialog = new TextInputDialog();
            emailDialog.setTitle("Customer Registration");
            emailDialog.setHeaderText("Register New Customer");
            emailDialog.setContentText("Email:");
            emailDialog.getEditor().setText("newcustomer@example.com");

            String email = emailDialog.showAndWait().orElse("");
            if (email.isEmpty()) return;

            TextInputDialog nameDialog = new TextInputDialog();
            nameDialog.setTitle("Customer Registration");
            nameDialog.setHeaderText("Enter your full name");
            nameDialog.setContentText("Name:");
            nameDialog.getEditor().setText("New Customer");

            String name = nameDialog.showAndWait().orElse("");
            if (name.isEmpty()) return;

            TextInputDialog phoneDialog = new TextInputDialog();
            phoneDialog.setTitle("Customer Registration");
            phoneDialog.setHeaderText("Enter your phone number");
            phoneDialog.setContentText("Phone:");
            phoneDialog.getEditor().setText("555-1234");

            String phone = phoneDialog.showAndWait().orElse("");

            Customer customer = authService.registerCustomer(name, phone, email);
            showSuccess("Registration Successful",
                    "Customer registered successfully!\n\n" +
                            "Name: " + customer.getName() + "\n" +
                            "Email: " + customer.getEmail() + "\n" +
                            "Phone: " + customer.getPhone());

            customerEmailField.setText(email);

        } catch (ReservationException e) {
            showError("Registration Error", e.getMessage());
        } catch (Exception e) {
            showError("System Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    private void openCustomerDashboard(Customer customer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrental/view/customer_dashboard.fxml"));
            Parent root = loader.load();

            CustomerDashboardController controller = loader.getController();
            controller.setCustomer(customer);

            Stage stage = (Stage) customerEmailField.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle("Car Rental System - Customer Dashboard - " + customer.getName());
            stage.setMaximized(true);




        } catch (Exception e) {
            showError("Navigation Error", "Cannot open customer dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openAdminDashboard(Admin admin) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrental/view/admin_dashboard.fxml"));
            Parent root = loader.load();

            // لا نحتاج لتمرير الـ Admin لأن الـ Controller سيحصل على البيانات من قاعدة البيانات
            Stage stage = (Stage) adminUsernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("Car Rental System - Admin Dashboard - " + admin.getName());
            stage.setMaximized(true);


        } catch (Exception e) {
            showError("Navigation Error", "Cannot open admin dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}