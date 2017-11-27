package at.fhooe.mc.emg.core

import at.fhooe.mc.emg.client.ChannelData
import at.fhooe.mc.emg.client.ClientCategory
import at.fhooe.mc.emg.client.EmgClient
import at.fhooe.mc.emg.client.network.NetworkClient
import at.fhooe.mc.emg.client.simulation.SimulationClient
import at.fhooe.mc.emg.filter.*
import at.fhooe.mc.emg.storage.CsvDataStorage
import at.fhooe.mc.emg.storage.DataStorage
import at.fhooe.mc.emg.tools.Tool
import at.fhooe.mc.emg.util.Configuration
import at.fhooe.mc.emg.view.EmgView
import at.fhooe.mc.emg.view.EmgViewCallback
import at.fhooe.mc.emg.view.VisualView
import java.util.*

/**
 * Author:  Martin Macheiner
 * Date:    07.07.2017
 */
abstract class EmgController(val clients: List<EmgClient>, val tools: List<Tool>,
                             val emgView: EmgView) : EmgViewCallback {

    private lateinit var client: EmgClient

    private lateinit var config: Configuration

    private lateinit var filters: List<Filter>

    abstract val visualView: VisualView<*>



    val currentDataPointer: Int
        get() = client.currentDataPointer

    init {
        initialize()
    }

    // ------------------------------------------ Private methods ------------------------------------------

    private fun initialize() {

        // Load configuration
        config = Configuration

        // Set default client
        client = clients[clients.size / 2]

        // Initialize filter
        filters = Arrays.asList(
                NoFilter(),
                BandstopFilter(),
                LowpassFilter(),
                RunningAverageFilter(config.runningAverageWindowSize),
                SavitzkyGolayFilter(config.savitzkyGolayFilterWidth))

        setupEmgView()
    }

    private fun setupEmgView() {

        emgView.setup(this, config)
        emgView.setupFilterViews(filters)
        emgView.setupEmgClientView(clients, client)
        emgView.setupToolsView(tools, this)

        val simClient = getClient(ClientCategory.SIMULATION) as? SimulationClient
        if (simClient != null) {
            emgView.setupSimulationClientView(simClient)
        }

        val netClient = getClient(ClientCategory.NETWORK) as? NetworkClient
        if (netClient != null) {
            emgView.setupNetworkClientView(netClient)
        }
    }

    private fun storeData(writeOnDisconnectFileName: String?) {

        if (config.isWriteToLogEnabled && client.isDataStorageEnabled && writeOnDisconnectFileName != null) {
            exportData(writeOnDisconnectFileName, CsvDataStorage())
        }
    }
    // ----------------------------------------- Abstract methods ------------------------------------------


    // ----------------------------------------------------------------------------------------------------

    // ------------------------------------------ Public methods ------------------------------------------

    fun getClient(category: ClientCategory): EmgClient? {
        clients.forEach {
            if (it.category === category) {
                return it
            }
        }
        return null
    }

    fun hasClient(category: ClientCategory): Boolean {
        clients.forEach {
            if (it.category === category) {
                return true
            }
        }
        return false
    }

    fun saveConfig() {
        config.save()
    }

    fun getSingleChannelDataSection(start: Int, stop: Int, channel: Int): ChannelData {
        return client.channelData.getSingleChannelSection(start, stop, channel)
    }

    fun setSimulationPlaybackLoopEnabled(isEnabled: Boolean) {
        config.isSimulationEndlessLoopEnabled = isEnabled
        // TODO Pass through
        // simulationClient.isEndlessLoopEnabled = isEnabled
    }

    private fun tryCopySimulationData(filename: String) {

        if (hasClient(ClientCategory.SIMULATION)
                && config.isCopyToSimulationEnabled
                && client.category !== ClientCategory.SIMULATION) {
            val simulationClient: SimulationClient? = getClient(ClientCategory.SIMULATION) as? SimulationClient
            if (simulationClient != null) {
                simulationClient.addFileAsSimulationSource(filename)
                simulationClient.reloadSources()

                emgView.setupSimulationClientView(simulationClient)
            }
        }
    }

    private fun updateStatus(isConnected: Boolean) {

        var text: String
        if (isConnected) {
            text = "Status: Connected to " + client.name
            val fs = client.samplingFrequency
            text += " | fs = ".plus(if (fs > 0) ("$fs Hz") else "N/A")
        } else {
            text = "Status: Not connected"
        }

        emgView.updateStatus(text)
    }

    // ----------------------------------------------------------------------------------------------------

    // -------------------------------------- EmgViewCallback methods -------------------------------------

    override fun exportData(filename: String, dataStorage: DataStorage) {

        // Only copy to folder if copy to simulation is enabled and the client isn't the simulation client
        // because then the simulationSource is already stored in the directory
        val success = dataStorage.store(filename, client.channelData)
        if (success) {
            tryCopySimulationData(filename)
        }
    }

    override fun setSamplingFrequency(frequency: Double) {
        client.samplingFrequency = frequency
        updateStatus(true)
    }

    override fun closeView(config: Configuration) {
        this.config = config
        saveConfig()
    }

    override fun setSelectedClient(client: EmgClient) {
        this.client = client
    }

    override fun connectToClient() {
        try {

            emgView.reset()

            client.connect()
            client.rawCallbackSubject.subscribe { emgView.onRawClientDataAvailable(it) }
            client.channeledCallbackSubject.subscribe { emgView.onChanneledClientDataAvailable(it, filters) }

            updateStatus(true)
            emgView.setDeviceControlsEnabled(true)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun disconnectFromClient(writeFileOnDisconnectFileName: String?) {
        client.disconnect()
        storeData(writeFileOnDisconnectFileName)

        emgView.setDeviceControlsEnabled(false)
        updateStatus(false)
    }



    // ----------------------------------------------------------------------------------------------------

}
