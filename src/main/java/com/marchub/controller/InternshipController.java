package com.marchub.controller;

import com.marchub.dto.InternshipRegisterRequest;
import com.marchub.dto.InternshipRequest;
import com.marchub.dto.AdminActionRequest;
import com.marchub.model.Internship;
import com.marchub.model.InternshipRegistration;
import com.marchub.service.MarchubService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internships")
public class InternshipController {

    private final MarchubService service;

    public InternshipController(MarchubService service) {
        this.service = service;
    }

    @GetMapping("/active")
    public List<Internship> getActive() {
        return service.getActiveInternships();
    }

    @GetMapping
    public List<Internship> getAll() {
        return service.getAllInternships();
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody InternshipRequest req) {
        return service.createInternship(req);
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody InternshipRequest req) {
        return service.updateInternship(id, req);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        return service.deleteInternship(id);
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody InternshipRegisterRequest req) {
        return service.registerForInternship(req);
    }

    @GetMapping("/registrations/{internshipId}")
    public List<InternshipRegistration> getRegistrations(@PathVariable Long internshipId) {
        return service.getInternshipRegistrations(internshipId);
    }

    @GetMapping("/all-registrations")
    public List<InternshipRegistration> getAllRegistrations() {
        return service.getAllRegistrations();
    }

    @PostMapping("/update-status")
    public Map<String, Object> updateStatus(@RequestBody AdminActionRequest req) {
        return service.updateRegistrationStatus(req);
    }
}
