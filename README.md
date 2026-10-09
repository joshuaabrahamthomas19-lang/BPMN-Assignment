# Experiment 3: BPMN Assignment

## Assignment 1: Student Project Approval and Allocation System

### Purpose

This assignment models how a department handles a final-year project proposal, from the team's first submission to confirmed faculty supervision.

### Objectives

- Check that the proposal contains enough information to evaluate.
- Verify team eligibility and avoid duplicate project topics.
- Give the coordinator and committee clear decision points.
- Match an approved project to a suitable guide.
- Resolve common problems before closing the case.
- End with a clear allocated, rejected, lapsed or withdrawn outcome.

### Participants and Responsibilities

- **Student / Team:** submits the proposal, explains the expected outcome and milestones, and completes revisions.
- **Project Coordinator:** screens the proposal, checks resources, helps resolve incomplete information and arranges manual allocation when needed.
- **Review Committee and HoD:** evaluate feasibility, request revision, approve or reject, and respond to delayed review.
- **Faculty Guide:** accepts or declines supervision.
- **Project Management System:** validates information, checks similarity, applies guide rules, records decisions and sends notifications.

### Normal Process

1. The team begins the project submission process.
2. A project record is opened.
3. The team submits its proposal.
4. The system checks the details and team rules.
5. The coordinator screens the proposal.
6. The coordinator checks the required resources. If they are unavailable, the team can submit one adjusted plan before the coordinator decides whether the project can continue.
7. A similarity check identifies possible overlap with previous projects.
8. The team records its expected outcome and planned milestones.
9. The committee evaluates the proposal and chooses approval, revision or rejection.
10. An approved project is matched to a guide using domain, availability and workload.
11. The system reserves the guide slot and sends an allocation request.
12. The guide accepts supervision. Any conditions proposed by the guide must also be agreed by the team.
13. The allocation is recorded and the student is notified.
14. The project ends as allocated.

### Problems and How They Are Handled

- **Proposal cannot be assessed:** Missing details or inconsistent team information prevent review. Return the specific errors to the team. After two unsuccessful submissions, ask the coordinator to resolve the issue.
- **Team does not meet the rules:** The team is too large or a member is already registered elsewhere. Explain the eligibility issue and close this submission. A corrected team can make a new submission.
- **Required resources are unavailable:** The proposed project needs software, equipment or data that cannot be obtained. Allow one adjusted resource plan, such as a smaller scope or accessible tools/data. The coordinator checks feasibility again; if it is still not feasible, send rejection reasons and use the new-topic option.
- **Topic overlaps an existing project:** The similarity result indicates that the proposed work may repeat earlier work. The coordinator checks the actual overlap and chooses clearance, a topic change or rejection.
- **Submission window expires:** The team has not submitted before the deadline. Ask the coordinator whether late submission can be accepted. Otherwise close the proposal as lapsed.
- **Committee cannot approve the current scope:** The idea needs clearer objectives or a smaller workload. Arrange a brief mentoring discussion and ask the team to revise. Limit revisions to two cycles, with seven days per revision, then require a final decision.
- **Proposal is rejected:** The coordinator or committee decides the project cannot proceed. Send reasons and allow one new-topic submission. Close as rejected if the team declines or that opportunity is exhausted.
- **Review is not completed on time:** The committee has not recorded a decision. Send a reminder after two days. After four days, inform the HoD while the review remains active.
- **No suitable guide is available:** No matching guide has space in the current workload. Ask the coordinator to consider manual allocation, a co-guide, an external guide or a limited waiting period.
- **Guide will accept only with conditions:** The guide proposes supervision expectations or project changes that the team has not accepted. Discuss those conditions with the team. Confirm allocation only when they agree; otherwise treat the request as unsuccessful and choose another guide.
- **Guide declines or does not reply:** The selected guide refuses or the response deadline expires. Release the reserved slot, exclude the guide and try another. After three requests, use coordinator-led allocation.
- **Notification or allocation record fails:** A system operation cannot be completed. Retry within the limit and then ask the coordinator to confirm the action manually.
- **Team or guide withdraws:** The team stops the project or the assigned guide leaves. Release the reserved slot. Close a team withdrawal; restart guide selection when the team still wants to continue.

### BPMN Approach

User tasks represent human submissions and decisions. Service tasks represent automatic checks and records. Business rule tasks represent guide-selection rules. Exclusive gateways select one outcome. Timers represent deadlines and reminders. Error recovery and compensation handle failed operations and released guide reservations. Subprocesses group related steps.

### Final Outcomes

- **Project allocated:** the guide accepts and the student is notified.
- **Rejected:** the proposal cannot proceed.
- **Lapsed:** a deadline or unresolved allocation issue closes the proposal.
- **Invalid team:** the current submission is closed because team rules are not met.
- **Withdrawn:** the team ends its participation.

### Modelling Assumptions

The initial submission window is seven days. Revision and response deadlines are shown in the process. Similarity thresholds, team limits and guide capacities are department policies. This is a business-process model within syllabus Units 1-4.

---

## Assignment 2: Logistics and Shipment Exception Management

