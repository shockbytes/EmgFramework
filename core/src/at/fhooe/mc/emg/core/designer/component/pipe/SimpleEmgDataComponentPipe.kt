package at.fhooe.mc.emg.core.designer.component.pipe

import at.fhooe.mc.emg.clientdriver.model.EmgData
import at.fhooe.mc.emg.designer.component.pipe.EmgComponentPipe
import kotlin.reflect.KClass

class SimpleEmgDataComponentPipe : EmgComponentPipe<EmgData, EmgData> {

    override val ports: Pair<KClass<*>, KClass<*>> = Pair(EmgData::class, EmgData::class)
    override val name: String = "Simple EmgData"

    override fun pipe(arg: EmgData): EmgData = arg
}