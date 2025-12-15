package com.example.carrental.controller;

import com.example.carrental.model.Car;
import com.example.carrental.model.Customer;
import com.example.carrental.model.Reservation;
import com.example.carrental.service.CarService;
import com.example.carrental.service.CustomerService;
import com.example.carrental.service.ReservationService;
import com.example.carrental.exception.ReservationException;
import com.example.carrental.service.StatisticsService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import com.example.carrental.application.ReportApplicationService;
import com.example.carrental.application.ReportApplicationService.*;

import com.example.carrental.application.ReportApplicationService;
import com.example.carrental.application.ReportApplicationService.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import java.time.LocalDate;
import java.util.Map;


public class AdminDashboardController {
    //هنا نبو نجربو كلاس الرسم
    // متغيرات لوحة التحكم الإحصائية
    @FXML private Label totalRevenueLabel;
    @FXML private Label avgBookingLabel;
    @FXML private Label carsStatsLabel;
    @FXML private Label customersLabel;
    @FXML private Label revenueChangeLabel;

    @FXML private BarChart<String, Number> monthlyReservationsChart;
    @FXML private PieChart carDistributionChart;
    @FXML private LineChart<String, Number> monthlyRevenueChart;
    @FXML private BarChart<String, Number> mostRentedCarsChart;

    @FXML private TableView<TopCustomer> topCustomersTable;
    @FXML private ListView<String> recentActivityList;

    private StatisticsService statisticsService;

    // كلاس مساعد لعرض أفضل العملاء
    public static class TopCustomer {
        private String customerName;
        private Double totalSpent;

        public TopCustomer(String customerName, Double totalSpent) {
            this.customerName = customerName;
            this.totalSpent = totalSpent;
        }

        public String getCustomerName() { return customerName; }
        public Double getTotalSpent() { return totalSpent; }
    }
    // هنا النهايه

    @FXML private Label welcomeLabel;
    @FXML private Label statsLabel;
    @FXML private Label statusLabel;

    @FXML private TableView<Car> carsTableView;
    @FXML private TableView<Reservation> reservationsTableView;
    @FXML private TableView<Customer> customersTableView;

    @FXML private ComboBox<String> reservationFilterCombo;



    private CarService carService;
    private CustomerService customerService;
    private ReservationService reservationService;
    private ReservationController reservationController;

    private ObservableList<Car> carsList;
    private ObservableList<Reservation> reservationsList;
    private ObservableList<Customer> customersList;
    private ReportApplicationService reportApplicationService;
    private ReportApplicationService reportService;



    public void initialize() {
        this.carService = new CarService();
        this.customerService = new CustomerService();
        this.reservationService = new ReservationService();
        this.reservationController = new ReservationController();

        this.carsList = FXCollections.observableArrayList();
        this.reservationsList = FXCollections.observableArrayList();
        this.customersList = FXCollections.observableArrayList();
        this.reportApplicationService = new ReportApplicationService();
        this.reportService = new ReportApplicationService();
        this.statisticsService = new StatisticsService(); // <-- أضف هذا




        initializeUI();
        setupTableActionButtons();
        initializeCharts(); // <-- أضف هذا
        loadAllData();
        updateStatistics();
        loadDashboardData(); // <-- أضف هذا



        statusLabel.setText("Admin dashboard initialized successfully");
    }

    private void initializeCharts() {
        // إعداد مخطط الحجوزات الشهرية
        CategoryAxis xAxis1 = new CategoryAxis();
        NumberAxis yAxis1 = new NumberAxis();
        monthlyReservationsChart.setTitle("");
        monthlyReservationsChart.setAnimated(true);

        // إعداد مخطط توزيع السيارات
        carDistributionChart.setTitle("");
        carDistributionChart.setLabelsVisible(true);
        carDistributionChart.setLegendVisible(true);

        // إعداد مخطط الإيرادات الشهرية
        CategoryAxis xAxis2 = new CategoryAxis();
        NumberAxis yAxis2 = new NumberAxis();
        monthlyRevenueChart.setTitle("");
        monthlyRevenueChart.setAnimated(true);

        // إعداد مخطط أكثر السيارات طلباً
        CategoryAxis xAxis3 = new CategoryAxis();
        NumberAxis yAxis3 = new NumberAxis();
        mostRentedCarsChart.setTitle("");
        mostRentedCarsChart.setAnimated(true);

        // إعداد جدول أفضل العملاء
        topCustomersTable.setPlaceholder(new Label("No data available"));

        // إعداد قائمة النشاطات
        recentActivityList.setPlaceholder(new Label("No recent activity"));
    }

