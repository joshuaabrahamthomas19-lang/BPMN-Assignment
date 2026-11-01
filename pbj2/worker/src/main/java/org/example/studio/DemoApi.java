package org.example.studio;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Local classroom API only. The fault control is deliberately unauthenticated. */
@RestController
public class DemoApi {
    private final Set<String> faults=ConcurrentHashMap.newKeySet();
    public boolean failing(String name) { return faults.contains(name); }
    @PutMapping("/demo/faults/{name}")
    public Map<String,Object> fault(@PathVariable String name, @RequestBody Map<String,Boolean> body) {
        if (!Set.of("credit","payment","slots","worker").contains(name)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unknown fault");
        if(Boolean.TRUE.equals(body.get("enabled"))) faults.add(name); else faults.remove(name);
        return Map.of("name",name,"enabled",failing(name));
    }
    @PostMapping("/api/{service}")
    public Map<String,Object> call(@PathVariable String service,@RequestBody Map<String,Object> body) {
        if(failing(service)) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Injected "+service+" outage");
        return switch(service) {
            case "credit" -> {
                LabLogic.text(body,"applicationId");double score=LabLogic.number(body,"mockCreditScore");
                if(score<300 || score>850) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Score outside demo range");
                yield Map.of("creditScore",score,"source","SYNTHETIC");
            }
            case "payment" -> {
                String id=LabLogic.text(body,"orderId"),token=LabLogic.text(body,"paymentToken");LabLogic.number(body,"orderTotal");
                yield Map.of("paymentAccepted",token.equals("demo_accept"),"transactionRef","DEMO-"+id);
            }
            case "slots" -> {
                LabLogic.text(body,"patientRef");LabLogic.text(body,"appointmentPreference");
                Object raw=body.getOrDefault("availableSlots",List.of());
                if(!(raw instanceof List<?> slots)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Slots must be an array");
                yield Map.of("slotAvailable",!slots.isEmpty(),"availableSlots",slots,"assignedSlot",slots.isEmpty()?"":slots.get(0));
            }
            default -> throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Unknown service");
        };
    }
}
