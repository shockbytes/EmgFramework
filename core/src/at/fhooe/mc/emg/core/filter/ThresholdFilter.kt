package at.fhooe.mc.emg.core.filter

import at.fhooe.mc.emg.designer.annotation.EmgComponent
import at.fhooe.mc.emg.designer.annotation.EmgComponentRelayPort
import at.fhooe.mc.emg.designer.EmgComponentType

@EmgComponent(type = EmgComponentType.FILTER)
class ThresholdFilter(private val threshold: Int): Filter() {

    override val name = "Threshold filter"

    override val shortName = "TH"

    @EmgComponentRelayPort(Double::class, Double::class)
    override fun step(x: Double): Double {
        return if (x > threshold) threshold.toDouble() else x
    }

    override fun reset() {
        // Do nothing...
    }

}