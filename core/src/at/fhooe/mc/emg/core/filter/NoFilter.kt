package at.fhooe.mc.emg.core.filter

import at.fhooe.mc.emg.designer.annotation.EmgComponent
import at.fhooe.mc.emg.designer.annotation.EmgComponentRelayPort
import at.fhooe.mc.emg.designer.EmgComponentType


@EmgComponent(type = EmgComponentType.FILTER)
class NoFilter : Filter() {

    override val name = "Raw"
    override val shortName= name

    @EmgComponentRelayPort(Double::class, Double::class)
    override fun step(x: Double): Double {
        return x
    }

    override fun reset() {
        // Do nothing
    }

}
