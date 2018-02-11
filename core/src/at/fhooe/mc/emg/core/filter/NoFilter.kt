package at.fhooe.mc.emg.core.filter

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
