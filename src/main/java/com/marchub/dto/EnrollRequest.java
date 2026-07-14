package com.marchub.dto;

public class EnrollRequest {
    private String name;
    private String email;
    private String phone;
    private String course;
    private String message;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
