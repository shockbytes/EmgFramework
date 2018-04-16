package at.fhooe.mc.emg.core.setup

import org.junit.Assert.assertEquals
import org.junit.Test

class CoreSetupTest {

    private var reflectionsProcessor: Setup = BasicReflectionsSetup()

    @Test
    fun testTools() {
        val tools = reflectionsProcessor.tools
        assertEquals(3, tools.size)
    }

    @Test
    fun testFilter() {
        val filter = reflectionsProcessor.filter
        assertEquals(13, filter.size)
    }

    @Test
    fun testComponents() {
        val components = reflectionsProcessor.components
        assertEquals(8, components.size)
    }

}