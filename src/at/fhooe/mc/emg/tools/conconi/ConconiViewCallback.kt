package at.fhooe.mc.emg.tools.conconi

import at.fhooe.mc.emg.tools.ViewCallback

/**
 * Author:  Mescht
 * Date:    08.07.2017
 */
interface ConconiViewCallback : ViewCallback {

    fun onStartClicked()

    fun onStopClicked()

    fun onSaveClicked(filename: String): Boolean

    fun onLoadClicked(filename: String): Boolean

}
