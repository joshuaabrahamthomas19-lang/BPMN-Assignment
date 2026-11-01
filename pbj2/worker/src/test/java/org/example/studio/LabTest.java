package org.example.studio;

import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import io.camunda.zeebe.model.bpmn.Bpmn;
import static org.junit.jupiter.api.Assertions.*;

class LabTest {
    @Test void dmnFilesPassSchemaValidation() throws Exception {
        try(var files=Files.list(Path.of("../models"))) {
            for(Path p:files.filter(p->p.toString().endsWith(".dmn")).toList()) {
                var model=org.camunda.bpm.model.dmn.Dmn.readModelFromFile(p.toFile());
                org.camunda.bpm.model.dmn.Dmn.validateModel(model);
            }
        }
    }
    @Test void bpmnModelsPassCamundaModelValidation() throws Exception {
        try(var files=Files.list(Path.of("../models"))) {
            for(Path p:files.filter(p->p.toString().endsWith(".bpmn")).toList()) Bpmn.validateModel(Bpmn.readModelFromFile(p.toFile()));
        }
    }
    private Map<String,Object> loan() {return new HashMap<>(Map.of("applicationId","L1","applicantAge",28,"identityVerified",true,"monthlyIncome",5000,"monthlyDebt",300,"requestedAmount",20000,"termMonths",36));}
    @Test void affordabilityIsCalculatedAndUnderageIdentityRejected() {
        var v=loan();var result=LabLogic.run("studio.loan.check",v);
        assertEquals(true,result.get("identityValid"));assertEquals((300+20000.0/36)/5000,(double)result.get("paymentRatio"),0.00001);
        v.put("applicantAge",17);assertEquals(false,LabLogic.run("studio.loan.check",v).get("identityValid"));
        v.put("termMonths",0);assertEquals(false,LabLogic.run("studio.loan.check",v).get("identityValid"));
    }
    @Test void inventoryAndCancellation() {
        var v=new HashMap<String,Object>(Map.of("orderId","O1","customerRef","C1","productId","SKU","quantity",2,"stockOnHand",4,"unitPrice",120));
        var result=LabLogic.run("studio.order.reserve",v);assertEquals(240.0,result.get("orderTotal"));assertEquals(true,result.get("stockReserved"));
        v.put("quantity",5);assertEquals(false,LabLogic.run("studio.order.reserve",v).get("stockReserved"));
        v.put("quantity",1.5);assertEquals(false,LabLogic.run("studio.order.reserve",v).get("stockReserved"));
        assertEquals(false,LabLogic.run("studio.order.release",v).get("stockReserved"));
    }
    @Test void cannotShipUnpaidOrder() {assertThrows(IllegalStateException.class,()->LabLogic.run("studio.order.ship",Map.of("paymentAccepted",false,"stockReserved",true)));}
    @Test void slotApiAndFaultRecovery() {
        var api=new DemoApi();var request=Map.<String,Object>of("patientRef","P1","appointmentPreference","ANY","availableSlots",List.of("2030-01-10T09:00:00Z"));
        assertEquals(true,api.call("slots",request).get("slotAvailable"));
        assertEquals(false,api.call("slots",Map.of("patientRef","P1","appointmentPreference","ANY")).get("slotAvailable"));
        api.fault("slots",Map.of("enabled",true));assertThrows(Exception.class,()->api.call("slots",request));
        api.fault("slots",Map.of("enabled",false));assertEquals(true,api.call("slots",request).get("slotAvailable"));
    }
    @Test void paymentDeclineAndCreditRange() {
        var api=new DemoApi();assertEquals(false,api.call("payment",Map.of("orderId","O1","orderTotal",100,"paymentToken","demo_decline")).get("paymentAccepted"));
        assertEquals(true,api.call("payment",Map.of("orderId","O1","orderTotal",100,"paymentToken","demo_accept")).get("paymentAccepted"));
        assertEquals(760.0,api.call("credit",Map.of("applicationId","L1","mockCreditScore",760)).get("creditScore"));
        assertThrows(Exception.class,()->api.call("credit",Map.of("applicationId","L1","mockCreditScore",900)));
    }
}
