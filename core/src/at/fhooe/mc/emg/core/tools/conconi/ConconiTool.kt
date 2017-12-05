package at.fhooe.mc.emg.core.tools.conconi

import at.fhooe.mc.emg.core.EmgController
import at.fhooe.mc.emg.core.tools.Tool
import at.fhooe.mc.emg.core.util.CoreUtils
import java.io.File
import java.io.IOException
import java.util.*

/**
 * Author:  Martin Macheiner
 * Date:    04.07.2017
 */
class ConconiTool(var view: ConconiView? = null) : Tool, ConconiViewCallback {

    private var timer: Timer? = null

    private lateinit var controller: EmgController
    private var data: ConconiData? = null

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

        timer = Timer()
        val timerTask = object : TimerTask() {

            internal var i = 0
            internal var idx = 0

            override fun run() {

                i++
                view?.onTick(i, times[idx])

                if (i + 5 == times[idx]) {
                    playCountdownSound()
                }

                if (i == times[idx]) {
                    storeRoundData(idx)

                    i = 0
                    idx++
                }

            }
        }

        val countdownTask = object : TimerTask() {

            internal var countdown = 6

            override fun run() {

                countdown--
                view?.onCountdownTick(countdown)

                if (countdown == 0) {
                    controller.connectToClient()
                    timer?.schedule(timerTask, 0, 1000)
                    cancel()
                }
            }
        }
        timer?.schedule(countdownTask, 0, 1000)
        playCountdownSound()

    }

    override fun onStopClicked() {

        if (timer != null) {
            timer?.cancel()
            controller.disconnectFromClient(null)
        }
    }

    override fun onSaveClicked(filename: String): Boolean {
        return saveData(filename)
    }

    override fun onLoadClicked(filename: String): Boolean{
        return loadData(filename)
    }

    override fun onViewClosed() {
        timer?.cancel()
    }

    private fun storeRoundData(index: Int) {

        dataStopPointer = controller.currentDataPointer
        val roundData = controller.getSingleChannelDataSection(dataStartPointer, dataStopPointer, 0)

        data?.addRoundData(roundData)
        view?.onRoundDataAvailable(roundData, index)

        dataStartPointer = dataStopPointer
    }

    private fun saveData(filename: String): Boolean {

        return try {

            if (data != null) {
                CoreUtils.serializeToFile(data!!, filename)
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    private fun loadData(filename: String): Boolean {

        return try {
            data = CoreUtils.deserializeFromFile(filename)
            (0 until data!!.roundCount).forEachIndexed { idx, _ ->
                view?.onRoundDataAvailable(data?.getRoundData(idx)!!, idx)
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
