package at.fhooe.mc.emg.core.setup

import at.fhooe.mc.emg.clientdriver.EmgClientDriver
import at.fhooe.mc.emg.clientdriver.EmgClientDriverConfigView
import at.fhooe.mc.emg.core.EmgComponent
import at.fhooe.mc.emg.core.filter.Filter
import at.fhooe.mc.emg.core.storage.FileStorage
import at.fhooe.mc.emg.core.storage.SimpleFileStorage
import at.fhooe.mc.emg.core.storage.config.EmgConfigStorage
import at.fhooe.mc.emg.core.storage.config.JsonEmgConfigStorage
import at.fhooe.mc.emg.core.tools.Tool
import at.fhooe.mc.emg.core.tools.ToolView
import org.reflections.Reflections
import java.io.File

open class BasicReflectionsSetup : Setup {

    protected val reflections = Reflections()

    override val tools: List<Tool> by lazy {

        val toolViews = reflections.getSubTypesOf(ToolView::class.java)
                .filter { !it.isInterface }
        val toolViewInterfaces = reflections.getSubTypesOf(ToolView::class.java)
                .filter { it.isInterface }

        reflections.getSubTypesOf(Tool::class.java)
                .mapNotNull { tc ->
                    try {
                        val toolView = toolViews
                                .find { tv -> tv.name.contains(tc.simpleName) }
                        val toolViewInterface = toolViewInterfaces
                                .find { tv -> tv.name.contains(tc.simpleName) }
                        toolByClass(tc, toolView, toolViewInterface)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
                .sortedBy { it.name }
    }

    override val filter: List<Filter> by lazy {
        reflections.getSubTypesOf(Filter::class.java).map { filterByClass(it) }
    }

    override val driver: List<EmgClientDriver> by lazy {

        val driverViews = reflections
                .getSubTypesOf(EmgClientDriverConfigView::class.java)
                .filter { !it.isInterface }

        reflections.getSubTypesOf(EmgClientDriver::class.java)
                .mapNotNull { dc ->
                    try {
                        val driverView = driverViews
                                .find { dv -> dv.name.contains(dc.simpleName) }
                        driverByClass(dc, driverView)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
                .sortedBy { it.name }
    }

    override val components: List<Class<*>> by lazy {
        reflections.getTypesAnnotatedWith(EmgComponent::class.java).toList()
    }

    override val fileStorage: FileStorage by lazy {
        SimpleFileStorage()
    }

    override val configStorage: EmgConfigStorage by lazy {
        JsonEmgConfigStorage(File(System.getProperty("user.dir") + "/data/config.json"))
    }

    // ------------------------------------------------------------------------

    private fun toolByClass(tc: Class<out Tool>, toolView: Class<*>?, toolViewInterface: Class<*>?): Tool {
        return if (tc.name.contains("Conconi")) {
            val constructor = tc.getConstructor(toolViewInterface, fileStorage.javaClass.interfaces[0])
            constructor.newInstance(toolView?.newInstance(), fileStorage)
        } else {
            val constructor = tc.getConstructor(toolViewInterface)
            constructor.newInstance(toolView?.newInstance())
        }
    }

    private fun filterByClass(fc: Class<out Filter>): Filter {
        return when {
            fc.name.contains("SavitzkyGolay") -> {
                val constructor = fc.getConstructor(Int::class.java)
                constructor.newInstance(configStorage.emgConfig.savitzkyGolayFilterWidth)
            }
            fc.name.contains("RunningAverage") -> {
                val constructor = fc.getConstructor(Int::class.java)
                constructor.newInstance(configStorage.emgConfig.runningAverageWindowSize)
            }
            else -> fc.newInstance()
        }
    }

    /**
     * Mark this method as protected, so platform specific implementations (like Android) only have to override
     * this method to handle special driver instantiation instead of overriding #driver
     */
    protected fun driverByClass(dc: Class<out EmgClientDriver>, driverView: Class<*>?): EmgClientDriver {

        val driverViewInterface = EmgClientDriverConfigView::class.java
        return if (dc.name.contains("Simulation")) {
            val constructor = dc.getConstructor(driverViewInterface, String::class.java)
            constructor.newInstance(driverView?.newInstance(), System.getProperty("user.dir") + "/data/simulation")
        } else {
            val constructor = dc.getConstructor(driverViewInterface)
            constructor.newInstance(driverView?.newInstance())
        }
    }

}