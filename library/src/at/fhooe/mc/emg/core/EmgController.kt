package at.fhooe.mc.emg.core

import at.fhooe.mc.emg.client.ChannelData
import at.fhooe.mc.emg.client.ClientCategory
import at.fhooe.mc.emg.client.EmgClient
import at.fhooe.mc.emg.client.simulation.SimulationClient
import at.fhooe.mc.emg.filter.*
import at.fhooe.mc.emg.storage.CsvDataStorage
import at.fhooe.mc.emg.storage.DataStorage
import at.fhooe.mc.emg.tools.Tool
import at.fhooe.mc.emg.util.Configuration
import at.fhooe.mc.emg.view.EmgView
import at.fhooe.mc.emg.view.VisualView
import io.reactivex.subjects.PublishSubject
import java.util.*

/**
 * Author:  Martin Macheiner
 * Date:    07.07.2017
 */
abstract class EmgController(val clients: List<EmgClient>, val tools: List<Tool>,
                             val emgView: EmgView) {

    lateinit var client: EmgClient

    lateinit var config: Configuration
    val simulationSourcesSubject: PublishSubject<Int> = PublishSubject.create()

    lateinit var filters: List<Filter>
        private set

    abstract val visualView: VisualView<*>

    val channeledClientCallbackSubject: PublishSubject<ChannelData> = PublishSubject.create<ChannelData>()
    val rawClientCallbackSubject: PublishSubject<String> = PublishSubject.create<String>()

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

        // TODO emgView.setup()
    }

    private fun storeData(writeOnDisconnectFileName: String?) {

        if (config.isWriteToLogEnabled && client.isDataStorageEnabled && writeOnDisconnectFileName != null) {
            exportData(writeOnDisconnectFileName, CsvDataStorage())
        }
    }
    // ----------------------------------------- Abstract methods ------------------------------------------


    // ----------------------------------------------------------------------------------------------------

    // ------------------------------------------ Public methods ------------------------------------------

    fun exportData(filename: String, dataStorage: DataStorage) {

        // Only copy to folder if copy to simulation is enabled and the client isn't the simulation client
        // because then the simulationSource is already stored in the directory
        val success = dataStorage.store(filename, client.channelData)
        if (success) {
            tryCopySimulationData(filename)
        }
    }

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

    fun connect() {
        try {
            client.connect()
            client.rawCallbackSubject.subscribe { rawClientCallbackSubject.onNext(it) }
            client.channeledCallbackSubject.subscribe { channeledClientCallbackSubject.onNext(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun disconnect(writeFileOnDisconnectFileName: String?) {
        client.disconnect()
        storeData(writeFileOnDisconnectFileName)
    }

    fun saveConfig() {
        config.save()
    }

    fun getSingleChannelDataSection(start: Int, stop: Int, channel: Int): ChannelData {
        return client.channelData.getSingleChannelSection(start, stop, channel)
    }

    fun setSamplingFrequency(frequency: Double) {
        client.samplingFrequency = frequency
    }

    fun setSimulationPlaybackLoopEnabled(isEnabled: Boolean) {
        config.isSimulationEndlessLoopEnabled = isEnabled
        // simulationClient.isEndlessLoopEnabled = isEnabled
    }

    private fun tryCopySimulationData(filename: String) {

        if (hasClient(ClientCategory.SIMULATION)
                && config.isCopyToSimulationEnabled
                && client.category !== ClientCategory.SIMULATION) {
            val simulationClient: SimulationClient? = getClient(ClientCategory.SIMULATION) as SimulationClient
            simulationClient?.addFileAsSimulationSource(filename)
            simulationClient?.reloadSources()
            simulationSourcesSubject.onNext(0)
        }
    }

    // ----------------------------------------------------------------------------------------------------

}
