package at.fhooe.mc.emg.designer.view

import at.fhooe.mc.emg.designer.DesignerViewCallback
import at.fhooe.mc.emg.designer.component.EmgBaseComponent

interface ComponentInteractionView {

    var interactionComponents: List<EmgBaseComponent>

    fun setup(viewCallback: DesignerViewCallback?)

}