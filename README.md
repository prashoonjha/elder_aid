# ElderAid

A bilingual (Finnish/English) platform connecting elderly people, or their family members, with verified
helpers for errands, walks, and companionship - addressing both practical day-to-day help and loneliness
in old age. Family members can request and pay for help; workers (often students) browse and apply for
posted tasks. The platform takes a commission on each completed booking and handles payouts via Stripe
Connect.

## Status

Early development. Core domain model and database schema are in place; authentication, the matching
engine, and the React frontend are next.

## Tech stack

- **Backend:** Java 21, Spring Boot 3.4, Spring Data JPA, Spring Security, PostgreSQL, Flyway
- **Frontend (planned):** React + TypeScript, Vite, TailwindCSS, react-i18next
- **Payments (planned):** Stripe Connect
- **Infra:** Docker Compose for local dev, GitHub Actions for CI

## Project structure

```
elderaid/
  backend/     Spring Boot REST API
  frontend/    React app (not yet scaffolded)
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

## Roadmap

- [x] Domain model and database schema (Flyway V1)
- [x] Authentication and role-based access control (JWT)
- [x] Worker verification flow (document upload + admin review)
- [x] Task posting, browsing, and application flow
- [ ] Matching/recommendation logic
- [ ] Booking lifecycle (accept/reject applications done; check-in/check-out next)
- [ ] Stripe Connect payment integration
- [ ] GDPR endpoints (data export, deletion/anonymization, consent records)
- [ ] React frontend with Finnish/English i18n (auth, profiles, task posting, and worker browse/apply done; booking lifecycle screens next)
- [ ] CI/CD and deployment (EU region)
