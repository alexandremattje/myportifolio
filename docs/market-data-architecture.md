# Market Data Provider Structure

The application uses a provider-neutral interface so quote retrieval does not leak into holdings, transactions or valuation logic.

## Current structure

- `MarketDataProvider`: common quote interface.
- `MarketQuote` and `HistoricalPrice`: normalized domain models.
- `GoogleSheetsQuoteProvider`: validates evaluated quote rows supplied by a gateway.
- `GoogleSheetsGateway`: boundary for Google Sheets API access and OAuth implementation.
- `UnavailableMarketDataProvider`: safe placeholder for a future dedicated/ licensed provider.
- CSV/XLSX imports remain the route for broker transactions and historical data until a historical-data provider is selected.

## Google Sheets contract

Prepare a spreadsheet with a symbol-keyed row containing the exchange-qualified ticker, evaluated current price, currency and an application-managed observation timestamp. The Google Sheets API integration should use read-only OAuth scopes and access only the configured spreadsheet.

The provider rejects missing prices, non-positive prices, mismatched symbols/currencies, missing timestamps, future timestamps and stale values. The current default maximum age is 30 minutes; configure this based on product requirements and the source's refresh behavior.

The `GoogleSheetsGateway` is intentionally an interface at this stage. OAuth consent, secure token storage, spreadsheet/range configuration, API client, retries and integration tests are implementation tasks; this scaffold does not yet connect to Google.

## Provider rules

1. Store quote provenance: provider ID, symbol, currency, observed time and source reference.
2. Never silently substitute a missing or stale quote.
3. Keep provider-specific symbols in a mapping layer; do not assume every B3 or international ticker is supported by `GOOGLEFINANCE()`.
4. Do not use this Google Sheets integration as a historical-price source. Historical data must come from imports or a provider whose API explicitly supports it.
5. Review data-provider terms, coverage, rate limits and costs before enabling a source for other users or commercial use.

## Next implementation steps

1. Add Google OAuth authorization with minimum read-only scopes and encrypted token persistence.
2. Implement a Google Sheets API gateway with configured spreadsheet ID and range.
3. Add provider selection and quote refresh scheduling.
4. Add tests for unsupported symbols, stale timestamps, currency mismatches and malformed values.
5. Evaluate a licensed market-data API for reliable B3, US and historical coverage.
