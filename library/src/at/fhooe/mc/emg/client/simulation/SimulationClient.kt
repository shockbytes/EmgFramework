package at.fhooe.mc.emg.client.simulation

import at.fhooe.mc.emg.client.ChannelData
import at.fhooe.mc.emg.client.ClientCategory
import at.fhooe.mc.emg.client.ClientDataCallback
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

    override val category: ClientCategory = ClientCategory.SIMULATION

    private var millis: Long = 0

    private var isEndlessLoopEnabled = false

    var simulationSource: SimulationSource? = null

    private var simulationData: List<Double>? = null

    private var intervalDisposable: Disposable? = null

    var simulationSources: List<SimulationSource>
        private set

    override var currentDataPointer: Int = 0
        private set

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
    override fun connect(callback: ClientDataCallback) {
        this.callback = callback

        if (simulationSource == null) {
            throw IllegalStateException("Source or listener cannot be null for simulation!")
        }
        disconnect() // Reset connection first
        simulationData = prepareSimulationData()

        var index = 0
        intervalDisposable = Observable.interval(millis, TimeUnit.MILLISECONDS, Schedulers.io())
                .subscribe {

                    val data = simulationData!![index]
                    callback.onRawDataAvailable(data.toString())

                    channelData.updateXYSeries(0, currentDataPointer.toDouble(), data)
                    callback.onChanneledDataAvailable(channelData)

                    currentDataPointer++; index++

                    if (index >= simulationData!!.size) {
                        if (isEndlessLoopEnabled) {
                            index = 0
                        } else {
                            disconnect()
                        }
                    }
                }
    }

    override fun disconnect() {

        if (intervalDisposable?.isDisposed == false) {
            intervalDisposable?.dispose()
        }
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

    private fun prepareSimulationData(): List<Double> {

        var list: List<Double> = ArrayList()
        try {
            Files.lines(Paths.get(simulationSource?.filePath)).use { stream ->

                list = stream
                        .map<Double> { s ->
                            val number = s.substring(s.indexOf(",") + 1, s.length)
                            if (number.isEmpty()) 0.toDouble() else java.lang.Double.parseDouble(number)
                        }
                        .toList()

            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return list
    }

}
