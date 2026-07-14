package com.marchub.service;

import com.marchub.dto.*;
import com.marchub.model.Enrollment;
import com.marchub.model.User;
import com.marchub.repository.EnrollmentRepository;
import com.marchub.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class MarchubService {

    private static final String ADMIN_EMAIL = "marchub2026@gmail.com";
    private static final String ADMIN_PASS = "Uppiyxdxv@2004";

    private final UserRepository userRepo;
    private final EnrollmentRepository enrollRepo;

    public MarchubService(UserRepository userRepo, EnrollmentRepository enrollRepo) {
        this.userRepo = userRepo;
        this.enrollRepo = enrollRepo;
    }

    public Map<String, Object> register(RegisterRequest req) {
        Map<String, Object> res = new HashMap<>();
        if (userRepo.existsByEmail(req.getEmail())) {
            res.put("success", false);
            res.put("message", "Email already registered");
            return res;
        }
        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPassword(req.getPassword());
        user.setPhone(req.getPhone() != null ? req.getPhone() : "");
        userRepo.save(user);
        res.put("success", true);
        res.put("message", "Account created successfully");
        return res;
    }

    public Map<String, Object> login(LoginRequest req) {
        Map<String, Object> res = new HashMap<>();
        if (ADMIN_EMAIL.equals(req.getEmail()) && ADMIN_PASS.equals(req.getPassword())) {
            Map<String, Object> user = new HashMap<>();
            user.put("name", "Admin");
            user.put("email", ADMIN_EMAIL);
            user.put("isAdmin", true);
            res.put("success", true);
            res.put("user", user);
            return res;
        }
        Optional<User> opt = userRepo.findByEmail(req.getEmail());
        if (opt.isEmpty() || !opt.get().getPassword().equals(req.getPassword())) {
            res.put("success", false);
            res.put("message", "Invalid Email or Password");
            return res;
        }
        User u = opt.get();
        Map<String, Object> user = new HashMap<>();
        user.put("name", u.getName());
        user.put("email", u.getEmail());
        user.put("isAdmin", false);
        res.put("success", true);
        res.put("user", user);
        return res;
    }

    public List<Map<String, Object>> getUsers() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (User u : userRepo.findAll()) {
            Map<String, Object> m = new HashMap<>();
            m.put("name", u.getName());
            m.put("email", u.getEmail());
            m.put("phone", u.getPhone() != null ? u.getPhone() : "");
            m.put("bio", u.getBio() != null ? u.getBio() : "");
            m.put("avatar", u.getAvatar() != null ? u.getAvatar() : "");
            m.put("createdAt", u.getCreatedAt() != null ? u.getCreatedAt().toString() : "");
            List<Enrollment> enrollments = enrollRepo.findByEmail(u.getEmail());
            List<Map<String, String>> courseList = new ArrayList<>();
            for (Enrollment e : enrollments) {
                Map<String, String> cm = new HashMap<>();
                cm.put("course", e.getCourse());
                cm.put("phone", e.getPhone() != null ? e.getPhone() : "");
                courseList.add(cm);
            }
            m.put("enrollments", courseList);
            list.add(m);
        }
        return list;
    }

    public Map<String, Object> verifyPassword(VerifyPasswordRequest req) {
        Map<String, Object> res = new HashMap<>();
        if (ADMIN_EMAIL.equals(req.getEmail())) {
            res.put("success", ADMIN_PASS.equals(req.getPassword()));
            return res;
        }
        Optional<User> opt = userRepo.findByEmail(req.getEmail());
        res.put("success", opt.isPresent() && opt.get().getPassword().equals(req.getPassword()));
        return res;
    }

    public Map<String, Object> updateProfile(UpdateProfileRequest req) {
        Map<String, Object> res = new HashMap<>();
        Optional<User> opt = userRepo.findByEmail(req.getEmail());
        if (opt.isEmpty()) {
            res.put("ok", false);
            res.put("msg", "User not found");
            return res;
        }
        User u = opt.get();
        if (req.getName() != null) u.setName(req.getName());
        if (req.getPhone() != null) u.setPhone(req.getPhone());
        if (req.getBio() != null) u.setBio(req.getBio());
        if (req.getAvatar() != null) u.setAvatar(req.getAvatar());
        userRepo.save(u);
        res.put("ok", true);
        res.put("msg", "Profile updated");
        return res;
    }

    public Map<String, Object> enroll(EnrollRequest req) {
        Map<String, Object> res = new HashMap<>();
        if (enrollRepo.existsByEmailAndCourse(req.getEmail(), req.getCourse())) {
            res.put("success", false);
            res.put("message", "Already enrolled");
            return res;
        }
        Enrollment e = new Enrollment();
        e.setName(req.getName());
        e.setEmail(req.getEmail());
        e.setPhone(req.getPhone());
        e.setCourse(req.getCourse());
        e.setMessage(req.getMessage());
        e.setProgress(0);
        enrollRepo.save(e);
        res.put("success", true);
        res.put("message", "Enrolled successfully");
        return res;
    }

    public Map<String, Object> enrollUser(String email, String course, String phone) {
        Map<String, Object> res = new HashMap<>();
        Optional<User> opt = userRepo.findByEmail(email);
        if (opt.isEmpty()) {
            res.put("success", false);
            res.put("message", "User not found");
            return res;
        }
        Enrollment e = new Enrollment();
        e.setName(opt.get().getName());
        e.setEmail(email);
        e.setPhone(phone);
        e.setCourse(course);
        e.setProgress(0);
        enrollRepo.save(e);
        res.put("success", true);
        return res;
    }

    public List<Enrollment> getEnrollments(String email) {
        return enrollRepo.findByEmail(email);
    }

    public List<Enrollment> getAllEnrollments() {
        return enrollRepo.findAll();
    }

    public Map<String, Object> deleteEnrollment(String email, String course) {
        Map<String, Object> res = new HashMap<>();
        enrollRepo.findByEmailAndCourse(email, course).ifPresent(enrollRepo::delete);
        res.put("success", true);
        return res;
    }

    public Map<String, Object> checkUser(CheckUserRequest req) {
        Map<String, Object> res = new HashMap<>();
        if (ADMIN_EMAIL.equals(req.getEmail()) || userRepo.existsByEmail(req.getEmail())) {
            res.put("success", true);
            res.put("message", "User found");
        } else {
            res.put("success", false);
            res.put("message", "No account with that email");
        }
        return res;
    }

    public Map<String, Object> resetPassword(ResetPasswordRequest req) {
        Map<String, Object> res = new HashMap<>();
        if (ADMIN_EMAIL.equals(req.getEmail())) {
            res.put("success", true);
            res.put("message", "Password reset successful");
            return res;
        }
        Optional<User> opt = userRepo.findByEmail(req.getEmail());
        if (opt.isEmpty()) {
            res.put("success", false);
            res.put("message", "User not found");
            return res;
        }
        User u = opt.get();
        u.setPassword(req.getNewPassword());
        userRepo.save(u);
        res.put("success", true);
        res.put("message", "Password reset successful");
        return res;
    }
}
