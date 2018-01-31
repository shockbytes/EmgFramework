package at.fhooe.mc.emg.desktop.client.bluetooth

import at.fhooe.mc.emg.clientdriver.EmgClientDriver
import at.fhooe.mc.emg.clientdriver.EmgClientDriverConfigView
import at.fhooe.mc.emg.core.util.CoreUtils
import java.awt.BorderLayout
import java.awt.GridLayout
import java.awt.Rectangle
import java.util.*
import javax.bluetooth.*
import javax.bluetooth.UUID
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener


/**
 * Author:  Mescht
 * Date:    27.11.2017
 */

class DesktopBluetoothClientDriverConfigView : EmgClientDriverConfigView, ListSelectionListener {

    override val name: String = "Bluetooth Config"

    private val frame: JFrame = JFrame()
    private val frameDevices: JFrame = JFrame()
    private lateinit var textFieldRemoteMac: JTextField
    private lateinit var textFieldUuid: JTextField
    private lateinit var textFieldChannel: JTextField
    private lateinit var btnApply: JButton
    private lateinit var listDevices: JList<String>
    private lateinit var labelDevices: JLabel
    private lateinit var labelServices: JLabel

    init {
        setupFrame()
        setupDeviceFrame()
    }

    private fun setupFrame() {

        frame.bounds = Rectangle(400, 200, 250, 370)
        frame.title = name
        frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE

        textFieldRemoteMac = JTextField()
        textFieldUuid = JTextField()
        textFieldChannel = JTextField()
        btnApply = JButton("APPLY")

        val contentPanel = JPanel()
        contentPanel.layout = GridLayout(12, 1, 4, 4)
        contentPanel.border = EmptyBorder(8, 8, 8, 8)
        contentPanel.add(JLabel("Remote MAC address"))
        contentPanel.add(textFieldRemoteMac)
        val btnScanDevices = JButton("Scan for devices")
        btnScanDevices.addActionListener { scanDevices() }
        contentPanel.add(btnScanDevices)
        contentPanel.add(JLabel())
        contentPanel.add(JLabel("Bluetooth Channel"))
        contentPanel.add(textFieldChannel)
        val btnScanServices = JButton("Scan for services")
        btnScanServices.addActionListener { scanServices() }
        contentPanel.add(btnScanServices)
        labelServices = JLabel()
        contentPanel.add(labelServices)
        contentPanel.add(JLabel("UUID"))
        contentPanel.add(textFieldUuid)
        contentPanel.add(JLabel())
        contentPanel.add(btnApply)
        frame.contentPane = contentPanel
    }

    private fun setupDeviceFrame() {
        frameDevices.bounds = Rectangle(700, 170, 230, 250)
        frameDevices.title = "Nearby"
        frameDevices.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        frameDevices.layout = BorderLayout()

        listDevices = JList()
        listDevices.border = EmptyBorder(4, 4, 4, 4)
        listDevices.removeListSelectionListener(this)
        listDevices.addListSelectionListener(this)
        frameDevices.add(listDevices, BorderLayout.CENTER)
        labelDevices = JLabel()
        labelDevices.border = EmptyBorder(4, 4, 4, 4)
        frameDevices.add(labelDevices, BorderLayout.SOUTH)
    }

    override fun show(client: EmgClientDriver) {
        client as DesktopBluetoothClientDriver

        textFieldRemoteMac.text = client.remoteDeviceMacAddress
        textFieldUuid.text = client.uuid.toString()
        textFieldChannel.text = client.channel

        btnApply.actionListeners.forEach { btnApply.removeActionListener(it) }
        btnApply.addActionListener {

            val remoteMac = textFieldRemoteMac.text
            if (CoreUtils.validateMacAddress(remoteMac)) {
                client.remoteDeviceMacAddress = remoteMac
                client.uuid = UUID(textFieldUuid.text.toString().trim(), false)
                client.channel = textFieldChannel.text
                frame.dispose()
            } else {
                JOptionPane.showMessageDialog(frame, "$remoteMac is not a valid MAC address format!")
            }
        }

        frame.isVisible = true
    }

    override fun valueChanged(e: ListSelectionEvent?) {

        val mac = listDevices.selectedValue.split("-")[1].trim()
        // Add the : between a pair of 2 digits
        val validMac = StringBuilder()
        for (i in 2..mac.length step 2) {
            validMac.append(mac.subSequence(i - 2, i))
            if (i < mac.length - 1) {
                validMac.append(":")
            }
        }
        textFieldRemoteMac.text = validMac.toString()
    }


    private fun scanDevices() {

        val localDevice = LocalDevice.getLocalDevice()
        val agent = localDevice.discoveryAgent

        frameDevices.isVisible = true
        labelDevices.text = "Searching..."

        val devices = Vector<String>()
        agent.startInquiry(DiscoveryAgent.GIAC, object : DiscoveryListener {
            override fun serviceSearchCompleted(p0: Int, p1: Int) {
            }

            override fun deviceDiscovered(p0: RemoteDevice?, p1: DeviceClass?) {
                devices.add("${p0?.getFriendlyName(false)} - ${p0?.bluetoothAddress}")
            }

            override fun servicesDiscovered(p0: Int, p1: Array<out ServiceRecord>?) {
            }

            override fun inquiryCompleted(p0: Int) {
                labelDevices.text = "Inquiry completed!"
                listDevices.setListData(devices)
            }
        })

    }

    private fun scanServices() {

        val localDevice = LocalDevice.getLocalDevice()
        val agent = localDevice.discoveryAgent

        val selectedMac = textFieldRemoteMac.text.replace(":", "")
        val selectedUuidSet = arrayOf(UUID(textFieldUuid.text, false))

        labelServices.text = "Search services @ ${textFieldRemoteMac.text}"
        agent.searchServices(null, selectedUuidSet, EmgBluetoothRemoteDevice(selectedMac), object : DiscoveryListener {
            override fun serviceSearchCompleted(p0: Int, p1: Int) {
            }

            override fun deviceDiscovered(p0: RemoteDevice?, p1: DeviceClass?) {
            }

            override fun servicesDiscovered(p0: Int, services: Array<out ServiceRecord>?) {
                val url = services?.get(0)?.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false)

                val startIdx = url?.indexOf(":", url.indexOf(":") + 1) ?: -1
                val endIdx = url?.indexOf(";") ?: -1

                if (startIdx < endIdx) {
                    val ch = url?.subSequence(startIdx + 1, endIdx) as? String
                    if (ch?.toIntOrNull() != null) {
                        textFieldChannel.text = ch
                        labelServices.text = "Service found at channel $ch"
                    }
                }

            }

            override fun inquiryCompleted(p0: Int) {
            }
        })
    }

}