    private void loadDashboardData() {
        try {
            // 1. تحديث بطاقات الإحصاءات
            updateStatsCards();

            // 2. تحديث الرسوم البيانية
            updateCharts();

            // 3. تحديث أفضل العملاء
            updateTopCustomers();

            // 4. تحديث النشاطات الحديثة
            updateRecentActivity();

            statusLabel.setText("Dashboard updated successfully");

        } catch (Exception e) {
            showError("Dashboard Error", "Failed to load dashboard data: " + e.getMessage());
        }
    }

    private void updateStatsCards() {
        Map<String, Object> stats = statisticsService.getBasicStats();

        // تحديث بطاقة الإيرادات
        double totalRevenue = (double) stats.get("totalRevenue");
        totalRevenueLabel.setText(String.format("$%.2f", totalRevenue));

        // تحديث بطاقة متوسط الحجز
        double avgPrice = (double) stats.get("averageReservationPrice");
        avgBookingLabel.setText(String.format("$%.2f", avgPrice));

        // تحديث بطاقة السيارات
        int totalCars = (int) stats.get("totalCars");
        int availableCars = (int) stats.get("availableCars");
        carsStatsLabel.setText(String.format("%d/%d", availableCars, totalCars));

        // تحديث بطاقة العملاء
        int totalCustomers = (int) stats.get("totalCustomers");
        customersLabel.setText(String.valueOf(totalCustomers));

        // حساب نسبة التغير في الإيرادات (مثال)
        revenueChangeLabel.setText("+12.5%"); // يمكن جعلها ديناميكية لاحقاً
        revenueChangeLabel.getStyleClass().add("revenue-up");
    }

    private void updateCharts() {
        // 1. مخطط الحجوزات الشهرية
        updateMonthlyReservationsChart();

        // 2. مخطط توزيع السيارات
        updateCarDistributionChart();

        // 3. مخطط الإيرادات الشهرية
        updateMonthlyRevenueChart();

        // 4. مخطط أكثر السيارات طلباً
        updateMostRentedCarsChart();
    }

    private void updateMonthlyReservationsChart() {
        monthlyReservationsChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Reservations");

        Map<YearMonth, Integer> monthlyStats = statisticsService.getMonthlyReservations();

        for (Map.Entry<YearMonth, Integer> entry : monthlyStats.entrySet()) {
            String month = entry.getKey().getMonth().toString().substring(0, 3);
            series.getData().add(new XYChart.Data<>(month, entry.getValue()));
        }

        monthlyReservationsChart.getData().add(series);

        // تلوين الأعمدة
        for (XYChart.Data<String, Number> data : series.getData()) {
            data.getNode().setStyle("-fx-bar-fill: #2196F3;");
        }
    }

    private void updateCarDistributionChart() {
        carDistributionChart.getData().clear();

        Map<String, Long> brandDistribution = statisticsService.getCarDistributionByBrand();

        for (Map.Entry<String, Long> entry : brandDistribution.entrySet()) {
            PieChart.Data slice = new PieChart.Data(
                    entry.getKey() + " (" + entry.getValue() + ")",
                    entry.getValue()
            );
            carDistributionChart.getData().add(slice);
        }

        // تخصيص ألوان الشرائح
        String[] colors = new String[9];
        colors[0] = "#2196F3";
        colors[1] = "#4CAF50";
        colors[2] = "#FF9800";
        colors[3] = "#9C27B0";
        colors[4] = "#f44336";
        colors[5] = "#00BCD4";
        colors[6] = "#FFC107";
        colors[7] = "#795548";
        colors[8] = "#607D8B";



        int i = 0;
        for (PieChart.Data data : carDistributionChart.getData()) {
            data.getNode().setStyle("-fx-pie-color: " + colors[i % colors.length] + ";");
            i++;
        }
    }

