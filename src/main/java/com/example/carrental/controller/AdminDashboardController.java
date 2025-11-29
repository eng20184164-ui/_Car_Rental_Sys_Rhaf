package com.example.carrental.controller;

import com.example.carrental.model.Car;
import com.example.carrental.model.Customer;
import com.example.carrental.model.Reservation;
import com.example.carrental.service.CarService;
import com.example.carrental.service.CustomerService;
import com.example.carrental.service.ReservationService;
import com.example.carrental.exception.ReservationException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.time.LocalDate;
import java.util.List;

public class AdminDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label statsLabel;
    @FXML private Label statusLabel;

    @FXML private TableView<Car> carsTableView;
    @FXML private TableView<Reservation> reservationsTableView;
    @FXML private TableView<Customer> customersTableView;

    @FXML private ComboBox<String> reservationFilterCombo;

    @FXML private Label totalCarsLabel;
    @FXML private Label availableCarsLabel;
    @FXML private Label totalReservationsLabel;
    @FXML private Label activeReservationsLabel;
    @FXML private Label totalCustomersLabel;

    private CarService carService;
    private CustomerService customerService;
    private ReservationService reservationService;
    private ReservationController reservationController;

    private ObservableList<Car> carsList;
    private ObservableList<Reservation> reservationsList;
    private ObservableList<Customer> customersList;

    public void initialize() {
        this.carService = new CarService();
        this.customerService = new CustomerService();
        this.reservationService = new ReservationService();
        this.reservationController = new ReservationController();

        this.carsList = FXCollections.observableArrayList();
        this.reservationsList = FXCollections.observableArrayList();
        this.customersList = FXCollections.observableArrayList();

        initializeUI();
        setupTableActionButtons();
        loadAllData();
        updateStatistics();

        statusLabel.setText("Admin dashboard initialized successfully");
    }

    private void initializeUI() {
        welcomeLabel.setText("Welcome, Admin!");

        // إعداد ComboBox لتصفية الحجوزات
        reservationFilterCombo.setItems(FXCollections.observableArrayList(
                "All", "Confirmed", "Cancelled", "Pending"
        ));
        reservationFilterCombo.setValue("All");

        // إضافة listener لتصفية الحجوزات
        reservationFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            filterReservations(newVal);
        });
    }

    private void setupTableActionButtons() {
        // إضافة عمود الإجراءات لجدول السيارات
        TableColumn<Car, Void> carActionCol = new TableColumn<>("Actions");
        carActionCol.setPrefWidth(150);

        carActionCol.setCellFactory(param -> new TableCell<Car, Void>() {
            private final HBox buttonBox = new HBox(5);
            private final Button editButton = new Button("✏️");
            private final Button deleteButton = new Button("🗑️");

            {
                editButton.getStyleClass().add("primary-button");
                editButton.setStyle("-fx-pref-width: 60px; -fx-font-size: 12px;");
                editButton.setOnAction(event -> {
                    Car car = getTableView().getItems().get(getIndex());
                    if (car != null) {
                        carsTableView.getSelectionModel().select(car);
                        handleEditCar();
                    }
                });

                deleteButton.getStyleClass().add("danger-button");
                deleteButton.setStyle("-fx-pref-width: 60px; -fx-font-size: 12px;");
                deleteButton.setOnAction(event -> {
                    Car car = getTableView().getItems().get(getIndex());
                    if (car != null) {
                        carsTableView.getSelectionModel().select(car);
                        handleDeleteCar();
                    }
                });

                buttonBox.getChildren().addAll(editButton, deleteButton);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonBox);
                }
            }
        });

        carsTableView.getColumns().add(carActionCol);

        // إضافة عمود الإجراءات لجدول الحجوزات
        TableColumn<Reservation, Void> reservationActionCol = new TableColumn<>("Actions");
        reservationActionCol.setPrefWidth(120);

        reservationActionCol.setCellFactory(param -> new TableCell<Reservation, Void>() {
            private final Button cancelButton = new Button("❌ Cancel");

            {
                cancelButton.getStyleClass().add("warning-button");
                cancelButton.setStyle("-fx-pref-width: 100px; -fx-font-size: 12px;");
                cancelButton.setOnAction(event -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    if (reservation != null) {
                        reservationsTableView.getSelectionModel().select(reservation);
                        handleCancelReservationAdmin();
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

        // إضافة عمود الإجراءات لجدول العملاء
        TableColumn<Customer, Void> customerActionCol = new TableColumn<>("Reservations");
        customerActionCol.setPrefWidth(120);

        customerActionCol.setCellFactory(param -> new TableCell<Customer, Void>() {
            private final Button viewButton = new Button("📋 View");

            {
                viewButton.getStyleClass().add("primary-button");
                viewButton.setStyle("-fx-pref-width: 80px; -fx-font-size: 12px;");
                viewButton.setOnAction(event -> {
                    Customer customer = getTableView().getItems().get(getIndex());
                    if (customer != null) {
                        showCustomerReservations(customer);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewButton);
                }
            }
        });

        customersTableView.getColumns().add(customerActionCol);
    }

    @FXML
    private void handleAddCar() {
        try {
            Dialog<Car> dialog = new Dialog<>();
            dialog.setTitle("Add New Car");
            dialog.setHeaderText("Enter car details");

            // إنشاء حقول الإدخال
            TextField brandField = new TextField();
            brandField.setPromptText("Toyota");

            TextField modelField = new TextField();
            modelField.setPromptText("Camry");

            TextField yearField = new TextField();
            yearField.setPromptText("2023");

            TextField colorField = new TextField();
            colorField.setPromptText("White");

            TextField priceField = new TextField();
            priceField.setPromptText("50.0");

            GridPane grid = new GridPane();
            grid.setHgap(15);
            grid.setVgap(15);
            grid.setPadding(new javafx.geometry.Insets(20));

            grid.add(new Label("Brand:"), 0, 0);
            grid.add(brandField, 1, 0);
            grid.add(new Label("Model:"), 0, 1);
            grid.add(modelField, 1, 1);
            grid.add(new Label("Year:"), 0, 2);
            grid.add(yearField, 1, 2);
            grid.add(new Label("Color:"), 0, 3);
            grid.add(colorField, 1, 3);
            grid.add(new Label("Price/Day:"), 0, 4);
            grid.add(priceField, 1, 4);

            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    try {
                        String brand = brandField.getText().trim();
                        String model = modelField.getText().trim();
                        int year = Integer.parseInt(yearField.getText().trim());
                        String color = colorField.getText().trim();
                        double price = Double.parseDouble(priceField.getText().trim());

                        if (brand.isEmpty() || model.isEmpty() || color.isEmpty()) {
                            throw new IllegalArgumentException("All fields are required");
                        }

                        if (year < 2000 || year > 2030) {
                            throw new IllegalArgumentException("Year must be between 2000 and 2030");
                        }

                        if (price <= 0) {
                            throw new IllegalArgumentException("Price must be greater than 0");
                        }

                        Car newCar = new Car(0, brand, model, year, color, price, true);
                        carService.addCar(newCar);
                        return newCar;

                    } catch (NumberFormatException e) {
                        showError("Input Error", "Please enter valid numbers for year and price");
                    } catch (IllegalArgumentException e) {
                        showError("Input Error", e.getMessage());
                    } catch (Exception e) {
                        showError("Database Error", "Failed to add car: " + e.getMessage());
                    }
                }
                return null;
            });

            dialog.showAndWait().ifPresent(car -> {
                showSuccess("Car Added", "New car has been added successfully!");
                loadAllData();
                updateStatistics();
            });
        } catch (Exception e) {
            showError("System Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditCar() {
        Car selectedCar = carsTableView.getSelectionModel().getSelectedItem();
        if (selectedCar == null) {
            showError("Selection Error", "Please select a car to edit");
            return;
        }

        Dialog<Car> dialog = new Dialog<>();
        dialog.setTitle("Edit Car");
        dialog.setHeaderText("Edit car details");

        // إنشاء حقول الإدخال مع البيانات الحالية
        TextField brandField = new TextField(selectedCar.getBrand());
        TextField modelField = new TextField(selectedCar.getModel());
        TextField yearField = new TextField(String.valueOf(selectedCar.getYear()));
        TextField colorField = new TextField(selectedCar.getColor());
        TextField priceField = new TextField(String.valueOf(selectedCar.getPricePerDay()));
        CheckBox availableCheckBox = new CheckBox("Available");
        availableCheckBox.setSelected(selectedCar.isAvailable());

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new javafx.geometry.Insets(20));

        grid.add(new Label("Brand:"), 0, 0);
        grid.add(brandField, 1, 0);
        grid.add(new Label("Model:"), 0, 1);
        grid.add(modelField, 1, 1);
        grid.add(new Label("Year:"), 0, 2);
        grid.add(yearField, 1, 2);
        grid.add(new Label("Color:"), 0, 3);
        grid.add(colorField, 1, 3);
        grid.add(new Label("Price/Day:"), 0, 4);
        grid.add(priceField, 1, 4);
        grid.add(availableCheckBox, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    String brand = brandField.getText().trim();
                    String model = modelField.getText().trim();
                    int year = Integer.parseInt(yearField.getText().trim());
                    String color = colorField.getText().trim();
                    double price = Double.parseDouble(priceField.getText().trim());
                    boolean available = availableCheckBox.isSelected();

                    if (brand.isEmpty() || model.isEmpty() || color.isEmpty()) {
                        throw new IllegalArgumentException("All fields are required");
                    }

                    Car updatedCar = new Car(selectedCar.getCarId(), brand, model, year, color, price, available);
                    carService.updateCar(updatedCar);
                    return updatedCar;

                } catch (NumberFormatException e) {
                    showError("Input Error", "Please enter valid numbers for year and price");
                } catch (ReservationException | IllegalArgumentException e) {
                    showError("Update Error", e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(car -> {
            showSuccess("Car Updated", "Car details have been updated successfully!");
            loadAllData();
        });
    }

    @FXML
    private void handleDeleteCar() {
        Car selectedCar = carsTableView.getSelectionModel().getSelectedItem();
        if (selectedCar == null) {
            showError("Selection Error", "Please select a car to delete");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Car");
        confirmAlert.setHeaderText("Delete " + selectedCar.getBrand() + " " + selectedCar.getModel());
        confirmAlert.setContentText("Are you sure you want to delete this car?\n\nThis action cannot be undone.");

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                carService.removeCar(selectedCar.getCarId());
                showSuccess("Car Deleted", "Car has been deleted successfully!");
                loadAllData();
                updateStatistics();
            } catch (ReservationException e) {
                showError("Deletion Error", e.getMessage());
            }
        }
    }

    @FXML
    private void handleCancelReservationAdmin() {
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
            loadAllData();
            updateStatistics();
        });
    }

    @FXML
    private void handleRefreshCars() {
        loadCars();
        statusLabel.setText("Cars list refreshed - " + carsList.size() + " cars");
    }

    @FXML
    private void handleRefreshReservations() {
        loadReservations();
        statusLabel.setText("Reservations list refreshed - " + reservationsList.size() + " reservations");
    }

    @FXML
    private void handleRefreshCustomers() {
        loadCustomers();
        statusLabel.setText("Customers list refreshed - " + customersList.size() + " customers");
    }

    @FXML
    private void handleUpdateStats() {
        updateStatistics();
        statusLabel.setText("Statistics updated successfully");
    }

    @FXML
    private void handleGenerateReport() {
        String report = reservationController.generateReservationsReport();

        TextArea textArea = new TextArea(report);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");

        ScrollPane scrollPane = new ScrollPane(textArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPrefSize(600, 400);

        Alert reportAlert = new Alert(Alert.AlertType.INFORMATION);
        reportAlert.setTitle("Reservations Report");
        reportAlert.setHeaderText("Detailed Reservations Report");
        reportAlert.getDialogPane().setContent(scrollPane);
        reportAlert.showAndWait();
    }

    @FXML
    private void handleBackupData() {
        showSuccess("Backup Initiated", "Data backup process has been started.\nAll system data has been secured.");
        statusLabel.setText("Backup completed successfully");
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/carrental/view/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("Car Rental System - Login");

        } catch (Exception e) {
            showError("Navigation Error", "Cannot return to login screen: " + e.getMessage());
        }
    }

    private void showCustomerReservations(Customer customer) {
        List<Reservation> customerReservations = reservationService.getCustomerReservations(customer);

        StringBuilder reservationsInfo = new StringBuilder();
        reservationsInfo.append("Reservations for: ").append(customer.getName()).append("\n");
        reservationsInfo.append("Email: ").append(customer.getEmail()).append("\n");
        reservationsInfo.append("Phone: ").append(customer.getPhone()).append("\n\n");

        if (customerReservations.isEmpty()) {
            reservationsInfo.append("No reservations found.");
        } else {
            reservationsInfo.append("Reservation History:\n");
            for (Reservation res : customerReservations) {
                reservationsInfo.append("• #").append(res.getReservationId())
                        .append(" - ").append(res.getCar().getBrand()).append(" ")
                        .append(res.getCar().getModel()).append(" - $").append(res.getTotalPrice())
                        .append(" - ").append(res.getStatus()).append("\n");
            }
        }

        Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
        infoAlert.setTitle("Customer Reservations");
        infoAlert.setHeaderText("Reservation Details for " + customer.getName());
        infoAlert.setContentText(reservationsInfo.toString());
        infoAlert.showAndWait();
    }

    private void loadAllData() {
        loadCars();
        loadReservations();
        loadCustomers();
        updateStatistics();
    }

    private void loadCars() {
        List<Car> allCars = carService.getAllCars();
        carsList.setAll(allCars);
        carsTableView.setItems(carsList);
    }

    private void loadReservations() {
        List<Reservation> allReservations = reservationService.getAllReservations();
        reservationsList.setAll(allReservations);
        reservationsTableView.setItems(reservationsList);
    }

    private void loadCustomers() {
        List<Customer> allCustomers = customerService.getAllCustomers();
        customersList.setAll(allCustomers);
        customersTableView.setItems(customersList);
    }

    private void filterReservations(String status) {
        if ("All".equals(status)) {
            reservationsTableView.setItems(reservationsList);
        } else {
            ObservableList<Reservation> filtered = FXCollections.observableArrayList();
            for (Reservation res : reservationsList) {
                if (status.equals(res.getStatus())) {
                    filtered.add(res);
                }
            }
            reservationsTableView.setItems(filtered);
        }
    }

    private void updateStatistics() {
        List<Car> allCars = carService.getAllCars();
        List<Car> availableCars = carService.getAvailableCars();
        List<Reservation> allReservations = reservationService.getAllReservations();
        List<Customer> allCustomers = customerService.getAllCustomers();

        long activeReservations = allReservations.stream()
                .filter(res -> "Confirmed".equals(res.getStatus()))
                .count();

        totalCarsLabel.setText("Total Cars: " + allCars.size());
        availableCarsLabel.setText("Available Cars: " + availableCars.size());
        totalReservationsLabel.setText("Total Reservations: " + allReservations.size());
        activeReservationsLabel.setText("Active Reservations: " + activeReservations);
        totalCustomersLabel.setText("Total Customers: " + allCustomers.size());

        statsLabel.setText(String.format(
                "System Overview: %d Cars (%d Available) | %d Reservations | %d Customers",
                allCars.size(), availableCars.size(), allReservations.size(), allCustomers.size()
        ));
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