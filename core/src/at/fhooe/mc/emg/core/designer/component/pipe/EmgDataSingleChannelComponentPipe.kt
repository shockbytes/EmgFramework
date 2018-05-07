package at.fhooe.mc.emg.core.designer.component.pipe

import at.fhooe.mc.emg.clientdriver.model.EmgData
import at.fhooe.mc.emg.designer.component.pipe.EmgComponentPipe
import kotlin.reflect.KClass

class EmgDataSingleChannelComponentPipe : EmgComponentPipe<EmgData, Double> {

    override val ports: Pair<KClass<*>, KClass<*>> = Pair(EmgData::class, Double::class)

    override val name = "EmgData to SingleChannel"

    override fun pipe(arg: EmgData): Double {
        return arg.lastOfChannel(0).y
    }
}