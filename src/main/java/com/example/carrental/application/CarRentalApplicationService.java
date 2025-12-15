package com.example.carrental.application;

import com.example.carrental.service.*;
import com.example.carrental.exception.ReservationException;
import com.example.carrental.model.*;
import java.time.LocalDate;

/**
 * Application Layer - تنسيق العمليات المعقدة
 * بين خدمات متعددة
 */
public class CarRentalApplicationService {
    private CarService carService;
    private CustomerService customerService;
    private ReservationService reservationService;

    public CarRentalApplicationService() {
        this.carService = new CarService();
        this.customerService = new CustomerService();
        this.reservationService = new ReservationService();
    }

    /**
     * عملية حجز كاملة مع جميع التحققات
     */
    public Reservation processCompleteReservation(
            String customerEmail,
            int carId,
            LocalDate startDate,
            LocalDate endDate) throws ReservationException {

        // 1. التحقق من العميل
        Customer customer = customerService.findCustomerByEmail(customerEmail);
        if (customer == null) {
            throw new ReservationException("Customer not found");
        }

        // 2. التحقق من السيارة
        Car car = carService.findCarById(carId);
        if (car == null) {
            throw new ReservationException("Car not found");
        }

        // 3. التحقق من التواريخ
        if (endDate.isBefore(startDate)) {
            throw new ReservationException("Invalid dates");
        }

        // 4. التحقق من توافر السيارة
        if (!reservationService.isCarAvailableForDates(carId, startDate, endDate)) {
            throw new ReservationException("Car not available for selected dates");
        }

        // 5. إنشاء الحجز
        Reservation reservation = reservationService.makeReservation(
                customer, car, startDate, endDate);

        // 6. تحديث حالة السيارة
        carService.updateCarAvailability(carId, false);

        return reservation;
    }

    /**
     * عملية إلغاء حجز كاملة
     */
    public boolean processCancellation(int reservationId) throws ReservationException {
        // 1. الحصول على الحجز
        Reservation reservation = reservationService.findReservationById(reservationId);
        if (reservation == null) {
            throw new ReservationException("Reservation not found");
        }

        // 2. إلغاء الحجز
        boolean cancelled = reservationService.cancelReservation(reservationId);

        if (cancelled) {
            // 3. تحديث حالة السيارة
            carService.updateCarAvailability(reservation.getCar().getCarId(), true);
        }

        return cancelled;
    }

    /**
     * عملية إضافة سيارة جديدة مع التحققات
     */
    public Car processAddNewCar(String brand, String model, int year,
                                String color, double pricePerDay) throws ReservationException {

        // التحققات المعقدة
        if (pricePerDay <= 0) {
            throw new ReservationException("Price must be positive");
        }

        if (year < 2000 || year > LocalDate.now().getYear() + 1) {
            throw new ReservationException("Invalid year");
        }

        Car newCar = new Car(0, brand, model, year, color, pricePerDay, true);
        carService.addCar(newCar);

        return newCar;
    }
}