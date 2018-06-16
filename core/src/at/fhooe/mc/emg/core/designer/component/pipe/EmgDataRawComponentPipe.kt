package at.fhooe.mc.emg.core.designer.component.pipe

import at.fhooe.mc.emg.clientdriver.model.EmgData
import at.fhooe.mc.emg.core.util.toLoggingString
import at.fhooe.mc.emg.designer.component.pipe.EmgComponentPipe
import kotlin.reflect.KClass

class EmgDataRawComponentPipe : EmgComponentPipe<EmgData, String> {

    override val ports: Pair<KClass<*>, KClass<*>> = Pair(EmgData::class, String::class)

    override val name = "Raw EmgData"

    override fun pipe(arg: EmgData): String  = arg.toLoggingString()

}