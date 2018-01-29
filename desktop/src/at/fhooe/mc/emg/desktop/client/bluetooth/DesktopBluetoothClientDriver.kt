package at.fhooe.mc.emg.desktop.client.bluetooth

import at.fhooe.mc.emg.clientdriver.ClientCategory
import at.fhooe.mc.emg.clientdriver.EmgClientDriver
import at.fhooe.mc.emg.clientdriver.EmgClientDriverConfigView
import at.fhooe.mc.emg.messaging.EmgMessaging
import com.intel.bluetooth.MicroeditionConnector
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.io.*
import javax.bluetooth.RemoteDevice
import javax.microedition.io.StreamConnection

/**
 * @author Martin Macheiner
 * Date: 26.01.2018
 *
 * StreamConnection conn = (StreamConnection) Connector.open("btspp://<mac>:1;authenticate=true;encrypt=tue;master=false;"
 *
 */

class DesktopBluetoothClientDriver(cv: EmgClientDriverConfigView? = null) : EmgClientDriver(cv) {

    override val name: String
        get() = "Bluetooth @ ${remoteDevice?.getFriendlyName(false)}"

    override val shortName = "Bluetooth"

    override val isDataStorageEnabled = true

    override val category = ClientCategory.BLUETOOTH

    override val protocolVersion = EmgMessaging.ProtocolVersion.V1

    // This property can be changed in the ConfigView, therefore it isn't private
    var remoteDeviceMacAddress: String = "D4:63:C6:39:DD:23" // TODO Change default to RP3
    // ----------------------------------

    private var remoteDevice: RemoteDevice? = null
    private var connection: StreamConnection? = null
    private var writer: PrintWriter? = null
    private var reader: BufferedReader? = null
    private var readerDisposable: Disposable? = null

    override fun connect(errorHandler: Consumer<Throwable>) {

        Single.fromCallable {

            try {

                remoteDevice = EmgBluetoothRemoteDevice(normalizedMac())
                connection = MicroeditionConnector.open(buildUrl()) as StreamConnection
                writer = PrintWriter(BufferedWriter(OutputStreamWriter(connection?.openOutputStream())))
                reader = BufferedReader(InputStreamReader(connection?.openInputStream()))
                subscribeToDataTransfer(errorHandler)

            } catch (e: Exception) {
                e.printStackTrace()
                errorHandler.accept(e)
            }

        }.subscribeOn(Schedulers.io()).subscribe()

    }

    override fun disconnect() {
        readerDisposable?.dispose()
        writer?.close()
        reader?.close()
        connection?.close()
    }

    override fun sendSamplingFrequencyToClient() {
        writer?.print(EmgMessaging.buildFrequencyMessage(samplingFrequency))
        writer?.flush()
    }

    private fun buildUrl(): String {

        val scheme = "btspp"
        val params = "authenticate=false;encrypt=false;master=false"
        val channel = "2"
        return "$scheme://${normalizedMac()}:$channel;$params"
    }

    private fun subscribeToDataTransfer(errorHandler: Consumer<Throwable>) {

        readerDisposable = Observable.create { e: ObservableEmitter<String> ->

            if (reader != null) {
                reader?.lines()?.forEach {
                    e.onNext(it)
                }
            } else {
                e.onError(Throwable("Reader is not present!"))
            }
            e.onComplete()
        }.subscribeOn(Schedulers.io()).subscribe({
            processMessage(it)
        }, {errorHandler.accept(it)})

    }

    private fun normalizedMac() = remoteDeviceMacAddress.replace(":", "")

}