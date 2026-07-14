package com.marchub.controller;

import com.marchub.dto.DeleteEnrollmentRequest;
import com.marchub.dto.EnrollRequest;
import com.marchub.model.Enrollment;
import com.marchub.service.MarchubService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class EnrollmentController {

    private final MarchubService service;

    public EnrollmentController(MarchubService service) {
        this.service = service;
    }

    @PostMapping("/enroll")
    public ResponseEntity<Map<String, Object>> enroll(@RequestBody EnrollRequest req) {
        Map<String, Object> result = service.enroll(req);
        if (!(boolean) result.get("success")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/enrollments/{email}")
    public List<Enrollment> getEnrollments(@PathVariable String email) {
        return service.getEnrollments(email);
    }

    @GetMapping("/all-enrollments")
    public List<Enrollment> getAllEnrollments() {
        return service.getAllEnrollments();
    }

    @PostMapping("/delete-enrollment")
    public Map<String, Object> deleteEnrollment(@RequestBody DeleteEnrollmentRequest req) {
        return service.deleteEnrollment(req.getEmail(), req.getCourse());
    }
}
