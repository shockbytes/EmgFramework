package at.fhooe.mc.emg.desktop.client.serial

import at.fhooe.mc.emg.client.EmgClient
import at.fhooe.mc.emg.client.EmgClientConfigView
import java.awt.GridLayout
import java.awt.Rectangle
import javax.swing.*
import javax.swing.border.EmptyBorder

/**
 * Author:  Mescht
 * Date:    27.11.2017
 */
class DesktopSerialClientConfigView : EmgClientConfigView {

    override val name: String = "Serial Config"

    private val frame: JFrame = JFrame()
    private lateinit var comboBoxPorts: JComboBox<String>
    private lateinit var comboBoxDatarates: JComboBox<String>
    private lateinit var btnReload: JButton
    private lateinit var btnApply: JButton

    init {
        setupFrame()
    }

    private fun setupFrame() {

        frame.bounds = Rectangle(400, 200, 275, 275)
        frame.title = name
        frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE

        comboBoxPorts = JComboBox()
        comboBoxDatarates = JComboBox(SerialClient.supportedDataRates.map { i -> i.toString() }.toTypedArray())
        comboBoxDatarates.selectedItem = SerialClient.defaultDataRate.toString()
        btnReload = JButton("RELOAD")
        btnApply = JButton("APPLY CHANGES")

        val contentPanel = JPanel()
        contentPanel.layout = GridLayout(6, 1, 4, 4)
        contentPanel.border = EmptyBorder(8, 8, 8, 8)
        contentPanel.add(JLabel("Ports"))
        contentPanel.add(comboBoxPorts)
        contentPanel.add(btnReload)
        contentPanel.add(JLabel("Data rate"))
        contentPanel.add(comboBoxDatarates)
        contentPanel.add(btnApply)
        frame.contentPane = contentPanel
    }

    override fun show(client: EmgClient) {

        client as SerialClient

        btnReload.addActionListener {
            comboBoxPorts.removeAllItems()
            client.getAvailablePortNames(true).forEach {
                comboBoxPorts.addItem(it)
            }
        }
        // Load the ports the first time
        btnReload.doClick()

        btnApply.actionListeners.forEach { btnApply.removeActionListener(it) }
        btnApply.addActionListener {

            // Set selected port
            val port: String? = comboBoxPorts.selectedItem?.toString()
            if (port != null) {
                client.setSerialPortSelected(port)
            }
            // Set data rate
            client.dataRate = comboBoxDatarates.selectedItem.toString().toInt()
            // Hide frame
            frame.dispose()
        }
        frame.isVisible = true
    }
}