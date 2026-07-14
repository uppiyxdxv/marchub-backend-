package com.marchub.service;

import com.marchub.dto.*;
import com.marchub.model.*;
import com.marchub.repository.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class MarchubService {

    private static final String ADMIN_EMAIL = "marchub2026@gmail.com";
    private static final String ADMIN_PASS = "Uppiyxdxv@2004";
    private static final Map<String, OtpEntry> otpStore = new java.util.concurrent.ConcurrentHashMap<>();
    private static final long OTP_EXPIRY_MS = 300_000; // 5 min

    private static class OtpEntry {
        final String code;
        final long expiresAt;
        OtpEntry(String code, long expiresAt) { this.code = code; this.expiresAt = expiresAt; }
    }

    private final UserRepository userRepo;
    private final EnrollmentRepository enrollRepo;
    private final CourseRepository courseRepo;
    private final CertificateRepository certRepo;
    private final InternshipRepository internRepo;
    private final InternshipRegistrationRepository internRegRepo;
    private final JavaMailSender mailSender;

    public MarchubService(UserRepository userRepo, EnrollmentRepository enrollRepo,
                          CourseRepository courseRepo, CertificateRepository certRepo,
                          InternshipRepository internRepo,
                          InternshipRegistrationRepository internRegRepo,
                          JavaMailSender mailSender) {
        this.userRepo = userRepo;
        this.enrollRepo = enrollRepo;
        this.courseRepo = courseRepo;
        this.certRepo = certRepo;
        this.internRepo = internRepo;
        this.internRegRepo = internRegRepo;
        this.mailSender = mailSender;
    }

    // ─── Auth ───

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

    // ─── Enrollment ───

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

        if (courseRepo.findByName(req.getCourse()).isPresent()) {
            Course c = courseRepo.findByName(req.getCourse()).get();
            if (c.getSlotsLeft() > 0) c.setSlotsLeft(c.getSlotsLeft() - 1);
            courseRepo.save(c);
        }

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

    public Map<String, Object> sendOtp(SendOtpRequest req) {
        Map<String, Object> res = new HashMap<>();
        String email = req.getEmail();
        if (ADMIN_EMAIL.equals(email)) {
            res.put("success", false);
            res.put("message", "Admin OTP not supported");
            return res;
        }
        Optional<User> opt = userRepo.findByEmail(email);
        if (opt.isEmpty()) {
            res.put("success", false);
            res.put("message", "No account with that email");
            return res;
        }
        String code = String.format("%06d", new Random().nextInt(999999));
        otpStore.put(email, new OtpEntry(code, System.currentTimeMillis() + OTP_EXPIRY_MS));
        // Send email synchronously and report failure
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true);
            helper.setTo(email);
            helper.setSubject("MarcHub – Password Reset OTP");
            String html = "<div style='font-family:sans-serif;max-width:480px;margin:auto;padding:2rem;border:1px solid #e5e7eb;border-radius:12px;'>"
                + "<h2 style='color:#7c3aed;'>MarcHub</h2>"
                + "<p>Your OTP for password reset:</p>"
                + "<div style='font-size:2rem;font-weight:800;letter-spacing:.2em;text-align:center;padding:1rem;background:#f5f3ff;border-radius:8px;color:#7c3aed;'>" + code + "</div>"
                + "<p style='color:#888;font-size:.85rem;'>This code expires in 5 minutes.</p>"
                + "<hr style='border:none;border-top:1px solid #e5e7eb;margin:1.5rem 0;'/>"
                + "<p style='color:#888;font-size:.78rem;'>If you didn't request this, ignore this email.</p></div>";
            helper.setText(html, true);
            helper.setFrom("marchub2026@gmail.com");
            mailSender.send(msg);
            res.put("success", true);
            res.put("message", "OTP sent to your email");
            res.put("otp", code);
        } catch (Exception e) {
            res.put("success", true); // still allow reset
            res.put("message", "Your OTP: " + code);
            res.put("otp", code);
            res.put("emailFailed", true);
        }
        return res;
    }

    public Map<String, Object> verifyOtp(VerifyOtpRequest req) {
        Map<String, Object> res = new HashMap<>();
        OtpEntry entry = otpStore.get(req.getEmail());
        if (entry == null) {
            res.put("success", false);
            res.put("message", "No OTP requested for this email");
            return res;
        }
        if (System.currentTimeMillis() > entry.expiresAt) {
            otpStore.remove(req.getEmail());
            res.put("success", false);
            res.put("message", "OTP expired. Request a new one.");
            return res;
        }
        if (!entry.code.equals(req.getOtp())) {
            res.put("success", false);
            res.put("message", "Incorrect OTP");
            return res;
        }
        otpStore.remove(req.getEmail());
        res.put("success", true);
        res.put("message", "OTP verified");
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

    // ─── Courses ───

    public List<Course> getAllCourses() {
        return courseRepo.findAll();
    }

    public Course getCourse(Long id) {
        return courseRepo.findById(id).orElse(null);
    }

    public Map<String, Object> createCourse(CourseRequest req) {
        Map<String, Object> res = new HashMap<>();
        if (courseRepo.findByName(req.getName()).isPresent()) {
            res.put("success", false);
            res.put("message", "Course already exists");
            return res;
        }
        Course c = new Course();
        c.setName(req.getName());
        c.setDescription(req.getDescription());
        c.setPrice(req.getPrice());
        c.setSlotsLeft(req.getSlotsLeft());
        if (req.getDueDate() != null) c.setDueDate(LocalDate.parse(req.getDueDate()));
        if (req.getNextBatchDate() != null) c.setNextBatchDate(LocalDate.parse(req.getNextBatchDate()));
        courseRepo.save(c);
        res.put("success", true);
        res.put("course", c);
        return res;
    }

    public Map<String, Object> updateCourse(Long id, CourseRequest req) {
        Map<String, Object> res = new HashMap<>();
        Optional<Course> opt = courseRepo.findById(id);
        if (opt.isEmpty()) {
            res.put("success", false);
            res.put("message", "Course not found");
            return res;
        }
        Course c = opt.get();
        if (req.getName() != null) c.setName(req.getName());
        if (req.getDescription() != null) c.setDescription(req.getDescription());
        c.setPrice(req.getPrice());
        c.setSlotsLeft(req.getSlotsLeft());
        if (req.getDueDate() != null) c.setDueDate(LocalDate.parse(req.getDueDate()));
        if (req.getNextBatchDate() != null) c.setNextBatchDate(LocalDate.parse(req.getNextBatchDate()));
        courseRepo.save(c);
        res.put("success", true);
        res.put("course", c);
        return res;
    }

    public Map<String, Object> deleteCourse(Long id) {
        Map<String, Object> res = new HashMap<>();
        courseRepo.deleteById(id);
        res.put("success", true);
        return res;
    }

    // ─── Certificates ───

    public List<Certificate> getUserCertificates(String email) {
        return certRepo.findByEmail(email);
    }

    public Map<String, Object> verifyCertificate(AdminActionRequest req) {
        Map<String, Object> res = new HashMap<>();
        Optional<Certificate> opt = certRepo.findByEmailAndCourse(req.getEmail(), req.getCourse());
        if (opt.isEmpty()) {
            Certificate c = new Certificate();
            c.setEmail(req.getEmail());
            c.setName("");
            c.setCourse(req.getCourse());
            c.setAdminVerified(true);
            certRepo.save(c);
            res.put("success", true);
            res.put("message", "Certificate issued");
        } else {
            Certificate c = opt.get();
            c.setAdminVerified(true);
            certRepo.save(c);
            res.put("success", true);
            res.put("message", "Certificate verified");
        }
        String html = """
            <div style="font-family:Arial,sans-serif;max-width:500px;margin:auto;border:2px solid #7c3aed;border-radius:12px;padding:2rem;text-align:center;">
              <h1 style="color:#7c3aed;">🎉 Certificate Issued!</h1>
              <p>Your <strong>%s</strong> certificate from <strong>MarcHub</strong> has been verified and issued.</p>
              <p>Login to your dashboard to view and download: <a href="https://marchub-backend-wexu.onrender.com">MarcHub Dashboard</a></p>
              <hr style="border:none;border-top:1px solid #e5e7eb;margin:1.5rem 0;">
              <p style="font-size:.8rem;color:#888;">— MarcHub Team</p>
            </div>
            """.formatted(req.getCourse());
        sendEmail(req.getEmail(), "MarcHub – Certificate Issued: " + req.getCourse(), html);
        return res;
    }

    public List<Certificate> getAllCertificates() {
        return certRepo.findAll();
    }

    // ─── Internships ───

    public List<Internship> getActiveInternships() {
        return internRepo.findByActiveTrue();
    }

    public List<Internship> getAllInternships() {
        return internRepo.findAll();
    }

    public Map<String, Object> createInternship(InternshipRequest req) {
        Map<String, Object> res = new HashMap<>();
        Internship i = new Internship();
        i.setTitle(req.getTitle());
        i.setDescription(req.getDescription());
        i.setRequirements(req.getRequirements());
        internRepo.save(i);
        res.put("success", true);
        res.put("internship", i);
        return res;
    }

    public Map<String, Object> updateInternship(Long id, InternshipRequest req) {
        Map<String, Object> res = new HashMap<>();
        Optional<Internship> opt = internRepo.findById(id);
        if (opt.isEmpty()) {
            res.put("success", false);
            res.put("message", "Internship not found");
            return res;
        }
        Internship i = opt.get();
        if (req.getTitle() != null) i.setTitle(req.getTitle());
        if (req.getDescription() != null) i.setDescription(req.getDescription());
        if (req.getRequirements() != null) i.setRequirements(req.getRequirements());
        if (req.getActive() != null) i.setActive(req.getActive());
        internRepo.save(i);
        res.put("success", true);
        res.put("internship", i);
        return res;
    }

    public Map<String, Object> deleteInternship(Long id) {
        Map<String, Object> res = new HashMap<>();
        internRepo.deleteById(id);
        res.put("success", true);
        return res;
    }

    public Map<String, Object> registerForInternship(InternshipRegisterRequest req) {
        Map<String, Object> res = new HashMap<>();
        InternshipRegistration reg = new InternshipRegistration();
        reg.setInternshipId(req.getInternshipId());
        reg.setName(req.getName());
        reg.setEmail(req.getEmail());
        reg.setPhone(req.getPhone());
        reg.setMessage(req.getMessage());
        internRegRepo.save(reg);
        res.put("success", true);
        res.put("message", "Registration submitted");
        return res;
    }

    public List<InternshipRegistration> getInternshipRegistrations(Long internshipId) {
        return internRegRepo.findByInternshipId(internshipId);
    }

    public List<InternshipRegistration> getAllRegistrations() {
        return internRegRepo.findAll();
    }

    public Map<String, Object> updateRegistrationStatus(AdminActionRequest req) {
        Map<String, Object> res = new HashMap<>();
        Optional<InternshipRegistration> opt = internRegRepo.findById(req.getId());
        if (opt.isEmpty()) {
            res.put("success", false);
            res.put("message", "Registration not found");
            return res;
        }
        InternshipRegistration reg = opt.get();
        String subject = "";
        String html = "";
        switch (req.getAction()) {
            case "approve" -> {
                reg.setStatus(InternshipRegistration.Status.APPROVED);
                reg.setOfferLetterUrl("https://marchub-backend-wexu.onrender.com/offer-letter/" + reg.getId());
                subject = "MarcHub – Internship Approved!";
                html = """
                    <div style="font-family:Arial,sans-serif;max-width:500px;margin:auto;border:2px solid #10b981;border-radius:12px;padding:2rem;text-align:center;">
                      <h1 style="color:#10b981;">✅ Approved!</h1>
                      <p>Congratulations <strong>%s</strong>, your internship application has been <strong>approved</strong>!</p>
                      <p>Check your offer letter: <a href="%s">Download Offer Letter</a></p>
                      <hr style="border:none;border-top:1px solid #e5e7eb;margin:1.5rem 0;">
                      <p style="font-size:.8rem;color:#888;">— MarcHub Team</p>
                    </div>
                    """.formatted(reg.getName(), reg.getOfferLetterUrl());
            }
            case "reject" -> {
                reg.setStatus(InternshipRegistration.Status.REJECTED);
                subject = "MarcHub – Internship Update";
                html = """
                    <div style="font-family:Arial,sans-serif;max-width:500px;margin:auto;border:2px solid #ef4444;border-radius:12px;padding:2rem;text-align:center;">
                      <h1 style="color:#ef4444;">😔 Application Status</h1>
                      <p>Dear <strong>%s</strong>, we regret to inform you that your internship application has not been selected at this time.</p>
                      <p>Keep learning and try again!</p>
                      <hr style="border:none;border-top:1px solid #e5e7eb;margin:1.5rem 0;">
                      <p style="font-size:.8rem;color:#888;">— MarcHub Team</p>
                    </div>
                    """.formatted(reg.getName());
            }
            case "complete" -> {
                reg.setStatus(InternshipRegistration.Status.COMPLETED);
                reg.setTasksCompleted(true);
                reg.setInternshipCertificateId("INT-CERT-" + System.currentTimeMillis());
                subject = "MarcHub – Internship Completed!";
                html = """
                    <div style="font-family:Arial,sans-serif;max-width:500px;margin:auto;border:2px solid #7c3aed;border-radius:12px;padding:2rem;text-align:center;">
                      <h1 style="color:#7c3aed;">🎉 Internship Complete!</h1>
                      <p>Congratulations <strong>%s</strong>, you have successfully completed your internship!</p>
                      <p>Your certificate ID: <code style="background:#f3f4f6;padding:.2rem .6rem;border-radius:4px;">%s</code></p>
                      <hr style="border:none;border-top:1px solid #e5e7eb;margin:1.5rem 0;">
                      <p style="font-size:.8rem;color:#888;">— MarcHub Team</p>
                    </div>
                    """.formatted(reg.getName(), reg.getInternshipCertificateId());
            }
        }
        internRegRepo.save(reg);
        sendEmail(reg.getEmail(), subject, html);
        res.put("success", true);
        res.put("registration", reg);
        return res;
    }

    private void sendEmail(String to, String subject, String html) {
        new Thread(() -> {
            try {
                MimeMessage msg = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(msg, true);
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(html, true);
                helper.setFrom("marchub2026@gmail.com");
                mailSender.send(msg);
                System.out.println("Email sent to " + to + " | subject: " + subject);
            } catch (Exception e) {
                System.err.println("Failed to send email to " + to + ": " + e.getMessage());
            }
        }).start();
    }
}
