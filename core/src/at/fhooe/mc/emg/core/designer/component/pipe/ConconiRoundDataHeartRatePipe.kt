package at.fhooe.mc.emg.core.designer.component.pipe

import at.fhooe.mc.emg.core.tool.conconi.ConconiRoundData
import at.fhooe.mc.emg.designer.component.pipe.EmgComponentPipe
import kotlin.reflect.KClass

class ConconiRoundDataHeartRatePipe: EmgComponentPipe<ConconiRoundData, Double> {

    override val ports: Pair<KClass<*>, KClass<*>> = Pair(ConconiRoundData::class, Double::class)

    override val name: String = "Conconi round data to heart rate"

    override fun pipe(arg: ConconiRoundData): Double = arg.hr.toDouble()
}