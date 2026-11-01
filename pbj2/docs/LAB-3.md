# Lab 3 implementation report: patient access coordination

## Architecture and execution

`studio.care.check` validates synthetic registration fields and prepares `schedulingRequest`. A REST Connector sends that request to the mock scheduling service. The DMN decision runs before scheduling so an urgent request goes directly to a human handoff task. Non-urgent cases look up slots. Routine cases with a slot book directly; priority requests and routine requests without a slot reach authorised staff review.

## FIRST-hit educational policy

| Urgency | Age | Classification |
| --- | --- | --- |
| CRITICAL | Any | Urgent Assessment |
| MODERATE | Any | Priority Review |
| Any other configured indicator | < 6 | Priority Review |
| Any other configured indicator | >= 70 | Priority Review |
| Otherwise | Any | Routine |

Use LOW, MODERATE or CRITICAL in the synthetic payloads. These rules are artificial classroom examples, not medical guidance. `symptomsSummary` is collected for the human context; this policy deliberately does not infer a diagnosis from free text.

The staff form returns BOOK, WAITLIST or DECLINE. BOOK requires a nonempty `assignedSlot` at the gateway. A missing slot takes the default decline route rather than falsely confirming a booking. WAITLIST and DECLINE have distinct BPMN end events. Urgent handoff completes at its own end and never books a routine appointment.

## Results and limits

The API's available-slot/empty-slot behavior and injected failure/recovery are tested locally; see `../VALIDATION.md`. Patient age must be 0 through 125 and registration must be true. Available slots are supplied synthetic fixtures; there is no calendar locking or real hospital integration. The worker prepares availability requests; the Connector performs the HTTP call. Live Tasklist, Operate and recovery evidence remain pending.

## Required evidence

Run H1-H5 and capture routine booking, priority review, urgent handoff, no-slot waitlist and scheduling outage/recovery.
