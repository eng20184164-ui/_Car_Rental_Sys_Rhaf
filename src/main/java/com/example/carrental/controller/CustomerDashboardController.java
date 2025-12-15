package com.example.carrental.controller;

import com.example.carrental.model.Car;
import com.example.carrental.model.Customer;
import com.example.carrental.model.Reservation;
import com.example.carrental.service.CarService;
import com.example.carrental.service.ReservationService;
import com.example.carrental.exception.ReservationException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.time.LocalDate;
import java.util.List;

public class CustomerDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label statusLabel;

    @FXML private TextField searchBrandField;
    @FXML private TextField searchModelField;
    @FXML private TextField searchMaxPriceField;

    @FXML private TableView<Car> carsTableView;
    @FXML private TableView<Reservation> reservationsTableView;

    private Customer customer;
    private CarService carService;
    private ReservationService reservationService;
    private ReservationController reservationController;
    private ObservableList<Car> carsList;
    private ObservableList<Reservation> reservationsList;

    public void setCustomer(Customer customer) {
        this.customer = customer;
        this.carService = new CarService();
        this.reservationService = new ReservationService();
        this.reservationController = new ReservationController();
        this.carsList = FXCollections.observableArrayList();
        this.reservationsList = FXCollections.observableArrayList();

        initializeDashboard();
    }

    private void initializeDashboard() {
        welcomeLabel.setText("Welcome, " + customer.getName() + "!");

        // إعداد أزرار الإجراءات للجداول
        setupTableActionButtons();

        loadAvailableCars();
        loadCustomerReservations();

        statusLabel.setText("Dashboard loaded successfully");
    }

    private void setupTableActionButtons() {
        // إضافة عمود الإجراءات لجدول السيارات برمجياً
        TableColumn<Car, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(120);

        actionCol.setCellFactory(param -> new TableCell<Car, Void>() {
            private final Button rentButton = new Button("🚀 Rent");

            {
                rentButton.getStyleClass().add("success-button");
                rentButton.setStyle("-fx-pref-width: 100px;");
                rentButton.setOnAction(event -> {
                    Car car = getTableView().getItems().get(getIndex());
                    if (car != null) {
                        carsTableView.getSelectionModel().select(car);
                        handleRentCar();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(rentButton);
                }
            }
        });

        carsTableView.getColumns().add(actionCol);

        // إضافة عمود الإجراءات لجدول الحجوزات برمجياً
        TableColumn<Reservation, Void> reservationActionCol = new TableColumn<>("Actions");
        reservationActionCol.setPrefWidth(120);

        reservationActionCol.setCellFactory(param -> new TableCell<Reservation, Void>() {
            private final Button cancelButton = new Button("❌ Cancel");

            {
                cancelButton.getStyleClass().add("danger-button");
                cancelButton.setStyle("-fx-pref-width: 100px;");
                cancelButton.setOnAction(event -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    if (reservation != null) {
                        reservationsTableView.getSelectionModel().select(reservation);
                        handleCancelReservation();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    // إظهار زر الإلغاء فقط للحجوزات المؤكدة
                    if (getIndex() < getTableView().getItems().size()) {
                        Reservation reservation = getTableView().getItems().get(getIndex());
                        if (reservation != null && "Confirmed".equals(reservation.getStatus())) {
                            setGraphic(cancelButton);
                        } else {
                            setGraphic(null);
                        }
                    }
                }
            }
        });

        reservationsTableView.getColumns().add(reservationActionCol);
    }

    @FXML
    private void handleSearchCars() {
        try {
            String brand = searchBrandField.getText().trim();
            String model = searchModelField.getText().trim();
            Double maxPrice = null;

            if (!searchMaxPriceField.getText().trim().isEmpty()) {
                try {
                    maxPrice = Double.parseDouble(searchMaxPriceField.getText().trim());
                    if (maxPrice <= 0) {
                        showError("Input Error", "Maximum price must be greater than 0");
                        return;
                    }
                } catch (NumberFormatException e) {
                    showError("Input Error", "Please enter a valid number for maximum price");
                    return;
                }
            }

            List<Car> searchResults = carService.searchCars(brand, model, maxPrice);
            carsList.setAll(searchResults);
            statusLabel.setText("Found " + searchResults.size() + " cars matching your criteria");

        } catch (Exception e) {
            showError("Search Error", "An error occurred while searching: " + e.getMessage());
        }
    }

    @FXML
    private void handleShowAllCars() {
        try {
            loadAvailableCars();
            searchBrandField.clear();
            searchModelField.clear();
            searchMaxPriceField.clear();
            statusLabel.setText("Showing all available cars");
        } catch (Exception e) {
            showError("Loading Error", "Failed to load cars: " + e.getMessage());
        }
    }

    @FXML
    private void handleRentCar() {
        Car selectedCar = carsTableView.getSelectionModel().getSelectedItem();
        if (selectedCar == null) {
            showError("Selection Error", "Please select a car to rent");
            return;
        }

        if (!selectedCar.isAvailable()) {
            showError("Not Available", "This car is not available for rent");
            return;
        }

        // استخدام ReservationController لعرض نافذة الحجز
        reservationController.showReservationDialog(customer, selectedCar, () -> {
            // Callback يتم استدعاؤه بعد نجاح الحجز
            loadAvailableCars();
            loadCustomerReservations();
            showSuccess("Reservation Successful",
                    "Your reservation has been confirmed!\n\n" +
                            "Car: " + selectedCar.getBrand() + " " + selectedCar.getModel() + "\n" +
                            "Period: " + reservationController.getLastStartDate() + " to " + reservationController.getLastEndDate() + "\n" +
                            "Total: $" + reservationController.getLastTotalPrice());
        });
    }

    @FXML
    private void handleCancelReservation() {
        Reservation selectedReservation = reservationsTableView.getSelectionModel().getSelectedItem();
        if (selectedReservation == null) {
            showError("Selection Error", "Please select a reservation to cancel");
            return;
        }

        if (!"Confirmed".equals(selectedReservation.getStatus())) {
            showError("Cancellation Error", "Only confirmed reservations can be cancelled");
            return;
        }

        // استخدام ReservationController لعرض نافذة الإلغاء
        reservationController.showCancellationDialog(selectedReservation, () -> {
            // Callback يتم استدعاؤه بعد نجاح الإلغاء
            loadAvailableCars();
            loadCustomerReservations();
        });
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrental/view/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("Car Rental System - Login");

        } catch (Exception e) {
            showError("Navigation Error", "Cannot return to login screen: " + e.getMessage());
        }
    }

    private void loadAvailableCars() {
        try {
            List<Car> availableCars = carService.getAvailableCars();
            carsList.setAll(availableCars);
            carsTableView.setItems(carsList);
            statusLabel.setText("Loaded " + availableCars.size() + " available cars");
        } catch (Exception e) {
            showError("Loading Error", "Failed to load available cars: " + e.getMessage());
        }
    }

    private void loadCustomerReservations() {
        try {
            List<Reservation> reservations = reservationService.getCustomerReservations(customer);
            reservationsList.setAll(reservations);
            reservationsTableView.setItems(reservationsList);
        } catch (Exception e) {
            showError("Loading Error", "Failed to load reservations: " + e.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        statusLabel.setText("Error: " + title);
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        statusLabel.setText("Success: " + title);
    }
}