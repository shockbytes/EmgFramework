package at.fhooe.mc.emg.desktop.view

import at.fhooe.mc.emg.desktop.client.serial.SerialClient
import at.fhooe.mc.emg.view.EmgView
import at.fhooe.mc.emg.view.VisualView

/**
 * Author:  Martin Macheiner
 * Date:    27.11.2017
 */

interface DesktopEmgView<in T> : EmgView {

    //fun setup(viewCallback: DesktopEmgController)

    fun setupSerialClientView(client: SerialClient)

    fun setVisualView(view: VisualView<T>)

}