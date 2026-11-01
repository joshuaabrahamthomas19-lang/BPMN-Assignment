package com.camunda.assignment.worker;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.api.worker.JobClient;
import io.camunda.client.api.worker.JobHandler;
import io.camunda.client.api.worker.JobWorker;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.time.Duration;
import java.util.*;

@Component
@ConditionalOnProperty(name="assignment.workers.enabled", havingValue="true", matchIfMissing=true)
public class AssignmentWorkers {

    private static final Logger log = LoggerFactory.getLogger(AssignmentWorkers.class);


    @Value("${simulation.fail-loan-worker:false}")
    private boolean failLoanWorker;

    @Value("${simulation.fail-order-worker:false}")
    private boolean failOrderWorker;

    @Value("${simulation.fail-hospital-worker:false}")
    private boolean failHospitalWorker;

    private CamundaClient client;
    private final List<JobWorker> registeredWorkers = new ArrayList<>();

    @PostConstruct
    public void startWorkers() {
        try {
            log.info("Starting Camunda workers using the configured connection");
            client = CamundaClient.newClientBuilder().build();

            // LAB 1 Workers
            registerWorker("validate-applicant-worker", this::handleValidateApplicant);
            registerWorker("disburse-loan-worker", this::handleDisburseLoan);

            // LAB 2 Workers
            registerWorker("validate-inventory-worker", this::handleValidateInventory);
            registerWorker("fulfill-order-worker", this::handleFulfillOrder);

            // LAB 3 Workers
            registerWorker("validate-patient-worker", this::handleValidatePatient);
            registerWorker("confirm-booking-worker", this::handleConfirmBooking);

            log.info("All 6 Zeebe job workers successfully registered and listening.");
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to initialise job workers", ex);
        }
    }

    private void registerWorker(String jobType, JobHandler handler) {
        JobWorker worker = client.newWorker()
                .jobType(jobType)
                .handler((jobClient, job) -> {
                    try { handler.handle(jobClient, job); }
                    catch (Exception error) {
                        jobClient.newFailCommand(job.getKey()).retries(Math.max(0, job.getRetries() - 1))
                            .retryBackoff(Duration.ofSeconds(5)).errorMessage(error.toString()).send().join();
                    }
                })
                .name(jobType)
                .open();
        registeredWorkers.add(worker);
    }

    @PreDestroy
    public void closeWorkers() {
        for (JobWorker w : registeredWorkers) {
            w.close();
        }
        if (client != null) {
            client.close();
        }
    }

    // ----------------------------------------------------
    // LAB 1: Loan Workers
    // ----------------------------------------------------
    private void handleValidateApplicant(JobClient jobClient, ActivatedJob job) {
        log.info("Processing validate-applicant-worker for job: {}", job.getKey());
        Map<String, Object> vars = job.getVariablesAsMap();

        if (failLoanWorker || Boolean.TRUE.equals(vars.get("simulateWorkerFailure"))) {
            log.error("Simulated Failure in validate-applicant-worker!");
            throw new RuntimeException("Simulated Failure: Applicant database connection timed out.");
        }

        Number ageNum = (Number) vars.getOrDefault("applicantAge", 0);
        int age = ageNum.intValue();
        String employmentStatus = (String) vars.getOrDefault("employmentStatus", "EMPLOYED");
        boolean isValid = age >= 18 && !"UNEMPLOYED".equalsIgnoreCase(employmentStatus)
            && !String.valueOf(vars.getOrDefault("applicantId", "")).isBlank()
            && !String.valueOf(vars.getOrDefault("applicantName", "")).isBlank();

        Map<String, Object> output = new HashMap<>();
        output.put("isValidApplicant", isValid);
        output.put("validationMessage", isValid ? "Applicant verified" : "Applicant under-age or unemployed");

        jobClient.newCompleteCommand(job.getKey())
                .variables(output)
                .send()
                .join();
    }

    private void handleDisburseLoan(JobClient jobClient, ActivatedJob job) {
        log.info("Processing disburse-loan-worker for job: {}", job.getKey());
        Map<String, Object> output = new HashMap<>();
        output.put("disbursementStatus", "DISBURSED");
        output.put("disbursementReference", "DISB-" + System.currentTimeMillis());

        jobClient.newCompleteCommand(job.getKey())
                .variables(output)
                .send()
                .join();
    }

