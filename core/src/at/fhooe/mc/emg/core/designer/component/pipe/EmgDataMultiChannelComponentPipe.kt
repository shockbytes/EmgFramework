package at.fhooe.mc.emg.core.designer.component.pipe

import at.fhooe.mc.emg.clientdriver.model.EmgData
import at.fhooe.mc.emg.designer.component.pipe.EmgComponentPipe
import kotlin.reflect.KClass

class EmgDataMultiChannelComponentPipe : EmgComponentPipe<EmgData, DoubleArray> {

    override val ports: Pair<KClass<*>, KClass<*>> = Pair(EmgData::class, DoubleArray::class)

    override val name = "EmgData MultiChannel"

    override fun pipe(arg: EmgData): DoubleArray {
        return arg.lastOfChannels().map { it.y }.toDoubleArray()
    }
}