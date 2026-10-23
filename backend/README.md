# VK Creation Backend

Spring Boot REST API foundation for the VK Creation photography booking platform.

## Requirements
- Java 21
- Maven 3.9+
- PostgreSQL 15+

## Run locally
Create a PostgreSQL database named `vk_creation`, then run:

```bash
cd backend
mvn spring-boot:run
```

Configuration can be supplied using `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `SERVER_PORT`. Flyway applies versioned SQL migrations automatically. Hibernate is configured to validate the schema rather than modify it.

Health endpoint: `GET http://localhost:8080/actuator/health`

## Current scope
This commit establishes the application, database configuration, and initial booking/payment schema. Booking APIs, authentication/authorization, payment-provider verification, receipts, and client portal endpoints are not implemented yet. Never treat a client-submitted payment reference or screenshot as verified payment.