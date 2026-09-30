package com.alexandremattje.myportifolio.marketdata.provider

import com.alexandremattje.myportifolio.marketdata.MarketDataProvider
import com.alexandremattje.myportifolio.marketdata.MarketQuote
import com.alexandremattje.myportifolio.marketdata.QuoteRequest

/**
 * Safe placeholder for a future licensed API adapter. It intentionally returns no
 * quote until a provider, credentials, symbol mapping and terms have been configured.
 */
class UnavailableMarketDataProvider(
    override val providerId: String
) : MarketDataProvider {
    override fun getQuote(request: QuoteRequest): MarketQuote? = null
}
