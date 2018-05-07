package at.fhooe.mc.emg.core

import at.fhooe.mc.emg.clientdriver.ClientCategory
import at.fhooe.mc.emg.clientdriver.EmgClientDriver
import at.fhooe.mc.emg.clientdriver.model.EmgData
import at.fhooe.mc.emg.core.analysis.FrequencyAnalysisMethod
import at.fhooe.mc.emg.core.client.simulation.SimulationClientDriver
import at.fhooe.mc.emg.core.filter.Filter
import at.fhooe.mc.emg.core.storage.CsvEmgDataStorage
import at.fhooe.mc.emg.core.storage.EmgDataStorage
import at.fhooe.mc.emg.core.storage.config.EmgConfigStorage
import at.fhooe.mc.emg.core.tool.Tool
import at.fhooe.mc.emg.core.util.EmgConfig
import at.fhooe.mc.emg.core.view.EmgView
import at.fhooe.mc.emg.core.view.EmgViewCallback
import at.fhooe.mc.emg.core.view.VisualView
import at.fhooe.mc.emg.designer.component.EmgBaseComponent
import at.fhooe.mc.emg.designer.component.pipe.EmgComponentPipe
import at.fhooe.mc.emg.messaging.MessageInterpreter
import io.reactivex.Observable
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
                            private val filter: List<Filter>,
                            private val frequencyAnalysisMethods: List<FrequencyAnalysisMethod>,
                            protected val designerComponents: Pair<List<EmgBaseComponent>, List<EmgComponentPipe<Any, Any>>>,
                            private val configStorage: EmgConfigStorage,
                            open var emgView: EmgView?) : EmgViewCallback, Toolable {

    abstract val visualView: VisualView<*>

    override val currentDataPointer: Int
        get() = client.currentDataPointer

    override val dataForFrequencyAnalysis: DoubleArray
        get() = visualView.dataForFrequencyAnalysis

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
        client = clients[clients.size - 1]
    }

    // ------------------------------------------ Private methods ------------------------------------------

    private fun setupEmgView() {
        emgView?.setupView(this, config)
        emgView?.setupFilterViews(filter)
        emgView?.setupEmgClientDriverView(clients, client)
        emgView?.setupToolsView(tools, this)
        emgView?.setupEmgClientDriverConfigViews(clients)
        emgView?.setupFrequencyAnalysisMethods(frequencyAnalysisMethods)
        emgView?.exposeRawClientDataObservable(rawCallbackSubject)
    }

    private fun storeData(writeOnDisconnectFileName: String?) {
        if (config.isWriteToLogEnabled && client.isDataStorageEnabled && writeOnDisconnectFileName != null) {
            exportData(writeOnDisconnectFileName, CsvEmgDataStorage())
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

    // ----------------------------------------------------------------------------------------------------

    // -------------------------------- EmgViewCallback & Toolable methods --------------------------------

    override fun exportData(filename: String, dataStorage: EmgDataStorage) {
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

    override fun getSingleChannelDataSection(start: Int, stop: Int, channel: Int): EmgData {
        return client.data.section(start, stop, channel)
    }

    override fun registerToolForUpdates(): Observable<EmgData> {
        return client.channeledCallbackSubject.subscribeOn(Schedulers.io())
    }

    override fun connectToClient(successHandler: Action?) {

        emgView?.reset()
        // Clear data storage, before new data is added
        client.clearData()

        // Set the selected filter to the visual view
        visualView.filter = filter.filter { it.isEnabled }

        client.connect(Action {

            rawDisposable = client.rawCallbackSubject.subscribe { rawCallbackSubject.onNext(it) }

            if (isVisualEnabled) {
                var channelCallback = client.channeledCallbackSubject.subscribeOn(Schedulers.io())
                if (visualView.requestScheduler) {
                    channelCallback = channelCallback.observeOn(visualView.scheduler)
                    channelDisposable = if (visualView.requestBufferedUpdates) {
                        channelCallback
                                .buffer(visualView.bufferSpan, TimeUnit.MILLISECONDS, visualView.scheduler)
                                .subscribe { notifyCachedChanneledCallbacks(it) }
                    } else {
                        channelCallback.subscribe { notifyChanneledCallbacks(it) }
                    }
                } else {
                    channelDisposable = channelCallback.subscribe { notifyChanneledCallbacks(it) }
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
        filter.forEach { it.reset() }

        rawDisposable?.dispose()
        channelDisposable?.dispose()

        emgView?.lockDeviceControls(false)
        updateStatus(false)
    }

    override fun requestFrequencyAnalysisView(method: FrequencyAnalysisMethod) {
        method.fs = client.samplingFrequency
        emgView?.showFrequencyAnalysisView(method, visualView.dataForFrequencyAnalysis)
    }

    override fun setVisualViewEnabled(visualEnabled: Boolean) {
        this.isVisualEnabled = visualEnabled
    }

    override fun isDataStorageEnabled() = client.isDataStorageEnabled

    override fun isHeartRateSensingSupported(): Boolean {
        return clients.any { it.msgInterpreter.protocolVersion == MessageInterpreter.ProtocolVersion.V3 }
    }

    // ----------------------------------------------------------------------------------------------------

    private fun notifyCachedChanneledCallbacks(data: List<EmgData>) {
        data.forEach {
            // Update visual view
            visualView.update(it)
        }
    }

    private fun notifyChanneledCallbacks(data: EmgData) {
        // Update visual view
        visualView.update(data)
    }

}
