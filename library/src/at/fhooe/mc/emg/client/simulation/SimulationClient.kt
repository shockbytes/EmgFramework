package at.fhooe.mc.emg.client.simulation

import EmgMessaging
import at.fhooe.mc.emg.client.ChannelData
import at.fhooe.mc.emg.client.ClientCategory
import at.fhooe.mc.emg.client.EmgClient
import at.fhooe.mc.emg.util.AppUtils
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.streams.toList

class SimulationClient(sampleFrequency: Double, maxAmount: Int, private val simulationFolder: String) : EmgClient() {

    private var millis: Long = 0

    private var isEndlessLoopEnabled = false

    private var simulationData: List<String>? = null

    private var intervalDisposable: Disposable? = null

    private var simulationIndex: Int = 0

    var simulationSource: SimulationSource? = null

    var simulationSources: List<SimulationSource>
        private set

    override val protocolVersion: EmgMessaging.ProtocolVersion = EmgMessaging.ProtocolVersion.V2

    override val category: ClientCategory = ClientCategory.SIMULATION

    override val name: String
        get() = if (simulationSource == null) shortName else "Simulator /w " + simulationSource?.name

    override val shortName: String
        get() = "Simulator"

    override val isDataStorageEnabled: Boolean
        get() = false

    override var samplingFrequency: Double
        get() = super.samplingFrequency
        set(sampleFrequency) {
            super.samplingFrequency = sampleFrequency
            millis = (1.0 / sampleFrequency * 1000).toLong()
        }

    init {
        samplingFrequency = sampleFrequency
        channelData = ChannelData(maxAmount)
        simulationSources = loadSimulationSources()
    }

    @Throws(Exception::class)
    override fun connect() {

        if (simulationSource == null) {
            throw IllegalStateException("Source cannot be null for simulation!")
        }
        disconnect() // Reset connection first
        simulationData = prepareSimulationData()

        intervalDisposable = Observable.interval(millis, TimeUnit.MILLISECONDS, Schedulers.io())
                .subscribe {

                    val data: String? = simulationData!![simulationIndex]
                    if (data != null) {
                        processMessage(data)
                    }

                    // Check for endless loop playback
                    checkEndlessLoopPlayback()
                }
    }

    override fun disconnect() {

        simulationIndex = 0
        currentDataPointer = 0

        if (intervalDisposable?.isDisposed == false) {
            intervalDisposable?.dispose()
        }
    }

    override fun sendSamplingFrequencyToClient() {
        // Do nothing here, simulator client runs local
    }

    fun addFileAsSimulationSource(srcPath: String) {

        val srcFile = File(srcPath)
        val destinationFile = File(simulationFolder + "/" + srcFile.name)

        try {

            FileUtils.copyFile(srcFile, destinationFile)
            val modified = Files.readAllLines(Paths.get(destinationFile.absolutePath))
                    .filter { s -> !s.isEmpty() && Character.isDigit(s[0]) }
                    .toList().joinToString("\n")
            AppUtils.writeFile(destinationFile, modified)

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun reloadSources() {
        loadSimulationSources()
    }

    private fun loadSimulationSources(): List<SimulationSource> {

        val simulationFiles = File(simulationFolder).listFiles()
        return Arrays.stream(Optional.ofNullable(simulationFiles).orElse(arrayOf()))
                .map { f -> SimulationSource(f.name.substring(0, f.name.lastIndexOf(".")), f.absolutePath) }
                .toList()
    }

    private fun prepareSimulationData(): List<String> {

        return try {
            Files.lines(Paths.get(simulationSource?.filePath)).use { stream -> stream.toList() }
        } catch (e: IOException) {
            e.printStackTrace()
            arrayListOf()
        }
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

}
