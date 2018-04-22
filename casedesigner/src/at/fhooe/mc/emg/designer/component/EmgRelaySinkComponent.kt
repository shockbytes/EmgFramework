package at.fhooe.mc.emg.designer.component

import at.fhooe.mc.emg.designer.component.util.EmgComponentParameter
import at.fhooe.mc.emg.designer.component.model.Origin

class EmgRelaySinkComponent(name: String = "",
                            qualifiedName: String = "",
                            parameter: List<EmgComponentParameter> = listOf(),
                            origin: Origin = Origin(0, 0)) : EmgBaseComponent(name, qualifiedName, parameter, origin) {

    override val portConfiguration: Pair<Boolean, Boolean> = Pair(true, true)

    override fun copyWithOrigin(x: Int, y: Int): EmgBaseComponent {
        return EmgRelaySinkComponent(name, qualifiedName, parameter, Origin(x, y))
    }
}