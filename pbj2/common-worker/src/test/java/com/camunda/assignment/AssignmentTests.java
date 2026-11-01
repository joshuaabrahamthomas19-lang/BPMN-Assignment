package com.camunda.assignment;

import com.camunda.assignment.mockapi.MockServicesController;
import com.camunda.assignment.worker.AssignmentWorkers;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.api.worker.JobClient;
import io.camunda.zeebe.model.bpmn.Bpmn;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AssignmentTests {
    @Test void bpmnModelsAreValid() throws Exception {
        for(String lab:List.of("lab1-loan-approval","lab2-ecommerce-fulfillment","lab3-hospital-triage")) {
            try(var paths=Files.list(Path.of("..",lab))) {
                for(var path:paths.filter(p->p.toString().endsWith(".bpmn")).toList())
                    Bpmn.validateModel(Bpmn.readModelFromFile(path.toFile()));
            }
        }
    }
    @Test void dmnModelsAreValid() throws Exception {
        for(String lab:List.of("lab1-loan-approval","lab2-ecommerce-fulfillment","lab3-hospital-triage")) {
            try(var paths=Files.list(Path.of("..",lab))) {
                for(var path:paths.filter(p->p.toString().endsWith(".dmn")).toList())
                    org.camunda.bpm.model.dmn.Dmn.validateModel(org.camunda.bpm.model.dmn.Dmn.readModelFromFile(path.toFile()));
            }
        }
    }
    @SuppressWarnings({"rawtypes","unchecked"})
    private Map<String,Object> run(String method,Map<String,Object> input) {
        JobClient client=mock(JobClient.class,RETURNS_DEEP_STUBS);
        ActivatedJob job=mock(ActivatedJob.class);
        when(job.getKey()).thenReturn(1L);when(job.getVariablesAsMap()).thenReturn(input);
        ReflectionTestUtils.invokeMethod(new AssignmentWorkers(),method,client,job);
        ArgumentCaptor<Map> output=ArgumentCaptor.forClass(Map.class);
        verify(client.newCompleteCommand(1L)).variables(output.capture());
        return output.getValue();
    }
    @Test void applicantValidation() {
        var valid=new HashMap<String,Object>(Map.of("applicantId","APP-1","applicantName","Demo Applicant","applicantAge",29,"employmentStatus","EMPLOYED"));
        assertEquals(true,run("handleValidateApplicant",valid).get("isValidApplicant"));
        valid.put("applicantAge",17);assertEquals(false,run("handleValidateApplicant",valid).get("isValidApplicant"));
    }
    @Test void inventoryRejectsInvalidQuantityAndCalculatesTotal() {
        assertEquals(1600.0,run("handleValidateInventory",Map.of("productId","PROD-LAPTOP","quantity",2)).get("orderTotal"));
        assertEquals(false,run("handleValidateInventory",Map.of("productId","PROD-PHONE","quantity",0)).get("isStockAvailable"));
        assertEquals(false,run("handleValidateInventory",Map.of("productId","PROD-PHONE","quantity",1.5)).get("isStockAvailable"));
        assertEquals(false,run("handleValidateInventory",Map.of("productId","PROD-OUT-OF-STOCK","quantity",1)).get("isStockAvailable"));
    }
    @Test void registrationPreparesAvailabilityRequest() {
        var result=run("handleValidatePatient",Map.of("patientRef","PAT-1","appointmentPreference","MORNING","simulateSchedulingError",false));
        assertEquals(true,result.get("isRegistrationValid"));assertTrue(result.get("schedulingRequest") instanceof Map);
        assertEquals(false,run("handleValidatePatient",Map.of("patientRef","INVALID-1")).get("isRegistrationValid"));
    }
    @Test void bookingRequiresSlot() {
        assertThrows(IllegalArgumentException.class,()->run("handleConfirmBooking",Map.of("assignedSlot","")));
        assertEquals("CONFIRMED",run("handleConfirmBooking",Map.of("assignedSlot","2030-02-04T10:00:00Z")).get("bookingStatus"));
    }
    @Test void mockPaymentAndCredit() {
        var api=new MockServicesController();
        assertEquals("DECLINED",api.processPayment(Map.of("paymentToken","tok_declined")).getBody().get("paymentStatus"));
        assertEquals("SUCCESS",api.processPayment(Map.of("paymentToken","tok_synthetic_success")).getBody().get("paymentStatus"));
        assertEquals(770,api.evaluateCredit(Map.of("monthlyIncome",6200,"customCreditScore",770)).getBody().get("creditScore"));
        assertEquals(500,api.evaluateCredit(Map.of("simulateError",true)).getStatusCode().value());
    }
    @Test void schedulingFailureAndCorrection() {
        var api=new MockServicesController();
        assertEquals(false,api.getAvailableSlots(Map.of("appointmentPreference","NONE_AVAILABLE")).getBody().get("hasAvailableSlot"));
        assertEquals(500,api.getAvailableSlots(Map.of("simulateError",true)).getStatusCode().value());
        assertEquals(true,api.getAvailableSlots(Map.of("appointmentPreference","MORNING","simulateError",false)).getBody().get("hasAvailableSlot"));
    }
}
