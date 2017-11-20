package at.fhooe.mc.emg.filter

abstract class Filter internal constructor() {

    var isEnabled: Boolean = false

    abstract val name: String

    abstract val shortName: String

    abstract fun step(x: Double): Double

}
