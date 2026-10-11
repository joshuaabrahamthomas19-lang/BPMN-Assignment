# Experiment 3: BPMN Assignment

This project contains two business-process models created using Camunda Modeler 8:

1. Student Project Approval and Allocation System
2. Logistics and Shipment Exception Management

Both models show the main successful flow and alternate routes for handling problems.

---

## Assignment 1: Student Project Approval and Allocation System

### Purpose

This process shows how a student project proposal is validated, reviewed and assigned to a suitable faculty guide.

### Objectives

- Validate proposal details and team eligibility.
- Check scope, resources and topic similarity.
- Handle approval, revision and rejection.
- Match the project with a suitable guide.
- Resolve allocation and notification problems.
- Show clear completion and closure outcomes.

### Participants

- **Student / Team:** submits proposals, corrects information and completes revisions.
- **Project Coordinator:** screens proposals, checks resources and handles manual recovery.
- **Review Committee:** evaluates proposals and decides whether to approve, revise or reject.
- **HoD:** receives notification when review remains delayed.
- **Faculty Guide:** accepts, declines or proposes supervision conditions.
- **Project Management System:** validates details, checks similarity, matches guides and records allocations.

The overview uses **Project department** and **Coordinator / support** lanes. Task names identify the roles involved in each stage.

### Main Process

1. The student submits the proposal.
2. The system validates proposal details and team rules.
3. The coordinator checks scope and resources.
4. The system checks topic similarity.
5. The committee evaluates the proposal.
6. An approved proposal enters guide allocation.
7. Guide rules check domain, availability and workload.
8. The system reserves a guide slot and sends a request.
9. The guide accepts supervision.
10. The system records the allocation and notifies the student and guide.
11. The process ends with **Project allocated**.

### Problems and How They Are Handled

| Problem | Solution |
|---|---|
| Missing details | Correct the proposal with coordinator support and submit again. |
| Invalid team | Close the current proposal through an error end event. |
| Resources unavailable | Agree an adjusted scope or resource plan and check feasibility again. |
| Similar topic | The coordinator accepts the topic, requests a change or closes the proposal. |
| Late submission | A seven-day boundary timer sends the late request to the coordinator for a decision. |
| Revision required | Allow two revision cycles, then require a final committee decision. |
| Revision deadline missed | A seven-day timer closes review and sends reasons. |
| Unsuccessful review | Allow one new-topic submission, otherwise close the proposal. |
| Review delayed | Send a reminder after two days and inform the HoD after four days while review continues. |
| Guide unavailable | The coordinator confirms a willing replacement guide. |
| Conditional acceptance | Discuss the guide's conditions with the team before confirming allocation. |
| Guide declines or remains silent | Release the slot, exclude the guide and retry. After three failed requests, use coordinator allocation. |
| Recording failure | The coordinator records the allocation manually. |
| Notification failure | The coordinator confirms notification manually. |
| Withdrawal during allocation | Release the reservation. Restart allocation if the team continues, otherwise end as withdrawn. |

### BPMN Elements Used

| Element | Use in the process |
|---|---|
| User task | Submission, screening, review, revision, guide responses and manual recovery. |
| Service task | Validation, similarity checking, reservation, recording and reservation release. |
| Business rule task | Matching guide domain and workload. |
| Send task | Requests, reminders, rejection reasons and notifications. |
| Exclusive gateway | Selecting validation, approval, allocation and retry outcomes. |
| Boundary timer | Submission, revision, review and guide-response deadlines. |
| Error event | Closing unsuccessful stages and handling system failures. |
| Message boundary event | Withdrawal during allocation. |
| Subprocess | Grouping proposal checking, review and guide allocation. |

This model uses boundary events for deadlines and exceptions. It does not contain standalone intermediate catch or throw events.

### Final Outcomes

- **Project allocated:** supervision is confirmed and notification is complete.
- **Proposal closed:** the proposal cannot proceed.
- **Team withdrawn:** the team stops participating during allocation.

### Modelling Notes

- Submission and revision deadlines are seven days.
- Review reminders occur after two days, followed by HoD notification after four days.
- The guide-response deadline is three days.
- Team rules, similarity thresholds and guide capacities depend on department policy.
- Reservation release uses a service task rather than a BPMN compensation event.

---

## Assignment 2: Logistics and Shipment Exception Management

### Purpose

This process shows how a parcel moves from booking and pickup through transport, delivery and shipment closure.

### Objectives

- Validate booking information before pickup.
- Check collection and packaging.
- Coordinate transport and tracking.
- Handle damage, delays, missing scans and customs problems.
- Resolve unsuccessful delivery attempts.
- Verify receipt before closing the shipment.

### Participants

