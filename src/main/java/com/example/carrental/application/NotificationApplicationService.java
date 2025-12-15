package com.example.carrental.application;

import com.example.carrental.model.Reservation;
import com.example.carrental.model.Car;

/**
 * Application Layer للإشعارات - تنسيق إرسال الإشعارات
 */
public class NotificationApplicationService {

    public void sendReservationConfirmation(Reservation reservation) {
        String customerEmail = reservation.getCustomer().getEmail();
        String subject = "Reservation Confirmation #" + reservation.getReservationId();
        String message = buildReservationConfirmationMessage(reservation);

        // في تطبيق حقيقي: إرسال بريد إلكتروني
        System.out.println("\n📧 Sending email to: " + customerEmail);
        System.out.println("Subject: " + subject);
        System.out.println("Message:\n" + message);
        System.out.println("✅ Email sent successfully!\n");
    }

    public void sendReservationCancellation(Reservation reservation) {
        String customerEmail = reservation.getCustomer().getEmail();
        String subject = "Reservation Cancellation #" + reservation.getReservationId();
        String message = buildCancellationMessage(reservation);

        System.out.println("\n📧 Sending cancellation email to: " + customerEmail);
        System.out.println("Subject: " + subject);
        System.out.println("Message:\n" + message);
        System.out.println("✅ Cancellation email sent!\n");
    }

    public void sendAdminAlert(String alertType, String details) {
        System.out.println("\n🚨 ADMIN ALERT");
        System.out.println("Type: " + alertType);
        System.out.println("Details: " + details);
        System.out.println("Time: " + java.time.LocalDateTime.now() + "\n");
    }

    public void sendMaintenanceReminder(Car car) {
        System.out.println("\n🔧 MAINTENANCE REMINDER");
        System.out.println("Car: " + car.getBrand() + " " + car.getModel());
        System.out.println("Car ID: " + car.getCarId());
        System.out.println("Year: " + car.getYear());
        System.out.println("Last Service: " + java.time.LocalDate.now().minusMonths(2));
        System.out.println("Next Service Due: " + java.time.LocalDate.now().plusMonths(1) + "\n");
    }

    public void sendLowInventoryAlert(Car car) {
        System.out.println("\n⚠️ LOW INVENTORY ALERT");
        System.out.println("Car " + car.getCarId() + " is low on inventory.");
        System.out.println("Brand: " + car.getBrand());
        System.out.println("Model: " + car.getModel());
        System.out.println("Action: Please order more units.\n");
    }

    private String buildReservationConfirmationMessage(Reservation reservation) {
        return String.format(
                "Dear %s,\n\n" +
                        "Your reservation has been confirmed!\n\n" +
                        "════════════════════════════════════════\n" +
                        "RESERVATION DETAILS\n" +
                        "════════════════════════════════════════\n" +
                        "• Reservation ID: #%d\n" +
                        "• Car: %s %s (%d)\n" +
                        "• Color: %s\n" +
                        "• Pickup Date: %s\n" +
                        "• Return Date: %s\n" +
                        "• Total Amount: $%.2f\n\n" +
                        "════════════════════════════════════════\n" +
                        "PICKUP INSTRUCTIONS\n" +
                        "════════════════════════════════════════\n" +
                        "1. Bring your driver's license\n" +
                        "2. Bring this confirmation email\n" +
                        "3. Arrive at our location 30 min early\n\n" +
                        "Thank you for choosing our service!\n\n" +
                        "Best regards,\n" +
                        "Car Rental System Team\n" +
                        "📞 123-456-7890\n" +
                        "📧 support@carrental.com",
                reservation.getCustomer().getName(),
                reservation.getReservationId(),
                reservation.getCar().getBrand(),
                reservation.getCar().getModel(),
                reservation.getCar().getYear(),
                reservation.getCar().getColor(),
                reservation.getStartDate(),
                reservation.getEndDate(),
                reservation.getTotalPrice()
        );
    }

    private String buildCancellationMessage(Reservation reservation) {
        return String.format(
                "Dear %s,\n\n" +
                        "Your reservation #%d has been cancelled.\n\n" +
                        "════════════════════════════════════════\n" +
                        "CANCELLATION DETAILS\n" +
                        "════════════════════════════════════════\n" +
                        "• Car: %s %s\n" +
                        "• Dates: %s to %s\n" +
                        "• Refund amount: $%.2f\n" +
                        "• Refund will be processed in 5-7 business days\n\n" +
                        "We're sorry to see you go and hope to serve you again soon!\n\n" +
                        "Best regards,\n" +
                        "Car Rental System Team",
                reservation.getCustomer().getName(),
                reservation.getReservationId(),
                reservation.getCar().getBrand(),
                reservation.getCar().getModel(),
                reservation.getStartDate(),
                reservation.getEndDate(),
                reservation.getTotalPrice()
        );
    }
}