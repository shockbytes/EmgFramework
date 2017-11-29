package at.fhooe.mc.emg.core.filter

//Band pass butterworth filter order=1 alpha1=50 alpha2=50.05
class BandstopFilter : Filter() {

    private val v: DoubleArray

    override val name: String
        get() = "50Hz Butterworth band stop"

    override val shortName: String
        get() = "BS"

    init {
        v = DoubleArray(3)
        v[0] = 0.0
        v[1] = 0.0
    }

    override fun step(x: Double): Double {
        v[0] = v[1]
        v[1] = v[2]
        v[2] = (9.291239228448944232e-1 * x
                + -0.72654252800537055812 * v[0]
                + 1.72654252800536989199 * v[1])
        return v[0] + v[2] - 2.000000 * v[1]
    }

}
