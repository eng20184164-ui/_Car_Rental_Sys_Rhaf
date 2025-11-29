package com.example.carrental.service;

import com.example.carrental.dao.CarDAO;
import com.example.carrental.model.Car;
import com.example.carrental.exception.ReservationException;
import java.util.List;

public class CarService {
    private CarDAO carDAO;

    public CarService() {
        this.carDAO = new CarDAO();
        // تم إزالة initializeSampleData() لأن البيانات الآن تأتي من قاعدة البيانات
    }

    public void addCar(Car car) throws ReservationException {
        if (car == null) {
            throw new ReservationException("Car cannot be null");
        }

        if (car.getBrand() == null || car.getBrand().trim().isEmpty()) {
            throw new ReservationException("Car brand is required");
        }

        if (car.getModel() == null || car.getModel().trim().isEmpty()) {
            throw new ReservationException("Car model is required");
        }

        if (car.getPricePerDay() <= 0) {
            throw new ReservationException("Price per day must be greater than 0");
        }

        boolean success = carDAO.addCar(car);
        if (!success) {
            throw new ReservationException("Failed to add car to database");
        }
    }

    public boolean removeCar(int carId) throws ReservationException {
        Car car = findCarById(carId);
        if (car == null) {
            throw new ReservationException("Car not found with ID: " + carId);
        }

        if (!car.isAvailable()) {
            throw new ReservationException("Cannot remove car that has active reservations");
        }

        return carDAO.deleteCar(carId);
    }

    public void updateCar(Car updatedCar) throws ReservationException {
        if (updatedCar == null) {
            throw new ReservationException("Car cannot be null");
        }

        Car existingCar = findCarById(updatedCar.getCarId());
        if (existingCar == null) {
            throw new ReservationException("Car not found with ID: " + updatedCar.getCarId());
        }

        boolean success = carDAO.updateCar(updatedCar);
        if (!success) {
            throw new ReservationException("Failed to update car in database");
        }
    }

    public Car findCarById(int carId) {
        return carDAO.getCarById(carId);
    }

    public List<Car> getAvailableCars() {
        return carDAO.getAvailableCars();
    }

    public List<Car> getAllCars() {
        return carDAO.getAllCars();
    }

    public List<Car> searchCars(String brand, String model, Double maxPrice) {
        return carDAO.searchCars(brand, model, maxPrice);
    }

    public boolean updateCarAvailability(int carId, boolean available) {
        return carDAO.updateCarAvailability(carId, available);
    }
}