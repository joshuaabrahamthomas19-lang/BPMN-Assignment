# Lab 2: E-Commerce Order Processing and Fulfilment

## Aim

To validate an order, check stock, decide delivery priority, process a mock payment and dispatch the order.

## Process

The inventory worker calculates the order total and checks whether the requested quantity is available. The DMN table decides the delivery priority and whether manual verification is needed. High-value orders go to an auditor in Tasklist. Approved orders continue to the payment API. Only a successful payment reaches fulfilment.

## Decision Rules

The DMN table uses FIRST hit policy.

| Condition | Priority | Shipping lane | Manual verification |
| --- | --- | --- | --- |
| Total at least 1200 | EXPRESS | SECURED_COURIER | Yes |
| VIP or PLATINUM, with EXPRESS or SAME_DAY delivery | CRITICAL | AIR_EXPRESS | No |
| REGULAR customer with EXPRESS delivery | HIGH | PRIORITY_GROUND | No |
| Total from 100 to 1199 with STANDARD delivery | STANDARD | STANDARD_GROUND | No |
| Otherwise | LOW | ECONOMY_POST | No |

The mock prices are 800 for a laptop, 550 for a phone and 60 for other products. Quantities must be positive whole numbers and no more than 80. `PROD-OUT-OF-STOCK` is used for the unavailable-stock test.

## API Example

`POST /api/payment`

```json
{"orderId":"ORD-2101","customerRef":"CUST-2101","orderTotal":550,"paymentToken":"tok_synthetic_success","simulateError":false}
```

The response contains `paymentStatus`, `transactionId` and `message`. `tok_declined` and `tok_failed` return a declined payment. The Connector saves the message as `paymentMessage`.

## Variables

| Variable | Type | Purpose |
| --- | --- | --- |
| orderId, customerRef, productId | string | Order references |
| quantity | number | Number of items |
| customerCategory, deliveryPreference | string | Priority inputs |
| paymentToken | string | Synthetic payment token |
| mockApiBaseUrl | string | API base address |
| simulateWorkerFailure, simulatePaymentError | boolean | Failure-test switches |
| isStockAvailable | boolean | Stock-check result |
| unitPrice, orderTotal | number | Calculated price and total |
| deliveryPriority, shippingLane | string | DMN outputs |
| requiresManualVerification | boolean | DMN review flag |
| isAuditorApproved | boolean | Auditor's decision |
| paymentStatus, transactionId, paymentMessage | string | Payment results; transactionId can be null on decline |
| fulfillmentStatus, trackingNumber | string | Dispatch results |

## Sample Process Input

Use these variables when starting an instance:

```json
{
  "orderId": "ORD-2101",
  "customerRef": "CUST-2101",
  "productId": "PROD-PHONE",
  "quantity": 1,
  "customerCategory": "REGULAR",
  "deliveryPreference": "STANDARD",
  "paymentToken": "tok_synthetic_success",
  "simulateWorkerFailure": false,
  "simulatePaymentError": false,
  "mockApiBaseUrl": "http://host.docker.internal:8090"
}
```

## Tests

| Scenario | Expected result | Actual engine result |
| --- | --- | --- |
| Stock available and payment successful | Paid and dispatched | Pending |
| Product out of stock | Cancelled before payment | Pending |
| Payment declined | Cancelled after payment decline | Pending |
| High-value order | Auditor review, then payment if approved | Pending |
| Payment API failure | Payment incident followed by recovery | Pending |

In the review form, check `Approve order?` to approve the purchase. For the payment failure, inspect the error in Operate, change `simulatePaymentError` to false, then retry the same job after assigning positive retries where required.

Capture the Modeler diagram, deployment result, auditor task and Operate failure/recovery screens. The worker uses simulated stock and does not update a real inventory database.
