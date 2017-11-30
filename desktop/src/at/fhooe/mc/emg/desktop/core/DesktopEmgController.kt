package at.fhooe.mc.emg.desktop.core

import at.fhooe.mc.emg.clientdriver.EmgClientDriver
import at.fhooe.mc.emg.core.EmgController
import at.fhooe.mc.emg.core.tools.Tool
import at.fhooe.mc.emg.core.view.VisualView
import at.fhooe.mc.emg.desktop.view.DesktopEmgView
import at.fhooe.mc.emg.desktop.view.XChartVisualView
import javax.swing.JComponent

/**
 * Author:  Mescht
 * Date:    22.11.2017
 */

class DesktopEmgController(c: List<EmgClientDriver>, t: List<Tool>, v: DesktopEmgView<JComponent>) : EmgController(c, t, v) {

    override val visualView: VisualView<JComponent> = XChartVisualView()

    init {
        v.setVisualView(visualView)
    }

}