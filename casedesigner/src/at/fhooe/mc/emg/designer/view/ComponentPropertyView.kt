package at.fhooe.mc.emg.designer.view

import at.fhooe.mc.emg.designer.component.EmgBaseComponent
import at.fhooe.mc.emg.designer.component.util.EmgComponentParameter

interface ComponentPropertyView {

    fun showPropertyView(component: EmgBaseComponent,
                         point: Pair<Int, Int>,
                         callback: ((EmgBaseComponent, EmgComponentParameter, String) -> Boolean))

}