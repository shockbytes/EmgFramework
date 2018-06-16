package at.fhooe.mc.emg.core.tool.fatigue

import at.fhooe.mc.emg.core.Toolable
import at.fhooe.mc.emg.core.analysis.model.MeanMedianFrequency
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
import java.time.LocalDateTime

/**
 * Author:  Martin Macheiner
 * Date:    07.01.2018
 */
@EmgComponent(type = EmgComponentType.SINK, displayTitle = "Muscle Fatigue detection")
open class MuscleFatigueTool(override var toolView: MuscleFatigueToolView? = null) : Tool, MuscleFatigueToolViewCallback {

    override val name = "Muscle Fatigue Detection"

    @JvmField
    @EmgComponentProperty("5.0", "Calculation window in seconds")
    var windowWithInSeconds: Double = 5.0

    @JvmField
    @EmgComponentProperty("100", "Sampling frequency")
    var fs: Int = 161

    private var periodCapacity: Int = (windowWithInSeconds * fs).toInt()

    private var rawValues: MutableList<Pair<Double, Double>> = mutableListOf()
    private var temporalValues: MutableList<Pair<Double, Double>> = mutableListOf()

    private val frequencyComponent: PeriodicMmfComponent = PeriodicMmfComponent()
    private val timeComponent: PeriodicRmsComponent = PeriodicRmsComponent()

    private val timeRegressionComputation = RegressionAnalysisBufferedComputation()
    private val frequencyRegressionComputation = RegressionAnalysisBufferedComputation()

    private var idxPointer = 0

    private var toolable: Toolable? = null

    private val compositeDisposable = CompositeDisposable()

    override fun start(toolable: Toolable?, showViewImmediate: Boolean) {
        this.toolable = toolable

        // Set capacity of sub components and value storage
        periodCapacity = (windowWithInSeconds * fs).toInt()
        frequencyComponent.capacity = periodCapacity
        timeComponent.capacity = periodCapacity
        timeRegressionComputation.capacity = (60 / windowWithInSeconds).toInt()
        frequencyRegressionComputation.capacity = (60 / windowWithInSeconds).toInt()

        // Setup the ToolView after capacities are set
        toolView?.setup(this, showViewImmediate)
        clear()

        // Register for Toolable updates, if toolable is null this indicates that tool is operated in case designer mode
        val disposable = toolable?.registerToolForUpdates()?.subscribe { update(it.lastOfChannel(0).y) }
        if (disposable != null) {
            compositeDisposable.add(disposable)
        }

        // Combine both observables in order to calculate muscle fatigue with the updated rawValues
        compositeDisposable.add(Observable.zip(frequencyComponent.outputPort, timeComponent.outputPort,
                BiFunction<MeanMedianFrequency, Double, Pair<MeanMedianFrequency, Double>> { f, t -> Pair(f, t) })
                .subscribe { (mmf, rms) -> updateCurrentRegression(mmf.median, rms) })

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
        val contentAsCsv = rawValues.joinToString("\n") { "${it.first},${it.second}" }
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

    private fun updateCurrentRegression(mdf: Double, rms: Double) {

        println("${LocalDateTime.now()} - Update current regression!")

        timeRegressionComputation.update(Pair(idxPointer.toDouble(), rms))
        frequencyRegressionComputation.update(Pair(idxPointer.toDouble(), mdf))
        idxPointer++
    }

    private fun updateMuscleFatigueAlgorithm(tSlope: Double, fSlope: Double) {

        println("${LocalDateTime.now()} - Update actual algorithm!")

        // Cannot compute temporal change with only 1 value
        if (rawValues.size > 0) {

            println("Calculate temporal values")
            // Calculate absolute temporal change
            val (lastTSlope, lastFSlope) = rawValues.last()
            val tmpChangeT = 100 * ((tSlope - lastTSlope) / lastTSlope)
            val tmpChangeF = 100 * ((fSlope - lastFSlope) / lastFSlope)

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
        rawValues.add(Pair(tSlope, fSlope))
    }


    private fun clear() {
        rawValues.clear()
        toolView?.clear()
        idxPointer = 0
    }

}