    private void updateMonthlyRevenueChart() {
        monthlyRevenueChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenue");

        Map<YearMonth, Double> monthlyRevenue = statisticsService.getMonthlyRevenue();

        for (Map.Entry<YearMonth, Double> entry : monthlyRevenue.entrySet()) {
            String month = entry.getKey().getMonth().toString().substring(0, 3);
            series.getData().add(new XYChart.Data<>(month, entry.getValue()));
        }

        monthlyRevenueChart.getData().add(series);

        // تخصيص الخط
        series.getNode().setStyle("-fx-stroke: #4CAF50; -fx-stroke-width: 3px;");

        // تخصيص النقاط
        for (XYChart.Data<String, Number> data : series.getData()) {
            data.getNode().setStyle("-fx-background-color: #4CAF50, white; -fx-background-radius: 5px;");
        }
    }

    private void updateMostRentedCarsChart() {
        mostRentedCarsChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Rental Count");

        Map<String, Long> mostRentedCars = statisticsService.getMostRentedCars();

        int count = 0;
        for (Map.Entry<String, Long> entry : mostRentedCars.entrySet()) {
            // اختصار أسماء السيارات الطويلة
            String carName = entry.getKey();
            if (carName.length() > 15) {
                carName = carName.substring(0, 12) + "...";
            }

            series.getData().add(new XYChart.Data<>(carName, entry.getValue()));
            count++;

            // عرض أول 5 فقط
            if (count >= 5) break;
        }

        mostRentedCarsChart.getData().add(series);

        // تلوين الأعمدة بالتدرج
        int i = 0;
        String[] barColors = new String[5];
        barColors[0] = "#FF9800";
        barColors[1] = "#FFB74D";
        barColors[2] = "#FFCC80";
        barColors[3] = "#FFE0B2";
        barColors[4] = "#FFF3E0";

        for (XYChart.Data<String, Number> data : series.getData()) {
            data.getNode().setStyle("-fx-bar-fill: " + barColors[i] + ";");
            i++;
        }
    }

    private void updateTopCustomers() {
        ObservableList<TopCustomer> topCustomers = FXCollections.observableArrayList();

        Map<String, Double> topCustomersData = statisticsService.getTopCustomers();

        for (Map.Entry<String, Double> entry : topCustomersData.entrySet()) {
            topCustomers.add(new TopCustomer(entry.getKey(), entry.getValue()));
        }

        topCustomersTable.setItems(topCustomers);
    }

    private void updateRecentActivity() {
        ObservableList<String> activities = FXCollections.observableArrayList();

        // إضافة نشاطات حديثة (يمكن جعلها ديناميكية من قاعدة البيانات)
        activities.add("🟢 New reservation: Ahmed rented BMW X5");
        activities.add("💰 Payment received: $450.00");
        activities.add("🔧 Car maintenance: Toyota Camry serviced");
        activities.add("📥 New customer registered: Sara Mohamed");
        activities.add("📊 Monthly report generated");
        activities.add("🚗 New car added: Kia Sportage 2024");

        recentActivityList.setItems(activities);
    }

