package at.fhooe.mc.emg.client.serial

import at.fhooe.mc.emg.client.ChannelData
import at.fhooe.mc.emg.client.ClientDataCallback
import at.fhooe.mc.emg.client.EmgClient
import gnu.io.*
import java.io.*
import java.util.*
import java.util.stream.IntStream
import kotlin.streams.toList


class SerialClient(maxAmount: Int) : EmgClient(), SerialPortEventListener {

    private var ports: List<CommPortIdentifier>? = null

    private var inputReader: BufferedReader? = null
    private var outputWriter: BufferedWriter? = null
    private var connectionPort: SerialPort? = null

    private var portName: String? = null

    override var currentDataPointer: Int = 0
        private set

    override val name: String
        get() = if (portName == null) shortName else "Serial device @ " + portName!!

    override val shortName: String
        get() = "Serial device"

    override val isDataStorageEnabled: Boolean
        get() = true

    override var samplingFrequency: Double
        get() = super.samplingFrequency
        set(fs) {
            super.samplingFrequency = fs
            sendSamplingFrequencyToDevice()
        }

    init {
        initializePorts()
        channelData = ChannelData(maxAmount)
        samplingFrequency = 100.toDouble()
    }

    private fun initializePorts() {
        ports = Collections.list<CommPortIdentifier>(CommPortIdentifier.getPortIdentifiers() as? Enumeration<CommPortIdentifier>?)
    }

    fun getAvailablePortNames(forceUpdate: Boolean): List<String> {

        if (forceUpdate) {
            initializePorts()
        }
        if (ports != null) {
            return ports!!.stream().map<String>({ it.name }).toList()
        }
        return arrayListOf()
    }

    @Throws(NoSuchPortException::class)
    private fun getPortByName(name: String?): CommPortIdentifier {
        return CommPortIdentifier.getPortIdentifier(name)
    }

    @Throws(Exception::class)
    override fun connect(callback: ClientDataCallback) {
        setClientCallback(callback)

        connectionPort = getPortByName(portName).open(javaClass.name, TIMEOUT) as SerialPort
        dataRate = DEFAULT_DATA_RATE
        setupConnectionParams()

        inputReader = BufferedReader(InputStreamReader(connectionPort?.inputStream))
        outputWriter = BufferedWriter(OutputStreamWriter(connectionPort?.outputStream))
        connectionPort?.addEventListener(this)
        connectionPort?.notifyOnDataAvailable(true)
    }

    @Throws(UnsupportedCommOperationException::class)
    private fun setupConnectionParams() {
        connectionPort?.setSerialPortParams(dataRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE)
    }

    override fun disconnect() {

        synchronized(this) {

            connectionPort?.removeEventListener()
            connectionPort?.close()

            inputReader?.close()
            outputWriter?.close()

            connectionPort = null
        }
    }

    override fun serialEvent(event: SerialPortEvent) {

        if (event.eventType == SerialPortEvent.DATA_AVAILABLE) {

            try {

                val inputLine = inputReader?.readLine()
                if (inputLine != null && !inputLine.isEmpty()) {
                    if (processLine(inputLine)) {
                        callback?.onRawDataAvailable(inputLine)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    fun setPortName(portName: String) {
        this.portName = portName
    }

    private fun processLine(inputLine: String): Boolean {

        // Always increment x counter value
        currentDataPointer++

        // Okay, there are more than 1 channel
        if (inputLine.contains(",")) {

            val values = Arrays
                    .stream(inputLine.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
                    .map<Float> { s -> if (s.trim { it <= ' ' }.isEmpty()) 0.toFloat() else java.lang.Float.parseFloat(s.trim { it <= ' ' }) }.toList()

            if (values.size <= 1) {
                return false // Do not process damaged packages
            }

            IntStream.range(0, values.size).forEach { idx -> channelData?.updateXYSeries(idx, currentDataPointer.toDouble(), values[idx].toDouble()) }

        } else {

            val value = if (Character.isDigit(inputLine[0])) java.lang.Float.parseFloat(inputLine) else java.lang.Float.MIN_VALUE
            if (value != java.lang.Float.MIN_VALUE) {
                channelData?.updateXYSeries(0, currentDataPointer.toDouble(), value.toDouble())
            }
        }

        callback?.onChanneledDataAvailable(channelData)
        return true
    }

    private fun sendSamplingFrequencyToDevice() {
        try {
            val millis = (1.0 / samplingFrequency * 1000).toInt()
            val command = "delay=" + millis + "\r\n"
            outputWriter?.write(command)
            outputWriter?.flush()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    companion object {

        private val TIMEOUT = 5000
        val DEFAULT_DATA_RATE = 115200

        val SUPPORTED_DATA_RATES = intArrayOf(4800, 9600, 19200, 57600, 115200, 230400)
    }
}
