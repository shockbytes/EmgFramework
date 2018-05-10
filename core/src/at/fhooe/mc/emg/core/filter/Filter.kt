package at.fhooe.mc.emg.core.filter

abstract class Filter {

    var isEnabled: Boolean = false

    init {
        this.reset()
    }

    abstract val name: String

    abstract val shortName: String

    abstract fun step(x: Double): Double

    abstract fun reset()

}
