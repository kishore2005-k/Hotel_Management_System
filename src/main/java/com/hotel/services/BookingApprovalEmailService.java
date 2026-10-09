package com.hotel.services;

import com.hotel.config.DatabaseConfig;
import com.hotel.models.Booking;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

public final class BookingApprovalEmailService {
    private static final String DEFAULT_SENDER_NAME = "Whispering Pines Lodge";

    public boolean isConfigured() {
        MailSettings settings = MailSettings.fromEnvironment();
        return settings.isConfigured();
    }

    public void sendApproval(Booking booking) throws MessagingException, IOException {
        MailSettings settings = MailSettings.fromEnvironment();
        if (!settings.isConfigured()) {
            throw new IllegalStateException("SMTP email settings are not configured.");
        }

        Properties properties = new Properties();
        properties.setProperty("mail.smtp.host", settings.host());
        properties.setProperty("mail.smtp.port", Integer.toString(settings.port()));
        properties.setProperty("mail.smtp.auth", Boolean.toString(!settings.username().isEmpty()));
        properties.setProperty("mail.smtp.starttls.enable", Boolean.toString(settings.startTls()));
        properties.setProperty("mail.smtp.starttls.required", Boolean.toString(settings.startTls()));
        properties.setProperty("mail.smtp.connectiontimeout", "10000");
        properties.setProperty("mail.smtp.timeout", "10000");
        properties.setProperty("mail.smtp.writetimeout", "10000");

        Session session = Session.getInstance(properties);
        MimeMessage message = new MimeMessage(session);
        InternetAddress sender = new InternetAddress(settings.fromAddress(), true);
        try {
            sender.setPersonal(settings.fromName(), StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException exception) {
            throw new MessagingException("Unable to encode the configured sender name.", exception);
        }
        message.setFrom(sender);
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(booking.getCustomerEmail(), true));
        message.setSubject("Your stay is confirmed - Whispering Pines Lodge", StandardCharsets.UTF_8.name());
        message.setText(createBody(booking, DatabaseConfig.getHotelContactPhone()), StandardCharsets.UTF_8.name());

        if (settings.username().isEmpty()) {
            Transport.send(message);
        } else {
            Transport.send(message, settings.username(), settings.password());
        }
    }

    private static String createBody(Booking booking, String receptionPhone) {
        StringBuilder body = new StringBuilder()
                .append("Hello ").append(booking.getCustomerName()).append(",\n\n")
                .append("Your reservation request has been approved. We look forward to welcoming you ")
                .append("to Whispering Pines Lodge.\n\n")
                .append("Reservation details\n")
                .append("Confirmation: #").append(booking.getId()).append('\n')
                .append("Room: ").append(booking.getRoomType())
                .append(" (").append(booking.getRoomNumber()).append(")\n")
                .append("Check-in: ").append(booking.getCheckInDate()).append('\n')
                .append("Check-out: ").append(booking.getCheckOutDate()).append('\n');
        List<String> foodItems = booking.getFoodItems();
        if (booking.isFoodRequested() && !foodItems.isEmpty()) {
            body.append("Food requested: ").append(String.join(", ", foodItems)).append('\n');
        }
        if (!receptionPhone.isBlank()) {
            body.append("\nFor assistance, call reception at ").append(receptionPhone).append(".\n");
        }
        return body.append("\nWarm regards,\nWhispering Pines Lodge\n").toString();
    }

    private record MailSettings(String host, int port, String username, String password,
                                String fromAddress, String fromName, boolean startTls) {
        private static MailSettings fromEnvironment() {
            String host = environment("HOTEL_SMTP_HOST");
            String portValue = environment("HOTEL_SMTP_PORT");
            int port;
            try {
                port = portValue.isEmpty() ? 587 : Integer.parseInt(portValue);
            } catch (NumberFormatException exception) {
                port = -1;
            }
            String username = environment("HOTEL_SMTP_USERNAME");
            String password = environment("HOTEL_SMTP_PASSWORD");
            String fromAddress = environment("HOTEL_MAIL_FROM");
            String fromName = environment("HOTEL_MAIL_FROM_NAME");
            boolean startTls = !"false".equalsIgnoreCase(environment("HOTEL_SMTP_STARTTLS"));
            return new MailSettings(host, port, username, password, fromAddress,
                    fromName.isEmpty() ? DEFAULT_SENDER_NAME : fromName, startTls);
        }

        private boolean isConfigured() {
            return !host.isEmpty() && port > 0 && port <= 65535
                    && !fromAddress.isEmpty()
                    && (username.isEmpty() == password.isEmpty());
        }

        private static String environment(String name) {
            String value = System.getenv(name);
            return value == null ? "" : value.trim();
        }
    }
}
