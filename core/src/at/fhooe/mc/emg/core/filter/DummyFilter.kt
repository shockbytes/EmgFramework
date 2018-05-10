package at.fhooe.mc.emg.core.filter

import at.fhooe.mc.emg.designer.EmgComponentType
import at.fhooe.mc.emg.designer.annotation.EmgComponent

@EmgComponent(type = EmgComponentType.FILTER, displayTitle = "Dummy Filter")
class DummyFilter : Filter() {

    override val name = "Dummy"
    override val shortName= name

    override fun step(x: Double): Double {
        return x
    }

    override fun reset() {
        // Do nothing
    }

}