package at.fhooe.mc.emg.desktop.client.bluetooth

import at.fhooe.mc.emg.clientdriver.EmgClientDriver
import at.fhooe.mc.emg.clientdriver.EmgClientDriverConfigView
import at.fhooe.mc.emg.core.util.CoreUtils
import java.awt.GridLayout
import java.awt.Rectangle
import javax.swing.*
import javax.swing.border.EmptyBorder

/**
 * Author:  Mescht
 * Date:    27.11.2017
 */

class DesktopBluetoothClientDriverConfigView : EmgClientDriverConfigView {

    override val name: String = "Bluetooth Config"

    private val frame: JFrame = JFrame()
    private lateinit var textFieldRemoteMac: JTextField
    private lateinit var btnApply: JButton

    init {
        setupFrame()
    }

    private fun setupFrame() {

        frame.bounds = Rectangle(400, 200, 300, 200)
        frame.title = name
        frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE

        textFieldRemoteMac = JTextField()
        btnApply = JButton("APPLY")

        val contentPanel = JPanel()
        contentPanel.layout = GridLayout(4, 1, 4, 4)
        contentPanel.border = EmptyBorder(8, 8, 8, 8)
        contentPanel.add(JLabel("Remote MAC address"))
        contentPanel.add(textFieldRemoteMac)
        contentPanel.add(JLabel())
        contentPanel.add(btnApply)
        frame.contentPane = contentPanel
    }

    override fun show(client: EmgClientDriver) {
        client as DesktopBluetoothClientDriver

        textFieldRemoteMac.text = client.remoteDeviceMacAddress

        btnApply.actionListeners.forEach { btnApply.removeActionListener(it) }
        btnApply.addActionListener {

            val remoteMac = textFieldRemoteMac.text
            if (CoreUtils.validateMacAddress(remoteMac)) {
                client.remoteDeviceMacAddress = remoteMac
                frame.dispose()
            } else {
                JOptionPane.showMessageDialog(frame, "$remoteMac is not a valid MAC address format!")
            }
        }

        frame.isVisible = true
    }

}