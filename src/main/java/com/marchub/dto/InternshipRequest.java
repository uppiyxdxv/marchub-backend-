package com.marchub.dto;

public class InternshipRequest {
    private String title;
    private String description;
    private String requirements;
    private Boolean active;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
