package com.marchub.controller;

import com.marchub.dto.RazorpayOrderRequest;
import com.marchub.dto.RazorpayVerifyRequest;
import com.marchub.model.Payment;
import com.marchub.model.InternshipRegistration;
import com.marchub.repository.PaymentRepository;
import com.marchub.repository.InternshipRegistrationRepository;
import com.marchub.service.MarchubService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final RazorpayClient razorpayClient;
    private final PaymentRepository paymentRepository;
    private final InternshipRegistrationRepository internRegRepo;
    private final MarchubService marchubService;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpaySecret;

    public PaymentController(RazorpayClient razorpayClient,
                             PaymentRepository paymentRepository,
                             InternshipRegistrationRepository internRegRepo,
                             MarchubService marchubService) {
        this.razorpayClient = razorpayClient;
        this.paymentRepository = paymentRepository;
        this.internRegRepo = internRegRepo;
        this.marchubService = marchubService;
    }

    @PostMapping("/create-order")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody RazorpayOrderRequest req) {
        Map<String, Object> res = new HashMap<>();
        try {
            if (req.getRegistrationId() == null) {
                res.put("success", false);
                res.put("error", "registrationId is required");
                return ResponseEntity.badRequest().body(res);
            }

            InternshipRegistration reg = internRegRepo.findById(req.getRegistrationId()).orElse(null);
            if (reg == null) {
                res.put("success", false);
                res.put("error", "Registration not found");
                return ResponseEntity.badRequest().body(res);
            }

            Integer amount = req.getAmount() != null ? req.getAmount() : 100;
            org.json.JSONObject orderRequest = new org.json.JSONObject()
                    .put("amount", amount)
                    .put("currency", "INR")
                    .put("receipt", "reg_" + req.getRegistrationId() + "_" + System.currentTimeMillis())
                    .put("notes", new org.json.JSONObject()
                            .put("registration_id", req.getRegistrationId().toString())
                            .put("user_email", req.getUserEmail() != null ? req.getUserEmail() : reg.getEmail()));

            Order order = razorpayClient.Orders.create(orderRequest);
            String orderId = order.get("id");

            Payment payment = new Payment();
            payment.setRegistrationId(req.getRegistrationId());
            payment.setRazorpayOrderId(orderId);
            payment.setAmount(amount);
            payment.setCurrency("INR");
            payment.setStatus(Payment.PaymentStatus.PENDING);
            paymentRepository.save(payment);

            res.put("success", true);
            res.put("order_id", orderId);
            res.put("amount", amount);
            res.put("currency", "INR");
            res.put("key_id", razorpayKeyId);
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            res.put("success", false);
            res.put("error", e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyPayment(@RequestBody RazorpayVerifyRequest req) {
        Map<String, Object> res = new HashMap<>();
        try {
            System.out.println("=== PAYMENT VERIFICATION DEBUG ===");
            System.out.println("Order ID: " + req.getRazorpayOrderId());
            System.out.println("Payment ID: " + req.getRazorpayPaymentId());
            System.out.println("Signature: " + req.getRazorpaySignature());
            System.out.println("Secret configured: " + (razorpaySecret != null && !razorpaySecret.isEmpty() ? "YES (length: " + razorpaySecret.length() + ")" : "NO"));
            
            String sign = req.getRazorpayOrderId() + "|" + req.getRazorpayPaymentId();
            System.out.println("Sign string: " + sign);
            
            Mac sha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(razorpaySecret.getBytes(), "HmacSHA256");
            sha256.init(keySpec);
            byte[] hash = sha256.doFinal(sign.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            String calculatedSignature = hexString.toString();
            System.out.println("Calculated signature: " + calculatedSignature);
            System.out.println("Received signature: " + req.getRazorpaySignature());
            System.out.println("Match: " + calculatedSignature.equals(req.getRazorpaySignature()));

            if (!calculatedSignature.equals(req.getRazorpaySignature())) {
                res.put("success", false);
                res.put("error", "Invalid signature");
                return ResponseEntity.badRequest().body(res);
            }

            Optional<Payment> optPayment = paymentRepository.findByRazorpayOrderId(req.getRazorpayOrderId());
            if (optPayment.isEmpty()) {
                res.put("success", false);
                res.put("error", "Payment record not found");
                return ResponseEntity.badRequest().body(res);
            }

            Payment payment = optPayment.get();
            payment.setRazorpayPaymentId(req.getRazorpayPaymentId());
            payment.setRazorpaySignature(req.getRazorpaySignature());
            payment.setStatus(Payment.PaymentStatus.VERIFIED);
            payment.setVerifiedAt(java.time.LocalDateTime.now());
            paymentRepository.save(payment);

            // Update internship registration status
            InternshipRegistration reg = internRegRepo.findById(payment.getRegistrationId()).orElse(null);
            if (reg != null) {
                reg.setStatus(InternshipRegistration.Status.COMPLETED);
                reg.setTasksCompleted(true);
                if (reg.getInternshipCertificateId() == null) {
                    reg.setInternshipCertificateId("INT-CERT-" + System.currentTimeMillis());
                }
                internRegRepo.save(reg);
            }

            // Send completion email using reflection to call private method
            if (reg != null) {
                try {
                    java.lang.reflect.Method sendEmailMethod = MarchubService.class.getDeclaredMethod("sendEmail", String.class, String.class, String.class);
                    sendEmailMethod.setAccessible(true);
                    sendEmailMethod.invoke(marchubService, reg.getEmail(), "MarcHub – Payment Verified & Certificate Ready",
                        String.format("""
                            <div style="font-family:Arial,sans-serif;max-width:500px;margin:auto;border:2px solid #10b981;border-radius:12px;padding:2rem;text-align:center;">
                              <h1 style="color:#10b981;">✅ Payment Verified!</h1>
                              <p>Hi <strong>%s</strong>, your payment of ₹%d has been verified.</p>
                              <p>Your internship certificate is now ready. Certificate ID: <code style="background:#f3f4f6;padding:.2rem .6rem;border-radius:4px;">%s</code></p>
                              <p>Login to your dashboard to download it.</p>
                              <hr style="border:none;border-top:1px solid #e5e7eb;margin:1.5rem 0;">
                              <p style="font-size:.8rem;color:#888;">— MarcHub Team</p>
                            </div>
                            """, reg.getName(), payment.getAmount() / 100, reg.getInternshipCertificateId())
                    );
                } catch (Exception ignore) {}
            }

            res.put("success", true);
            res.put("message", "Payment verified successfully");
            res.put("payment_id", req.getRazorpayPaymentId());
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            res.put("success", false);
            res.put("error", "Verification failed: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    @GetMapping("/status/{registrationId}")
    public ResponseEntity<Map<String, Object>> getPaymentStatus(@PathVariable Long registrationId) {
        Map<String, Object> res = new HashMap<>();
        var payments = paymentRepository.findByRegistrationId(registrationId);
        if (payments.isEmpty()) {
            res.put("paid", false);
            return ResponseEntity.ok(res);
        }
        Payment payment = payments.get(payments.size() - 1); // latest
        res.put("paid", payment.getStatus() == Payment.PaymentStatus.VERIFIED);
        res.put("amount", payment.getAmount());
        res.put("currency", payment.getCurrency());
        res.put("status", payment.getStatus().toString());
        res.put("razorpay_payment_id", payment.getRazorpayPaymentId());
        res.put("verified_at", payment.getVerifiedAt());
        return ResponseEntity.ok(res);
    }
}