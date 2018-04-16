package at.fhooe.mc.emg.core.tools.fatigue

import at.fhooe.mc.emg.core.EmgComponent
import at.fhooe.mc.emg.core.EmgPresenter
import at.fhooe.mc.emg.core.tools.Tool

/**
 * Author:  Martin Macheiner
 * Date:    07.01.2018
 */

// TODO Implement Tool for Muscle Fatigue Detection
@EmgComponent
class MuscleFatigueTool (override var toolView: MuscleFatigueToolView? = null) : Tool, MuscleFatigueToolViewCallback {


    override val name = "Muscle Fatigue"

    override fun start(presenter: EmgPresenter, showViewImmediate: Boolean) {
        toolView?.setup(this, showViewImmediate)
    }

    override fun onViewClosed() {

    }

}