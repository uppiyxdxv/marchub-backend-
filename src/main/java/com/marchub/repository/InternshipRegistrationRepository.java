package com.marchub.repository;

import com.marchub.model.InternshipRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InternshipRegistrationRepository extends JpaRepository<InternshipRegistration, Long> {
    List<InternshipRegistration> findByInternshipId(Long internshipId);
    List<InternshipRegistration> findByEmail(String email);
    List<InternshipRegistration> findByStatus(InternshipRegistration.Status status);
}
