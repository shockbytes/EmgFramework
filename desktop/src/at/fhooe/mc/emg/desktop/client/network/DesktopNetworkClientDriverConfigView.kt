package at.fhooe.mc.emg.desktop.client.network

import at.fhooe.mc.emg.clientdriver.EmgClientDriver
import at.fhooe.mc.emg.clientdriver.EmgClientDriverConfigView
import at.fhooe.mc.emg.core.client.network.NetworkClientDriver
import java.awt.GridLayout
import java.awt.Rectangle
import javax.swing.*
import javax.swing.border.EmptyBorder

/**
 * Author:  Mescht
 * Date:    27.11.2017
 */

class DesktopNetworkClientDriverConfigView : EmgClientDriverConfigView {

    override val name: String = "Network EmgConfigOld"

    private val frame: JFrame = JFrame()
    private lateinit var textFieldIp: JTextField
    private lateinit var textFieldPort: JTextField
    private lateinit var btnApply: JButton

    init {
        setupFrame()
    }

    private fun setupFrame() {

        frame.bounds = Rectangle(400, 200, 300, 200)
        frame.title = name
        frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE

        textFieldIp = JTextField()
        textFieldPort = JTextField()
        btnApply = JButton("APPLY")

        val contentPanel = JPanel()
        contentPanel.layout = GridLayout(6, 1, 4, 4)
        contentPanel.border = EmptyBorder(8, 8, 8, 8)
        contentPanel.add(JLabel("IP Address"))
        contentPanel.add(textFieldIp)
        contentPanel.add(JLabel())
        contentPanel.add(JLabel("Port"))
        contentPanel.add(textFieldPort)
        contentPanel.add(btnApply)
        frame.contentPane = contentPanel
    }

    override fun show(client: EmgClientDriver) {

        client as NetworkClientDriver

        textFieldPort.text = client.port.toString()
        textFieldIp.text = client.ip

        btnApply.actionListeners.forEach { btnApply.removeActionListener(it) }
        btnApply.addActionListener {

            val port = textFieldPort.text.toIntOrNull()
            if (port != null) {
                client.setSocketOptions(textFieldIp.text, port)
                frame.dispose()
            } else {
                JOptionPane.showMessageDialog(frame, "Port must be an integer value!")
                textFieldPort.text = ""
            }
        }

        frame.isVisible = true
    }

}