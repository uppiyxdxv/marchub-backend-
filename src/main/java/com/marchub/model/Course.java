package com.marchub.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private double price;
    private int slotsLeft;
    private LocalDate dueDate;
    private LocalDate nextBatchDate;
    private boolean active = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getSlotsLeft() { return slotsLeft; }
    public void setSlotsLeft(int slotsLeft) { this.slotsLeft = slotsLeft; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public LocalDate getNextBatchDate() { return nextBatchDate; }
    public void setNextBatchDate(LocalDate nextBatchDate) { this.nextBatchDate = nextBatchDate; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
