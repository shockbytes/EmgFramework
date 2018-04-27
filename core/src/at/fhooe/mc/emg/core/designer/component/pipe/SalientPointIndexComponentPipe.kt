package at.fhooe.mc.emg.core.designer.component.pipe

import at.fhooe.mc.emg.core.tool.salient.SalientPoint
import at.fhooe.mc.emg.designer.component.pipe.EmgComponentPipe
import kotlin.reflect.KClass

class SalientPointIndexComponentPipe: EmgComponentPipe<SalientPoint, Double> {

    override val ports: Pair<KClass<*>, KClass<*>> = Pair(SalientPoint::class, Double::class)
    override val name: String = "Salient Point Index"

    override fun pipe(arg: SalientPoint): Double = arg.x.toDouble()
}