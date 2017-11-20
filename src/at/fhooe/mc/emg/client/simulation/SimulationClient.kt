package at.fhooe.mc.emg.client.simulation

import at.fhooe.mc.emg.client.ChannelData
import at.fhooe.mc.emg.client.ClientDataCallback
import at.fhooe.mc.emg.client.EmgClient
import at.fhooe.mc.emg.util.AppUtils
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.streams.toList

class SimulationClient(sampleFrequency: Double, maxAmount: Int, var isEndlessLoopEnabled: Boolean) : EmgClient() {

    private val SIMULATION_FOLDER = System.getProperty("user.dir") + "/data/simulation"

    private var millis: Int = 0

    var simulationSource: SimulationSource? = null

    private var t: Timer? = null
    private var thread: Thread? = null
    private var simulationData: List<Double>? = null

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
            millis = (1.0 / sampleFrequency * 1000).toInt()
        }

    init {
        samplingFrequency = sampleFrequency
        channelData = ChannelData(maxAmount)
    }

    @Throws(Exception::class)
    override fun connect(callback: ClientDataCallback) {
        setClientCallback(callback)
        if (simulationSource == null) {
            throw IllegalStateException("Source or listener cannot be null for simulation!")
        }
        disconnect() // Reset connection first
        simulationData = prepareSimulationData()

        // TODO Replace with RxJava
        thread = Thread {

            t = Timer()
            val tt = object : TimerTask() {

                internal var index = 0

                override fun run() {

                    val data = simulationData!![index]
                    callback.onRawDataAvailable(data.toString())

                    channelData.updateXYSeries(0, currentDataPointer.toDouble(), data)
                    callback.onChanneledDataAvailable(channelData)

                    currentDataPointer++
                    index++

                    if (index >= simulationData!!.size) {
                        if (isEndlessLoopEnabled) {
                            index = 0
                        } else {
                            disconnect()
                        }
                    }
                }
            }
            t?.schedule(tt, 0, millis.toLong())
        }
        thread?.start()
    }

    override fun disconnect() {

        synchronized(this) {

            t?.cancel()
            t?.purge()
            t = null

            thread?.join()
            thread = null
        }

    }

    fun addFileAsSimulationSource(srcPath: String) {

        val srcFile = File(srcPath)
        val destFile = File(SIMULATION_FOLDER + "/" + srcFile.name)

        try {

            FileUtils.copyFile(srcFile, destFile)
            val modified = Files.readAllLines(Paths.get(destFile.absolutePath))
                    .filter { s -> !s.isEmpty() && Character.isDigit(s[0]) }
                    .toList().joinToString("\n")
            AppUtils.writeFile(destFile, modified)

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun loadSimulationSources(): List<SimulationSource> {

        val simulationFiles = File(SIMULATION_FOLDER).listFiles()
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
