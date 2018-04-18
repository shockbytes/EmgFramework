package at.fhooe.mc.emg.designer.component

import at.fhooe.mc.emg.designer.draw.model.Origin

class EmgSinkComponent(name: String = "",
                       qualifiedName: String = "",
                       origin: Origin = Origin(0, 0)) : EmgBaseComponent(name, qualifiedName, origin) {

    override val portConfiguration: Pair<Boolean, Boolean> = Pair(true, false)

    override fun copyWithOrigin(x: Int, y: Int): EmgBaseComponent {
        return EmgSinkComponent(name, qualifiedName, Origin(x, y))
    }
}