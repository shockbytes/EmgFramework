package at.fhooe.mc.emg.core.injection

import at.fhooe.mc.emg.clientdriver.EmgClientDriver
import at.fhooe.mc.emg.clientdriver.EmgClientDriverConfigView
import at.fhooe.mc.emg.core.analysis.FrequencyAnalysisMethod
import at.fhooe.mc.emg.core.analysis.FrequencyAnalysisView
import at.fhooe.mc.emg.core.filter.Filter
import at.fhooe.mc.emg.core.storage.FileStorage
import at.fhooe.mc.emg.core.storage.SimpleFileStorage
import at.fhooe.mc.emg.core.storage.config.EmgConfigStorage
import at.fhooe.mc.emg.core.storage.config.JsonEmgConfigStorage
import at.fhooe.mc.emg.core.tool.Tool
import at.fhooe.mc.emg.core.tool.ToolView
import at.fhooe.mc.emg.designer.annotation.EmgComponent
import at.fhooe.mc.emg.designer.annotation.EmgComponentProperty
import at.fhooe.mc.emg.designer.component.EmgBaseComponent
import at.fhooe.mc.emg.designer.component.EmgComponentFactory
import at.fhooe.mc.emg.designer.component.pipe.EmgComponentPipe
import at.fhooe.mc.emg.designer.component.util.EmgComponentParameter
import org.reflections.Reflections
import org.reflections.scanners.FieldAnnotationsScanner
import org.reflections.scanners.SubTypesScanner
import org.reflections.scanners.TypeAnnotationsScanner
import java.io.File

open class BasicReflectionsDependencyInjection : DependencyInjection {

    protected val reflections = Reflections(FieldAnnotationsScanner(),
            SubTypesScanner(), TypeAnnotationsScanner())

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
        reflections.getSubTypesOf(Filter::class.java)
                .map {
                    val filter = filterByClass(it)
                    filter.isEnabled = filter.name == "Raw"
                    filter
                }
                .sortedBy { it.name }
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

    override val frequencyAnalysisMethods: List<FrequencyAnalysisMethod> by lazy {

        val frequencyViewClass = reflections
                .getSubTypesOf(FrequencyAnalysisView::class.java)
                .firstOrNull { !it.isInterface }

        reflections.getSubTypesOf(FrequencyAnalysisMethod::class.java)
                .filter { !it.isInterface }
                .map {
                    val instance = it.newInstance()
                    if (instance.hasDisplay && frequencyViewClass != null) {
                        instance.view = frequencyViewClass.newInstance()
                    }
                    instance
                }
                .sortedBy { it.name }
    }

    override val components: List<EmgBaseComponent> by lazy {

        val params = reflections.getFieldsAnnotatedWith(EmgComponentProperty::class.java)
                .map { EmgComponentParameter(it.declaringClass.name, it.type.name, it.name) }

        reflections.getTypesAnnotatedWith(EmgComponent::class.java)
                .map { cls ->
                    // This cast must always succeed, because the reflections API is queried only for those classes
                    val component = cls.annotations.find { it.annotationClass == EmgComponent::class } as EmgComponent
                    EmgComponentFactory.byType(cls.simpleName, cls.name,
                            params.filter { it.declaringClass == cls.name },
                            component.type)
                }
                .sortedBy { it.name }
    }

    override val componentPipes: List<EmgComponentPipe<*, *>> by lazy {
        reflections.getSubTypesOf(EmgComponentPipe::class.java)
                .map { it.newInstance() }
                .sortedBy { it.name }
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
            fc.name.contains("Threshold") -> {
                val constructor = fc.getConstructor(Int::class.java)
                constructor.newInstance(configStorage.emgConfig.thresholdFilterValue)
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