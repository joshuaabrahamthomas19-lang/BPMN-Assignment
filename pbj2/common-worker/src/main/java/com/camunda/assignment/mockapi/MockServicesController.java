package com.camunda.assignment.mockapi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class MockServicesController {

    @Value("${simulation.fail-credit-api:false}")
    private boolean failCreditApi;

    @Value("${simulation.fail-payment-api:false}")
    private boolean failPaymentApi;

    @Value("${simulation.fail-hospital-api:false}")
    private boolean failHospitalApi;

    // ==========================================
    // LAB 1: Credit Assessment API
    // ==========================================
    @PostMapping("/credit-assessment")
    public ResponseEntity<Map<String, Object>> evaluateCredit(@RequestBody Map<String, Object> request) {
        if (failCreditApi || Boolean.TRUE.equals(request.get("simulateError"))) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "ERROR", "message", "Credit Assessment Service Unavailable (Timeout / Failure)"));
        }

        String applicantId = (String) request.getOrDefault("applicantId", "APP-UNKNOWN");
        Number monthlyIncomeNum = (Number) request.getOrDefault("monthlyIncome", 3000.0);
        double monthlyIncome = monthlyIncomeNum.doubleValue();

        int creditScore;
        String creditTier;

        if (monthlyIncome >= 5500) {
            creditScore = 770;
            creditTier = "PRIME";
        } else if (monthlyIncome >= 3500) {
            creditScore = 690;
            creditTier = "STANDARD";
        } else {
            creditScore = 550;
            creditTier = "SUBPRIME";
        }

        // Allow explicit override if passed in payload
        if (request.containsKey("customCreditScore")) {
            creditScore = ((Number) request.get("customCreditScore")).intValue();
            creditTier = creditScore >= 740 ? "PRIME" : (creditScore >= 650 ? "STANDARD" : "SUBPRIME");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("applicantId", applicantId);
        response.put("creditScore", creditScore);
        response.put("creditTier", creditTier);
        response.put("creditReportId", "CR-" + System.currentTimeMillis() % 100000);
        response.put("status", "SUCCESS");

        return ResponseEntity.ok(response);
    }

    // ==========================================
    // LAB 2: E-Commerce Payment Gateway API
    // ==========================================
    @PostMapping("/payment")
    public ResponseEntity<Map<String, Object>> processPayment(@RequestBody Map<String, Object> request) {
        if (failPaymentApi || Boolean.TRUE.equals(request.get("simulateError"))) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("status", "ERROR", "message", "Payment Gateway Unavailable"));
        }

        String orderId = (String) request.getOrDefault("orderId", "ORD-UNKNOWN");
        String paymentToken = (String) request.getOrDefault("paymentToken", "tok_synthetic_success");

        Map<String, Object> response = new HashMap<>();
        response.put("orderId", orderId);

        if ("tok_declined".equalsIgnoreCase(paymentToken) || "tok_failed".equalsIgnoreCase(paymentToken)) {
            response.put("paymentStatus", "DECLINED");
            response.put("transactionId", null);
            response.put("message", "Insufficient funds or synthetic card declined");
        } else {
            response.put("paymentStatus", "SUCCESS");
            response.put("transactionId", "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            response.put("message", "Transaction completed successfully");
        }

        return ResponseEntity.ok(response);
    }

    // ==========================================
    // LAB 3: Hospital Scheduling API
    // ==========================================
    @PostMapping("/hospital-slots")
    public ResponseEntity<Map<String, Object>> getAvailableSlots(@RequestBody Map<String, Object> request) {
        if (failHospitalApi || Boolean.TRUE.equals(request.get("simulateError"))) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "ERROR", "message", "Hospital Scheduling Database Disconnected"));
        }

        String patientRef = (String) request.getOrDefault("patientRef", "PAT-000");
        String preference = (String) request.getOrDefault("appointmentPreference", "ANY");

        Map<String, Object> response = new HashMap<>();
        response.put("patientRef", patientRef);

        if ("NONE_AVAILABLE".equalsIgnoreCase(preference)) {
            response.put("hasAvailableSlot", false);
            response.put("availableSlots", Collections.emptyList());
            response.put("assignedSlot", null);
            response.put("message", "No matching clinical slots found");
        } else {
            List<String> slots = List.of("2030-02-04T10:00:00Z", "2030-02-04T15:00:00Z", "2030-02-05T11:30:00Z");
            response.put("hasAvailableSlot", true);
            response.put("availableSlots", slots);
            response.put("assignedSlot", slots.get(0));
            response.put("message", "Slot allocated");
        }

        return ResponseEntity.ok(response);
    }
}
