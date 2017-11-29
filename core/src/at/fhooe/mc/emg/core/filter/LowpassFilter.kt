package at.fhooe.mc.emg.core.filter

class LowpassFilter : Filter() {

    private val v: DoubleArray

    override val name: String
        get() = "10Hz Chebyshev low pass"

    override val shortName: String
        get() = "LP"

    init {
        v = DoubleArray(2)
        v[0] = 0.0
    }

    override fun step(x: Double): Double {

        v[0] = v[1]
        v[1] = 2.456770461833230612e-1 * x + 0.50864590763335382206 * v[0]
        return v[0] + v[1]
    }

}
