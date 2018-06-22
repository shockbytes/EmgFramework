package at.fhooe.mc.emg.core.tool.fatigue

import at.fhooe.mc.emg.core.Toolable
import at.fhooe.mc.emg.core.computation.PeriodicMmfComponent
import at.fhooe.mc.emg.core.computation.PeriodicRmsComponent
import at.fhooe.mc.emg.core.computation.RegressionAnalysisBufferedComputation
import at.fhooe.mc.emg.core.tool.Tool
import at.fhooe.mc.emg.core.util.CoreUtils
import at.fhooe.mc.emg.designer.EmgComponentType
import at.fhooe.mc.emg.designer.annotation.EmgComponent
import at.fhooe.mc.emg.designer.annotation.EmgComponentInputPort
import at.fhooe.mc.emg.designer.annotation.EmgComponentProperty
import at.fhooe.mc.emg.designer.annotation.EmgComponentStartablePoint
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import java.io.File

/**
 * Author:  Martin Macheiner
 * Date:    07.01.2018
 */
@EmgComponent(type = EmgComponentType.SINK, displayTitle = "Muscle Fatigue detection")
open class MuscleFatigueTool(override var toolView: MuscleFatigueToolView? = null) : Tool, MuscleFatigueToolViewCallback {

    override val name = "Muscle Fatigue Detection"

    @JvmField
    @EmgComponentProperty("5.0", "Calculation window in seconds")
    var windowWithInSecondsLowerLimit: Double = 2.0

    @JvmField
    @EmgComponentProperty("100", "Sampling frequency")
    var fs: Int = 934

    @JvmField
    @EmgComponentProperty("2", "Window proportion between time and frequency buffer")
    var proportionFactor: Double = 2.5

    // TODO Change to 60
    private val regressionBufferSize: Int = (20 / windowWithInSecondsLowerLimit).toInt()
    private var periodCapacity: Int = (windowWithInSecondsLowerLimit * fs).toInt()

    private var rawValues: MutableList<Pair<Double, Double>> = mutableListOf()
    private var temporalValues: MutableList<Pair<Double, Double>> = mutableListOf()

    private val frequencyComponent: PeriodicMmfComponent = PeriodicMmfComponent()
    private val timeComponent: PeriodicRmsComponent = PeriodicRmsComponent()

    private val timeRegressionComputation = RegressionAnalysisBufferedComputation()
    private val frequencyRegressionComputation = RegressionAnalysisBufferedComputation()

    private var idxFreqPointer = 0
    private var idxTimePointer = 0

    private var toolable: Toolable? = null

    private val compositeDisposable = CompositeDisposable()

    override fun start(toolable: Toolable?, showViewImmediate: Boolean) {
        this.toolable = toolable

        // Set capacity of sub components and value storage
        periodCapacity = (windowWithInSecondsLowerLimit * fs).toInt()
        frequencyComponent.capacity = periodCapacity
        timeComponent.capacity = (periodCapacity * proportionFactor).toInt() // Time components has doubled size
        timeRegressionComputation.capacity = (regressionBufferSize / proportionFactor).toInt() // Time components has doubled size
        frequencyRegressionComputation.capacity = regressionBufferSize

        // Set sampling frequency of mmf component
        frequencyComponent.samplingFrequency = fs.toDouble()

        // Setup the ToolView after capacities are set
        toolView?.setup(this, showViewImmediate)
        clear()

        // Register for Toolable updates, if toolable is null this indicates that tool is operated in case designer mode
        val disposable = toolable?.registerToolForUpdates()?.subscribe { update(it.lastOfChannel(0).y) }
        if (disposable != null) {
            compositeDisposable.add(disposable)
        }

        compositeDisposable.add(frequencyComponent.outputPort.subscribe { mmf ->
            println("Update frequency regression!")
            frequencyRegressionComputation.update(Pair(idxFreqPointer.toDouble(), mmf.median))
            idxFreqPointer++
        })

        compositeDisposable.add(timeComponent.outputPort.subscribe { rms ->
            println("Update time regression!")
            timeRegressionComputation.update(Pair(idxTimePointer.toDouble(), rms))
            idxTimePointer++
        })

        // Listen for actual slope data
        compositeDisposable.add(Observable.zip(timeRegressionComputation.outputPort, frequencyRegressionComputation.outputPort,
            BiFunction<Double, Double, Pair<Double, Double>> { t, f -> Pair(t, f) })
                .subscribe { (tSlope, fSlope) -> updateMuscleFatigueAlgorithm(tSlope, fSlope)})
    }

    override fun onViewClosed() {
        compositeDisposable.dispose()
    }

    @EmgComponentStartablePoint("toolView", MuscleFatigueToolView::class)
    override fun externalStart() {
        start(null, true)
    }

    override fun save(filePath: String) {
        val contentAsCsv = temporalValues.joinToString("\n") { "${it.first},${it.second}" }
        CoreUtils.writeFile(File(filePath), contentAsCsv)
    }

    override fun load(filePath: String) {
        CoreUtils.readFileLines(File(filePath))
                .map {
                    it.mapTo(mutableListOf()) {
                        val s = it.split(",")
                        Pair(s[0].toDouble(), s[1].toDouble())
                    }
                }
                .subscribe({ loadedValues ->
                    rawValues.clear()
                    rawValues.addAll(loadedValues)
                }, { throwable -> toolView?.showError("Status: Error while loading ${throwable.message}") })
    }

    @EmgComponentInputPort(Double::class)
    fun update(value: Double) {
        frequencyComponent.update(value)
        timeComponent.update(value)
    }

    private fun updateMuscleFatigueAlgorithm(tSlope: Double, fSlope: Double) {

        val tSlopeCorr = if (tSlope == Double.NaN) 0.0 else tSlope
        val fSlopeCorr = if (fSlope == Double.NaN) 0.0 else fSlope

        // Cannot compute temporal change with only 1 value
        if (rawValues.size > 0) {

            // Calculate absolute temporal change
            val (lastTSlope, lastFSlope) = rawValues.last()
            val tmpChangeT = 100 * ((tSlopeCorr - lastTSlope) / lastTSlope)
            val tmpChangeF = 100 * ((fSlopeCorr - lastFSlope) / lastFSlope)

            // Calculate relative change from last value (increment last value)
            val lastTemporalValues = temporalValues.lastOrNull()
            if (lastTemporalValues != null) {
                val relativeChangeT = tmpChangeT  + lastTemporalValues.first
                val relativeChangeF = tmpChangeF  + lastTemporalValues.second
                temporalValues.add(Pair(relativeChangeT, relativeChangeF))
            } else {
                // If no temporal change recorded, put a new one in to initialize coordinate system
                temporalValues.add(Pair(0.0, 0.0))
            }

            toolView?.update(temporalValues)
        }

        // Add raw values at last, in order to not interfere with .last() method of temporal calculation
        rawValues.add(Pair(tSlopeCorr, fSlopeCorr))

        // Clear pointer for valid regression lines
        idxFreqPointer = 0
        idxTimePointer = 0
    }


    private fun clear() {
        rawValues.clear()
        toolView?.clear()
        idxTimePointer = 0
        idxFreqPointer = 0

        timeRegressionComputation.reset()
        frequencyRegressionComputation.reset()
        frequencyComponent.reset()
        timeComponent.reset()
    }

}