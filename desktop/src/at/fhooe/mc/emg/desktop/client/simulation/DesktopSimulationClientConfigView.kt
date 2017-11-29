package at.fhooe.mc.emg.desktop.client.simulation

import at.fhooe.mc.emg.client.EmgClient
import at.fhooe.mc.emg.client.EmgClientConfigView
import at.fhooe.mc.emg.client.simulation.SimulationClient
import at.fhooe.mc.emg.client.simulation.SimulationSource
import java.awt.GridLayout
import java.awt.Rectangle
import javax.swing.*
import javax.swing.border.EmptyBorder

/**
 * Author:  Mescht
 * Date:    27.11.2017
 */

class DesktopSimulationClientConfigView : EmgClientConfigView {

    override val name: String = "Simulation Config"

    private val frame: JFrame = JFrame()

    private lateinit var comboBoxSources: JComboBox<String>
    private lateinit var cbPlaybackLoop: JCheckBox
    private lateinit var btnApply: JButton

    init {
        setupFrame()
    }

    private fun setupFrame() {

        frame.bounds = Rectangle(400, 200, 300, 200)
        frame.title = name
        frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE

        btnApply = JButton("APPLY")
        comboBoxSources = JComboBox()
        cbPlaybackLoop = JCheckBox("Playback loop", false)

        val contentPanel = JPanel()
        contentPanel.layout = GridLayout(6, 1, 4, 4)
        contentPanel.border = EmptyBorder(8, 8, 8, 8)
        contentPanel.add(JLabel("Simulation source"))
        contentPanel.add(comboBoxSources)
        contentPanel.add(JLabel())
        contentPanel.add(cbPlaybackLoop)
        contentPanel.add(JLabel())
        contentPanel.add(btnApply)
        frame.contentPane = contentPanel
    }

    override fun show(client: EmgClient) {

        client as SimulationClient

        client.simulationSources.forEach {
            comboBoxSources.addItem(it.name)
        }
        comboBoxSources.selectedItem = client.simulationSource?.name

        cbPlaybackLoop.isSelected = client.isEndlessLoopEnabled

        btnApply.actionListeners.forEach { btnApply.removeActionListener(it) }
        btnApply.addActionListener {

            client.simulationSource = findSimulationSourceByName(client, comboBoxSources.selectedItem as String)
            client.isEndlessLoopEnabled = cbPlaybackLoop.isSelected
            frame.dispose()
        }

        frame.isVisible = true
    }

    private fun findSimulationSourceByName(client: SimulationClient, name: String): SimulationSource? {
        client.simulationSources.forEach {
            if (it.name == name) {
                return it
            }
        }
        return null
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