package com.marchub.dto;

public class RazorpayOrderRequest {
    private Integer amount;
    private Long registrationId;
    private String userEmail;

    public RazorpayOrderRequest() {}

    public RazorpayOrderRequest(Integer amount, Long registrationId, String userEmail) {
        this.amount = amount;
        this.registrationId = registrationId;
        this.userEmail = userEmail;
    }

    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }
    public Long getRegistrationId() { return registrationId; }
    public void setRegistrationId(Long registrationId) { this.registrationId = registrationId; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
}