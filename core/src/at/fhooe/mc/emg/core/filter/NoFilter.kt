package at.fhooe.mc.emg.core.filter

import at.fhooe.mc.emg.core.EmgComponent
import at.fhooe.mc.emg.core.EmgComponentType

@EmgComponent(type = EmgComponentType.FILTER)
class NoFilter : Filter() {

    override val name = "Raw"
    override val shortName= name

    override fun step(x: Double): Double {
        return x
    }

    override fun reset() {
        // Do nothing
    }

}
