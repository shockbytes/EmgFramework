package at.fhooe.mc.emg.core.tool.fatigue

import at.fhooe.mc.emg.core.Toolable
import at.fhooe.mc.emg.core.tool.Tool
import at.fhooe.mc.emg.designer.EmgComponentType
import at.fhooe.mc.emg.designer.annotation.EmgComponent

/**
 * Author:  Martin Macheiner
 * Date:    07.01.2018
 */

// TODO Implement Tool for Muscle Fatigue Detection
@EmgComponent(EmgComponentType.RELAY_SINK)
class MuscleFatigueTool (override var toolView: MuscleFatigueToolView? = null) : Tool, MuscleFatigueToolViewCallback {

    override val name = "Muscle Fatigue Detection"

    override fun start(toolable: Toolable, showViewImmediate: Boolean) {
        toolView?.setup(this, showViewImmediate)
    }

    override fun onViewClosed() {

    }

}