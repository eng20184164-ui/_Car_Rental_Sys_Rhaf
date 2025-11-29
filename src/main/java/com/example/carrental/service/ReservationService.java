package com.example.carrental.service;

import com.example.carrental.dao.ReservationDAO;
import com.example.carrental.dao.CarDAO;
import com.example.carrental.model.Car;
import com.example.carrental.model.Customer;
import com.example.carrental.model.Reservation;
import com.example.carrental.exception.ReservationException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class ReservationService implements Reservable {
    private ReservationDAO reservationDAO;
    private CarDAO carDAO;
    private int nextReservationId;

    public ReservationService() {
        this.reservationDAO = new ReservationDAO();
        this.carDAO = new CarDAO();
        this.nextReservationId = reservationDAO.getReservationCount() + 1;
    }

    @Override
    public Reservation makeReservation(Customer customer, Car car, LocalDate startDate, LocalDate endDate)
            throws ReservationException {

        // التحقق من البيانات
        if (customer == null || car == null || startDate == null || endDate == null) {
            throw new ReservationException("All reservation details are required");
        }

        if (endDate.isBefore(startDate)) {
            throw new ReservationException("End date cannot be before start date");
        }

        if (startDate.isBefore(LocalDate.now())) {
            throw new ReservationException("Start date cannot be in the past");
        }

        if (!car.isAvailable()) {
            throw new ReservationException("Car is not available for reservation");
        }

        // التحقق من توفر السيارة في التواريخ المطلوبة
        if (!reservationDAO.isCarAvailableForDates(car.getCarId(), startDate, endDate, 0)) {
            throw new ReservationException("Car is not available for the selected dates");
        }

        // إنشاء الحجز
        Reservation reservation = new Reservation(0, customer, car, startDate, endDate);

        // إدخال الحجز في قاعدة البيانات
        int reservationId = reservationDAO.addReservation(reservation);
        if (reservationId == -1) {
            throw new ReservationException("Failed to create reservation in database");
        }

        // تحديث حالة السيارة إلى غير متاحة
        carDAO.updateCarAvailability(car.getCarId(), false);

        // الحصول على الحجز مع ID الذي تم إنشاؤه
        Reservation createdReservation = reservationDAO.getReservationById(reservationId);
        if (createdReservation == null) {
            throw new ReservationException("Failed to retrieve created reservation");
        }

        return createdReservation;
    }

    @Override
    public boolean cancelReservation(int reservationId) throws ReservationException {
        Reservation reservation = findReservationById(reservationId);

        if (reservation == null) {
            throw new ReservationException("Reservation not found with ID: " + reservationId);
        }

        if ("Cancelled".equals(reservation.getStatus())) {
            throw new ReservationException("Reservation is already cancelled");
        }

        // إلغاء الحجز في قاعدة البيانات
        boolean success = reservationDAO.cancelReservation(reservationId);
        if (!success) {
            throw new ReservationException("Failed to cancel reservation in database");
        }

        // تحديث حالة السيارة إلى متاحة
        carDAO.updateCarAvailability(reservation.getCar().getCarId(), true);

        return true;
    }

    @Override
    public double calculatePrice(Car car, LocalDate startDate, LocalDate endDate) {
        if (car == null || startDate == null || endDate == null) {
            return 0.0;
        }

        long days = ChronoUnit.DAYS.between(startDate, endDate);
        if (days <= 0) {
            return car.getPricePerDay(); // الحد الأدنى يوم واحد
        }

        return days * car.getPricePerDay();
    }

    // طرق مساعدة
    public Reservation findReservationById(int reservationId) {
        return reservationDAO.getReservationById(reservationId);
    }

    public List<Reservation> getCustomerReservations(Customer customer) {
        if (customer == null) {
            return new ArrayList<>();
        }
        return reservationDAO.getReservationsByCustomerId(customer.getCustomerId());
    }

    public List<Reservation> getAllReservations() {
        return reservationDAO.getAllReservations();
    }

    public int getTotalReservations() {
        return reservationDAO.getReservationCount();
    }

    public boolean isCarAvailableForDates(int carId, LocalDate startDate, LocalDate endDate) {
        return reservationDAO.isCarAvailableForDates(carId, startDate, endDate, 0);
    }
}