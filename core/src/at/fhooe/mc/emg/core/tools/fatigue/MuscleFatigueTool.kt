package at.fhooe.mc.emg.core.tools.fatigue

import at.fhooe.mc.emg.core.EmgPresenter
import at.fhooe.mc.emg.core.tools.Tool

/**
 * Author:  Martin Macheiner
 * Date:    07.01.2018
 */

// TODO Implement Tool for Muscle Fatigue Detection
class MuscleFatigueTool (override var view: MuscleFatigueView? = null) : Tool, MuscleFatigueViewCallback {


    override val name = "Muscle Fatigue"

    override fun start(presenter: EmgPresenter, showViewImmediate: Boolean) {
        view?.setup(this, showViewImmediate)
    }

    override fun onViewClosed() {

    }

}