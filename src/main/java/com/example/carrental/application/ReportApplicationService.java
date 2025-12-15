package com.example.carrental.application;

import com.example.carrental.model.*;
import com.example.carrental.service.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Application Layer للتقارير - تنسيق توليد التقارير المعقدة
 */
public class ReportApplicationService {
    private CarService carService;
    private CustomerService customerService;
    private ReservationService reservationService;

    public ReportApplicationService() {
        this.carService = new CarService();
        this.customerService = new CustomerService();
        this.reservationService = new ReservationService();
    }

    /**
     * 1️⃣ تقرير مالي مفصل
     */
    public FinancialReport generateFinancialReport(LocalDate startDate, LocalDate endDate) {
        List<Reservation> reservations = reservationService.getAllReservations();

        double totalRevenue = 0;
        double confirmedRevenue = 0;
        double cancelledRevenue = 0;
        int totalBookings = 0;
        int confirmedBookings = 0;
        int cancelledBookings = 0;

        Map<String, Double> revenueByCarType = new HashMap<>();
        Map<LocalDate, Double> dailyRevenue = new TreeMap<>();

        for (Reservation reservation : reservations) {
            if (!isDateInRange(reservation.getStartDate(), startDate, endDate)) {
                continue;
            }

            totalBookings++;

            if ("Confirmed".equals(reservation.getStatus())) {
                confirmedBookings++;
                confirmedRevenue += reservation.getTotalPrice();

                // تجميع حسب نوع السيارة
                String carType = reservation.getCar().getBrand();
                revenueByCarType.put(carType,
                        revenueByCarType.getOrDefault(carType, 0.0) + reservation.getTotalPrice());

                // تجميع يومي
                dailyRevenue.put(reservation.getStartDate(),
                        dailyRevenue.getOrDefault(reservation.getStartDate(), 0.0) + reservation.getTotalPrice());

            } else if ("Cancelled".equals(reservation.getStatus())) {
                cancelledBookings++;
                cancelledRevenue += reservation.getTotalPrice();
            }
        }

        totalRevenue = confirmedRevenue + cancelledRevenue;

        return new FinancialReport(
                startDate, endDate,
                totalRevenue, confirmedRevenue, cancelledRevenue,
                totalBookings, confirmedBookings, cancelledBookings,
                revenueByCarType, dailyRevenue
        );
    }

    /**
     * 2️⃣ تقرير استخدام الأسطول
     */
    public FleetUtilizationReport generateFleetUtilizationReport() {
        List<Car> allCars = carService.getAllCars();
        List<Reservation> allReservations = reservationService.getAllReservations();

        int totalCars = allCars.size();
        int availableCars = (int) allCars.stream().filter(Car::isAvailable).count();
        int rentedCars = totalCars - availableCars;

        Map<String, UtilizationStats> carTypeStats = new HashMap<>();

        for (Car car : allCars) {
            String carType = car.getBrand() + " " + car.getModel();

            // حساب أيام التأجير لهذه السيارة
            long rentalDays = allReservations.stream()
                    .filter(r -> r.getCar().getCarId() == car.getCarId() && "Confirmed".equals(r.getStatus()))
                    .mapToLong(r -> r.getStartDate().until(r.getEndDate()).getDays())
                    .sum();

            UtilizationStats stats = new UtilizationStats(carType, rentalDays);
            carTypeStats.put(carType, stats);
        }

        // حساب نسبة الاستخدام
        double utilizationRate = totalCars > 0 ? (double) rentedCars / totalCars * 100 : 0;

        return new FleetUtilizationReport(
                LocalDate.now(),
                totalCars, availableCars, rentedCars,
                utilizationRate, carTypeStats
        );
    }

    /**
     * 3️⃣ تقرير أداء الموظفين (للمديرين)
     */
    public PerformanceReport generatePerformanceReport() {
        List<Reservation> reservations = reservationService.getAllReservations();

        int processedReservations = reservations.size();
        int successfulReservations = (int) reservations.stream()
                .filter(r -> "Confirmed".equals(r.getStatus())).count();

        double successRate = processedReservations > 0 ?
                (double) successfulReservations / processedReservations * 100 : 0;

        return new PerformanceReport(
                LocalDate.now(),
                processedReservations,
                successfulReservations,
                successRate
        );
    }

