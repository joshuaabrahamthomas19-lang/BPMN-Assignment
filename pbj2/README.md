# Workflow Studio: three Camunda 8 practical labs

An original implementation of the PBJ-2 loan, commerce and patient-access exercises. The common pattern is validation by a Java worker, service integration through a REST Connector, DMN routing, human decisions in Tasklist and observable recovery in Operate.

**Status:** source and offline checks are provided. A running Camunda cluster is required to finish deployment, execution and screenshot evidence. No live results or screenshots are claimed by this repository.

## What is different

| Lab | Preserved concept | Original design choices |
| --- | --- | --- |
| Loan | Applicant validation, credit API, decision, officer review, payout | Affordability ratio, rejection rules evaluated first, new thresholds and a principal-only payment model |
| Commerce | Inventory, priority, payment, review, shipping | Explicit stock-release path after refusal or payment decline; member delivery priority; new high-value threshold |
| Patient access | Registration, triage, slots, staff intervention | All non-urgent requests look up slots before review; separate waitlist and decline endings; staff must supply a slot to book |

These are educational simulations. No real transfers, inventory reservations or clinical decisions occur. The patient rules illustrate orchestration only.

## Files

- `models/`: three executable BPMN diagrams with diagram coordinates and three FIRST-hit DMN tables.
- `forms/`: four Camunda user-task forms.
- `worker/`: Java/Spring Boot job workers, mock APIs and JUnit tests.
- `examples/`: synthetic start payloads and scenario variations.
- `docs/`: individual implementation reports, variables, API contracts and evidence checklist.
- `scripts/validate_models.py`: offline links, graph and rule-boundary checks.
- `VALIDATION.md`: actual local validation scope and results.

## Run the services

Requirements: JDK 21 or newer, Maven 3.9+, Camunda 8.8 with Tasklist, Operate and the REST Connector runtime. The model metadata and Java client are deliberately pinned to 8.8.0; use a compatible patched 8.8 installation. Python 3.11+ is needed only for the offline validator.

From `worker/`:

```powershell
mvn clean verify
$env:CAMUNDA_REST_ADDRESS='http://localhost:8080'
$env:CAMUNDA_GRPC_ADDRESS='http://localhost:26500'
mvn spring-boot:run
```

The mock API listens on port 8090. The default client reads its connection and authentication settings from the Camunda environment variables. For an authenticated cluster, also configure `CAMUNDA_CLIENT_ID`, `CAMUNDA_CLIENT_SECRET`, `CAMUNDA_AUTHORIZATION_SERVER_URL` and the audience required by your installation. Keep secrets outside Git. See [Camunda 8.8 Java client configuration](https://docs.camunda.io/docs/8.8/apis-tools/java-client/getting-started/).

To run only the mock API, set `STUDIO_WORKERS_ENABLED=false` before starting. Fault-control endpoints are for a trusted local classroom network only.

## Deploy and execute

1. Start your Camunda 8.8 installation and its REST Connector runtime. Configure Modeler's deployment connection for that cluster.
2. Open each BPMN and DMN file in Camunda Modeler. Deploy the DMNs and all four forms, then the BPMNs to the same tenant. User-task form references resolve by form ID; decision references resolve by decision ID.
3. Confirm the connector runtime can reach the Spring Boot API. Each start payload contains `mockApiBaseUrl`. `http://host.docker.internal:8090` is intended for Docker Desktop connectors calling the host. Use `http://localhost:8090` for a connector running directly on the host. For remote/SaaS connectors, supply a reachable test API URL. A remote connector cannot reach your computer's localhost.
4. Start `studio_loan`, `studio_order` or `studio_care` using the matching JSON in `examples/`. Use Modeler's start dialog or your cluster's process-instance API. Set variables as JSON with booleans and numbers retained as their native types.
5. Complete review tasks in Tasklist. Loan: `officerApproved`; order: `purchaseApproved`; patient scheduling: `staffResolution` and `assignedSlot`; urgent handoff: `handoffAcknowledged`. No fixed assignee is baked into the models: use the test account permitted to claim tasks in your installation.
6. Follow `docs/TEST-AND-EVIDENCE.md` to execute the required scenarios and capture your own screenshots.

REST Connector tasks use type `io.camunda:http-json:1`, POST, no authentication to the local mock, five-second timeouts and three retries. Input mappings contain the request bodies; `resultExpression` headers unpack response variables. They require a Connector runtime, not the Java workers in this project. Reference: [REST Connector](https://docs.camunda.io/docs/8.9/components/connectors/protocol/rest/).

## Validate locally

From the project root:

```powershell
python scripts/validate_models.py
mvn -f worker/pom.xml clean verify
```

The Python rule evaluator implements only the comparisons and string literals used here. It is not a general FEEL implementation. The Maven tests validate BPMN using Camunda's model API and exercise worker/API behavior. Neither check establishes engine deployment or incident recovery.

## Publish to your repository

The project contains no copied source files, compiled reference classes or credentials. Read `docs/PROVENANCE.md` for the conceptual reference and its inspected commit.

Create an empty repository in your account, then from this folder:

```powershell
git remote add origin https://github.com/YOUR_ACCOUNT/YOUR_REPOSITORY.git
git push -u origin main
```

Authenticate with your own account when prompted. If the destination already contains work, inspect it and merge intentionally rather than force-pushing.

For a downloaded copy with no Git history, first clone your target repository, copy this folder into `pbj2`, then run `git add pbj2` and `git commit -m "Add original Camunda 8 practical labs"` from the repository root. This preserves any earlier assignments.
