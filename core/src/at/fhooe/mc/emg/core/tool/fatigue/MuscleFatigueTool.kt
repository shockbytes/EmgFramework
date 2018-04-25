package at.fhooe.mc.emg.core.tool.fatigue

import at.fhooe.mc.emg.core.Toolable
import at.fhooe.mc.emg.core.analysis.model.MeanMedianFrequency
import at.fhooe.mc.emg.core.misc.PeriodicMmfComponent
import at.fhooe.mc.emg.core.misc.PeriodicRmsComponent
import at.fhooe.mc.emg.core.tool.Tool
import at.fhooe.mc.emg.designer.EmgComponentType
import at.fhooe.mc.emg.designer.annotation.EmgComponent
import at.fhooe.mc.emg.designer.annotation.EmgComponentInputPort
import at.fhooe.mc.emg.designer.annotation.EmgComponentProperty
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction

/**
 * Author:  Martin Macheiner
 * Date:    07.01.2018
 */

@EmgComponent(EmgComponentType.SINK)
class MuscleFatigueTool(override var toolView: MuscleFatigueToolView? = null) : Tool, MuscleFatigueToolViewCallback {

    override val name = "Muscle Fatigue Detection"

    @EmgComponentProperty
    var periodCapacity: Int = 2000
        set(value) {
            frequencyComponent.capacity = value
            timeComponent.capacity = value
        }

    private val frequencyComponent: PeriodicMmfComponent = PeriodicMmfComponent()
    private val timeComponent: PeriodicRmsComponent = PeriodicRmsComponent()

    private var disposable: Disposable? = null
    private var toolable: Toolable? = null

    override fun start(toolable: Toolable, showViewImmediate: Boolean) {
        this.toolable = toolable

        toolable.registerToolForUpdates(this)
        toolView?.setup(this, showViewImmediate)

        disposable = Observable.zip(frequencyComponent.outputPort, timeComponent.outputPort,
                BiFunction<MeanMedianFrequency, Double, Pair<MeanMedianFrequency, Double>> { f, t -> Pair(f, t) })
                .subscribe { (mmf, rms) -> calculateMuscleFatigue(mmf, rms) }
    }

    override fun onViewClosed() {
        disposable?.dispose()
        toolable?.unregisterToolUpdates(this)
    }

    @EmgComponentInputPort(Double::class)
    override fun update(value: Double) {
        frequencyComponent.update(value)
        timeComponent.update(value)
    }

    private fun calculateMuscleFatigue(mmf: MeanMedianFrequency, rms: Double) {
        println("Update muscle fatigue algorithm")
        // TODO Implement Tool for Muscle Fatigue Detection
    }

}