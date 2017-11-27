package at.fhooe.mc.emg.desktop.core

import at.fhooe.mc.emg.client.EmgClient
import at.fhooe.mc.emg.core.EmgController
import at.fhooe.mc.emg.desktop.view.DesktopEmgView
import at.fhooe.mc.emg.desktop.view.XChartVisualView
import at.fhooe.mc.emg.tools.Tool
import at.fhooe.mc.emg.view.VisualView
import javax.swing.JComponent

/**
 * Author:  Mescht
 * Date:    22.11.2017
 */

class DesktopEmgController(c: List<EmgClient>, t: List<Tool>, v: DesktopEmgView<JComponent>) : EmgController(c, t, v) {

    override val visualView: VisualView<JComponent> = XChartVisualView()

    init {
        v.setVisualView(visualView)
    }

}