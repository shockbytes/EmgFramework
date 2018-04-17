package at.fhooe.mc.emg.core.tool

import at.fhooe.mc.emg.core.EmgPresenter

/**
 * Author:  Martin Macheiner
 * Date:    04.07.2017
 */
interface Tool {

    val name: String

    val toolView: ToolView<*>?

    fun start(presenter: EmgPresenter, showViewImmediate: Boolean)
}
