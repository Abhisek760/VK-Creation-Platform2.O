# VK Creation Backend

Spring Boot REST API for the VK Creation photography booking platform.

## Stack
- Java 21 / Spring Boot 3
- Spring Web, Spring Security, JWT (JJWT), Spring Data JPA / Hibernate
- MySQL 8+ with Flyway migrations
- Bean Validation

## Local setup
1. Install Java 21, Maven 3.9+, and MySQL 8+.
2. Create the database: `CREATE DATABASE vk_creation CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`
3. Set environment variables before starting:
   - `DB_URL=jdbc:mysql://localhost:3306/vk_creation?useSSL=false&serverTimezone=UTC`
   - `DB_USERNAME=your_mysql_user`
   - `DB_PASSWORD=your_mysql_password`
   - `JWT_SECRET=<base64-encoded secret of at least 32 bytes>`
   - `CORS_ALLOWED_ORIGINS=http://localhost:3000`
4. Run `cd backend && mvn spring-boot:run`.

Do not use the development fallback for JWT in production. Configure a strong secret in the environment. The public registration endpoint always creates a CLIENT; provision the first ADMIN through a trusted database/operations process, never by accepting a role from public registration.

## API outline
- `GET /actuator/health` — public health check
- `POST /api/auth/register` — register a client
- `POST /api/auth/login` — issue JWT
- `POST /api/bookings` — authenticated client creates a booking
- `GET /api/bookings/my` — client's own bookings
- `GET /api/bookings` — admin booking list
- `PATCH /api/bookings/{id}/advance` — admin sets total and initial advance
- `PATCH /api/bookings/{id}/installment` — admin sets the next installment amount
- `PATCH /api/bookings/{id}/status` — admin updates status (confirmation is blocked until the advance is verified)
- `POST /api/bookings/{id}/payments` — client submits the currently due installment and proof/reference for manual review
- `GET /api/payments/my` — client's payment history
- `PATCH /api/payments/{id}/verify` — admin verifies or rejects a submitted payment
- `GET /api/payments/{id}/receipt` — client's printable receipt for a verified installment
- `GET /api/bookings/{id}/final-bill` — client's printable final bill after the full total is verified

Payment references are never treated as verified merely because a client submits them. Booking-row locks serialize installment submission and payment review operations. Receipt and final-bill endpoints return printable HTML (not PDF). A real payment gateway/webhook integration, media/package management, notifications, and end-to-end deployment verification remain separate work before production.
