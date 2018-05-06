package at.fhooe.mc.emg.designer.component

import at.fhooe.mc.emg.designer.component.model.Origin
import at.fhooe.mc.emg.designer.component.util.EmgComponentParameter

class EmgRelayComponent(name: String = "",
                        qualifiedName: String = "",
                        parameter: List<EmgComponentParameter> = listOf(),
                        displayTitle: String = "",
                        origin: Origin = Origin(0, 0)) : EmgBaseComponent(name, qualifiedName, parameter, displayTitle, origin) {

    override val portConfiguration: Pair<Boolean, Boolean> = Pair(true, true)

    override fun copyWithOrigin(x: Int, y: Int): EmgBaseComponent {
        return EmgRelayComponent(name, qualifiedName, parameter, displayTitle, Origin(x, y))
    }
}