### Purpose

This assignment models a parcel's journey from shipment booking to pickup, hub movement, delivery and closure, including the exceptions that may interrupt the journey.

### Objectives

- Validate a booking before scheduling collection.
- Keep parcel movement and tracking updates coordinated.
- Detect damage, delays and missing scans.
- Resolve delivery problems through controlled recovery paths.
- Record proof before declaring successful delivery.
- End each shipment as delivered, returned, lost or cancelled.

### Participants and Responsibilities

- **Shipper / Customer:** books the shipment, corrects booking information and provides customs documents.
- **Control Tower / Dispatcher:** schedules pickup, monitors movement, handles delays and coordinates recovery.
- **Pickup Agent:** collects the parcel, records the pickup scan and checks packaging.
- **Hub / Warehouse:** sorts, scans and moves the parcel through each shipment leg.
- **Delivery Agent:** attempts delivery and records proof or the reason for failure.
- **Recipient:** supplies delivery information, accepts the parcel or arranges collection.
- **Claims Department:** inspects damage and resolves eligible claims or compensation.

### Normal Process

1. The shipper begins the booking.
2. A shipment record is opened.
3. The shipper enters the parcel and recipient information.
4. Booking rules validate the address, weight and service level and calculate charges.
5. Pickup is scheduled and a driver is assigned.
6. The pickup agent collects the parcel and records its scan.
7. The agent checks the packaging. Unsafe packing must be corrected and rechecked before dispatch.
8. Shipment charges are recorded.
9. Parcel movement and the tracking update proceed in parallel.
10. The parcel is sorted, scanned and transported through each hub and line-haul leg.
11. The dispatcher records delivery instructions such as access details or a landmark.
12. The recipient is notified that the parcel is out for delivery.
13. The delivery agent hands over the parcel and records proof of delivery.
14. The delivery evidence is checked. The shipment is closed and invoiced only after receipt is verified.

### Problems and How They Are Handled

- **Pickup cannot be completed:** The shipper is absent or the parcel is not ready. Reschedule within the two-attempt limit. If pickup still fails, cancel the booking and apply the relevant fee.
- **Parcel is unsafe to transport:** The seal, protective packing or fragile-item protection is inadequate. Ask the shipper to repack once and check it again. Proceed only if it is safe; otherwise cancel pickup.
- **Address is incorrect:** The booking address is incomplete or the driver discovers a wrong address. Get the corrected address before proceeding. For a delivery-stage change, check whether the new zone needs a surcharge.
- **Parcel is damaged:** Damage is found at a hub or at the delivery stage. Inspect the damage and check insurance. Handle an eligible refund, replacement or reshipment; record and report an uninsured case.
- **Parcel cannot be located:** No arrival scan is received within the expected tracking window. Trace it after 48 hours without an arrival scan. If tracing cannot find it within five days, record the loss and handle compensation.
- **Transit falls behind schedule:** Weather, a breakdown or capacity problems delay movement. Inform the customer, update the expected arrival and arrange an alternate route or vehicle where possible.
- **Customs will not release the parcel:** The international shipment documents are missing or unacceptable. Request corrected documents and review them. Return the shipment if the five-day deadline or correction limit is exceeded.
- **Recipient cannot receive the parcel:** Delivery attempts fail because the recipient is unavailable. After the attempt limit, offer depot pickup or authorized collection during the holding period. Check collector identity and capture delivery proof; return the parcel if it remains uncollected.
- **Recipient refuses or cannot pay:** The parcel is refused or cash-on-delivery payment fails. Start the return-to-sender route and adjust the shipment charges.
- **Scan or tracking information cannot be recorded:** A scanner is offline or the tracking records disagree. Have a supervisor capture the information manually, then reconcile the record before continuing.
- **Delivery evidence is missing or unreadable:** The handover is reported as successful, but proof is incomplete. Check the evidence before closure. Recover the proof or verify receipt directly with the recipient and record the confirmation. Keep the task open if receipt cannot be verified.
- **Shipper cancels after dispatch:** The cancellation arrives after movement has started. Intercept the parcel, return it and refund the eligible amount after the cancellation fee.

### BPMN Approach

Human actions use user tasks. Automatic scheduling, scanning and recording use service tasks. Business rule tasks represent booking and surcharge rules. Exclusive gateways select outcomes. Parallel gateways coordinate movement and tracking. Event-based gateways wait for messages or timers. A sequential multi-instance subprocess represents repeated shipment legs. Error recovery and compensation handle failed operations and cancelled charges.

### Final Outcomes

- **Delivered:** the parcel is handed over, proof is recorded and the shipment is closed.
- **Returned:** the parcel goes back to the sender after an unresolved delivery or transit problem.
- **Lost:** tracing fails and the loss is recorded with compensation.
- **Cancelled:** the booking ends before successful dispatch or collection.

### Modelling Assumptions

A shipment may contain several transport legs. Customs processing applies to international shipments. Retry, tracing and holding periods are defined by courier policy. An authorized collector must provide acceptable identity and authorization. This is a business-process model within syllabus Units 1-4.