- **Shipper / Customer:** books the shipment, corrects details and supplies documents.
- **Dispatcher:** schedules pickup and coordinates movement.
- **Pickup Agent:** collects the parcel and checks packaging.
- **Hub / Warehouse Staff:** sort, scan and transport parcels.
- **Delivery Agent:** attempts delivery and captures proof.
- **Recipient:** receives the parcel or collects it from the depot.
- **Supervisor / Claims Staff:** resolve recording errors, damage and claims.

The overview uses **Shipment operations** and **Support / claims** lanes.

### Main Process

1. The shipper enters booking details.
2. Booking rules validate the address, weight and service and calculate the charge.
3. The system schedules pickup and assigns a driver.
4. The agent collects the parcel and checks packaging and the pickup scan.
5. The system records the shipment charge.
6. Transport and tracking notification begin in parallel.
7. The parcel is sorted, scanned and transported through the planned legs.
8. Transport and tracking reach the parallel join.
9. The recipient receives a delivery notification.
10. The agent delivers the parcel and captures proof.
11. Staff verify delivery proof or confirm receipt.
12. The system closes the shipment and issues an invoice.
13. The process ends with **Shipment delivered**.

### Problems and How They Are Handled

| Problem | Solution |
|---|---|
| Incorrect booking | Return the details to the shipper for correction. |
| Pickup failure | Reschedule once and retry the next day. A second failure cancels the booking and applies the fee. |
| Unsafe packaging | Allow one repacking attempt. Cancel pickup if packaging remains unsafe. |
| Tracking failure | A supervisor updates tracking manually before the join. |
| Hub scan failure | A supervisor records and reconciles the scan manually. |
| Damage at a hub | Inspect the parcel and agree a claim, then continue with a suitable resolution or return it. |
| Transit delay | Notify the customer, arrange an alternate route and wait again for arrival. |
| Customs hold | Request documents, check them and allow one correction. |
| Customs deadline missed | A five-day boundary timer leads to return. |
| Missing arrival scan | A 48-hour boundary timer interrupts movement and starts tracing. |
| Tracing fails | Record loss and pay compensation if tracing fails or its five-day deadline expires. |
| Wrong delivery address | Correct the address and agree any surcharge before retrying, otherwise return. |
| Recipient unavailable | Allow three delivery attempts, then wait for depot collection. |
| Depot collection | Verify the collector and record proof. Return the parcel if the seven-day hold expires. |
| Damage at delivery | Agree a replacement and retry delivery, or return the parcel. |
| Refusal or COD failure | Return the parcel and adjust charges. |
| Missing delivery proof | Recover proof or verify receipt. Keep verification open until receipt is confirmed. |
| Cancellation during transport | Interrupt transport, compensate the recorded charge minus the fee and return the parcel. |

### BPMN Elements Used

| Element | Use in the process |
|---|---|
| User task | Booking, collection, inspections, document checks, delivery and proof verification. |
| Service task | Scheduling, recording, scanning, movement, replacement, return and invoicing. |
| Business rule task | Booking validation and charge calculation. |
| Send task | Tracking, customs requests, delay notices and delivery notifications. |
| Receive task | Customs-document receipt and depot collection. |
| Exclusive gateway | Selecting booking, pickup, transit and delivery outcomes. |
| Parallel gateway | Starting and joining transport and tracking. |
| Event-based gateway | Waiting for arrival, delay or an ETA timeout. |
| Intermediate timer catch | Next-day retries and the ETA deadline. |
| Intermediate message catch | Arrival and delay messages. |
| Boundary event | Handling timers, errors, cancellation and compensation. |
| Intermediate compensation throw | Triggering the shipment-charge refund. |
| Sequential multi-instance subprocess | Repeating transport for each planned leg. |
| Terminate end event | Ending remaining parallel work after return or loss. |
| Association | Linking the compensation boundary to the refund task. |

### Final Outcomes

- **Shipment delivered:** receipt is verified and the shipment is closed.
- **Booking cancelled:** pickup fails or packaging remains unsafe.
- **Returned to sender:** an unresolved problem or cancellation requires return.
- **Parcel lost:** tracing fails and compensation is recorded.

### Modelling Notes

- Pickup permits two attempts.
- Delivery permits three attempts.
- Depot collection has a seven-day holding period.
- Missing scans trigger tracing after 48 hours.
- Tracing has a five-day deadline.
- Customs-document requests have a five-day response deadline.
- Transport repeats sequentially for the planned shipment legs.
- Address changes and surcharges are handled through a user task and gateway.

---

## Model Scope

These are non-executable BPMN diagrams for Camunda Modeler 8.

The main path shows successful completion. Alternate paths show corrections, retries, manual recovery and unsuccessful outcomes.