    /**
     * 4️⃣ تقرير الاتجاهات الموسمية
     */
    public SeasonalTrendsReport generateSeasonalTrendsReport(int year) {
        Map<String, MonthlyStats> monthlyStats = new TreeMap<>();
        List<Reservation> reservations = reservationService.getAllReservations();

        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

        // تهيئة الأشهر
        for (String month : months) {
            monthlyStats.put(month, new MonthlyStats(month, 0, 0.0));
        }

        // جمع البيانات
        for (Reservation reservation : reservations) {
            if (reservation.getStartDate().getYear() == year && "Confirmed".equals(reservation.getStatus())) {
                String month = reservation.getStartDate().getMonth().toString().substring(0, 3);
                MonthlyStats stats = monthlyStats.get(month);

                if (stats != null) {
                    // تحديث الإحصائيات
                    stats.setBookings(stats.getBookings() + 1);
                    stats.setRevenue(stats.getRevenue() + reservation.getTotalPrice());
                }
            }
        }

        return new SeasonalTrendsReport(year, monthlyStats);
    }

    private boolean isDateInRange(LocalDate date, LocalDate start, LocalDate end) {
        return !date.isBefore(start) && !date.isAfter(end);
    }

    // ========== Inner Classes for Reports ==========

    public static class FinancialReport {
        private LocalDate periodStart;
        private LocalDate periodEnd;
        private double totalRevenue;
        private double confirmedRevenue;
        private double cancelledRevenue;
        private int totalBookings;
        private int confirmedBookings;
        private int cancelledBookings;
        private Map<String, Double> revenueByCarType;
        private Map<LocalDate, Double> dailyRevenue;

        public FinancialReport(LocalDate periodStart, LocalDate periodEnd,
                               double totalRevenue, double confirmedRevenue, double cancelledRevenue,
                               int totalBookings, int confirmedBookings, int cancelledBookings,
                               Map<String, Double> revenueByCarType, Map<LocalDate, Double> dailyRevenue) {
            this.periodStart = periodStart;
            this.periodEnd = periodEnd;
            this.totalRevenue = totalRevenue;
            this.confirmedRevenue = confirmedRevenue;
            this.cancelledRevenue = cancelledRevenue;
            this.totalBookings = totalBookings;
            this.confirmedBookings = confirmedBookings;
            this.cancelledBookings = cancelledBookings;
            this.revenueByCarType = revenueByCarType;
            this.dailyRevenue = dailyRevenue;
        }

        // Getters
        public LocalDate getPeriodStart() { return periodStart; }
        public LocalDate getPeriodEnd() { return periodEnd; }
        public double getTotalRevenue() { return totalRevenue; }
        public double getConfirmedRevenue() { return confirmedRevenue; }
        public double getCancelledRevenue() { return cancelledRevenue; }
        public int getTotalBookings() { return totalBookings; }
        public int getConfirmedBookings() { return confirmedBookings; }
        public int getCancelledBookings() { return cancelledBookings; }
        public Map<String, Double> getRevenueByCarType() { return revenueByCarType; }
        public Map<LocalDate, Double> getDailyRevenue() { return dailyRevenue; }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("💰 FINANCIAL REPORT\n");
            sb.append("===================\n");
            sb.append(String.format("Period: %s to %s\n", periodStart, periodEnd));
            sb.append(String.format("Total Revenue: $%.2f\n", totalRevenue));
            sb.append(String.format("Confirmed Revenue: $%.2f\n", confirmedRevenue));
            sb.append(String.format("Cancelled Revenue: $%.2f\n", cancelledRevenue));
            sb.append(String.format("Total Bookings: %d\n", totalBookings));
            sb.append(String.format("Confirmed Bookings: %d\n", confirmedBookings));
            sb.append(String.format("Cancelled Bookings: %d\n", cancelledBookings));

            sb.append("\nRevenue by Car Type:\n");
            for (Map.Entry<String, Double> entry : revenueByCarType.entrySet()) {
                sb.append(String.format("  %s: $%.2f\n", entry.getKey(), entry.getValue()));
            }

