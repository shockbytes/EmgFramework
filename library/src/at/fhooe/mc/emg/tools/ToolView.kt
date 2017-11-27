package at.fhooe.mc.emg.tools

/**
 * Author:  Mescht
 * Date:    20.11.2017
 */
interface ToolView<in T> {

    fun setup(viewCallback: T)

}