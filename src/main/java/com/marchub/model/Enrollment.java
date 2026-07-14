package com.marchub.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments")
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    private String phone;

    @Column(nullable = false)
    private String course;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "enrolled_at")
    private LocalDateTime enrolledAt;

    private int progress;

    @Column(name = "user_id")
    private String userId;

    @PrePersist
    protected void onCreate() {
        enrolledAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public LocalDateTime getEnrolledAt() { return enrolledAt; }
    public void setEnrolledAt(LocalDateTime enrolledAt) { this.enrolledAt = enrolledAt; }
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
