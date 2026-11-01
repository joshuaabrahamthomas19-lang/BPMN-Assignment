# PBJ-2 Practical Assignment

This assignment contains three workflows made using Camunda 8:

1. Loan approval
2. E-commerce order processing and fulfilment
3. Hospital appointment and patient triage

Each lab has a BPMN process and a DMN decision table. The `common-worker` folder contains the Spring Boot workers and mock APIs used by all three labs.

## Folder Structure

```text
pbj2/
  README.md
  common-worker/
    pom.xml
    mvnw
    mvnw.cmd
    src/
  lab1-loan-approval/
    loan_approval_process.bpmn
    loan_classification.dmn
    loan_review.form
    README.md
  lab2-ecommerce-fulfillment/
    order_processing_process.bpmn
    order_priority.dmn
    order_verification.form
    README.md
  lab3-hospital-triage/
    hospital_triage_process.bpmn
    patient_triage.dmn
    staff_review.form
    urgent_assessment.form
    README.md
```

## Requirements

- Java 21 or later
- Camunda 8.8, with Tasklist and Operate
- REST Connector runtime connected to the same Camunda cluster
- Camunda Modeler

The Maven wrapper is included, so a separate Maven installation is optional.

## Running the Application

Start Camunda first. Open Command Prompt in `common-worker` and run:

```cmd
set CAMUNDA_REST_ADDRESS=http://localhost:8080
set CAMUNDA_GRPC_ADDRESS=http://localhost:26500
mvnw.cmd clean verify
mvnw.cmd spring-boot:run
```

The mock API runs on port 8090, separate from Camunda's port 8080. These commands are for a local setup. If the cluster requires authentication, configure the Java client's authentication environment variables for that cluster.

Open the BPMN, DMN and form files in Modeler. Deploy the DMN files and forms first, followed by the BPMN files, using the same cluster and tenant. Then start an instance using the sample process variables in the lab's README.

The input variable `mockApiBaseUrl` tells the REST Connector where the API is running. The examples use `http://host.docker.internal:8090`, for a Connector runtime running in Docker Desktop. Change it to `http://localhost:8090` if the Connector runs directly on the host. A remote Connector needs an API address it can reach.

## Process IDs

| Lab | Process ID |
| --- | --- |
| Loan approval | `PBJ2_LoanApproval` |
| Order processing | `PBJ2_OrderProcessing` |
| Hospital triage | `PBJ2_HospitalTriage` |

## Testing

The lab READMEs explain the inputs, decision rules and expected results. They also include the failure and recovery steps. The source tests are included in `common-worker/src/test` and can be run with `mvnw.cmd clean verify`.

Camunda deployment, Tasklist screenshots and Operate incident-recovery screenshots still need to be recorded from a running instance. The test tables mark those results as pending.

All input data and API responses are synthetic. Payments, loan disbursement and bookings are demonstrations only.

Reference: [PBJ-2 assignments](https://github.com/AlphinShabu/camunda/tree/main/pbj2-assignments). This version adapts that structure and implementation with changes to the rules, inputs and setup.
