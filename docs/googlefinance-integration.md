# Google Finance / GOOGLEFINANCE Integration

## Decision

Use Google Sheets with the `GOOGLEFINANCE()` function as an optional market-data source, not as a direct backend API. The application must keep market-data providers behind the existing `MarketDataProvider` abstraction so another provider can be added without changing portfolio valuation or transaction logic.

## Important limitations

- Google documents `GOOGLEFINANCE()` as a Google Sheets function; it is not a supported direct Google Finance REST API for backend use.
- Quotes may be delayed by up to 20 minutes, may be unavailable for some symbols, and most international exchanges are not supported. Brazilian tickers and US tickers therefore need explicit coverage checks before being enabled.
- Historical values are viewable in Sheets, but Google states they cannot be retrieved through the Sheets API or Apps Script. Historical prices should continue to come from a user-provided CSV/XLSX import or a separately licensed market-data provider.
- Google states the data is informational and subject to usage restrictions; it is not intended for professional financial-industry use, and professional use may require third-party licensing.

## Proposed workflow

1. User connects or selects a Google spreadsheet prepared for the portfolio.
2. The spreadsheet contains a symbol list and `GOOGLEFINANCE()` formulas for supported current quote attributes.
3. The user authorizes read-only access to the spreadsheet through Google OAuth.
4. The backend reads evaluated current quote cells through the Google Sheets API, validates the symbol, currency, timestamp/refresh status and numeric value, then stores a sourced quote snapshot.
5. If a symbol is unsupported or a value is missing/stale, the UI marks it unavailable and does not silently substitute a price.
6. Historical prices remain handled by CSV/XLSX import or another provider. Never attempt to retrieve historical `GOOGLEFINANCE()` formula results through the Sheets API.

## Suggested spreadsheet layout

| Symbol | Exchange-qualified ticker | Currency | Current price | Retrieved at |
|---|---|---|---:|---|
| Example US asset | NASDAQ:GOOG | USD | `=GOOGLEFINANCE(B2,"price")` | User/app refresh timestamp |

Use exchange-qualified tickers where possible (for example, `NASDAQ:GOOG`). Actual ticker mappings must be configured and tested per asset; do not assume every B3 or international symbol is supported.

## Backend design

Implement a `GoogleSheetsQuoteProvider` that implements `MarketDataProvider` and reads only current quote cells from an authorized spreadsheet. Keep OAuth tokens encrypted at rest, request the minimum Google scopes, and never store Google account credentials in plain text. Store quote provenance (provider, source spreadsheet, symbol, fetched-at time and currency) with each snapshot.

This integration is an optional quote source and must not be treated as an exchange-grade real-time feed, a trade execution source, or the source of record for transaction history.

## Official references

- [Google Sheets GOOGLEFINANCE help](https://support.google.com/docs/answer/3093281?hl=en)
- [Google Workspace announcement on historical data access](https://workspaceupdates.googleblog.com/2016/09/historical-googlefinance-data-no-longer.html)
