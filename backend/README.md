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
   - `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET` (required for signed gallery uploads)
   - `PHONEPE_ENABLED=false` by default; after merchant onboarding configure `PHONEPE_CLIENT_ID`, `PHONEPE_CLIENT_SECRET`, `PHONEPE_CLIENT_VERSION`, `PHONEPE_BASE_URL`, and `PHONEPE_REDIRECT_URL` to the values for your approved sandbox/production environment.
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
- `GET /api/payments/{id}/receipt` and `GET /api/payments/{id}/receipt.pdf` — HTML/PDF receipt for a verified installment
- `GET /api/bookings/{id}/final-bill` and `GET /api/bookings/{id}/final-bill.pdf` — HTML/PDF final bill after the full total is verified
- `GET /api/packages` — active public package list; admin CRUD under `/api/packages`
- `GET /api/gallery` — published portfolio media; admin CRUD under `/api/gallery`
- `POST /api/media/signature` — admin-only Cloudinary upload signature
- `GET /api/settings` and admin-only `PUT /api/settings` — studio branding, UPI/bank instructions, and client terms
- `POST /api/payments/phonepe/start/{bookingId}` — creates a PhonePe checkout order when configured
- `GET /api/payments/phonepe/status/{orderId}` — checks status directly with PhonePe; only a server-reported completed payment is counted as verified

Payment references are never treated as verified merely because a client submits them. Booking-row locks serialize installment submission and payment review operations. Receipt and final-bill endpoints return printable HTML (not PDF). PhonePe integration requires valid merchant credentials and sandbox/UAT verification before live use. Cloudinary uploads require valid account credentials. There is not yet a PhonePe webhook; the portal polls the server-side order status. Email/SMS notifications, immutable audit/archive history, and generated PDF documents remain future work before production.
