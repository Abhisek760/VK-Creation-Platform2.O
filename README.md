# VK Creation Platform

Photography booking and studio management platform using Next.js, TypeScript, Tailwind CSS, Spring Boot, Spring Security/JWT, JPA/Hibernate and MySQL.

## Repository structure
- `frontend/` — Next.js web app (landing page, login, registration, client booking portal)
- `backend/` — Spring Boot REST API and Flyway database migrations

## Run locally
1. Install Java 21, Maven 3.9+, Node.js 20+, and MySQL 8+.
2. Create database `vk_creation` (or allow MySQL user to create it through the configured URL).
3. In `backend/`, configure `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `JWT_SECRET` (base64 secret decoding to at least 32 bytes), then run `mvn spring-boot:run`.
4. In `frontend/`, copy `.env.example` to `.env.local`, run `npm install`, then `npm run dev`.
5. Open `http://localhost:3000`.

Public registration creates CLIENT accounts only. Create the first ADMIN through a trusted database/operations process; do not enable public admin registration. Payment references remain unverified until an admin reviews them. A real gateway, deployment secrets, and end-to-end runtime verification are still required before production use.

## Main API routes
- `POST /api/auth/register`, `POST /api/auth/login`
- `POST /api/bookings`, `GET /api/bookings/my`
- Admin: `GET /api/bookings`, `PATCH /api/bookings/{id}/advance`, `PATCH /api/bookings/{id}/status`
- Client: `POST /api/bookings/{id}/payments`, `GET /api/payments/my`
- Admin: `GET /api/payments`, `PATCH /api/payments/{id}/verify`
