package com.example.carrental.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Reservation {
    private int reservationId;
    private Customer customer;
    private Car car;
    private LocalDate startDate;
    private LocalDate endDate;
    private double totalPrice;
    private String status; // "Pending", "Confirmed", "Cancelled"

    public Reservation(int reservationId, Customer customer, Car car, LocalDate startDate, LocalDate endDate) {
        this.reservationId = reservationId;
        this.customer = customer;
        this.car = car;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalPrice = calculateTotalPrice();
        this.status = "Confirmed";
    }

    // طريقة لحساب السعر الإجمالي - تطبيق للـ Business Logic
    private double calculateTotalPrice() {
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        return days * car.getPricePerDay();
    }

    // Getters and Setters
    public int getReservationId() {
        return reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Reservation #" + reservationId + " - " + customer.getName() + " - " +
                car.getBrand() + " " + car.getModel() + " - $" + totalPrice;
    }
}