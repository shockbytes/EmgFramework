package at.fhooe.mc.emg.core.tools.conconi

import at.fhooe.mc.emg.clientdriver.model.EmgData
import at.fhooe.mc.emg.core.EmgController
import at.fhooe.mc.emg.core.storage.FileStorage
import at.fhooe.mc.emg.core.tools.Tool
import at.fhooe.mc.emg.core.util.CoreUtils
import at.fhooe.mc.emg.core.util.PeakDetector
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Author:  Martin Macheiner
 * Date:    04.07.2017
 */
class ConconiTool(override var view: ConconiView? = null,
                  private var fileStorage: FileStorage) : Tool, ConconiViewCallback {

    private lateinit var controller: EmgController
    private var data: ConconiData = ConconiData()

    private var timerDisposable: Disposable? = null

    private var dataStartPointer: Int = 0
    private var dataStopPointer: Int = 0

    override val name = "Conconi Test"

    override fun start(controller: EmgController, showViewImmediate: Boolean) {
        this.controller = controller

        view?.setup(this, showViewImmediate)

        data = ConconiData()
        dataStartPointer = 0
        dataStopPointer = 0
    }

    override fun onStartClicked() {
        view?.onPlayCountdownSound()
        startCountdown() // Start the countdown and then start the actual test
    }

    override fun onStopClicked() {
        timerDisposable?.dispose()
        controller.disconnectFromClient(null)
    }

    override fun onSaveClicked(filename: String?, errorHandler: Consumer<Throwable>) {

        if (filename != null) {
            // No action required if everything works fine
            fileStorage.storeFileAsObject(data, filename).subscribe(Action {}, errorHandler)
        } else {
            errorHandler.accept(NullPointerException("Filename must not be null!"))
        }
    }

    override fun onLoadClicked(filename: String?, errorHandler: Consumer<Throwable>) {

        if (filename != null) {
            fileStorage.loadFromFileAsObject<ConconiData>(filename).subscribe(Consumer {

                if (it == null) {
                    throw IOException("Cannot read data from $filename")
                }

                data = it
                (0 until data.roundCount).forEachIndexed { idx, _ ->
                    view?.onRoundDataAvailable(emg2ConconiRoundData(data.getRoundData(idx), idx), idx)
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
        return fileStorage.listFiles(directory, concatToBase, ".ctf")
    }

    override fun onViewClosed() {
        timerDisposable?.dispose()
    }

    private fun startCountdown() {

        var disposable: Disposable? = null
        var counter = 6
        disposable = Observable.interval(1, TimeUnit.SECONDS).subscribe {

            counter--
            view?.onCountdownTick(counter)
            if (counter == 0) {
                startTest()
                disposable?.dispose()
            }
        }
    }

    private fun startTest() {

        // TODO Handle somehow the case, that the controller cannot connect to the client
        controller.connectToClient()

        // Start timer for Conconi test
        var tick = 0
        var roundIdx = 0
        timerDisposable = Observable.interval(1000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.computation())
                .subscribe {

                    tick++
                    view?.onTick(tick, times[roundIdx])

                    // Check if a new round starts in 5 seconds, and play a sound if it starts
                    if (tick + 5 == times[roundIdx]) {
                        view?.onPlayCountdownSound()
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

        dataStopPointer = controller.currentDataPointer
        val roundData = controller.getSingleChannelDataSection(dataStartPointer, dataStopPointer, 0)

        data.addRoundData(roundData)
        view?.onRoundDataAvailable(emg2ConconiRoundData(roundData, index), index)

        dataStartPointer = dataStopPointer
    }

    private fun emg2ConconiRoundData(roundData: EmgData, round: Int): ConconiRoundData {
        val speed = speeds[round]
        val yData = roundData.plotData(0).map { it.y }.toDoubleArray()
        val peaks = PeakDetector.detectSimpleThresholdPeaks(yData, 200)
        val avg = CoreUtils.roundDouble(yData.average(), 2)
        return ConconiRoundData(speed, peaks, avg)
    }

    companion object {

        val times = intArrayOf(72, 69, 65, 63, 60, 58, 55, 53, 51, 50,
                48, 46, 45, 44, 42, 41, 40, 39, 38, 37, 36)

        val speeds = doubleArrayOf(10.0, 10.5, 11.0, 11.5, 12.0, 12.5,
                13.0, 13.5, 14.0, 14.5, 15.0, 15.5, 16.0, 16.5, 17.0, 17.5, 18.0, 18.5, 19.0, 19.5, 20.0)
    }

}