            return sb.toString();
        }
    }

    public static class FleetUtilizationReport {
        private LocalDate reportDate;
        private int totalCars;
        private int availableCars;
        private int rentedCars;
        private double utilizationRate;
        private Map<String, UtilizationStats> carTypeStats;

        public FleetUtilizationReport(LocalDate reportDate,
                                      int totalCars, int availableCars, int rentedCars,
                                      double utilizationRate, Map<String, UtilizationStats> carTypeStats) {
            this.reportDate = reportDate;
            this.totalCars = totalCars;
            this.availableCars = availableCars;
            this.rentedCars = rentedCars;
            this.utilizationRate = utilizationRate;
            this.carTypeStats = carTypeStats;
        }

        // Getters
        public LocalDate getReportDate() { return reportDate; }
        public int getTotalCars() { return totalCars; }
        public int getAvailableCars() { return availableCars; }
        public int getRentedCars() { return rentedCars; }
        public double getUtilizationRate() { return utilizationRate; }
        public Map<String, UtilizationStats> getCarTypeStats() { return carTypeStats; }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("🚗 FLEET UTILIZATION REPORT\n");
            sb.append("===========================\n");
            sb.append(String.format("Report Date: %s\n", reportDate));
            sb.append(String.format("Total Cars: %d\n", totalCars));
            sb.append(String.format("Available Cars: %d\n", availableCars));
            sb.append(String.format("Rented Cars: %d\n", rentedCars));
            sb.append(String.format("Utilization Rate: %.2f%%\n", utilizationRate));

            sb.append("\nCar Type Statistics:\n");
            for (Map.Entry<String, UtilizationStats> entry : carTypeStats.entrySet()) {
                sb.append(String.format("  %s: %d rental days\n",
                        entry.getKey(), entry.getValue().getTotalRentalDays()));
            }

            return sb.toString();
        }
    }

    public static class UtilizationStats {
        private String carType;
        private long totalRentalDays;

        public UtilizationStats(String carType, long totalRentalDays) {
            this.carType = carType;
            this.totalRentalDays = totalRentalDays;
        }

        public String getCarType() { return carType; }
        public long getTotalRentalDays() { return totalRentalDays; }
    }

    public static class PerformanceReport {
        private LocalDate reportDate;
        private int processedReservations;
        private int successfulReservations;
        private double successRate;

        public PerformanceReport(LocalDate reportDate,
                                 int processedReservations,
                                 int successfulReservations,
                                 double successRate) {
            this.reportDate = reportDate;
            this.processedReservations = processedReservations;
            this.successfulReservations = successfulReservations;
            this.successRate = successRate;
        }

        // Getters
        public LocalDate getReportDate() { return reportDate; }
        public int getProcessedReservations() { return processedReservations; }
        public int getSuccessfulReservations() { return successfulReservations; }
        public double getSuccessRate() { return successRate; }

        @Override
        public String toString() {
            return String.format(
                    "📊 PERFORMANCE REPORT\n" +
                            "====================\n" +
                            "Date: %s\n" +
                            "Processed Reservations: %d\n" +
                            "Successful Reservations: %d\n" +
                            "Success Rate: %.2f%%\n",
                    reportDate, processedReservations, successfulReservations, successRate
            );
        }
    }

    public static class SeasonalTrendsReport {
        private int year;
        private Map<String, MonthlyStats> monthlyStats;

        public SeasonalTrendsReport(int year, Map<String, MonthlyStats> monthlyStats) {
            this.year = year;
            this.monthlyStats = monthlyStats;
        }

        public int getYear() { return year; }
        public Map<String, MonthlyStats> getMonthlyStats() { return monthlyStats; }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("📈 SEASONAL TRENDS REPORT %d\n", year));
            sb.append("==============================\n");

            for (Map.Entry<String, MonthlyStats> entry : monthlyStats.entrySet()) {
                MonthlyStats stats = entry.getValue();
                sb.append(String.format("%s: %d bookings, $%.2f revenue\n",
                        stats.getMonth(), stats.getBookings(), stats.getRevenue()));
            }

            return sb.toString();
        }
    }

    public static class MonthlyStats {
        private String month;
        private int bookings;
        private double revenue;

        public MonthlyStats(String month, int bookings, double revenue) {
            this.month = month;
            this.bookings = bookings;
            this.revenue = revenue;
        }

        // Getters and Setters
        public String getMonth() { return month; }
        public int getBookings() { return bookings; }
        public void setBookings(int bookings) { this.bookings = bookings; }
        public double getRevenue() { return revenue; }
        public void setRevenue(double revenue) { this.revenue = revenue; }
    }
}