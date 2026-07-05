# ElderAid

A bilingual (Finnish/English) platform connecting elderly people, or their family members, with verified
helpers for errands, walks, and companionship - addressing both practical day-to-day help and loneliness
in old age. Family members can request and pay for help; workers (often students) browse and apply for
posted tasks. The platform takes a commission on each completed booking, with payouts to workers planned
via Stripe Connect.

## Status

Active development. The backend REST API (authentication, worker verification, task/application/booking
lifecycle, reviews, and GDPR endpoints) is in place, along with a React frontend covering the main user
flows in both Finnish and English. Payments (Stripe Connect) and deployment are the main pieces still to
come.

## Tech stack

- **Backend:** Java 21, Spring Boot 3.4, Spring Data JPA, Spring Security, PostgreSQL, Flyway
- **Frontend:** React + TypeScript, Vite, TailwindCSS, react-i18next, React Query
- **Testing:** JUnit + Testcontainers (backend integration tests against real Postgres), Vitest + React Testing Library (frontend)
- **Payments (planned):** Stripe Connect
- **Infra:** Docker Compose for local dev

## Project structure

```
elderaid/
  backend/     Spring Boot REST API
  frontend/    React + TypeScript app
  docker-compose.yml
```

## Running locally

Requires Docker and Docker Compose.

```bash
docker compose up --build
```

This starts Postgres and the backend API on `http://localhost:8080`. Flyway migrations run automatically
on startup.

To run the backend outside Docker (e.g. from your IDE), start just the database:

```bash
docker compose up postgres
```

then run `BackendApplication` with the `dev` profile active.

To run the frontend:

```bash
cd frontend
npm install
npm run dev
```

This serves the app on `http://localhost:5173` and expects the backend at `http://localhost:8080`. The
frontend test suite runs with `npm test`.

## Roadmap

- [x] Domain model and database schema (Flyway)
- [x] Authentication and role-based access control (JWT with rotating refresh tokens, httpOnly cookie persistence)
- [x] Worker verification flow (document upload + admin review)
- [x] Task posting, browsing, and application flow
- [ ] Matching/recommendation logic
- [x] Booking lifecycle (accept/reject applications, check-in/check-out)
- [x] Reviews and ratings for completed bookings
- [ ] Stripe Connect payment integration
- [x] GDPR endpoints (data export, account deletion/anonymization including uploaded verification file cleanup)
- [x] React frontend with Finnish/English i18n (auth, profiles, task posting/browsing/applying, application review, worker bookings/check-in-out, family booking status, reviews, and privacy settings)
- [x] Backend integration tests (Testcontainers) and frontend unit tests (Vitest)
- [ ] Worker verification document upload UI (backend done, frontend pending)
- [ ] CI/CD and deployment (EU region)
