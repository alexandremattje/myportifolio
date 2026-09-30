package com.alexandremattje.myportifolio.marketdata.google

import com.alexandremattje.myportifolio.marketdata.MarketDataProvider
import com.alexandremattje.myportifolio.marketdata.MarketQuote
import com.alexandremattje.myportifolio.marketdata.QuoteRequest
import java.math.BigDecimal
import java.time.Clock
import java.time.Duration
import java.time.Instant

/**
 * Converts evaluated values from a user-authorized Google Sheet into quote snapshots.
 * Google Sheets API/OAuth transport is deliberately isolated behind GoogleSheetsGateway.
 */
class GoogleSheetsQuoteProvider(
    private val gateway: GoogleSheetsGateway,
    private val clock: Clock = Clock.systemUTC(),
    private val maxAge: Duration = Duration.ofMinutes(30)
) : MarketDataProvider {
    override val providerId: String = "google-sheets-googlefinance"

    override fun getQuote(request: QuoteRequest): MarketQuote? {
        val row = gateway.readQuote(request.symbol) ?: return null
        if (!row.symbol.equals(request.symbol, ignoreCase = true)) return null
        if (row.currency.isBlank() || (request.currency != null &&
                !row.currency.equals(request.currency, ignoreCase = true))) return null

        val observedAt = row.observedAt ?: return null
        val age = Duration.between(observedAt, Instant.now(clock))
        if (age.isNegative || age > maxAge) return null

        val price = row.price?.takeIf { it > BigDecimal.ZERO } ?: return null
        return MarketQuote(
            symbol = request.symbol.uppercase(),
            price = price,
            currency = row.currency.uppercase(),
            observedAt = observedAt,
            provider = providerId,
            sourceReference = row.spreadsheetReference
        )
    }
}

interface GoogleSheetsGateway {
    /** Reads one evaluated quote row from the configured, read-only spreadsheet. */
    fun readQuote(symbol: String): GoogleFinanceSheetRow?
}

data class GoogleFinanceSheetRow(
    val symbol: String,
    val price: BigDecimal?,
    val currency: String,
    val observedAt: Instant?,
    val spreadsheetReference: String? = null
)
