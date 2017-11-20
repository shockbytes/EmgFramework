package at.fhooe.mc.emg.core

import at.fhooe.mc.emg.client.ChannelData
import at.fhooe.mc.emg.client.ClientDataCallback
import at.fhooe.mc.emg.client.EmgClient
import at.fhooe.mc.emg.client.serial.SerialClient
import at.fhooe.mc.emg.client.simulation.SimulationClient
import at.fhooe.mc.emg.client.simulation.SimulationSource
import at.fhooe.mc.emg.filter.*
import at.fhooe.mc.emg.storage.CsvDataStorage
import at.fhooe.mc.emg.storage.DataStorage
import at.fhooe.mc.emg.tools.Tool
import at.fhooe.mc.emg.tools.peak.PeakDetectionTool
import at.fhooe.mc.emg.util.Configuration
import at.fhooe.mc.emg.visual.Visual
import java.util.*

/**
 * Author:  Mescht
 * Date:    07.07.2017
 */
// TODO Find a better solution
class EmgController<T>(val visual: Visual<T>) : ClientDataCallback {

    lateinit var client: EmgClient
    private lateinit var serialClient: SerialClient
    private lateinit var simulationClient: SimulationClient

    lateinit var config: Configuration
    var simulationListener: OnSimulationSourcesChangedListener? = null
        private set

    lateinit var tools: List<Tool>
        private set
    lateinit var filters: List<Filter>
        private set
    lateinit var clients: List<EmgClient>
        private set

    lateinit var simulationSources: List<SimulationSource>
        private set
    lateinit var clientCallbacks: MutableList<ClientDataCallback>

    val currentDataPointer: Int
        get() = client.currentDataPointer

    interface OnSimulationSourcesChangedListener {

        fun onSourcesChanged()
    }

    init {
        initialize()
    }

    // ------------------------------------------ Private methods ------------------------------------------

    private fun initialize() {

        // All the listeners will have to subscribe here
        clientCallbacks = ArrayList()

        // Load configuration
        config = Configuration.getInstance()

        initializeClients()

        // Initialize filter
        filters = Arrays.asList(
                NoFilter(),
                BandstopFilter(),
                LowpassFilter(),
                RunningAverageFilter(config.runningAverageWindowSize),
                SavitzkyGolayFilter(config.savitzkyGolayFilterWidth))

        tools = Arrays.asList<Tool>(
                // TODO new ConconiTool(),
                PeakDetectionTool())

    }

    private fun initializeClients() {

        // Initialize serial client
        serialClient = SerialClient(MAX_AMOUNT)

        // Initialize simulation client
        // TODO Do not hard code it and read it from data source
        simulationClient = SimulationClient(100.0, MAX_AMOUNT,
                config.isSimulationEndlessLoopEnabled)
        simulationSources = simulationClient.loadSimulationSources()

        // Serial client as default client
        client = serialClient

        clients = Arrays.asList<EmgClient>(serialClient, simulationClient)
    }

    private fun storeData() {

        // TODO Ask for Filepath
        if (config.isWriteToLogEnabled && client.isDataStorageEnabled) {
            exportData("/data/test.csv", CsvDataStorage())
        }
    }

    // ----------------------------------------------------------------------------------------------------

    // ------------------------------------------ Public methods ------------------------------------------

    fun setOnSimulationSourcesChangedListener(listener: OnSimulationSourcesChangedListener) {
        this.simulationListener = listener
    }

    fun exportData(filename: String, dataStorage: DataStorage) {

        val success = dataStorage.store(filename, client.channelData)
        // Only copy to folder if copy to simulation is enabled and the client isn't the simulation client
        // because then the source is already stored in the directory
        if (success && config.isCopyToSimulationEnabled && client !== simulationClient) {
            simulationClient.addFileAsSimulationSource(filename)
            simulationSources = simulationClient.loadSimulationSources()
            if (simulationListener != null) {
                simulationListener?.onSourcesChanged()
            }
        }
    }

    fun addCallbackListener(callback: ClientDataCallback) {
        if (!clientCallbacks.contains(callback)) {
            clientCallbacks.add(callback)
        }
    }

    fun connect() {
        try {
            client.connect(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun disconnect() {
        client.disconnect()
        storeData()
    }

    fun setSelectedSimulationSource(src: SimulationSource) {
        simulationClient.setSimulationSource(src)
    }

    fun saveConfig() {
        config.save()
    }

    fun getAvailableSerialPorts(forceUpdate: Boolean): List<String> {
        return serialClient.getAvailablePortNames(forceUpdate)
    }

    fun setSerialPortSelected(port: String) {
        serialClient.setPortName(port)
    }

    fun getSingleChannelDataSection(start: Int, stop: Int, channel: Int): ChannelData {
        return client.channelData.getSingleChannelSection(start, stop, channel)
    }

    fun setSamplingFrequency(frequency: Double) {
        client.samplingFrequency = frequency
    }

    fun setSimulationPlaybackLoopEnabled(isEnabled: Boolean) {
        config.isSimulationEndlessLoopEnabled = isEnabled
        simulationClient.setEndlessLoopPlayback(isEnabled)
    }

    // ----------------------------------------------------------------------------------------------------

    override fun onChanneledDataAvailable(channelData: ChannelData) {
        clientCallbacks.forEach { c -> c.onChanneledDataAvailable(channelData) }
    }

    override fun onRawDataAvailable(line: String) {
        clientCallbacks.forEach { c -> c.onRawDataAvailable(line) }
    }

    companion object {

        private val MAX_AMOUNT = 512
    }
}
