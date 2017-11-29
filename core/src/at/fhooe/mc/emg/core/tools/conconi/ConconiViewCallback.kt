package at.fhooe.mc.emg.core.tools.conconi

import at.fhooe.mc.emg.core.tools.ToolViewCallback

/**
 * Author:  Mescht
 * Date:    08.07.2017
 */
interface ConconiViewCallback : ToolViewCallback {

    fun onStartClicked()

    fun onStopClicked()

    fun onSaveClicked(filename: String): Boolean

    fun onLoadClicked(filename: String): Boolean

}
