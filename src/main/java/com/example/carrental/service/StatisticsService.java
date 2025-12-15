package com.example.carrental.service;

import com.example.carrental.dao.CarDAO;
import com.example.carrental.dao.CustomerDAO;
import com.example.carrental.dao.ReservationDAO;
import com.example.carrental.model.Car;
import com.example.carrental.model.Reservation;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.*;

public class StatisticsService {
    private ReservationDAO reservationDAO;
    private CarDAO carDAO;
    private CustomerDAO customerDAO;

    public StatisticsService() {
        this.reservationDAO = new ReservationDAO();
        this.carDAO = new CarDAO();
        this.customerDAO = new CustomerDAO();
    }

    // الإحصائيات الأساسية
    public Map<String, Object> getBasicStats() {
        Map<String, Object> stats = new HashMap<>();

        List<Reservation> reservations = reservationDAO.getAllReservations();
        List<Car> cars = carDAO.getAllCars();

        // إجمالي الإيرادات
        double totalRevenue = reservations.stream()
                .filter(r -> "Confirmed".equals(r.getStatus()))
                .mapToDouble(Reservation::getTotalPrice)
                .sum();

        // متوسط سعر الحجز
        long confirmedCount = reservations.stream()
                .filter(r -> "Confirmed".equals(r.getStatus()))
                .count();
        double averageReservationPrice = confirmedCount > 0 ? totalRevenue / confirmedCount : 0;

        stats.put("totalRevenue", totalRevenue);
        stats.put("averageReservationPrice", averageReservationPrice);
        stats.put("totalCars", cars.size());
        stats.put("availableCars", carDAO.getAvailableCars().size());
        stats.put("totalReservations", reservations.size());
        stats.put("totalCustomers", customerDAO.getAllCustomers().size());
        stats.put("activeReservations", reservations.stream()
                .filter(r -> "Confirmed".equals(r.getStatus())).count());

        return stats;
    }

    // إحصائيات الحجوزات الشهرية
    public Map<YearMonth, Integer> getMonthlyReservations() {
        Map<YearMonth, Integer> monthlyStats = new TreeMap<>();
        List<Reservation> reservations = reservationDAO.getAllReservations();

        // تهيئة الأشهر الـ 12 الماضية
        for (int i = 11; i >= 0; i--) {
            YearMonth month = YearMonth.now().minusMonths(i);
            monthlyStats.put(month, 0);
        }

        // تجميع الحجوزات حسب الشهر
        for (Reservation reservation : reservations) {
            YearMonth month = YearMonth.from(reservation.getStartDate());
            monthlyStats.put(month, monthlyStats.getOrDefault(month, 0) + 1);
        }

        return monthlyStats;
    }

    // إحصائيات توزيع السيارات حسب الماركة
    public Map<String, Long> getCarDistributionByBrand() {
        List<Car> cars = carDAO.getAllCars();
        Map<String, Long> brandDistribution = new HashMap<>();

        for (Car car : cars) {
            String brand = car.getBrand();
            brandDistribution.put(brand, brandDistribution.getOrDefault(brand, 0L) + 1);
        }

        return brandDistribution;
    }

    // الإيرادات الشهرية
    public Map<YearMonth, Double> getMonthlyRevenue() {
        Map<YearMonth, Double> monthlyRevenue = new TreeMap<>();
        List<Reservation> reservations = reservationDAO.getAllReservations();

        // تهيئة الأشهر الـ 12 الماضية
        for (int i = 11; i >= 0; i--) {
            YearMonth month = YearMonth.now().minusMonths(i);
            monthlyRevenue.put(month, 0.0);
        }

        // تجميع الإيرادات حسب الشهر
        for (Reservation reservation : reservations) {
            if ("Confirmed".equals(reservation.getStatus())) {
                YearMonth month = YearMonth.from(reservation.getStartDate());
                double current = monthlyRevenue.getOrDefault(month, 0.0);
                monthlyRevenue.put(month, current + reservation.getTotalPrice());
            }
        }

        return monthlyRevenue;
    }

    // أكثر السيارات طلباً
    public Map<String, Long> getMostRentedCars() {
        List<Reservation> reservations = reservationDAO.getAllReservations();
        Map<String, Long> carRentalCount = new HashMap<>();

        for (Reservation reservation : reservations) {
            String carName = reservation.getCar().getBrand() + " " + reservation.getCar().getModel();
            carRentalCount.put(carName, carRentalCount.getOrDefault(carName, 0L) + 1);
        }

        // ترتيب تنازلي حسب عدد الحجوزات
        return carRentalCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(LinkedHashMap::new,
                        (m, e) -> m.put(e.getKey(), e.getValue()),
                        Map::putAll);
    }

    // أفضل العملاء
    public Map<String, Double> getTopCustomers() {
        List<Reservation> reservations = reservationDAO.getAllReservations();
        Map<String, Double> customerSpending = new HashMap<>();

        for (Reservation reservation : reservations) {
            if ("Confirmed".equals(reservation.getStatus())) {
                String customerName = reservation.getCustomer().getName();
                double current = customerSpending.getOrDefault(customerName, 0.0);
                customerSpending.put(customerName, current + reservation.getTotalPrice());
            }
        }

        // ترتيب تنازلي حسب الإنفاق
        return customerSpending.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(10)
                .collect(LinkedHashMap::new,
                        (m, e) -> m.put(e.getKey(), e.getValue()),
                        Map::putAll);
    }

    // إحصائيات الحجوزات اليومية لهذا الشهر
    public Map<LocalDate, Integer> getDailyReservationsThisMonth() {
        Map<LocalDate, Integer> dailyStats = new TreeMap<>();
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);

        List<Reservation> reservations = reservationDAO.getAllReservations();

        // تهيئة جميع أيام الشهر الحالي
        for (int day = 1; day <= currentMonth.lengthOfMonth(); day++) {
            LocalDate date = LocalDate.of(currentMonth.getYear(), currentMonth.getMonth(), day);
            if (!date.isAfter(today)) {
                dailyStats.put(date, 0);
            }
        }

        // تجميع الحجوزات اليومية
        for (Reservation reservation : reservations) {
            LocalDate date = reservation.getStartDate();
            if (YearMonth.from(date).equals(currentMonth) && !date.isAfter(today)) {
                dailyStats.put(date, dailyStats.getOrDefault(date, 0) + 1);
            }
        }

        return dailyStats;
    }
}