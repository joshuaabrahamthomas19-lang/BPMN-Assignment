# API contracts and process variables

All API examples use synthetic data. The application hosts these endpoints at port 8090. Connector URL is `mockApiBaseUrl + /api/<service>`. Each response is JSON.

## REST requests and responses

| POST path | Request | Response |
| --- | --- | --- |
| `/api/credit` | `{"applicationId":"L-DEMO-01","mockCreditScore":760}` | `{"creditScore":760,"source":"SYNTHETIC"}` |
| `/api/payment` | `{"orderId":"O-DEMO-01","orderTotal":240,"paymentToken":"demo_accept"}` | `{"paymentAccepted":true,"transactionRef":"DEMO-O-DEMO-01"}` |
| `/api/slots` | `{"patientRef":"P-DEMO-01","appointmentPreference":"MORNING","availableSlots":["2030-01-10T09:00:00Z"]}` | `{"slotAvailable":true,"availableSlots":["2030-01-10T09:00:00Z"],"assignedSlot":"2030-01-10T09:00:00Z"}` |

Any payment token other than `demo_accept` is declined. An empty slot list returns false and an empty assigned slot. The mock echoes deterministic references so repeating an example is easy to identify.

## Connector configuration

All three tasks define `io.camunda:http-json:1`, retries 3, authentication `noAuth`, method POST, connection/read timeout 5 seconds, FEEL request-body contexts and `resultExpression` task headers. Credit maps `creditScore`; payment maps `paymentAccepted` and `transactionRef`; scheduling maps `slotAvailable`, `assignedSlot` and `availableSlots`. No custom Java worker subscribes to the connector type. Do not create top-level variables named `body`, `url`, `method` or `authentication`, as these collide with connector inputs.

## Shared variables

| Variable | Type | Writer / purpose |
| --- | --- | --- |
| mockApiBaseUrl | string | Start payload; API reachable from connector runtime |
| route | string | DMN result; one scalar output from FIRST table |

## Loan

| Variable | Type | Writer / purpose |
| --- | --- | --- |
| applicationId | string | Start; synthetic application identity |
| applicantAge | number | Start; identity eligibility age |
| identityVerified | boolean | Start; synthetic identity check fixture |
| monthlyIncome, monthlyDebt, requestedAmount | number | Start; affordability inputs in one consistent currency |
| termMonths | number | Start; positive whole repayment months |
| mockCreditScore | number | Start; synthetic API fixture, 300-850 |
| identityValid | boolean | Validation worker; route before credit call |
| paymentRatio | number | Worker; principal-only monthly obligation / income |
| creditScore | number | Credit REST response; DMN input |
| officerApproved | boolean | Tasklist form; approval on manual path |
| disbursementStatus, disbursementRef | string | Payout worker; simulated result |

## Order

| Variable | Type | Writer / purpose |
| --- | --- | --- |
| orderId, customerRef, productId | string | Start; synthetic references |
| quantity, stockOnHand, unitPrice | number | Start; inventory fixtures; quantity must be positive integer |
| customerCategory | string | Start; MEMBER or REGULAR policy input |
| deliveryMode | string | Start; EXPRESS or STANDARD |
| paymentToken | string | Start; demo_accept or demo_decline |
| stockReserved | boolean | Reserve/release workers; simulated reservation state |
| orderTotal | number | Inventory worker; quantity times unit price |
| reservationRef | string | Inventory worker; stable synthetic reservation reference |
| purchaseApproved | boolean | Tasklist form; manual purchase decision |
| paymentAccepted | boolean | REST payment response |
| transactionRef | string | Payment response; stable demo reference |
| deliveryPriority | string | Dispatch worker; records final DMN route |
| fulfilmentStatus, trackingRef | string | Dispatch worker; trackingRef only on dispatched path |
| reservationReleased | boolean | Release worker; cancellation output |

## Patient access

| Variable | Type | Writer / purpose |
| --- | --- | --- |
| patientRef | string | Start; synthetic patient reference |
| patientAge | number | Start; 0-125 age fixture |
| registered | boolean | Start; registration fixture |
| urgency | string | Start; LOW, MODERATE, CRITICAL |
| appointmentPreference, symptomsSummary | string | Start; scheduling preference and human context |
| availableSlots | array of strings | Start fixture; echoed by scheduling API |
| registrationValid | boolean | Worker; registration gate |
| schedulingRequest | context/object | Worker; body prepared for REST scheduling call |
| slotAvailable | boolean | API; availability gate |
| assignedSlot | string | API or staff form; selected synthetic slot |
| staffResolution | string | Form; BOOK, WAITLIST, DECLINE |
| handoffAcknowledged | boolean | Urgent human task; handoff record |
| bookingStatus, bookingRef, bookedSlot | string | Booking worker; confirmed booking output |

Rejected, invalid, waitlisted and urgent cases are identified by their distinct end-event IDs in Operate; only the booking path writes `bookingStatus`. Inspect the end event rather than expecting every branch to set a booking variable.