    // ----------------------------------------------------
    // LAB 2: E-Commerce Workers
    // ----------------------------------------------------
    private void handleValidateInventory(JobClient jobClient, ActivatedJob job) {
        log.info("Processing validate-inventory-worker for job: {}", job.getKey());
        Map<String, Object> vars = job.getVariablesAsMap();

        if (failOrderWorker || Boolean.TRUE.equals(vars.get("simulateWorkerFailure"))) {
            log.error("Simulated Failure in validate-inventory-worker!");
            throw new RuntimeException("Simulated Failure: Warehouse stock query deadlock.");
        }

        String productId = (String) vars.getOrDefault("productId", "PROD-STANDARD");
        Number qtyNum = (Number) vars.getOrDefault("quantity", 1);
        int qty = qtyNum.intValue();

        boolean isStockAvailable = !"PROD-OUT-OF-STOCK".equalsIgnoreCase(productId) && qty > 0 && qtyNum.doubleValue() == qty && qty <= 80;
        double unitPrice = productId.contains("LAPTOP") ? 800.0 : (productId.contains("PHONE") ? 550.0 : 60.0);
        double orderTotal = unitPrice * qty;

        Map<String, Object> output = new HashMap<>();
        output.put("isStockAvailable", isStockAvailable);
        output.put("unitPrice", unitPrice);
        output.put("orderTotal", orderTotal);

        jobClient.newCompleteCommand(job.getKey())
                .variables(output)
                .send()
                .join();
    }

    private void handleFulfillOrder(JobClient jobClient, ActivatedJob job) {
        log.info("Processing fulfill-order-worker for job: {}", job.getKey());
        Map<String, Object> output = new HashMap<>();
        output.put("fulfillmentStatus", "DISPATCHED");
        output.put("trackingNumber", "TRK-" + System.currentTimeMillis() + "-EXP");

        jobClient.newCompleteCommand(job.getKey())
                .variables(output)
                .send()
                .join();
    }

    // ----------------------------------------------------
    // LAB 3: Hospital Workers
    // ----------------------------------------------------
    private void handleValidatePatient(JobClient jobClient, ActivatedJob job) {
        log.info("Processing validate-patient-worker for job: {}", job.getKey());
        Map<String, Object> vars = job.getVariablesAsMap();

        if (failHospitalWorker || Boolean.TRUE.equals(vars.get("simulateWorkerFailure"))) {
            log.error("Simulated Failure in validate-patient-worker!");
            throw new RuntimeException("Simulated Failure: Hospital registry offline.");
        }

        String patientRef = (String) vars.getOrDefault("patientRef", "");
        boolean isValid = patientRef != null && !patientRef.trim().isEmpty() && !patientRef.contains("INVALID");

        Map<String, Object> output = new HashMap<>();
        output.put("isRegistrationValid", isValid);
        output.put("schedulingRequest", Map.of(
            "patientRef", patientRef,
            "department", "GENERAL_MEDICINE",
            "appointmentPreference", vars.getOrDefault("appointmentPreference", "ANY"),
            "simulateError", vars.getOrDefault("simulateSchedulingError", false)));
        output.put("assignedSlot", "");

        jobClient.newCompleteCommand(job.getKey())
                .variables(output)
                .send()
                .join();
    }

    private void handleConfirmBooking(JobClient jobClient, ActivatedJob job) {
        log.info("Processing confirm-booking-worker for job: {}", job.getKey());
        Map<String, Object> output = new HashMap<>();
        Object slot = job.getVariablesAsMap().get("assignedSlot");
        if (!(slot instanceof String) || ((String) slot).isBlank()) {
            throw new IllegalArgumentException("An assigned appointment slot is required");
        }
        output.put("bookingStatus", "CONFIRMED");
        output.put("bookingReference", "APT-2026-" + (System.currentTimeMillis() % 100000));

        jobClient.newCompleteCommand(job.getKey())
                .variables(output)
                .send()
                .join();
    }
}
