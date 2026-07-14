package com.marchub.controller;

import com.marchub.dto.AdminActionRequest;
import com.marchub.model.Certificate;
import com.marchub.service.MarchubService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/certificates")
public class CertificateController {

    private final MarchubService service;

    public CertificateController(MarchubService service) {
        this.service = service;
    }

    @GetMapping("/user/{email}")
    public List<Certificate> getUserCertificates(@PathVariable String email) {
        return service.getUserCertificates(email);
    }

    @GetMapping
    public List<Certificate> getAll() {
        return service.getAllCertificates();
    }

    @PostMapping("/verify")
    public Map<String, Object> verify(@RequestBody AdminActionRequest req) {
        return service.verifyCertificate(req);
    }
}
