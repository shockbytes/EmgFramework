package at.fhooe.mc.emg.core.injection

import at.fhooe.mc.emg.clientdriver.EmgClientDriver
import at.fhooe.mc.emg.clientdriver.EmgClientDriverConfigView
import at.fhooe.mc.emg.core.analysis.FrequencyAnalysisMethod
import at.fhooe.mc.emg.core.analysis.FrequencyAnalysisView
import at.fhooe.mc.emg.core.filter.Filter
import at.fhooe.mc.emg.core.filter.RunningAverageFilter
import at.fhooe.mc.emg.core.filter.SavitzkyGolayFilter
import at.fhooe.mc.emg.core.filter.ThresholdFilter
import at.fhooe.mc.emg.core.storage.FileStorage
import at.fhooe.mc.emg.core.storage.config.EmgConfigStorage
import at.fhooe.mc.emg.core.tool.Tool
import at.fhooe.mc.emg.core.tool.ToolView
import at.fhooe.mc.emg.designer.annotation.EmgComponent
import at.fhooe.mc.emg.designer.annotation.EmgComponentProperty
import at.fhooe.mc.emg.designer.component.EmgBaseComponent
import at.fhooe.mc.emg.designer.component.EmgComponentFactory
import at.fhooe.mc.emg.designer.component.pipe.EmgComponentPipe
import at.fhooe.mc.emg.designer.component.util.EmgComponentParameter
import at.fhooe.mc.emg.designer.ComponentInspection

open class BasicReflectionsDependencyInjection(private val platformConfig: PlatformConfiguration) : DependencyInjection {

    protected val reflections = ComponentInspection.reflections

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
                    if (frequencyViewClass != null) {
                        instance.view = frequencyViewClass.newInstance()
                    }
                    instance
                }
                .sortedBy { it.name }
    }

    override val components: List<EmgBaseComponent> by lazy {

        val params = reflections.getFieldsAnnotatedWith(EmgComponentProperty::class.java)
                .map {
                    val propAnnotation = it.annotations.find { it is EmgComponentProperty } as EmgComponentProperty
                    EmgComponentParameter(it.declaringClass.name, it.type.name, it.name,
                            propAnnotation.defaultValue, propAnnotation.displayName)
                }

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

    override val componentPipes: List<EmgComponentPipe<Any, Any>> by lazy {
        reflections.getSubTypesOf(EmgComponentPipe::class.java)
                .map {
                    @Suppress("UNCHECKED_CAST")
                    it.newInstance() as EmgComponentPipe<Any, Any>
                }
                .sortedBy { it.name }
    }

    override val fileStorage: FileStorage by lazy {
        platformConfig.fileStorage
    }

    override val configStorage: EmgConfigStorage by lazy {
        platformConfig.configStorage
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
                val sg = fc.newInstance() as SavitzkyGolayFilter
                sg.sgFilterWidth = configStorage.emgConfig.savitzkyGolayFilterWidth
                sg
            }
            fc.name.contains("RunningAverage") -> {
                val rAvg = fc.newInstance() as RunningAverageFilter
                rAvg.size = configStorage.emgConfig.runningAverageWindowSize
                rAvg
            }
            fc.name.contains("Threshold") -> {
                val threshold = fc.newInstance() as ThresholdFilter
                threshold.threshold = configStorage.emgConfig.thresholdFilterValue
                threshold
            }
            else -> fc.newInstance()
        }
    }

    private fun driverByClass(dc: Class<out EmgClientDriver>, driverView: Class<*>?): EmgClientDriver {

        val driverViewInterface = EmgClientDriverConfigView::class.java
        return if (dc.name.contains("Simulation")) {
            val constructor = dc.getConstructor(driverViewInterface, String::class.java)
            constructor.newInstance(driverView?.newInstance(), platformConfig.simulationFolder)
        } else {
            val constructor = dc.getConstructor(driverViewInterface)
            constructor.newInstance(driverView?.newInstance())
        }
    }

}