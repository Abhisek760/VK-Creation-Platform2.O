# VK Creation Platform

VK Creation is a photography and wedding-booking management platform. The current repository contains a Next.js client/admin interface and a Spring Boot REST API backed by MySQL.

## Technology stack

- **Frontend:** Next.js 15, React 19, TypeScript, Tailwind CSS 4
- **Backend:** Java 21, Spring Boot 3, Spring Web, Spring Security, JWT, Spring Data JPA / Hibernate, Bean Validation
- **Database:** MySQL 8+ with Flyway schema migrations
- **Media:** Cloudinary signed browser uploads (admin-only signature endpoint)
- **Payments:** PhonePe Standard Checkout integration scaffold, disabled until merchant credentials are configured
- **CI:** GitHub Actions builds the backend and frontend on pushes and pull requests to `main`

## Repository structure

- `frontend/` — landing page, authentication, client portal, admin dashboard
- `backend/` — REST API, authentication/security, booking/payment services, Flyway migrations
- `.github/workflows/build.yml` — automated backend verification and frontend production build

## Run locally

1. Install Java 21, Maven 3.9+, Node.js 20+, and MySQL 8+.
2. Create a database: `CREATE DATABASE vk_creation CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`
3. In `backend/`, set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, and `CORS_ALLOWED_ORIGINS`. The JWT secret must be base64-encoded and decode to at least 32 bytes. Example CORS value: `http://localhost:3000`.
4. Start the API from `backend/` with `mvn spring-boot:run`.
5. In `frontend/`, copy `.env.example` to `.env.local`, install dependencies with `npm install`, and start the app with `npm run dev`.
6. Open `http://localhost:3000`.

Optional integrations:
- Set `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, and `CLOUDINARY_API_SECRET` in the backend environment to enable signed image/video uploads from `/admin/catalog`.
- After PhonePe merchant onboarding, set `PHONEPE_ENABLED=true`, `PHONEPE_CLIENT_ID`, `PHONEPE_CLIENT_SECRET`, and `PHONEPE_CLIENT_VERSION`; set `PHONEPE_BASE_URL` to the sandbox or production base URL provided by PhonePe, and set `PHONEPE_REDIRECT_URL` to the deployed client portal URL. Never put these secrets in frontend variables or commit them.

See [backend/README.md](backend/README.md) for backend configuration details. Never commit real credentials or production secrets.

## Booking and payment behavior currently implemented

- Public registration creates CLIENT accounts only. The first ADMIN must be provisioned through a trusted operations/database process.
- Public package listing and admin package CRUD are available at `/api/packages` and `/api/packages/admin`.
- Public gallery listing and admin gallery management are available at `/api/gallery` and `/api/gallery/admin`; `/api/media/signature` issues signed Cloudinary upload parameters to admins only.
- Studio contact details, UPI/QR/bank instructions, and client terms can be managed at `/api/settings` (read) and admin-only `PUT /api/settings`.
- Clients can submit booking requests and see their bookings.
- Admins can set booking totals and the initial advance, change booking status, and set subsequent installment amounts.
- Clients submit a payment method, transaction reference, and proof URL for manual review. **A submitted proof is not a payment confirmation.** The admin must verify it.
- The client must submit the currently configured installment amount. A new installment cannot be set while a submitted payment is awaiting review.
- A unique receipt number and generated PDF receipt are available for each verified installment. A generated PDF final bill becomes available only when verified payments cover the full booking total.
- Booking-row locking is used to serialize installment submissions and payment reviews against concurrent requests.

## API routes

- `POST /api/auth/register`, `POST /api/auth/login`
- Client: `POST /api/bookings`, `GET /api/bookings/my`
- Admin: `GET /api/bookings`, `PATCH /api/bookings/{id}/advance`, `PATCH /api/bookings/{id}/installment`, `PATCH /api/bookings/{id}/status`
- Client: `POST /api/bookings/{id}/payments`, `GET /api/payments/my`, `GET /api/payments/{id}/receipt`, `GET /api/payments/{id}/receipt.pdf`, `GET /api/bookings/{id}/final-bill`, `GET /api/bookings/{id}/final-bill.pdf`
- Admin: `GET /api/payments`, `PATCH /api/payments/{id}/verify`
- PhonePe: `POST /api/payments/phonepe/start/{bookingId}`, `GET /api/payments/phonepe/status/{orderId}`

## Important production limitations

This is **not yet a production-verified deployment**. PhonePe checkout code and server-side status polling are implemented but disabled until valid merchant credentials, merchant onboarding, callback/redirect URLs, and sandbox/UAT tests are configured. Do not enable it for live payments until PhonePe confirms the environment and a full payment/refund/reconciliation test is complete. Cloudinary signed upload and gallery management require Cloudinary credentials. Studio settings now include editable legal terms, but there is no e-signature workflow. Receipts/final bills are printable HTML pages, not generated PDF documents; automated notifications, true immutable archive/audit log, and production end-to-end tests remain outstanding. Configure production secrets, deploy the services, and complete end-to-end tests before accepting real customer data or payments.
