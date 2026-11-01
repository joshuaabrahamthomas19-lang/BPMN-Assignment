# Validation record

Executed locally on 1 November 2026.

| Check | Actual result |
| --- | --- |
| Java/Spring Boot compilation and executable JAR packaging | PASS, Maven package |
| JUnit suite | PASS, 7 tests, 0 failures, 0 errors |
| Camunda 8.8 BPMN model/schema validation | PASS, all 3 BPMN files |
| DMN model/schema validation | PASS, all 3 DMN files, using the Camunda DMN model API |
| Offline graph integrity | PASS, 42 nodes; valid sequence references, incoming/outgoing consistency, start reachability and paths to end events |
| Diagram coverage and resource links | PASS, shapes and edges for all flow nodes/flows; decision and form references resolve locally |
| DMN rule boundaries | PASS, 18 cases via a restricted offline evaluator |
| Spring Boot HTTP smoke test | PASS, credit score response, payment decline, empty slots, injected HTTP 503 and success after disabling fault |
| Deployment to Camunda | NOT RUN; no configured cluster available |
| Engine execution / FEEL evaluation | NOT RUN |
| Tasklist and Operate screenshots | PENDING; must come from an actual running instance |
| Engine incident recovery | NOT RUN; local mock recovery is not equivalent |

The JUnit suite covers model validity, affordability and identity guards, inventory and cancellation outputs, refusal to ship an unpaid order, mock slot failure/correction and payment/credit API behavior. Tests use Java 23 while compiling to the Java 21 target.

The DMN schema check uses `org.camunda.bpm.model:camunda-dmn-model:7.23.0` as a test-only XML model parser. This is not a Camunda 7 execution dependency or proof of Camunda 8 decision execution. Production code uses `io.camunda:camunda-client-java:8.8.0`.

Commands from project root:

```powershell
python scripts/validate_models.py
mvn -f worker/pom.xml package
```

The HTTP smoke test started the packaged Spring Boot application with workers disabled on temporary port 18090. Production defaults remain port 8090. No live Camunda evidence is fabricated; use the scenario log in `docs/TEST-AND-EVIDENCE.md` to finish the submission evidence.
