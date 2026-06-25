package lux.vexium.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import lux.vexium.app.core.common.Resource

class ExampleUnitTest {

    @Test
    fun `Resource Success holds data correctly`() {
        val resource = Resource.Success("test data")
        assertEquals("test data", resource.data)
    }

    @Test
    fun `Resource Error holds message correctly`() {
        val resource = Resource.Error("Something went wrong")
        assertEquals("Something went wrong", resource.message)
    }

    @Test
    fun `Resource Loading is singleton`() {
        val loading = Resource.Loading
        assertNotNull(loading)
    }
}
