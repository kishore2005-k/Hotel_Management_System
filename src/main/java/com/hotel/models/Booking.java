package com.hotel.models;

import java.time.LocalDate;
import java.util.List;

public class Booking {
    private long id;
    private long userId;
    private long roomId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String status;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String specialRequests;
    private boolean foodRequested;
    private List<String> foodItems = List.of();
    private String roomNumber;
    private String roomType;
    private java.math.BigDecimal nightlyRate;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getSpecialRequests() {
        return specialRequests;
    }

    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests;
    }

    public boolean isFoodRequested() {
        return foodRequested;
    }

    public void setFoodRequested(boolean foodRequested) {
        this.foodRequested = foodRequested;
    }

    public List<String> getFoodItems() {
        return foodItems;
    }

    public void setFoodItems(List<String> foodItems) {
        this.foodItems = List.copyOf(foodItems);
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public java.math.BigDecimal getNightlyRate() {
        return nightlyRate;
    }

    public void setNightlyRate(java.math.BigDecimal nightlyRate) {
        this.nightlyRate = nightlyRate;
    }
}
