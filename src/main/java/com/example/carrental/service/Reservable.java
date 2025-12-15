package com.example.carrental.service;

import com.example.carrental.model.Car;
import com.example.carrental.model.Customer;
import com.example.carrental.model.Reservation;
import com.example.carrental.exception.ReservationException;
import java.time.LocalDate;

public interface Reservable {
    Reservation makeReservation(Customer customer, Car car, LocalDate startDate, LocalDate endDate)
            throws ReservationException;

    boolean cancelReservation(int reservationId) throws ReservationException;

    double calculatePrice(Car car, LocalDate startDate, LocalDate endDate);

    // إضافة طريقة جديدة للتحقق من توفر السيارة
    boolean isCarAvailableForDates(int carId, LocalDate startDate, LocalDate endDate);
}