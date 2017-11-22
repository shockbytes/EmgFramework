package at.fhooe.mc.emg.core

import at.fhooe.mc.emg.client.EmgClient
import at.fhooe.mc.emg.tools.Tool
import at.fhooe.mc.emg.visual.Visual
import at.fhooe.mc.emg.visual.XChartVisual
import javax.swing.JComponent

/**
 * Author:  Mescht
 * Date:    22.11.2017
 */

class DesktopEmgController(c: List<EmgClient>, t: List<Tool>) : EmgController(c, t) {

    override val visual: Visual<JComponent> = XChartVisual()

}