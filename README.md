# MyPortfolio

Investment portfolio management application for Brazilian and US markets.

## Initial scope

The first release is planned to support:

- Brazilian stocks (B3)
- FIIs
- Tesouro Direto
- CDBs and other fixed-income investments
- US stocks
- ETFs
- Cryptocurrencies
- Manual transaction entry
- CSV import
- Automatic market-data integrations
- Multi-user access with isolated portfolios
- Local-first deployment

## Planned architecture

The project will start as a modular monolith with separate market-data processing capabilities:

- **Backend:** Kotlin + Spring Boot
- **Frontend:** React + TypeScript
- **Database:** PostgreSQL
- **Migrations:** Flyway
- **Local environment:** Docker Compose
- **API:** REST, with GraphQL as an optional extension
- **Testing:** Unit, integration, API, and end-to-end tests

## Repository structure

```text
.
├── backend/              # Kotlin/Spring Boot application
├── frontend/             # React/TypeScript application
├── database/             # Database documentation and migration notes
├── docs/                 # Architecture and product documentation
├── infrastructure/       # Local infrastructure configuration
├── docker-compose.yml    # Local development services
└── README.md
```

## Current status

This repository contains the initial project documentation and scaffolding. Business modules will be implemented incrementally, beginning with authentication, users, portfolios, assets, and transactions.

## Development principles

- Keep user data isolated by account and portfolio ownership.
- Store monetary values using precise decimal types, never binary floating-point values.
- Preserve transaction history as an auditable source of truth.
- Separate market-data providers behind adapters.
- Make imports idempotent and provide validation previews before persistence.
- Keep the local development experience reproducible through Docker Compose.
