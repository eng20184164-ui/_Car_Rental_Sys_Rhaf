package com.example.carrental.model;

public class Car {
    private int carId;
    private String brand;
    private String model;
    private int year;
    private String color;
    private double pricePerDay;
    private boolean available;

    public Car(int carId, String brand, String model, int year, String color, double pricePerDay, boolean available) {
        this.carId = carId;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.color = color;
        this.pricePerDay = pricePerDay;
        this.available = available;
    }

    // Getters and Setters
    public int getCarId() {
        return carId;
    }

    public void setCarId(int carId) {
        this.carId = carId;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public double getPricePerDay() {
        return pricePerDay;
    }

    public void setPricePerDay(double pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return brand + " " + model + " (" + year + ") - $" + pricePerDay + "/day - " +
                (available ? "Available" : "Not Available");
    }
}