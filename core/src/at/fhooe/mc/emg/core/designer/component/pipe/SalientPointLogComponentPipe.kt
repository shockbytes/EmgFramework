package at.fhooe.mc.emg.core.designer.component.pipe

import at.fhooe.mc.emg.core.tool.salient.SalientPoint
import at.fhooe.mc.emg.designer.component.pipe.EmgComponentPipe
import kotlin.reflect.KClass

class SalientPointLogComponentPipe: EmgComponentPipe<SalientPoint, String> {
    override val ports: Pair<KClass<*>, KClass<*>> = Pair(SalientPoint::class, String::class)

    override val name = "Salient Point Data Log"

    override fun pipe(arg: SalientPoint): String = arg.toString()
}