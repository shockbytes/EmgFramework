package at.fhooe.mc.emg.core

import at.fhooe.mc.emg.client.ChannelData
import at.fhooe.mc.emg.client.ClientCategory
import at.fhooe.mc.emg.client.EmgClientDriver
import at.fhooe.mc.emg.client.simulation.SimulationClientDriver
import at.fhooe.mc.emg.filter.*
import at.fhooe.mc.emg.storage.CsvDataStorage
import at.fhooe.mc.emg.storage.DataStorage
import at.fhooe.mc.emg.tools.Tool
import at.fhooe.mc.emg.util.Configuration
import at.fhooe.mc.emg.util.FrequencyAnalysis
import at.fhooe.mc.emg.view.EmgView
import at.fhooe.mc.emg.view.EmgViewCallback
import at.fhooe.mc.emg.view.VisualView
import java.util.*

/**
 * Author:  Martin Macheiner
 * Date:    07.07.2017
 */
abstract class EmgController(private val clients: List<EmgClientDriver>, private val tools: List<Tool>,
                             open var emgView: EmgView?) : EmgViewCallback {

    private lateinit var client: EmgClientDriver

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
    }

    private fun setupEmgView() {
        emgView?.setupView(this, config)
        emgView?.setupFilterViews(filters)
        emgView?.setupEmgClientDriverView(clients, client)
        emgView?.setupToolsView(tools, this)
        emgView?.setupEmgClientDriverConfigViews(clients)
    }

    private fun storeData(writeOnDisconnectFileName: String?) {
        if (config.isWriteToLogEnabled && client.isDataStorageEnabled && writeOnDisconnectFileName != null) {
            exportData(writeOnDisconnectFileName, CsvDataStorage())
        }
    }

    // ------------------------------------------ Public methods ------------------------------------------

    fun start() {
        setupEmgView()
    }

    fun getClient(category: ClientCategory): EmgClientDriver? {
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

    fun getSingleChannelDataSection(start: Int, stop: Int, channel: Int): ChannelData {
        return client.channelData.getSingleChannelSection(start, stop, channel)
    }

    private fun tryCopySimulationData(filename: String) {

        if (hasClient(ClientCategory.SIMULATION)
                && config.isCopyToSimulationEnabled
                && client.category !== ClientCategory.SIMULATION) {
            val simulationClient: SimulationClientDriver? = getClient(ClientCategory.SIMULATION) as? SimulationClientDriver
            if (simulationClient != null) {
                simulationClient.addFileAsSimulationSource(filename)
                simulationClient.reloadSources()
            }
        }
    }

    private fun saveConfig() {
        config.save()
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

        emgView?.updateStatus(text)
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

    override fun setSelectedClient(client: EmgClientDriver) {
        this.client = client
    }

    override fun setSimulationPlaybackLoopEnabled(isEnabled: Boolean) {
        config.isSimulationEndlessLoopEnabled = isEnabled
        if (hasClient(ClientCategory.SIMULATION)) {
            val simClient = getClient(ClientCategory.SIMULATION) as SimulationClientDriver
            simClient.isEndlessLoopEnabled = isEnabled
        }
    }

    override fun connectToClient() {
        try {

            emgView?.reset()

            client.connect()
            client.rawCallbackSubject.subscribe { emgView?.onRawClientDataAvailable(it) }
            client.channeledCallbackSubject.subscribe { emgView?.onChanneledClientDataAvailable(it, filters) }

            updateStatus(true)
            emgView?.setDeviceControlsEnabled(true)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun disconnectFromClient(writeFileOnDisconnectFileName: String?) {
        client.disconnect()
        storeData(writeFileOnDisconnectFileName)

        emgView?.setDeviceControlsEnabled(false)
        updateStatus(false)
    }

    override fun requestFrequencyAnalysis(type: FrequencyAnalysis.AnalysisType) {
        emgView?.showFrequencyAnalysis(type, client.samplingFrequency)
    }


    // ----------------------------------------------------------------------------------------------------

}
