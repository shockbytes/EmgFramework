package at.fhooe.mc.emg.core.client.simulation

import at.fhooe.mc.emg.clientdriver.ClientCategory
import at.fhooe.mc.emg.clientdriver.EmgClientDriver
import at.fhooe.mc.emg.clientdriver.EmgClientDriverConfigView
import at.fhooe.mc.emg.core.util.CoreUtils
import at.fhooe.mc.emg.messaging.EmgMessaging
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

class SimulationClientDriver(cv: EmgClientDriverConfigView? = null,
                             private val simulationFolder: String) : EmgClientDriver(cv) {

    private var millis: Long = 0

    private var simulationData: List<String>? = null

    private var intervalDisposable: Disposable? = null

    private var simulationIndex: Int = 0

    var simulationSource: SimulationSource? = null

    var simulationSources: List<SimulationSource>
        private set

    var isEndlessLoopEnabled = false

    override val protocolVersion: EmgMessaging.ProtocolVersion = EmgMessaging.ProtocolVersion.V2

    override val category: ClientCategory = ClientCategory.SIMULATION

    override val name: String
        get() = if (simulationSource == null) shortName else "Simulator /w " + simulationSource?.name

    override val shortName = "Simulator"

    override val isDataStorageEnabled = false

    override var samplingFrequency: Double
        get() = super.samplingFrequency
        set(sampleFrequency) {
            super.samplingFrequency = sampleFrequency
            millis = (1.0 / sampleFrequency * 1000).toLong()
        }

    init {
        this.samplingFrequency = samplingFrequency // This call is necessary to trigger the set of the var millis
        simulationSources = loadSimulationSources()

        // Select the default simulation source
        if (simulationSources.isNotEmpty()) {
            simulationSource = simulationSources[simulationSources.size / 2 + 1]
        }
    }

    @Throws(Exception::class)
    override fun connect(successHandler: Action, errorHandler: Consumer<Throwable>) {

        if (simulationSource == null) {
            throw IllegalStateException("Simulation source cannot be null!")
        }
        disconnect() // Reset connection first

        prepareSimulationData()
        prepareSamplingFrequency()

        intervalDisposable = Observable.interval(millis, TimeUnit.MILLISECONDS, Schedulers.io())
                .subscribe(Consumer {

                    val data: String? = simulationData?.get(simulationIndex)
                    if (data != null) {
                        processMessage(data)
                    }
                    // Check for endless loop playback
                    checkEndlessLoopPlayback()
                }, errorHandler)

        // Everything set up
        successHandler.run()
    }

    override fun disconnect() {

        simulationIndex = 0
        intervalDisposable?.dispose()
    }

    override fun sendSamplingFrequencyToClient() {
        // Do nothing here, simulator client runs local
    }

    fun addFileAsSimulationSource(srcPath: String, fsOfRecording: Double) {

        val srcFile = File(srcPath)
        val srcPathExt = FilenameUtils.getExtension(srcPath)
        val modSrcPath = FilenameUtils.removeExtension(srcPath)
        val destinationFile = File("$simulationFolder/${modSrcPath}_$fsOfRecording.$srcPathExt")

        try {

            val modified = FileUtils.readLines(srcFile, Charset.forName("UTF-8"))
                    .filter { s -> !s.isEmpty() && Character.isDigit(s[0]) }
                    .joinToString("\n") { line -> line.replace(',', ':') }
            CoreUtils.writeFile(destinationFile, modified)

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun reloadSources() {
        loadSimulationSources()
    }

    private fun loadSimulationSources(): List<SimulationSource> {

        val simulationFiles = File(simulationFolder).listFiles()
        return simulationFiles
                .map { f ->
                    val shortName = f.name.substring(0, f.name.lastIndexOf("_"))
                    val fsStr = f.name.substring(f.name.lastIndexOf("_") + 1, f.name.lastIndexOf("."))
                    // Fallback to 100 Hz if parsing is not possible
                    val fs = if (fsStr.toDoubleOrNull() != null) fsStr.toDouble() else DEFAULT_FS
                    SimulationSource(shortName, f.absolutePath, fs)
                }
    }

    private fun prepareSimulationData() {

        simulationData = try {
            FileUtils.readLines(File(simulationSource?.filePath), Charset.forName("UTF-8"))
                    .filter { it.isNotEmpty() }
        } catch (e: IOException) {
            e.printStackTrace()
            arrayListOf()
        }
    }

    private fun prepareSamplingFrequency() {
        samplingFrequency = simulationSource?.fs ?: 100.0
    }

    private fun checkEndlessLoopPlayback() {
        simulationIndex++
        if (simulationIndex >= simulationData!!.size) {
            if (isEndlessLoopEnabled) {
                simulationIndex = 0
            } else {
                disconnect()
            }
        }
    }

    companion object {

        const val DEFAULT_FS = 100.0
    }

}
