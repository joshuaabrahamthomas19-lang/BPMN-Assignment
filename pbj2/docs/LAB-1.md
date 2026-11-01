# Lab 1 implementation report: affordability-first lending

## Architecture and execution

`studio_loan.bpmn` coordinates the Spring Boot `studio.loan.check` job, the credit REST Connector, `studio_lending_policy.dmn`, an optional officer task and a simulated payout worker. Invalid identity/age or invalid monetary inputs terminate before the credit call. A technical error stops at the failing activity and uses job retries/incidents rather than silently becoming a loan rejection.

## Educational policy

The worker computes `paymentRatio = (monthlyDebt + requestedAmount / termMonths) / monthlyIncome`. This uses principal only and is not a real lending model. FIRST-hit rules run in this order:

| Credit score | Payment ratio | Amount | Classification |
| --- | --- | --- | --- |
| < 580 | Any | Any | Rejected |
| Any | > 0.45 | Any | Rejected |
| >= 720 | <= 0.30 | <= 40000 | Approved |
| Any | Any | Any | Manual Review |

Example: income 5000, debt 300, amount 20000, term 36 and score 760 produce a ratio of approximately 0.1711 and approval. A score of 579 rejects. Score 680 at the same affordability goes to review. The task form returns `officerApproved`; only true reaches payout. Payout generates a stable demonstration reference from the application ID.

## Results and limits

Worker calculations, identity guards, mock API behavior and XML model validity are checked locally; see `../VALIDATION.md`. Engine deployment, human task execution and incident recovery are pending. No actual money is moved. No interest or creditworthiness claim is made. A failed credit service requires correction and retry, not a substitute credit score.

## Required evidence

Run L1-L5 in `TEST-AND-EVIDENCE.md`. Capture the officer task, rejected and approved variables, exhausted retries, the diagnosed cause and the recovered instance. Add actual outcomes only after execution.
