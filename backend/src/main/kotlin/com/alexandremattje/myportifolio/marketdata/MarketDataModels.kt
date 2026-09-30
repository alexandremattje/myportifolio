package com.alexandremattje.myportifolio.marketdata

import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

data class MarketQuote(
    val symbol: String,
    val price: BigDecimal,
    val currency: String,
    val observedAt: Instant,
    val provider: String,
    val sourceReference: String? = null
)

data class HistoricalPrice(
    val symbol: String,
    val date: LocalDate,
    val close: BigDecimal,
    val currency: String,
    val provider: String
)

data class QuoteRequest(val symbol: String, val currency: String? = null)
