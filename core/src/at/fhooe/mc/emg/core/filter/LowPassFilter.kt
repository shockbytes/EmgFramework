package at.fhooe.mc.emg.core.filter

import at.fhooe.mc.emg.core.EmgComponent
import at.fhooe.mc.emg.core.EmgComponentType

@EmgComponent(type = EmgComponentType.FILTER)
class LowPassFilter : Filter() {

    private var v: DoubleArray = DoubleArray(2)

    override val name = "10Hz Chebyshev low pass"
    override val shortName = "LP"

    override fun step(x: Double): Double {
        v[0] = v[1]
        v[1] = 2.456770461833230612e-1 * x + 0.50864590763335382206 * v[0]
        return v[0] + v[1]
    }

    override fun reset() {
        v = DoubleArray(2)
        v[0] = 0.0
    }

}