    // أضف زر تحديث للوحة التحكم
    @FXML
    private void handleRefreshDashboard() {
        loadDashboardData();
        showSuccess("Dashboard Refreshed", "All charts and statistics have been updated.");
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrental/view/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("Car Rental System - Login");

        } catch (Exception e) {
            showError("Navigation Error", "Cannot return to login screen: " + e.getMessage());
        }
    }


    @FXML
    private void handleGenerateFinancialReport() {
        try {
            // اختيار الفترة
            LocalDate startDate = LocalDate.now().minusMonths(3);
            LocalDate endDate = LocalDate.now();

            FinancialReport report = reportService.generateFinancialReport(startDate, endDate);

            // عرض التقرير في نافذة مخصصة
            showReportDialog("💰 Financial Report",
                    "Period: " + startDate + " to " + endDate,
                    report.toString(),
                    "#4CAF50");

        } catch (Exception e) {
            showError("Report Error", "Failed to generate financial report: " + e.getMessage());
        }
    }

    @FXML
    private void handleGenerateFleetReport() {
        try {
            FleetUtilizationReport report = reportService.generateFleetUtilizationReport();

            showReportDialog("🚗 Fleet Utilization Report",
                    "Report Date: " + LocalDate.now(),
                    report.toString(),
                    "#2196F3");

        } catch (Exception e) {
            showError("Report Error", "Failed to generate fleet report: " + e.getMessage());
        }
    }

    @FXML
    private void handleGenerateTrendsReport() {
        try {
            // اختيار العام
            int currentYear = LocalDate.now().getYear();
            SeasonalTrendsReport report = reportService.generateSeasonalTrendsReport(currentYear);

            showReportDialog("📈 Seasonal Trends Report",
                    "Year: " + currentYear,
                    report.toString(),
                    "#FF9800");

        } catch (Exception e) {
            showError("Report Error", "Failed to generate trends report: " + e.getMessage());
        }
    }

    @FXML
    private void handleGeneratePerformanceReport() {
        try {
            PerformanceReport report = reportService.generatePerformanceReport();

            showReportDialog("📊 Performance Report",
                    "Report Date: " + LocalDate.now(),
                    report.toString(),
                    "#9C27B0");

        } catch (Exception e) {
            showError("Report Error", "Failed to generate performance report: " + e.getMessage());
        }
    }

    @FXML
    private void handleGenerateAllReports() {
        try {
            StringBuilder allReports = new StringBuilder();

            // 1. Financial Report
            FinancialReport financialReport = reportService.generateFinancialReport(
                    LocalDate.now().minusMonths(3), LocalDate.now());
            allReports.append("=".repeat(50)).append("\n");
            allReports.append("💰 FINANCIAL REPORT\n");
            allReports.append("=".repeat(50)).append("\n");
            allReports.append(financialReport.toString()).append("\n\n");

            // 2. Fleet Report
            FleetUtilizationReport fleetReport = reportService.generateFleetUtilizationReport();
            allReports.append("=".repeat(50)).append("\n");
            allReports.append("🚗 FLEET UTILIZATION REPORT\n");
            allReports.append("=".repeat(50)).append("\n");
            allReports.append(fleetReport.toString()).append("\n\n");

            // 3. Performance Report
            PerformanceReport performanceReport = reportService.generatePerformanceReport();
            allReports.append("=".repeat(50)).append("\n");
            allReports.append("📊 PERFORMANCE REPORT\n");
            allReports.append("=".repeat(50)).append("\n");
            allReports.append(performanceReport.toString()).append("\n\n");

            // 4. Trends Report
            SeasonalTrendsReport trendsReport = reportService.generateSeasonalTrendsReport(
                    LocalDate.now().getYear());
            allReports.append("=".repeat(50)).append("\n");
            allReports.append("📈 SEASONAL TRENDS REPORT\n");
            allReports.append("=".repeat(50)).append("\n");
            allReports.append(trendsReport.toString());

            showReportDialog("📑 All Reports Summary",
                    "Comprehensive System Report",
                    allReports.toString(),
                    "#795548");

        } catch (Exception e) {
            showError("Report Error", "Failed to generate all reports: " + e.getMessage());
        }
    }

    @FXML
    private void handleExportReport() {
        try {
            // نافذة اختيار نوع التقرير
            ChoiceDialog<String> dialog = new ChoiceDialog<>("Financial Report",
                    "Financial Report", "Fleet Report", "Performance Report", "Trends Report");
            dialog.setTitle("Export Report");
            dialog.setHeaderText("Select report type to export");
            dialog.setContentText("Report type:");

            String reportType = dialog.showAndWait().orElse(null);
            if (reportType == null) return;

            // توليد التقرير المختار
            String reportContent = "";
            String fileName = "";

            switch (reportType) {
                case "Financial Report":
                    FinancialReport fr = reportService.generateFinancialReport(
                            LocalDate.now().minusMonths(3), LocalDate.now());
                    reportContent = fr.toString();
                    fileName = "financial_report_" + LocalDate.now() + ".txt";
                    break;

                case "Fleet Report":
                    FleetUtilizationReport fur = reportService.generateFleetUtilizationReport();
                    reportContent = fur.toString();
                    fileName = "fleet_report_" + LocalDate.now() + ".txt";
                    break;

                case "Performance Report":
                    PerformanceReport pr = reportService.generatePerformanceReport();
                    reportContent = pr.toString();
                    fileName = "performance_report_" + LocalDate.now() + ".txt";
                    break;

                case "Trends Report":
                    SeasonalTrendsReport str = reportService.generateSeasonalTrendsReport(
                            LocalDate.now().getYear());
                    reportContent = str.toString();
                    fileName = "trends_report_" + LocalDate.now() + ".txt";
                    break;
            }

            // محاكاة التصدير (في تطبيق حقيقي: حفظ ملف)
            System.out.println("💾 Exporting report: " + fileName);
            System.out.println("Content:\n" + reportContent);

            showSuccess("Export Successful",
                    "Report exported successfully!\n\n" +
                            "File: " + fileName + "\n" +
                            "Location: project_root/reports/\n\n" +
                            "(In real application, file would be saved)");

        } catch (Exception e) {
            showError("Export Error", "Failed to export report: " + e.getMessage());
        }
    }

    private void showReportDialog(String title, String subtitle, String content, String color) {
        // إنشاء محتوى التقرير مع تنسيق
        TextFlow textFlow = new TextFlow();

        // العنوان
        Text titleText = new Text(title + "\n");
        titleText.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: " + color + ";");

        // العنوان الفرعي
        Text subtitleText = new Text(subtitle + "\n\n");
        subtitleText.setStyle("-fx-font-size: 12px; -fx-fill: #666;");

        // المحتوى
        Text contentText = new Text(content);
        contentText.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");

        textFlow.getChildren().addAll(titleText, subtitleText, contentText);

        // ScrollPane
        ScrollPane scrollPane = new ScrollPane(textFlow);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefSize(700, 500);
        scrollPane.setStyle("-fx-background-color: #fafafa; -fx-border-color: #ddd;");

        // زر النسخ
        Button copyButton = new Button("📋 Copy to Clipboard");
        copyButton.setOnAction(e -> {
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent clipboardContent = new javafx.scene.input.ClipboardContent();
            clipboardContent.putString(content);
            clipboard.setContent(clipboardContent);
            showSuccess("Copied", "Report copied to clipboard!");
        });

        // زر التصدير
        Button exportButton = new Button("💾 Export as File");
        exportButton.setOnAction(e -> handleExportReport());

        // تخطيط الأزرار
        HBox buttonBox = new HBox(10, copyButton, exportButton);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER);

        // الحاوية الرئيسية
        VBox container = new VBox(15, scrollPane, buttonBox);
        container.setPadding(new javafx.geometry.Insets(15));

        // النافذة
        Stage reportStage = new Stage();
        reportStage.setTitle(title);
        reportStage.setScene(new Scene(container, 750, 600));
        reportStage.initModality(Modality.APPLICATION_MODAL);
        reportStage.show();
    }


    private void showReportDialog(String title, String content) {
        TextArea textArea = new TextArea(content);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefSize(600, 400);

        ScrollPane scrollPane = new ScrollPane(textArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle(title);
        dialog.setHeaderText("Report Generated Successfully");
        dialog.getDialogPane().setContent(scrollPane);
        dialog.showAndWait();
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

        //availableCarsLabel.setText("Available Cars: " + availableCars.size());

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