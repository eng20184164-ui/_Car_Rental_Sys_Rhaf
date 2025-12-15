package com.example.carrental.controller;

import com.example.carrental.model.Car;
import com.example.carrental.model.Customer;
import com.example.carrental.model.Reservation;
import com.example.carrental.service.ReservationService;
import com.example.carrental.exception.ReservationException;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.util.List;

public class ReservationController {
    private ReservationService reservationService;
    private LocalDate lastStartDate;
    private LocalDate lastEndDate;
    private double lastTotalPrice;


    public ReservationController() {
        this.reservationService = new ReservationService();
    }

    /**
     * إنشاء حجز جديد
     */
//    public Reservation createReservation(Customer customer, Car car, LocalDate startDate, LocalDate endDate)
//            throws ReservationException {
//        return reservationService.makeReservation(customer, car, startDate, endDate);
//    }
    // تحديث methods الحجز لتخزين البيانات الأخيرة
    public Reservation createReservation(Customer customer, Car car, LocalDate startDate, LocalDate endDate)
            throws ReservationException {
        this.lastStartDate = startDate;
        this.lastEndDate = endDate;
        this.lastTotalPrice = calculateReservationPrice(car, startDate, endDate);

        return reservationService.makeReservation(customer, car, startDate, endDate);
    }

    /**
     * إلغاء حجر موجود
     */
    public boolean cancelReservation(int reservationId) throws ReservationException {
        return reservationService.cancelReservation(reservationId);
    }

    /**
     * حساب سعر الحجز
     */
    public double calculateReservationPrice(Car car, LocalDate startDate, LocalDate endDate) {
        return reservationService.calculatePrice(car, startDate, endDate);
    }

    /**
     * التحقق من توفر السيارة في التواريخ المحددة
     */
    public boolean isCarAvailableForDates(int carId, LocalDate startDate, LocalDate endDate) {
        return reservationService.isCarAvailableForDates(carId, startDate, endDate);
    }

    /**
     * الحصول على جميع حجوزات العميل
     */
    public List<Reservation> getCustomerReservations(Customer customer) {
        return reservationService.getCustomerReservations(customer);
    }

    /**
     * الحصول على جميع الحجوزات في النظام
     */
    public List<Reservation> getAllReservations() {
        return reservationService.getAllReservations();
    }

    /**
     * الحصول على حجز محدد بالرقم
     */
    public Reservation getReservationById(int reservationId) {
        return reservationService.findReservationById(reservationId);
    }

    /**
     * عرض نافذة حجز السيارة
     */
    public void showReservationDialog(Customer customer, Car car, Runnable onSuccess) {
        Dialog<Reservation> dialog = new Dialog<>();
        dialog.setTitle("Rent Car");
        dialog.setHeaderText("Rent " + car.getBrand() + " " + car.getModel());

        // إنشاء عناصر النافذة
        DatePicker startDatePicker = new DatePicker();
        DatePicker endDatePicker = new DatePicker();
        Label totalPriceLabel = new Label("Total: $0.0");
        totalPriceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2196F3;");

        // حساب السعر عند تغيير التواريخ
        startDatePicker.setOnAction(e -> updateTotalPrice(car, startDatePicker, endDatePicker, totalPriceLabel));
        endDatePicker.setOnAction(e -> updateTotalPrice(car, startDatePicker, endDatePicker, totalPriceLabel));

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new javafx.geometry.Insets(20));

