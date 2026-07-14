package com.marchub.repository;

import com.marchub.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByEmail(String email);
    Optional<Enrollment> findByEmailAndCourse(String email, String course);
    boolean existsByEmailAndCourse(String email, String course);
}
