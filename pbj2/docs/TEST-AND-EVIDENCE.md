# Live execution and evidence plan

No placeholder screenshots are included. `../examples/scenarios.json` contains start-variable changes for all minimum scenarios. Begin with the matching base payload and apply the listed patch before starting a fresh instance.

## Failure and recovery procedure

1. Enable a persistent mock fault before starting the instance:

```powershell
Invoke-RestMethod -Method Put -Uri http://localhost:8090/demo/faults/credit -ContentType application/json -Body '{"enabled":true}'
```

2. Start the relevant instance. Wait until retries are exhausted and an incident is visible in Operate. Capture the process instance key, activity, variables and error. Connector HTTP 503 is technical failure; it is not a business decline.
3. Correct the cause by sending the same PUT with `{"enabled":false}`. For L5 use `worker`, for O5 `payment`, for H5 `slots`. The injected worker fault affects all job types, so isolate that test from concurrent lab runs.
4. In Operate, use the retry/resolve action available in your version and give the failed job positive retries if needed. Follow the same instance to its expected end. Record the exact action and after-recovery screenshot. Disabling a fault does not itself reset an exhausted job.

To demonstrate a credit timeout instead of 503, temporarily use an unreachable test API base URL, inspect the timeout, restore `mockApiBaseUrl` and retry. Do not overwrite the failing instance with a new one as evidence of recovery.

## Required scenario log

| ID | Start payload / action | Expected route | Actual outcome | Status |
| --- | --- | --- | --- | --- |
| L1 | Loan base | Approved, simulated payout | Not run on engine | Pending |
| L2 | mockCreditScore=550 | Rejected, no payout | Not run on engine | Pending |
| L3 | mockCreditScore=680; officer approves | Manual Review, payout | Not run on engine | Pending |
| L4 | Credit fault on; fix and retry | Credit incident, then payout | Not run on engine | Pending |
| L5 | Worker fault on; fix and retry | Worker incident, then payout | Not run on engine | Pending |
| O1 | Order base | Reserved, payment accepted, dispatched | Not run on engine | Pending |
| O2 | stockOnHand=0 | Stock unavailable; no payment | Not run on engine | Pending |
| O3 | paymentToken=demo_decline | Release reservation, cancel | Not run on engine | Pending |
| O4 | unitPrice=900; auditor approves | AUDIT task, then dispatch | Not run on engine | Pending |
| O5 | Payment fault on; fix and retry | Payment incident, then dispatch | Not run on engine | Pending |
| H1 | Patient base | Routine, confirmed booking | Not run on engine | Pending |
| H2 | urgency=MODERATE; staff BOOK | Priority Review, confirmed | Not run on engine | Pending |
| H3 | urgency=CRITICAL | Immediate human handoff | Not run on engine | Pending |
| H4 | availableSlots=[]; staff WAITLIST | Staff review, waitlist end | Not run on engine | Pending |
| H5 | Slots fault on; fix and retry | Scheduling incident, then confirmed | Not run on engine | Pending |

Additional checks: decline both review forms; decline a patient request; try BOOK without an assigned slot; submit an underage loan; inspect 1500 exactly for the audit boundary. Review checkboxes left unchecked mean false, so the default gateway rejects.

## Capture checklist

- For each lab: Modeler BPMN and DMN screenshots; deployment result with version/key; at least one successful instance with variables; all expected branch outcomes.
- L3 and O4: Tasklist screenshot before and after the review decision.
- H2/H3/H4: Tasklist staff/urgent tasks and corresponding Operate end states.
- L4/L5/O5/H5: before-fix incident, diagnosed cause, corrective action, retry action and the same instance completed after recovery.
- Save screenshots under `evidence/<lab>/<scenario>/` with a short note containing instance key, date, input file and observed result. Only add images captured from your own implementation.

Update the actual-outcome and status columns after execution and include those results in the individual lab reports. The local JUnit API fault test proves only the mock can fail and recover; it does not satisfy the engine incident evidence requirement.