        grid.add(new Label("Start Date:"), 0, 0);
        grid.add(startDatePicker, 1, 0);
        grid.add(new Label("End Date:"), 0, 1);
        grid.add(endDatePicker, 1, 1);
        grid.add(totalPriceLabel, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // تحقق من صحة البيانات قبل الإغلاق
        final Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            if (!validateReservationDates(startDatePicker, endDatePicker, car)) {
                e.consume();
            }
        });

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    return createReservation(customer, car, startDatePicker.getValue(), endDatePicker.getValue());
                } catch (ReservationException e) {
                    showAlert(Alert.AlertType.ERROR, "Reservation Error", e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(reservation -> {
            if (onSuccess != null) {
                onSuccess.run();
            }
        });
    }

    /**
     * عرض نافذة تأكيد إلغاء الحجز
     */
    public void showCancellationDialog(Reservation reservation, Runnable onSuccess) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Cancel Reservation");
        confirmAlert.setHeaderText("Cancel Reservation #" + reservation.getReservationId());
        confirmAlert.setContentText("Are you sure you want to cancel this reservation?\n\n" +
                "Car: " + reservation.getCar().getBrand() + " " + reservation.getCar().getModel() + "\n" +
                "Period: " + reservation.getStartDate() + " to " + reservation.getEndDate() + "\n" +
                "Total: $" + reservation.getTotalPrice());

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                cancelReservation(reservation.getReservationId());
                showAlert(Alert.AlertType.INFORMATION, "Cancellation Successful",
                        "Reservation has been cancelled successfully");
                if (onSuccess != null) {
                    onSuccess.run();
                }
            } catch (ReservationException e) {
                showAlert(Alert.AlertType.ERROR, "Cancellation Error", e.getMessage());
            }
        }
    }

    /**
     * تحديث السعر الإجمالي في الواجهة
     */
    private void updateTotalPrice(Car car, DatePicker startDate, DatePicker endDate, Label totalLabel) {
        if (startDate.getValue() != null && endDate.getValue() != null && car != null) {
            double total = calculateReservationPrice(car, startDate.getValue(), endDate.getValue());
            totalLabel.setText(String.format("Total: $%.2f", total));
        }
    }

    /**
     * التحقق من صحة تواريخ الحجز
     */
    private boolean validateReservationDates(DatePicker startDate, DatePicker endDate, Car car) {
        if (startDate.getValue() == null || endDate.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please select both start and end dates");
            return false;
        }

        if (endDate.getValue().isBefore(startDate.getValue())) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "End date cannot be before start date");
            return false;
        }

        if (startDate.getValue().isBefore(LocalDate.now())) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Start date cannot be in the past");
            return false;
        }

        if (!isCarAvailableForDates(car.getCarId(), startDate.getValue(), endDate.getValue())) {
            showAlert(Alert.AlertType.ERROR, "Availability Error", "Car is not available for the selected dates");
            return false;
        }

        return true;
    }

    /**
     * عرض رسائل للمستخدم
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * إنشاء تقرير الحجوزات
     */
    public String generateReservationsReport() {
        List<Reservation> reservations = getAllReservations();
        StringBuilder report = new StringBuilder();

        report.append("=== Reservations Report ===\n");
        report.append("Generated on: ").append(LocalDate.now()).append("\n\n");

        report.append("Total Reservations: ").append(reservations.size()).append("\n\n");

        int confirmed = 0, cancelled = 0;
        double totalRevenue = 0;

        for (Reservation res : reservations) {
            if ("Confirmed".equals(res.getStatus())) {
                confirmed++;
                totalRevenue += res.getTotalPrice();
            } else if ("Cancelled".equals(res.getStatus())) {
                cancelled++;
            }

            report.append("Reservation #").append(res.getReservationId())
                    .append(" - ").append(res.getCustomer().getName())
                    .append(" - ").append(res.getCar().getBrand()).append(" ").append(res.getCar().getModel())
                    .append(" - $").append(res.getTotalPrice())
                    .append(" - ").append(res.getStatus())
                    .append("\n");
        }

        report.append("\nSummary:\n");
        report.append("Confirmed Reservations: ").append(confirmed).append("\n");
        report.append("Cancelled Reservations: ").append(cancelled).append("\n");
        report.append("Total Revenue: $").append(String.format("%.2f", totalRevenue)).append("\n");

        return report.toString();
    }

    // Getters للبيانات الأخيرة
    public LocalDate getLastStartDate() {
        return lastStartDate;
    }

    public LocalDate getLastEndDate() {
        return lastEndDate;
    }

    public double getLastTotalPrice() {
        return lastTotalPrice;
    }


}