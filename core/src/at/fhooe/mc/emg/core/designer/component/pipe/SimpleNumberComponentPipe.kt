package at.fhooe.mc.emg.core.designer.component.pipe

import at.fhooe.mc.emg.designer.component.pipe.EmgComponentPipe
import kotlin.reflect.KClass

class SimpleNumberComponentPipe : EmgComponentPipe<Double, Double> {

    override val ports: Pair<KClass<*>, KClass<*>> = Pair(Double::class, Double::class)
    override val name: String = "Simple Number"

    override fun pipe(arg: Double): Double = arg
}