package at.fhooe.mc.emg.designer

import at.fhooe.mc.emg.designer.component.EmgBaseComponent
import at.fhooe.mc.emg.designer.view.DesignerView
import java.io.File

/**
 * Author:  Martin Macheiner
 * Date:    16.04.2018
 */
interface DesignerPresenter : DesignerViewCallback {

    val view: DesignerView

    val components: List<EmgBaseComponent>

    fun start(file: File? = null)

}