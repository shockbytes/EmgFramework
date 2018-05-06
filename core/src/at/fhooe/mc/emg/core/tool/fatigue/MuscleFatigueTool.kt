package at.fhooe.mc.emg.core.tool.fatigue

import at.fhooe.mc.emg.core.Toolable
import at.fhooe.mc.emg.core.analysis.model.MeanMedianFrequency
import at.fhooe.mc.emg.core.misc.PeriodicMmfComponent
import at.fhooe.mc.emg.core.misc.PeriodicRmsComponent
import at.fhooe.mc.emg.core.tool.Tool
import at.fhooe.mc.emg.core.util.CoreUtils
import at.fhooe.mc.emg.designer.EmgComponentType
import at.fhooe.mc.emg.designer.annotation.EmgComponent
import at.fhooe.mc.emg.designer.annotation.EmgComponentInputPort
import at.fhooe.mc.emg.designer.annotation.EmgComponentProperty
import at.fhooe.mc.emg.designer.annotation.EmgComponentStartablePoint
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
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
    @EmgComponentProperty("5.12", "Calculation window in seconds")
    var windowWithInSeconds: Double = 5.12

    @JvmField
    @EmgComponentProperty("100", "Sampling frequency")
    var fs: Int = 100

    private var periodCapacity: Int = (windowWithInSeconds * fs).toInt()

    private var values: MutableList<Pair<Double, Double>> = mutableListOf()

    private val frequencyComponent: PeriodicMmfComponent = PeriodicMmfComponent()
    private val timeComponent: PeriodicRmsComponent = PeriodicRmsComponent()

    private var dataDisposable: Disposable? = null
    private var disposable: Disposable? = null
    private var toolable: Toolable? = null

    override fun start(toolable: Toolable?, showViewImmediate: Boolean) {
        this.toolable = toolable

        // Set capacity of sub components and value storage
        periodCapacity = (windowWithInSeconds * fs).toInt()
        frequencyComponent.capacity = periodCapacity
        timeComponent.capacity = periodCapacity

        // Setup the ToolView after capacities are set
        toolView?.setup(this, showViewImmediate)
        clear()

        // Register for Toolable updates, if toolable is null this indicates that tool is operated in case designer mode
        dataDisposable = toolable?.registerToolForUpdates()?.subscribe { update(it.lastOfChannel(0).y) }

        // Combine both observables in order to calculate muscle fatigue with the updated values
        disposable = Observable.zip(frequencyComponent.outputPort, timeComponent.outputPort,
                BiFunction<MeanMedianFrequency, Double, Pair<MeanMedianFrequency, Double>> { f, t -> Pair(f, t) })
                .subscribe { (mmf, rms) -> calculateMuscleFatigue(mmf, rms) }
    }

    override fun onViewClosed() {
        disposable?.dispose()
        dataDisposable?.dispose()
    }

    @EmgComponentStartablePoint("toolView", MuscleFatigueToolView::class)
    override fun externalStart() {
        start(null, true)
    }

    override fun save(filePath: String) {
        val contentAsCsv = values.joinToString("\n") { "${it.first},${it.second}" }
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
                    values.clear()
                    values.addAll(loadedValues)
                }, { throwable -> toolView?.showError("Status: Error while loading ${throwable.message}") })
    }

    @EmgComponentInputPort(Double::class)
    fun update(value: Double) {
        frequencyComponent.update(value)
        timeComponent.update(value)
    }

    private fun calculateMuscleFatigue(mmf: MeanMedianFrequency, rms: Double) {
        values.add(Pair(mmf.median, rms))
        toolView?.update(values)
    }

    private fun clear() {
        values.clear()
        toolView?.clear()
    }

}