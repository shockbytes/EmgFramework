package at.fhooe.mc.emg.designer.view

import at.fhooe.mc.emg.designer.DesignerViewCallback
import at.fhooe.mc.emg.designer.component.EmgBaseComponent
import java.io.File

/**
 * Author:  Martin Macheiner
 * Date:    16.04.2018
 */
interface DesignerView {

    var components: List<EmgBaseComponent>

    fun show(viewCallback: DesignerViewCallback, components: List<EmgBaseComponent>)

    fun askForStorageBeforeQuit(): File?

    fun showStatusMessage(msg: String)

}