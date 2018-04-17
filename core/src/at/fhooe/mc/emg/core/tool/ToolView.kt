package at.fhooe.mc.emg.core.tool

/**
 * Author:  Mescht
 * Date:    20.11.2017
 */
interface ToolView<in T> {

    fun setup(toolViewCallback: T, showViewImmediate: Boolean)

    fun showView()
}