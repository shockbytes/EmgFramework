package at.fhooe.mc.emg.tools

import at.fhooe.mc.emg.core.EmgController

/**
 * Author:  Martin Macheiner
 * Date:    04.07.2017
 */
interface Tool {

    val name: String

    fun start(controller: EmgController<*>)
}
