package com.alexandremattje.myportifolio.marketdata

import java.time.LocalDate

interface MarketDataProvider {
    val providerId: String

    fun getQuote(request: QuoteRequest): MarketQuote?

    fun getHistoricalPrices(
        symbol: String,
        from: LocalDate,
        to: LocalDate
    ): List<HistoricalPrice> = emptyList()
}
