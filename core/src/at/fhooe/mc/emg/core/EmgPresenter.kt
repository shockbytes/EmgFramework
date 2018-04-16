package at.fhooe.mc.emg.core

import at.fhooe.mc.emg.clientdriver.ClientCategory
import at.fhooe.mc.emg.clientdriver.EmgClientDriver
import at.fhooe.mc.emg.clientdriver.model.EmgData
import at.fhooe.mc.emg.core.analysis.FrequencyAnalysisMethod
import at.fhooe.mc.emg.core.client.simulation.SimulationClientDriver
import at.fhooe.mc.emg.core.filter.Filter
import at.fhooe.mc.emg.core.storage.CsvDataStorage
import at.fhooe.mc.emg.core.storage.DataStorage
import at.fhooe.mc.emg.core.storage.config.EmgConfigStorage
import at.fhooe.mc.emg.core.tools.Tool
import at.fhooe.mc.emg.core.util.EmgConfig
import at.fhooe.mc.emg.core.view.EmgView
import at.fhooe.mc.emg.core.view.EmgViewCallback
import at.fhooe.mc.emg.core.view.VisualView
import at.fhooe.mc.emg.messaging.MessageParser
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
abstract class EmgPresenter(private val clients: List<EmgClientDriver>,
                            private val tools: List<Tool>,
                            private val filters: List<Filter>,
                            private val configStorage: EmgConfigStorage,
                            open var emgView: EmgView?) : EmgViewCallback {

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

    private var client: EmgClientDriver
    private var config: EmgConfig

    init {
        // Load configuration
        config = configStorage.emgConfig

        // Set default client
        client = clients[clients.size / 2]
    }

    // ------------------------------------------ Private methods ------------------------------------------

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
        return clients.find { it.category == category }
    }

    private fun hasClient(category: ClientCategory): Boolean {
        return clients.find { (it.category === category) } != null
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

    override fun connectToClient(successHandler: Action?) {

        emgView?.reset()

        client.clearData() // Clear data storage, before new data is added
        client.connect(Action {

            rawDisposable = client.rawCallbackSubject.subscribe { rawCallbackSubject.onNext(it) }

            // TODO Refactor this!!!
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

            // Tell the caller that the connection is established
            successHandler?.run()

        }, connectionErrorHandler)
    }

    override fun disconnectFromClient(writeFileOnDisconnectFileName: String?) {
        client.disconnect()
        storeData(writeFileOnDisconnectFileName)
        filters.forEach { it.reset() }

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

    override fun isHeartRateSensingSupported(): Boolean {
        return clients.any { it.msgParser.protocolVersion == MessageParser.ProtocolVersion.V3 }
    }

    // ----------------------------------------------------------------------------------------------------

}
