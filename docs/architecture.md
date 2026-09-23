# Architecture Overview

## Architectural style

The application will begin as a modular monolith to reduce operational complexity while keeping clear domain boundaries. Market-data synchronization can run as an independently scheduled worker within the same deployable system initially and be extracted later if required.

## Initial modules

- `identity`: users, authentication, authorization, and account isolation
- `portfolio`: portfolios and portfolio-level settings
- `asset`: asset catalog and asset classification
- `transaction`: purchases, sales, transfers, fees, taxes, dividends, interest, and other events
- `valuation`: positions, average cost, realized/unrealized results, and historical valuation
- `marketdata`: provider adapters, quotes, exchange rates, and synchronization jobs
- `import`: CSV upload, mapping, validation, preview, deduplication, and import execution
- `reporting`: dashboard summaries, allocation, performance, and cash-flow reports

## Core design rules

1. Transactions are immutable business records whenever possible; corrections should be represented by compensating events or explicit audited adjustments.
2. Monetary amounts must use `BigDecimal` or an equivalent precise decimal representation.
3. Every user-owned record must be scoped to the authenticated user or account.
4. External market-data providers must be accessed through interfaces and adapters.
5. Market data must include source, timestamp, currency, and freshness metadata.
6. Imports must support dry-run validation before writing data.
7. Time and currency handling must be explicit, including Brazilian and US market conventions.

## Initial data model concepts

- User
- Portfolio
- Asset
- AssetIdentifier
- Transaction
- TransactionLot
- CashAccount
- MarketQuote
- CorporateAction
- IncomeEvent
- ImportBatch
- ImportRow
- AuditEvent

## Delivery sequence

1. Repository and local infrastructure scaffolding
2. Backend application bootstrap and database migrations
3. Authentication and user isolation
4. Portfolios, assets, and transactions
5. Position and performance calculations
6. CSV import pipeline
7. Market-data provider adapters
8. Dashboard and reporting
9. Automated tests and CI improvements
