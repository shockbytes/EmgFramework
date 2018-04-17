package at.fhooe.mc.emg.designer.view

import at.fhooe.mc.emg.designer.component.EmgBaseComponent

interface MiniMap {

    var scale: Int

    var miniMapComponents : List<EmgBaseComponent>

    fun invalidateMiniMap()
}