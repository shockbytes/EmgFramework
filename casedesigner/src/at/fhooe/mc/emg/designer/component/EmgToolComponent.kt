package at.fhooe.mc.emg.designer.component

import at.fhooe.mc.emg.designer.draw.model.Origin

class EmgToolComponent(name: String = "",
                       qualifiedName: String = "",
                       origin: Origin = Origin(0, 0)) : EmgBaseComponent(name, qualifiedName, origin) {

    override fun copyWithOrigin(x: Int, y: Int): EmgBaseComponent {
        return EmgToolComponent(name, qualifiedName, Origin(x, y))
    }
}