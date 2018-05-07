package at.fhooe.mc.emg.core.designer.component.pipe

import at.fhooe.mc.emg.designer.component.pipe.EmgComponentPipe
import kotlin.reflect.KClass

class NumberStringComponentPipe : EmgComponentPipe<Double, String> {

    override val ports: Pair<KClass<*>, KClass<*>> = Pair(Double::class, String::class)

    override val name = "Number to String Pipe"

    override fun pipe(arg: Double): String = arg.toString()

}