# Lab 3: Hospital Appointment and Patient Triage

## Aim

To validate patient registration, classify an appointment request and book a slot or send it for staff review.

## Process

The worker checks the synthetic patient reference and prepares the scheduling request. The DMN table returns Routine, Priority Review or Urgent Assessment. Routine requests call the scheduling API. Priority requests go to staff review. Urgent requests go directly to a human assessment task.

When no slot is available, staff can assign one manually, waitlist the request or decline it. The booking worker requires an assigned slot before confirming an appointment.

## Decision Rules

| Input condition | Category | Maximum wait | Department |
| --- | --- | --- | --- |
| CRITICAL or EMERGENCY | Urgent Assessment | 15 minutes | EMERGENCY_CARE |
| MODERATE or HIGH, listed symptoms and age 6 or below / 70 or above | Priority Review | 3 hours | URGENT_OUTPATIENT |
| MODERATE | Priority Review | 8 hours | SPECIALIST_CLINIC |
| LOW or ROUTINE | Routine | 36 hours | GENERAL_MEDICINE |
| Otherwise | Routine | 48 hours | GENERAL_MEDICINE |

The listed symptoms are FEVER_HIGH, DIZZINESS and FRACTURE_SUSPECTED. These are artificial rules for demonstrating DMN routing, not medical advice.

## API Example

`POST /api/hospital-slots`

```json
{"patientRef":"PAT-2101","department":"GENERAL_MEDICINE","appointmentPreference":"MORNING","simulateError":false}
```

The response contains `hasAvailableSlot`, `availableSlots`, `assignedSlot` and `message`. Setting the preference to `NONE_AVAILABLE` returns no slots. The dates are fixed test fixtures.

## Variables

| Variable | Type | Purpose |
| --- | --- | --- |
| patientRef | string | Synthetic patient reference |
| patientAge | number | DMN age input |
| symptomsSummary, urgencyIndicator | string | DMN symptom and urgency inputs |
| appointmentPreference | string | MORNING, AFTERNOON, ANY or NONE_AVAILABLE |
| mockApiBaseUrl | string | API base address |
| simulateWorkerFailure, simulateSchedulingError | boolean | Failure-test switches |
| isRegistrationValid | boolean | Registration-check result |
| schedulingRequest | object | Prepared scheduling API request |
| triageCategory, recommendedDepartment | string | DMN outputs |
| maxWaitTimeHours | number | Educational maximum wait |
| hasAvailableSlot | boolean | Scheduling response |
| availableSlots | array of strings | Scheduling response |
| assignedSlot | string or null | API-selected or staff-entered slot |
| staffResolution | string | BookSlot, Waitlist or Decline |
| assessmentNotes | string | Notes from the urgent-assessment form |
| bookingStatus, bookingReference | string | Confirmed booking results |

## Sample Process Input

Use these variables when starting an instance:

```json
{
  "patientRef": "PAT-2101",
  "patientAge": 32,
  "symptomsSummary": "ROUTINE_CHECK",
  "urgencyIndicator": "LOW",
  "appointmentPreference": "MORNING",
  "simulateWorkerFailure": false,
  "simulateSchedulingError": false,
  "mockApiBaseUrl": "http://host.docker.internal:8090"
}
```

## Tests

| Scenario | Expected result | Actual engine result |
| --- | --- | --- |
| Routine request with a slot | Routine appointment confirmed | Pending |
| Priority request | Staff review task | Pending |
| Urgent assessment | Immediate human-assessment task | Pending |
| No slots available | Staff review; choose Waitlist | Pending |
| Scheduling API failure | Scheduling incident followed by recovery | Pending |

For staff booking, choose BookSlot and enter a synthetic appointment slot, such as `2030-02-04T10:00:00Z`. Without a slot, the request follows the waitlist/alternate-facility ending. Urgent cases end after the human assessment and do not enter routine booking.

For the scheduling failure, change `simulateSchedulingError` to false in Operate and also set `schedulingRequest.simulateError` to false, since the worker prepared this request before the failing API call. Retry the same job with positive retries where needed. Capture the incident and the recovered instance.

The live deployment, Tasklist and Operate screenshots are still pending. Only synthetic patient data should be used.
