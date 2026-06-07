package lux.vexium.app.core.common

import java.text.NumberFormat
import java.util.Locale

/**
 * Useful extension functions used across the app.
 */

// Format number as currency
fun Double.toCurrency(locale: Locale = Locale.US): String {
    return NumberFormat.getCurrencyInstance(locale).format(this)
}

// Truncate wallet address for display: 0x1234...abcd
fun String.truncateAddress(prefixLength: Int = 6, suffixLength: Int = 4): String {
    return if (this.length > prefixLength + suffixLength) {
        "${take(prefixLength)}...${takeLast(suffixLength)}"
    } else {
        this
    }
}

// Convert Wei to ETH
fun Long.weiToEth(): Double = this / 1_000_000_000_000_000_000.0
