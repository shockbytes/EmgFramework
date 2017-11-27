package at.fhooe.mc.emg.view

import at.fhooe.mc.emg.client.ChannelData
import at.fhooe.mc.emg.client.EmgClient
import at.fhooe.mc.emg.client.network.NetworkClient
import at.fhooe.mc.emg.client.simulation.SimulationClient
import at.fhooe.mc.emg.core.EmgController
import at.fhooe.mc.emg.filter.Filter
import at.fhooe.mc.emg.tools.Tool
import at.fhooe.mc.emg.util.Configuration

/**
 * Author:  Mescht
 * Date:    22.11.2017
 */
interface EmgView {

    // TODO Keep EmgController for now, but maybe replace it with EmgViewCallback
    fun setup(viewCallback: EmgViewCallback, config: Configuration)

    fun reset()

    fun updateStatus(status: String)

    fun setDeviceControlsEnabled(isEnabled: Boolean)

    fun onRawClientDataAvailable(raw: String)

    fun onChanneledClientDataAvailable(cd: ChannelData, filters: List<Filter>)

    // ----------------------- Clients -----------------------

    fun setupSimulationClientView(client: SimulationClient)

    fun setupNetworkClientView(client: NetworkClient)

    // -------------------------------------------------------

    // ------------------ Utility list views -----------------

    fun setupToolsView(tools: List<Tool>, controller: EmgController)

    fun setupFilterViews(filter: List<Filter>)

    fun setupEmgClientView(clients: List<EmgClient>, defaultClient: EmgClient)

    // -------------------------------------------------------


}