# Lab 2 implementation report: fulfilment with explicit release

## Architecture and execution

The Spring Boot inventory worker checks synthetic order references, quantity, price and stock and computes `orderTotal`. Insufficient inventory ends before payment. `studio_dispatch_policy.dmn` selects the dispatch route. Orders at or above 1500 go to a Tasklist verification form. Declined review releases the simulated reservation. Approved or direct orders use a payment REST Connector. Declined payments also release the reservation; successful payments proceed to dispatch.

## FIRST-hit priority policy

| Value | Customer category | Preference | Route |
| --- | --- | --- | --- |
| >= 1500 | Any | Any | AUDIT |
| Any | MEMBER | EXPRESS | PRIORITY |
| Any | Any | EXPRESS | EXPRESS |
| Any | Any | Any | ECONOMY |

The high-value rule intentionally wins over customer benefits. The scalar decision result is stored in `route`; the dispatch worker copies it to `deliveryPriority`. The default example totals 240 and routes to PRIORITY. A high-value approved order keeps AUDIT as its recorded priority category.

## Results and limits

Local checks cover stock and quantity boundaries, total calculation, payment acceptance/decline, release output and the unpaid-dispatch guard. Engine tests and screenshots remain pending. Stock is modeled in process variables, not a shared inventory database. This is not concurrency-safe reservation software. Stable demo tracking/transaction references aid repeatable classroom tests, but do not establish production payment idempotency.

## Required evidence

Run O1-O5. Capture a high-value review task, payment-decline release variables, the payment-service outage incident and the recovered dispatch.
