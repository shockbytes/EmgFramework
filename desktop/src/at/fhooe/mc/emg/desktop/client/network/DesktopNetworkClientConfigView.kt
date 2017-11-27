package at.fhooe.mc.emg.desktop.client.network

import at.fhooe.mc.emg.client.EmgClient
import at.fhooe.mc.emg.client.EmgClientConfigView
import at.fhooe.mc.emg.client.network.NetworkClient
import java.awt.Rectangle
import javax.swing.JFrame

/**
 * Author:  Mescht
 * Date:    27.11.2017
 */

class DesktopNetworkClientConfigView : EmgClientConfigView {

    override val name: String = "Network Config"

    private val frame: JFrame = JFrame()
    private lateinit var networkClient: NetworkClient

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
        networkClient = client as NetworkClient

        frame.isVisible = true
    }

}