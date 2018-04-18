package at.fhooe.mc.emg.designer.component

import at.fhooe.mc.emg.designer.draw.model.Origin

class EmgDeviceComponent(name: String = "",
                         qualifiedName: String = "",
                         origin: Origin = Origin(0, 0)) : EmgBaseComponent(name, qualifiedName, origin) {

    override val portConfiguration: Pair<Boolean, Boolean> = Pair(false, true)

    override fun copyWithOrigin(x: Int, y: Int): EmgBaseComponent {
        return EmgDeviceComponent(name, qualifiedName, Origin(x, y))
    }
}