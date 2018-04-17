package at.fhooe.mc.emg.core.tool.salient

import at.fhooe.mc.emg.core.EmgPresenter
import at.fhooe.mc.emg.core.tool.Tool
import at.fhooe.mc.emg.designer.EmgComponent
import at.fhooe.mc.emg.designer.EmgComponentType

/**
 * Author:  Martin Macheiner
 * Date:    17.04.2018
 *
 * Detects salient points of a calculated metric during time
 *
 */
@EmgComponent(EmgComponentType.RELAY_SINK)
class SalientPointTool (override var toolView: SalientPointToolView? = null) : Tool, SalientPointToolViewCallback {

    override val name = "Salient Point Detection"

    override fun start(presenter: EmgPresenter, showViewImmediate: Boolean) {
        toolView?.setup(this, showViewImmediate)
    }

    override fun onViewClosed() {

    }

}
