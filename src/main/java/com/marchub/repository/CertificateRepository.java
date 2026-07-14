package com.marchub.repository;

import com.marchub.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    List<Certificate> findByEmail(String email);
    Optional<Certificate> findByEmailAndCourse(String email, String course);
    Optional<Certificate> findByCertificateId(String certificateId);
}
