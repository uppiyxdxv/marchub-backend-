package com.marchub.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "certificates")
public class Certificate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String course;

    @Column(name = "issue_date")
    private LocalDateTime issueDate;

    private boolean adminVerified;

    @Column(columnDefinition = "TEXT")
    private String pdfData;

    @Column(name = "certificate_id", unique = true)
    private String certificateId;

    @PrePersist
    protected void onCreate() {
        issueDate = LocalDateTime.now();
        if (certificateId == null) {
            certificateId = "CERT-" + System.currentTimeMillis();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }
    public LocalDateTime getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDateTime issueDate) { this.issueDate = issueDate; }
    public boolean isAdminVerified() { return adminVerified; }
    public void setAdminVerified(boolean adminVerified) { this.adminVerified = adminVerified; }
    public String getPdfData() { return pdfData; }
    public void setPdfData(String pdfData) { this.pdfData = pdfData; }
    public String getCertificateId() { return certificateId; }
    public void setCertificateId(String certificateId) { this.certificateId = certificateId; }
}
