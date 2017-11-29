package at.fhooe.mc.emg.core.tools

/**
 * Author:  Mescht
 * Date:    20.11.2017
 */
interface ToolView<in T> {

    fun setup(viewCallback: T)

}