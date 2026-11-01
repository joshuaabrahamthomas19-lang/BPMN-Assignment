# Lab 1: Loan Approval

## Aim

To process a loan application using applicant validation, a credit-assessment API, a DMN decision and human review.

## Process

The worker checks the applicant's ID, name, age and employment status. A valid application goes to the credit API. The DMN table returns Approved, Manual Review or Rejected. Approved applications reach the disbursement worker. Review cases go to the loan officer in Tasklist, who can approve, reject or send the application back for validation.

An HTTP 500 from the credit API is caught by the boundary error event and sent for manual review. Other technical failures can exhaust retries and require attention in Operate.

## Decision Rules

The table uses FIRST hit policy, so the first matching rule is used.

| Credit score | Monthly income | Requested amount | Result | Rate |
| --- | --- | --- | --- | --- |
| 740 or more | 5000 or more | Up to 40000 | Approved | 5.9% |
| 700 or more | 7000 or more | Above 40000 | Manual Review | 6.8% |
| 650-739 | 4000 or more | Up to 25000 | Approved | 8.2% |
| 600-649 | 3000 or more | Up to 30000 | Manual Review | 9.5% |
| Any | Below 2500 | Any | Rejected | 0% |
| Below 600 | Any | Any | Rejected | 0% |
| Otherwise | Any | Any | Manual Review | 10% |

These are sample rules for the assignment, not a real lending policy.

## API Example

`POST /api/credit-assessment`

```json
{"applicantId":"APP-2101","monthlyIncome":6200,"requestedAmount":30000,"customCreditScore":770,"simulateError":false}
```

The response includes `creditScore`, `creditTier`, `creditReportId` and `status`. The custom score is a test input so each decision route can be checked.

## Variables

| Variable | Type | Purpose |
| --- | --- | --- |
| applicantId, applicantName | string | Applicant details |
| applicantAge | number | Must be at least 18 |
| employmentStatus | string | EMPLOYED or UNEMPLOYED |
| monthlyIncome, requestedAmount | number | DMN inputs, in the same currency |
| customCreditScore | number | Synthetic API score override |
| mockApiBaseUrl | string | API base address reachable by the Connector |
| simulateWorkerFailure, simulateCreditError | boolean | Failure-test switches |
| isValidApplicant, validationMessage | boolean, string | Validation results |
| creditScore, creditTier, creditReportId | number, string, string | Credit API results |
| loanClassification, riskRating, interestRate | string, string, number | DMN outputs |
| officerDecision | string | Approve, Reject or Retry |
| disbursementStatus, disbursementReference | string | Simulated payout results |

## Sample Process Input

Use these variables when starting an instance:

```json
{
  "applicantId": "APP-2101",
  "applicantName": "Demo Applicant",
  "applicantAge": 29,
  "employmentStatus": "EMPLOYED",
  "monthlyIncome": 6200,
  "requestedAmount": 30000,
  "customCreditScore": 770,
  "simulateWorkerFailure": false,
  "simulateCreditError": false,
  "mockApiBaseUrl": "http://host.docker.internal:8090"
}
```

## Tests

Start separate instances for the scenarios below, changing the sample variables as needed.

| Scenario | Expected result | Actual engine result |
| --- | --- | --- |
| Approval criteria met | Approved and disbursed | Pending |
| Low income and credit score | Rejected by DMN | Pending |
| Large loan requiring review | Loan officer task | Pending |
| Credit API error | Boundary error leads to officer review | Pending |
| Worker failure | Incident, then recovery after correction | Pending |

For the worker failure, wait for retries to reach zero. In Operate, inspect the error, set `simulateWorkerFailure` to false and retry the failed job with positive retries. Record the completed instance after recovery. For the credit-error case, clear `simulateCreditError` before choosing Retry if a fresh credit check is needed.

Capture the Modeler diagram, deployment result, manual review task and Operate incident/recovery screens from the running application. No live screenshots are included yet.
