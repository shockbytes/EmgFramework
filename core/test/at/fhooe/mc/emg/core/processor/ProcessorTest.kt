package at.fhooe.mc.emg.core.processor

import at.fhooe.mc.emg.core.setup.BasicReflectionsSetup
import at.fhooe.mc.emg.core.setup.Setup
import org.junit.Assert.assertEquals
import org.junit.Test

class ProcessorTest {

    private var reflectionsProcessor: Setup = BasicReflectionsSetup()

    @Test
    fun testTools() {
        val tools = reflectionsProcessor.tools
        assertEquals(3, tools.size)
    }

    @Test
    fun testFilter() {
        val filter = reflectionsProcessor.filter
        assertEquals(5, filter.size)
    }

    @Test
    fun testComponents() {
        val components = reflectionsProcessor.components
        assertEquals(3, components.size)
    }

}