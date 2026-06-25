package lux.vexium.app

import org.junit.Assert.assertEquals
import org.junit.Test
import lux.vexium.app.core.common.truncateAddress
import lux.vexium.app.core.common.weiToEth

class ExtensionsTest {

    @Test
    fun `truncateAddress formats wallet address correctly`() {
        val address = "0x1234567890abcdef1234567890abcdef12345678"
        val truncated = address.truncateAddress()
        assertEquals("0x1234...5678", truncated)
    }

    @Test
    fun `truncateAddress returns full string if short`() {
        val address = "0x1234"
        val truncated = address.truncateAddress()
        assertEquals("0x1234", truncated)
    }

    @Test
    fun `weiToEth converts correctly`() {
        val wei = 1_000_000_000_000_000_000L  // 1 ETH
        val eth = wei.weiToEth()
        assertEquals(1.0, eth, 0.0001)
    }
}
