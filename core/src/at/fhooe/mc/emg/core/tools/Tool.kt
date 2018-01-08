package at.fhooe.mc.emg.core.tools

import at.fhooe.mc.emg.core.EmgController

/**
 * Author:  Martin Macheiner
 * Date:    04.07.2017
 */
interface Tool {

    val name: String

    val view: ToolView<*>?

    fun start(controller: EmgController, showViewImmediate: Boolean)
}
