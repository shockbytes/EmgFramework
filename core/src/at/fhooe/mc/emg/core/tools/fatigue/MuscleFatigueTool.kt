package at.fhooe.mc.emg.core.tools.fatigue

import at.fhooe.mc.emg.core.EmgController
import at.fhooe.mc.emg.core.tools.Tool

/**
 * Author:  Martin Macheiner
 * Date:    07.01.2018
 */

// TODO Implement Tool for Muscle Fatigue Detection
class MuscleFatigueTool (var view: MuscleFatigueView? = null) : Tool, MuscleFatigueViewCallback {


    override val name = "Muscle Fatigue Detection"

    override fun start(controller: EmgController) {
        view?.setup(this)
    }

    override fun onViewClosed() {

    }

}