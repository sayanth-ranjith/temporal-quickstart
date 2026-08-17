# Order Shipment Workflow

A Temporal + Spring Boot service that models an order's lifecycle as a durable
workflow: placed → received by team → packed by team → dispatched → delivered,
with a 15 second post-delivery window during which a return can be requested,
spinning up a `ReturnItemWorkflow` as a child workflow (item picked up → money
refunded).

Both the worker and its task queue are named **`order-shipment-worker`**.

## Prerequisites

- Java 17
- The Temporal CLI, to run a local Temporal server — install from
  [temporal.io](https://docs.temporal.io/cli#install)

## 1. Start Temporal

Spin up a local Temporal server (this also serves the Temporal Web UI):

```bash
temporal server start-dev
```

Leave this running. It listens on `127.0.0.1:7233` (matches
`src/main/resources/application.yml`) and serves the Web UI at
[http://localhost:8233](http://localhost:8233) — keep it open in a browser tab
so you can watch workflow executions, their event history, and activity
retries as you trigger the APIs below.

## 2. Start the app

In a second terminal, from the project root:

```bash
./mvnw spring-boot:run
```

On startup you should see log lines confirming the worker registered its
workflows and activities on the `order-shipment-worker` task queue. The API
is served on `http://localhost:8080`.

## 3. Walk through the order flow

Trigger these one at a time and check the status/Web UI between steps to see
the workflow advance.

**Place the order** — starts the workflow (`order-ORD-1001` in the Web UI):

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"orderId":"ORD-1001","customerId":"CUST-5001","items":["Running Shoes - Size 9","Cotton Socks (2 pack)"],"amount":79.98}'
```

**Check status** (workflow is now waiting on a signal):

```bash
curl http://localhost:8080/api/orders/ORD-1001/status
# "ORDER_PLACED"
```

**Team confirms receipt:**

```bash
curl -X POST http://localhost:8080/api/orders/ORD-1001/received-by-team
curl http://localhost:8080/api/orders/ORD-1001/status
# "ORDER_RECEIVED_BY_TEAM"
```

**Team confirms packing:**

```bash
curl -X POST http://localhost:8080/api/orders/ORD-1001/packed-by-team
curl http://localhost:8080/api/orders/ORD-1001/status
# "ORDER_PACKED_BY_TEAM"
```

**Confirm dispatch** — this also marks the order delivered and opens the
15 second return window:

```bash
curl -X POST http://localhost:8080/api/orders/ORD-1001/dispatched
curl http://localhost:8080/api/orders/ORD-1001/status
# "ORDER_DISPATCHED" then "ORDER_DELIVERED"
```

**Option A — let it complete normally:** do nothing for 15 seconds. The
workflow finishes on its own; the Web UI shows it as `Completed`.

**Option B — request a return within the 15 second window:**

```bash
curl -X POST http://localhost:8080/api/orders/ORD-1001/return \
  -H "Content-Type: application/json" \
  -d '{"orderId":"ORD-1001","reason":"Wrong size","amount":79.98}'
```

Watch the Web UI: the parent workflow starts a `return-ORD-1001` **child
workflow**, which runs `pickupOrderItem` then `refundMoney`, then the parent
completes with `RETURN_COMPLETED`.

```bash
curl http://localhost:8080/api/orders/ORD-1001/status
# "RETURN_INITIATED" -> "RETURN_COMPLETED"
```

A return request sent *after* the 15 second window is logged and ignored —
try it late to see the difference.

## 4. Watch Temporal retry (and fail)

A separate, self-contained demo endpoint — not part of the order flow —
proves out Temporal's retry behavior. It calls the workflow synchronously, so
the `curl` call blocks while retries happen; watch the app console (and the
`retry-demo-<uuid>` execution in the Web UI) while it runs.

**Retry then recover** (fails on attempts 1–2, succeeds on attempt 3):

```bash
curl -X POST "http://localhost:8080/api/diagnostics/retry-demo?succeedOnAttempt=3"
```

**Exhaust retries and fail** (asks to succeed on attempt 99, past the
workflow's configured cap of 5 attempts):

```bash
curl -X POST "http://localhost:8080/api/diagnostics/retry-demo?succeedOnAttempt=99"
```

The first call returns `200 OK` after ~3 seconds of visible backoff. The
second retries 5 times over ~12 seconds, then returns `500` once the
workflow fails.

## Project layout

```
config/      TemporalConstants — shared task queue name
model/       Request/response records + OrderStatus enum
activity/    @ActivityInterface + Spring-managed @ActivityImpl implementations
workflow/    @WorkflowInterface + @WorkflowImpl implementations
controller/  REST endpoints that start workflows, send signals, and query status
```

Workflow implementations are plain classes (Temporal, not Spring, owns their
lifecycle); activity implementations are `@Component` beans so they can have
real dependencies (DB, HTTP clients, etc.) injected as this evolves beyond a
demo.
