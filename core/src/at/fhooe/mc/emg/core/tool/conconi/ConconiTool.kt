package at.fhooe.mc.emg.core.tool.conconi

import at.fhooe.mc.emg.clientdriver.model.EmgData
import at.fhooe.mc.emg.core.Toolable
import at.fhooe.mc.emg.core.storage.CsvEmgDataStorage
import at.fhooe.mc.emg.core.storage.FileStorage
import at.fhooe.mc.emg.core.tool.Tool
import at.fhooe.mc.emg.core.tool.peaks.PeakDetector
import at.fhooe.mc.emg.core.util.CoreUtils
import at.fhooe.mc.emg.core.util.rms
import at.fhooe.mc.emg.designer.EmgComponentType
import at.fhooe.mc.emg.designer.annotation.EmgComponent
import at.fhooe.mc.emg.designer.annotation.EmgComponentInputPort
import at.fhooe.mc.emg.designer.annotation.EmgComponentStartablePoint
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import org.apache.commons.io.FilenameUtils
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Author:  Martin Macheiner
 * Date:    04.07.2017
 *
 * TODO Refactor for ACD usage
 *
 */
@EmgComponent(type = EmgComponentType.TOOL, displayTitle = "Conconi Test")
class ConconiTool(override var toolView: ConconiToolView? = null,
                  private var fileStorage: FileStorage? = null) : Tool, ConconiToolViewCallback {

    override val name = "Conconi Test"

    private var data: ConconiData = ConconiData()

    private var dataDisposable: Disposable? = null
    private var timerDisposable: Disposable? = null
    private var toolable: Toolable? = null

    private var dataStartPointer: Int = 0
    private var dataStopPointer: Int = 0

    override fun start(toolable: Toolable?, showViewImmediate: Boolean) {
        this.toolable = toolable
        toolView?.setup(this, showViewImmediate)

        dataDisposable = toolable?.registerToolForUpdates()?.subscribe { update(it) }

        data = ConconiData()
        dataStartPointer = 0
        dataStopPointer = 0
    }

    @EmgComponentStartablePoint("toolView", ConconiToolView::class)
    override fun externalStart() {
        start(null, true)
    }

    override fun onStartClicked() {
        toolView?.onPlayCountdownSound()
        startCountdown() // Start the countdown and then start the actual test
    }

    override fun onStopClicked() {
        timerDisposable?.dispose()
        toolable?.disconnectFromClient(null)
    }

    override fun onSaveClicked(filename: String?, errorHandler: Consumer<Throwable>) {

        // Remove extension, because 2 different types are necessary
        val nameWoExt = FilenameUtils.removeExtension(filename)
        if (nameWoExt != null) {

            val fileNameConconi = "$nameWoExt.ctf"
            val fileNameRaw = "$nameWoExt.csv"

            // No action required if everything works fine
            fileStorage?.storeFileAsObject(data, fileNameConconi)?.subscribe(Action {}, errorHandler)
            toolable?.exportData(fileNameRaw, CsvEmgDataStorage())
        } else {
            errorHandler.accept(NullPointerException("Filename must not be null!"))
        }
    }

    override fun onLoadClicked(filename: String?, errorHandler: Consumer<Throwable>) {

        if (filename != null) {
            fileStorage?.loadFromFileAsObject<ConconiData>(filename)?.subscribe(Consumer {

                if (it == null) {
                    throw IOException("Cannot read data from $filename")
                }

                data = it
                (0 until data.roundCount).forEachIndexed { idx, _ ->
                    toolView?.onRoundDataAvailable(emg2ConconiRoundData(data.getRoundData(idx), idx), idx)
                }

            }, errorHandler)
        } else {
            errorHandler.accept(NullPointerException("Filename must not be null!"))
        }
    }

    /**
     * This method is only used on platforms where the fileStorage instance does not have access to the whole
     * file system (mainly mobile applications)
     */
    override fun requestStoredConconiFiles(directory: String, concatToBase: Boolean): Single<List<String>?> {
        return fileStorage?.listFiles(directory, concatToBase, ".ctf")
                ?: Single.error(Throwable("FileStorage not present!"))
    }

    override fun onViewClosed() {
        timerDisposable?.dispose()
        dataDisposable?.dispose()
    }

    // --------------------------------------------------------------------------------

    @EmgComponentInputPort(EmgData::class)
    fun update(emgData: EmgData) {

    }

    // --------------------------------------------------------------------------------

    private fun startCountdown() {

        var disposable: Disposable? = null
        var counter = 6
        disposable = Observable.interval(1, TimeUnit.SECONDS).subscribe {

            counter--
            toolView?.onCountdownTick(counter)
            if (counter == 0) {
                connectAndStart()
                disposable?.dispose()
            }
        }
    }

    private fun connectAndStart() {
        toolable?.connectToClient(Action { startTimer() })
    }

    private fun startTimer() {
        // Start timer for Conconi test
        var tick = 0
        var roundIdx = 0
        timerDisposable = Observable.interval(1000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.computation())
                .subscribe {

                    tick++
                    toolView?.onTick(tick, times[roundIdx])

                    // Check if a new round starts in 5 seconds, and play a sound if it starts
                    if (tick + 5 == times[roundIdx]) {
                        toolView?.onPlayCountdownSound()
                    }
                    // Store round data after a round has finished
                    if (tick == times[roundIdx]) {
                        storeRoundData(roundIdx)
                        tick = 0
                        roundIdx++
                    }
                }

    }

    private fun storeRoundData(index: Int) {

        toolable?.let {t ->

            dataStopPointer = t.currentDataPointer
            val roundData = t.getSingleChannelDataSection(dataStartPointer, dataStopPointer, 0)

            data.addRoundData(roundData)
            val crd = emg2ConconiRoundData(roundData, index)
            toolView?.onRoundDataAvailable(crd, index)
        }

        dataStartPointer = dataStopPointer
    }

    private fun emg2ConconiRoundData(roundData: EmgData, round: Int): ConconiRoundData {

        val speed = speeds[round]
        val yData = roundData.channel(0).map { it.y }.toDoubleArray()
        val peaks = PeakDetector.detectSimpleThresholdPeaks(yData)
        val rms = CoreUtils.roundDouble(yData.rms(), 2)

        val hr = roundData.heartRateData.average().toInt()
        return ConconiRoundData(speed, peaks, rms, hr)
    }

    companion object {

        val times = intArrayOf(72, 69, 65, 63, 60, 58, 55, 53, 51, 50,
                48, 46, 45, 44, 42, 41, 40, 39, 38, 37, 36)

        val speeds = doubleArrayOf(10.0, 10.5, 11.0, 11.5, 12.0, 12.5,
                13.0, 13.5, 14.0, 14.5, 15.0, 15.5, 16.0, 16.5, 17.0, 17.5, 18.0, 18.5, 19.0, 19.5, 20.0)
    }

}
