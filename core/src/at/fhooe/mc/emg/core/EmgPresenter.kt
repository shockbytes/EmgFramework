package at.fhooe.mc.emg.core

import at.fhooe.mc.emg.clientdriver.ClientCategory
import at.fhooe.mc.emg.clientdriver.EmgClientDriver
import at.fhooe.mc.emg.clientdriver.model.EmgData
import at.fhooe.mc.emg.core.analysis.FrequencyAnalysisMethod
import at.fhooe.mc.emg.core.client.simulation.SimulationClientDriver
import at.fhooe.mc.emg.core.filter.*
import at.fhooe.mc.emg.core.storage.CsvDataStorage
import at.fhooe.mc.emg.core.storage.DataStorage
import at.fhooe.mc.emg.core.tools.Tool
import at.fhooe.mc.emg.core.util.config.EmgConfig
import at.fhooe.mc.emg.core.util.config.EmgConfigStorage
import at.fhooe.mc.emg.core.view.EmgView
import at.fhooe.mc.emg.core.view.EmgViewCallback
import at.fhooe.mc.emg.core.view.VisualView
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

/**
 * Author:  Martin Macheiner
 * Date:    07.07.2017
 */
abstract class EmgPresenter(private val clients: List<EmgClientDriver>, private val tools: List<Tool>,
                            open var emgView: EmgView?, private val configStorage: EmgConfigStorage) : EmgViewCallback {

    abstract val visualView: VisualView<*>

    val currentDataPointer: Int
        get() = client.currentDataPointer

    private val rawCallbackSubject: PublishSubject<String> = PublishSubject.create()

    private val connectionErrorHandler: Consumer<Throwable> = Consumer {
        disconnectFromClient()
        emgView?.showConnectionError(it)
    }

    private var rawDisposable: Disposable? = null
    private var channelDisposable: Disposable? = null

    private var isVisualEnabled: Boolean = true

    private lateinit var client: EmgClientDriver

    private lateinit var config: EmgConfig

    private lateinit var filters: List<Filter>

    init {
        initialize()
    }

    // ------------------------------------------ Private methods ------------------------------------------

    private fun initialize() {

        // Load configuration
        config = configStorage.load()

        // Set default client
        client = clients[clients.size / 2]

        // Initialize filter
        filters = listOf(
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
        emgView?.exposeRawClientDataObservable(rawCallbackSubject)
    }

    private fun storeData(writeOnDisconnectFileName: String?) {
        if (config.isWriteToLogEnabled && client.isDataStorageEnabled && writeOnDisconnectFileName != null) {
            exportData(writeOnDisconnectFileName, CsvDataStorage())
        }
    }

    private fun getClient(category: ClientCategory): EmgClientDriver? {
        clients.forEach {
            if (it.category === category) {
                return it
            }
        }
        return null
    }

    private fun hasClient(category: ClientCategory): Boolean {
        clients.forEach {
            if (it.category === category) {
                return true
            }
        }
        return false
    }

    /**
     * Only copy to folder if copy to simulation is enabled and the client isn't the simulation client
     * because then the simulationSource is already stored in the directory
     */
    private fun tryCopySimulationData(filename: String, fsOfRecording: Double) {

        if (hasClient(ClientCategory.SIMULATION)
                && config.isCopyToSimulationEnabled
                && client.category !== ClientCategory.SIMULATION
                && client.isDataStorageEnabled) {
            val simulationClient: SimulationClientDriver? = getClient(ClientCategory.SIMULATION) as? SimulationClientDriver
            if (simulationClient != null) {
                simulationClient.addFileAsSimulationSource(filename, fsOfRecording)
                simulationClient.reloadSources()
            }
        }
    }

    private fun updateStatus(isConnected: Boolean) {

        var text: String
        if (isConnected) {
            text = client.name
            val fs = client.samplingFrequency
            text += " | fs = ".plus(if (fs > 0) ("$fs Hz") else "N/A")
        } else {
            text = "Not connected"
        }

        emgView?.updateStatus(text)
    }

    // ------------------------------------------ Public methods ------------------------------------------

    fun start() {
        setupEmgView()
    }

    fun getSingleChannelDataSection(start: Int, stop: Int, channel: Int): EmgData {
        return client.data.section(start, stop, channel)
    }

    // ----------------------------------------------------------------------------------------------------

    // -------------------------------------- EmgViewCallback methods -------------------------------------

    override fun exportData(filename: String, dataStorage: DataStorage) {
        val success = dataStorage.store(filename, client.data)
        if (success) {
            tryCopySimulationData(filename, client.samplingFrequency)
        }
    }

    override fun setSamplingFrequency(frequency: Double) {
        client.samplingFrequency = frequency
        updateStatus(true)
    }

    override fun closeView(config: EmgConfig) {
        this.config = config
        configStorage.store(config)
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

            client.clearData() // Clear data storage, before new data is added
            client.connect(Action {

                rawDisposable = client.rawCallbackSubject.subscribe { rawCallbackSubject.onNext(it) }

                if (isVisualEnabled) {
                    var channelCallback = client.channeledCallbackSubject.subscribeOn(Schedulers.io())
                    if (visualView.requestScheduler) {
                        channelCallback = channelCallback.observeOn(visualView.scheduler)
                        channelDisposable = if (visualView.requestBufferedUpdates) {
                            channelCallback
                                    .buffer(visualView.bufferSpan, TimeUnit.MILLISECONDS, visualView.scheduler)
                                    .subscribe { it.forEach { visualView.update(it, filters) } }
                        } else {
                            channelCallback.subscribe { visualView.update(it, filters) }
                        }
                    } else {
                        channelDisposable = channelCallback.subscribe { visualView.update(it, filters) }
                    }
                }

                updateStatus(true)
                emgView?.lockDeviceControls(true)

            }, connectionErrorHandler)

        } catch (e: Exception) {
            connectionErrorHandler.accept(e)
        }
    }

    override fun disconnectFromClient(writeFileOnDisconnectFileName: String?) {
        client.disconnect()
        storeData(writeFileOnDisconnectFileName)

        rawDisposable?.dispose()
        channelDisposable?.dispose()

        emgView?.lockDeviceControls(false)
        updateStatus(false)
    }

    override fun requestFrequencyAnalysisView(method: FrequencyAnalysisMethod.Method) {
        emgView?.showFrequencyAnalysisView(FrequencyAnalysisMethod(method, visualView.dataForFrequencyAnalysis,
                client.samplingFrequency))
    }

    override fun setVisualViewEnabled(visualEnabled: Boolean) {
        this.isVisualEnabled = visualEnabled
    }

    override fun isDataStorageEnabled() = client.isDataStorageEnabled

    // ----------------------------------------------------------------------------------------------------

}
