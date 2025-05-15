#  Home Assignment: Payment Service


````markdown

A compact Java 21 ✕ Spring Boot 3 application

---

## 1  Quick start

```bash
# build
mvn clean package

java -jar target/payment-service-0.0.1-SNAPSHOT.jar
````

The service require a Postgres database.
Flyway scripts will auto-create the `payments` table.

---

## Endpoint summary

| Method | Path              | Purpose                                | Idempotency key                         |
| ------ |-------------------|----------------------------------------| --------------------------------------- |
| POST   | `api/v1/payments` | Make a payment and route to a provider | **Required** – `Idempotency-Key` header |

---

## Routing logic 

| Rule order | Predicate             | Provider | Rationale                                 |
| ---------- | --------------------- | -------- | ----------------------------------------- |
| 1          | `BIN` starts with `4` | A        | Mimics Visa routing in many PSPs          |
| 2          | `currency == EUR`     | B        | Demonstrates currency-based optimisation  |
| 3          | `amount > 1000`       | B        | Large-ticket flows may need a cheaper PSP |
| 4          | *(default)*           | A        | Simplicity: fall back to first provider   |

---

## Resilience & graceful degradation

| Concern               | Resilience4j guard    |  Behaviour                             |
| --------------------- | --------------------- | ------------------------------------ |
| Transient 5xx / I/O   | `Retry`               | 3–4 attempts, 200–300 ms back-off    |
| Persistent failure    | `CircuitBreaker`      | Opens after N errors, auto-half-opens |
| Overload              | `RateLimiter`         | 50 req/s to A, 20 req/s to B         |
| Timeout               | `WebClient` + `Retry` | 2 s read timeout & retry             |
| Validation error      | Bean Validation       | 400 Bad Request                      |
| 429 Too Many Requests | No retry              | Passes 429 to caller                 |

---

## Idempotency & data model

* Each caller **must** send an `Idempotency-Key` header.

This guarantees **at-most-once** execution across a multi-pod deployment.

---