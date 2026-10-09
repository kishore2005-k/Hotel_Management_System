package com.hotel.services;

import com.hotel.config.DatabaseConfig;
import com.hotel.models.Booking;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class BookingConfirmationSmsService {
    private static final String DEFAULT_SENDER_NAME = "Whispering Pines Lodge";
    private static final Logger LOGGER = LogManager.getLogger(BookingConfirmationSmsService.class);

    public boolean isConfigured() {
        SmsSettings settings = SmsSettings.fromEnvironment();
        boolean configured = settings.isConfigured();
        LOGGER.debug("SMS service configured: {}", configured);
        if (!configured) {
            LOGGER.warn("SMS settings missing - accountSid: {}, authToken: {}, fromNumber: {}",
                    mask(settings.accountSid()), mask(settings.authToken()), mask(settings.fromNumber()));
        }
        return configured;
    }

    public void sendConfirmation(Booking booking) throws IOException, InterruptedException {
        SmsSettings settings = SmsSettings.fromEnvironment();
        if (!settings.isConfigured()) {
            throw new IllegalStateException("SMS settings are not configured.");
        }

        String toPhone = booking.getCustomerPhone();
        LOGGER.info("Sending SMS confirmation to {} for booking #{}", maskPhone(toPhone), booking.getId());

        String body = createBody(booking, DatabaseConfig.getHotelContactPhone());
        sendSms(settings, toPhone, body);
    }

    private void sendSms(SmsSettings settings, String toPhone, String body)
            throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        String url = String.format("%s/Accounts/%s/Messages.json", settings.baseUrl(), settings.accountSid());
        String auth = settings.accountSid() + ":" + settings.authToken();
        String encodedAuth = java.util.Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        String formData = String.format("From=%s&To=%s&Body=%s",
                java.net.URLEncoder.encode(settings.fromNumber(), StandardCharsets.UTF_8),
                java.net.URLEncoder.encode(toPhone, StandardCharsets.UTF_8),
                java.net.URLEncoder.encode(body, StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Basic " + encodedAuth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            LOGGER.error("Failed to send SMS: {} - {}", response.statusCode(), response.body());
            throw new IOException("Failed to send SMS: " + response.statusCode() + " " + response.body());
        }
        LOGGER.info("SMS sent successfully to {}", maskPhone(toPhone));
    }

    private static String createBody(Booking booking, String receptionPhone) {
        StringBuilder body = new StringBuilder()
                .append("Booking Confirmed! ")
                .append("Confirmation: #").append(booking.getId()).append(". ")
                .append(booking.getRoomType()).append(" (").append(booking.getRoomNumber()).append("). ")
                .append("Check-in: ").append(booking.getCheckInDate()).append(", Check-out: ").append(booking.getCheckOutDate()).append(". ");

        List<String> foodItems = booking.getFoodItems();
        if (booking.isFoodRequested() && !foodItems.isEmpty()) {
            body.append("Food: ").append(String.join(", ", foodItems)).append(". ");
        }

        if (!receptionPhone.isBlank()) {
            body.append("Call ").append(receptionPhone).append(" for assistance. ");
        }

        body.append("- ").append(DEFAULT_SENDER_NAME);
        return body.toString();
    }

    private static String mask(String value) {
        if (value == null || value.isEmpty()) return "NOT_SET";
        if (value.length() <= 4) return "****";
        return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.isEmpty()) return "NOT_SET";
        if (phone.length() <= 4) return "****";
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 2);
    }

    private record SmsSettings(String accountSid, String authToken, String fromNumber, String baseUrl) {
        private static SmsSettings fromEnvironment() {
            String accountSid = environment("HOTEL_SMS_ACCOUNT_SID");
            String authToken = environment("HOTEL_SMS_AUTH_TOKEN");
            String fromNumber = environment("HOTEL_SMS_FROM_NUMBER");
            String baseUrl = environment("HOTEL_SMS_BASE_URL");
            if (baseUrl.isEmpty()) {
                baseUrl = "https://api.twilio.com/2010-04-01";
            }
            return new SmsSettings(accountSid, authToken, fromNumber, baseUrl);
        }

        private boolean isConfigured() {
            return !accountSid.isEmpty() && !authToken.isEmpty() && !fromNumber.isEmpty();
        }

        private static String environment(String name) {
            String value = System.getenv(name);
            return value == null ? "" : value.trim();
        }
    }
}