package org.example.studio;

import java.util.List;
import java.util.Map;

/** Deterministic teaching simulations: no money, inventory or clinical systems are changed. */
public final class LabLogic {
    private LabLogic() {}
    public static double number(Map<String,Object> v, String k) {
        Object n = v.get(k);
        if (!(n instanceof Number) || !Double.isFinite(((Number)n).doubleValue()))
            throw new IllegalArgumentException("Missing or invalid numeric variable: " + k);
        return ((Number)n).doubleValue();
    }
    public static String text(Map<String,Object> v, String k) {
        Object s=v.get(k);
        if (!(s instanceof String) || ((String)s).isBlank()) throw new IllegalArgumentException("Missing text: "+k);
        return (String)s;
    }
    public static Map<String,Object> run(String type, Map<String,Object> v) {
        return switch(type) {
            case "studio.loan.check" -> {
                text(v,"applicationId");
                double income=number(v,"monthlyIncome"), debt=number(v,"monthlyDebt"), amount=number(v,"requestedAmount"), term=number(v,"termMonths");
                boolean valid=Boolean.TRUE.equals(v.get("identityVerified")) && number(v,"applicantAge")>=18 && income>0 && debt>=0 && amount>0 && term>=1 && term==Math.floor(term);
                // Principal-only monthly payment: deliberately simple, not a financial quote.
                yield Map.of("identityValid",valid,"paymentRatio",valid?(debt+amount/term)/income:1.0);
            }
            case "studio.loan.pay" -> Map.of("disbursementStatus","SIMULATED_DISBURSED","disbursementRef","DEMO-"+text(v,"applicationId"));
            case "studio.order.reserve" -> {
                text(v,"orderId");text(v,"customerRef");text(v,"productId");
                double quantity=number(v,"quantity"), stock=number(v,"stockOnHand"), price=number(v,"unitPrice");
                boolean valid=quantity>0 && quantity==Math.floor(quantity) && price>0 && stock>=quantity;
                yield Map.of("stockReserved",valid,"orderTotal",quantity*price,"reservationRef",valid?"RES-"+text(v,"orderId"):"");
            }
            case "studio.order.ship" -> {
                if (!Boolean.TRUE.equals(v.get("paymentAccepted")) || !Boolean.TRUE.equals(v.get("stockReserved"))) throw new IllegalStateException("Payment and reservation required");
                yield Map.of("fulfilmentStatus","DISPATCHED","trackingRef","DEMO-"+text(v,"orderId"),"deliveryPriority",text(v,"route"));
            }
            case "studio.order.release" -> Map.of("stockReserved",false,"fulfilmentStatus","CANCELLED","reservationReleased",true);
            case "studio.care.check" -> {
                String ref=text(v,"patientRef"), preference=text(v,"appointmentPreference");text(v,"symptomsSummary");text(v,"urgency");
                double age=number(v,"patientAge");
                Object slots=v.getOrDefault("availableSlots",List.of());
                if (!(slots instanceof List<?>)) throw new IllegalArgumentException("availableSlots must be an array");
                yield Map.of("registrationValid",Boolean.TRUE.equals(v.get("registered")) && age>=0 && age<=125,
                    "schedulingRequest",Map.of("patientRef",ref,"appointmentPreference",preference,"availableSlots",slots));
            }
            case "studio.care.book" -> Map.of("bookingStatus","CONFIRMED","bookingRef","DEMO-"+text(v,"patientRef"),"bookedSlot",text(v,"assignedSlot"));
            default -> throw new IllegalArgumentException("Unknown job type: "+type);
        };
    }
    public static final List<String> TYPES=List.of("studio.loan.check","studio.loan.pay","studio.order.reserve","studio.order.ship","studio.order.release","studio.care.check","studio.care.book");
}
