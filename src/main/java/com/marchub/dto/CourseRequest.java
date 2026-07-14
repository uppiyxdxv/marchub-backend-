package com.marchub.dto;

public class CourseRequest {
    private String name;
    private String description;
    private double price;
    private int slotsLeft;
    private String dueDate;
    private String nextBatchDate;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getSlotsLeft() { return slotsLeft; }
    public void setSlotsLeft(int slotsLeft) { this.slotsLeft = slotsLeft; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public String getNextBatchDate() { return nextBatchDate; }
    public void setNextBatchDate(String nextBatchDate) { this.nextBatchDate = nextBatchDate; }
}
