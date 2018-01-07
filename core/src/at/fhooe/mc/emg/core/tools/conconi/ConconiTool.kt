package at.fhooe.mc.emg.core.tools.conconi

import at.fhooe.mc.emg.core.EmgController
import at.fhooe.mc.emg.core.tools.Tool
import at.fhooe.mc.emg.core.util.CoreUtils
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Author:  Martin Macheiner
 * Date:    04.07.2017
 */
class ConconiTool(var view: ConconiView? = null) : Tool, ConconiViewCallback {

    private lateinit var controller: EmgController
    private var data: ConconiData = ConconiData()

    private var timerDisposable: Disposable? = null

    private var dataStartPointer: Int = 0
    private var dataStopPointer: Int = 0

    override val name: String
        get() = "Conconi Test"

    override fun start(controller: EmgController) {
        this.controller = controller

        view?.setup(this)

        data = ConconiData()
        dataStartPointer = 0
        dataStopPointer = 0
    }

    private fun playCountdownSound() {

        val file = File(System.getProperty("user.dir") + "/data/sound/conconi_countdown.wav")
        CoreUtils.playSound(file)
    }

    override fun onStartClicked() {
        playCountdownSound()
        startCountdown() // Start the countdown and then start the actual test
    }

    override fun onStopClicked() {
        timerDisposable?.dispose()
        controller.disconnectFromClient(null)
    }

    override fun onSaveClicked(filename: String): Boolean {
        return saveData(filename)
    }

    override fun onLoadClicked(filename: String): Boolean {
        return loadData(filename)
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
                controller.connectToClient()
                startTest()
                disposable?.dispose()
            }
        }
    }

    private fun startTest() {

        var tick = 0
        var roundIdx = 0
        timerDisposable = Observable.interval(1000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.computation())
                .subscribe {

                    tick++
                    view?.onTick(tick, times[roundIdx])

                    // Check if a new round starts in 5 seconds, and play a sound if it starts
                    if (tick + 5 == times[roundIdx]) {
                        playCountdownSound()
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
        view?.onRoundDataAvailable(roundData, index)

        dataStartPointer = dataStopPointer
    }

    private fun saveData(filename: String): Boolean {
        return try {
            CoreUtils.serializeToFile(data, filename)
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    private fun loadData(filename: String): Boolean {
        return try {
            data = CoreUtils.deserializeFromFile(filename)
            (0 until data.roundCount).forEachIndexed { idx, _ ->
                view?.onRoundDataAvailable(data.getRoundData(idx), idx)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    companion object {

        val times = intArrayOf(72, 69, 65, 63, 60, 58, 55, 53, 51, 50, 48, 46, 45, 44, 42, 41, 40, 39, 38, 37, 36)

        val speeds = doubleArrayOf(10.0, 10.5, 11.0, 11.5, 12.0, 12.5, 13.0, 13.5, 14.0, 14.5, 15.0, 15.5, 16.0, 16.5, 17.0, 17.5, 18.0, 18.5, 19.0, 19.5, 20.0)
    }

}
