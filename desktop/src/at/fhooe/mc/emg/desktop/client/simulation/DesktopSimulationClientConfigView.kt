package at.fhooe.mc.emg.desktop.client.simulation

import at.fhooe.mc.emg.client.EmgClient
import at.fhooe.mc.emg.client.EmgClientConfigView
import at.fhooe.mc.emg.client.simulation.SimulationClient
import java.awt.Rectangle
import javax.swing.JFrame

/**
 * Author:  Mescht
 * Date:    27.11.2017
 */

class DesktopSimulationClientConfigView : EmgClientConfigView {

    override val name: String = "Simulation Config"

    private val frame: JFrame = JFrame()
    private lateinit var simulationClient: SimulationClient

    init {
        setupFrame()
    }

    private fun setupFrame() {

        frame.bounds = Rectangle(400, 200, 300, 200)
        frame.title = name
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

    }

    override fun show(client: EmgClient) {

        // TODO

        simulationClient = client as SimulationClient

        frame.isVisible = true
    }

    // -------------------------------------------------------------

    /*
    override fun setupSimulationClientView(client: SimulationClient) {

        mnSimulationData?.removeAll()
        client.simulationSources.forEach { src ->

            val item = JCheckBoxMenuItem(src.name)
            item.addActionListener {
                mnSimulationData?.menuComponents?.forEach {
                    val otherItem = it as JCheckBoxMenuItem
                    if (otherItem !== item) {
                        otherItem.isSelected = false
                    }
                }
                client.simulationSource = src
            }
            mnSimulationData?.add(item)
        }

        if (client.simulationSources.isNotEmpty()) {
            mnSimulationData?.getItem(client.simulationSources.size / 2 + 1)?.doClick()
        }
    }

    */

}