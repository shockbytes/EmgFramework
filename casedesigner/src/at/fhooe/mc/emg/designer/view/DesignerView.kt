package at.fhooe.mc.emg.designer.view

import at.fhooe.mc.emg.designer.DesignerViewCallback
import at.fhooe.mc.emg.designer.component.EmgBaseComponent

/**
 * Author:  Martin Macheiner
 * Date:    16.04.2018
 */
interface DesignerView {

    fun show(viewCallback: DesignerViewCallback, components: List<EmgBaseComponent>)

}