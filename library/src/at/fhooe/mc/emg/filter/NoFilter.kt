package at.fhooe.mc.emg.filter

class NoFilter : Filter() {

    override val name: String
        get() = "Raw"

    override val shortName: String
        get() = name

    override fun step(x: Double): Double {
        return x
    }

}