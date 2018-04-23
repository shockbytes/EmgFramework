package at.fhooe.mc.emg.core.designer.component

import at.fhooe.mc.emg.clientdriver.model.EmgData
import at.fhooe.mc.emg.designer.component.pipe.EmgComponentPipe
import kotlin.reflect.KClass

class EmgDataHeartRateComponentPipe: EmgComponentPipe<EmgData, Int> {

    override val ports: Pair<KClass<*>, KClass<*>> = Pair(EmgData::class, Int::class)

    override val name = "EmgData to Heart rate"

    override fun pipe(arg: EmgData): Int {
        return arg.lastOfHeartRate()
    }
}