# PBJ-2 Practical Assignment

This folder contains the three Camunda 8 assignments: loan approval, order processing and hospital appointment management. Each lab has a BPMN process, a DMN decision table and Java workers built with Spring Boot. Mock REST APIs are used for credit checks, payments and appointment slots.

## Lab 1: Loan Approval

The process checks the applicant's details and gets a credit score from the mock API. The decision table then classifies the application as Approved, Rejected or Manual Review.

The rules consider the credit score, requested amount and repayment affordability. Applications needing review go to a loan officer in Tasklist. Approved applications continue to a simulated disbursement step.

## Lab 2: Order Processing and Fulfilment

The worker checks the order details, verifies stock and calculates the total. The decision table sets the delivery priority based on order value, customer category and delivery preference.

Orders worth 1500 or more need manual verification. After approval, the process calls the mock payment API. A successful payment leads to dispatch. If payment or manual verification is declined, the process releases the stock reservation and cancels the order.

## Lab 3: Hospital Appointment and Patient Triage

The process validates the patient's registration and uses a decision table to classify the request as Routine, Priority Review or Urgent Assessment.

Urgent requests go directly to a human assessment task. Other requests use the mock scheduling API to check available slots. Routine requests with a slot can be booked directly. Priority requests and requests without a slot go to staff review, where staff can book an appointment, add the patient to the waitlist or decline the request.

The patient details and triage rules are only for this assignment. They are not intended for medical use.

## Folder Contents

| Folder | Contents |
| --- | --- |
| `models` | BPMN processes and DMN decision tables for all three labs |
| `worker` | Spring Boot application, Java workers, mock APIs and tests |
| `forms` | Forms for the human tasks in Tasklist |
| `examples` | Sample input data and test scenarios |
| `docs` | Lab reports, API details, process variables and the test checklist |
| `scripts` | A Python script for checking the model files |

## How to Run

The project uses Java 21 or later, Maven 3.9 or later and Camunda 8.8. Tasklist, Operate and the REST Connector runtime are needed to run the complete workflows.

Start Camunda, then open a terminal in the `worker` folder and run:

```powershell
mvn clean verify
$env:CAMUNDA_REST_ADDRESS='http://localhost:8080'
$env:CAMUNDA_GRPC_ADDRESS='http://localhost:26500'
mvn spring-boot:run
```

The mock APIs run on port 8090. These commands assume a local Camunda setup. If the cluster requires authentication, configure its client credentials as described in the [Camunda Java client documentation](https://docs.camunda.io/docs/8.8/apis-tools/java-client/getting-started/).

Next:

1. Open the model files in Camunda Modeler.
2. Deploy the DMN files and forms, followed by the BPMN files, to the same cluster and tenant.
3. Start a process using the matching JSON file in `examples`.
4. Complete any review tasks in Tasklist and check the process in Operate.

The process IDs are `studio_loan`, `studio_order` and `studio_care`.

Each sample input includes `mockApiBaseUrl`. Use `http://host.docker.internal:8090` when the Connector runtime runs in Docker Desktop and the API runs on the host. Use `http://localhost:8090` when both run directly on the same computer. A remote cluster needs an API address it can reach.

## Testing

The Java build and seven automated tests pass. The BPMN and DMN files also pass model validation, and the Python script checks 18 decision-rule cases. The mock APIs were checked for normal responses, payment decline, no available slots and recovery after a simulated outage.

To run the checks from the `pbj2` folder:

```powershell
mvn -f worker/pom.xml clean verify
python scripts/validate_models.py
```

Full workflow execution in Camunda, Tasklist screenshots and Operate incident-recovery evidence are still pending. The scenarios and steps for recording those results are in [the test checklist](docs/TEST-AND-EVIDENCE.md). Details of the completed checks are in [VALIDATION.md](VALIDATION.md).

## Lab Reports

- [Loan approval](docs/LAB-1.md)
- [Order processing and fulfilment](docs/LAB-2.md)
- [Hospital appointment and triage](docs/LAB-3.md)
- [API examples and process variables](docs/API-AND-VARIABLES.md)

The concept reference is recorded in [PROVENANCE.md](docs/PROVENANCE.md).
