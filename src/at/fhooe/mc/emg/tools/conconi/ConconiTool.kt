package at.fhooe.mc.emg.tools.conconi

import at.fhooe.mc.emg.core.EmgController
import at.fhooe.mc.emg.tools.Tool
import at.fhooe.mc.emg.util.AppUtils
import java.io.File
import java.util.*

/**
 * Author:  Martin Macheiner
 * Date:    04.07.2017
 */
class ConconiTool : Tool, ConconiFormListener {

    private var timer: Timer? = null
    // TODO Replace with ConconiView?
    private val form: ConconiToolListener? = null

    private var controller: EmgController<*>? = null
    private var data: ConconiData? = null

    private var dataStartPointer: Int = 0
    private var dataStopPointer: Int = 0

    override val name: String
        get() = "Conconi Test"

    override fun start(controller: EmgController<*>) {
        this.controller = controller

        // TODO Use ConconiView instead
        /*
        ConconiForm conconiForm = new ConconiForm();
        conconiForm.setFormListener(this);
        JFrame frame = conconiForm.wrap(getName());
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                if (timer != null) {
                    timer.cancel();
                }
            }
        });
        frame.setVisible(true);

        form = conconiForm;
        */

        data = ConconiData()
        dataStartPointer = 0
        dataStopPointer = 0
    }

    private fun playCountdownSound() {

        val file = File(System.getProperty("user.dir") + "/data/sound/conconi_countdown.wav")
        AppUtils.playSound(file)
    }

    override fun onStartClicked() {

        timer = Timer()
        val timerTask = object : TimerTask() {

            internal var i = 0
            internal var idx = 0

            override fun run() {

                i++
                form!!.onTick(i, TIMES[idx])

                if (i + 5 == TIMES[idx]) {
                    playCountdownSound()
                }

                if (i == TIMES[idx]) {
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
                form!!.onCountdownTick(countdown)

                if (countdown == 0) {
                    controller!!.connect()
                    timer!!.schedule(timerTask, 0, 1000)
                    cancel()
                }
            }
        }
        timer!!.schedule(countdownTask, 0, 1000)
        playCountdownSound()

    }

    override fun onStopClicked() {

        if (timer != null) {
            timer!!.cancel()
            controller!!.disconnect()
        }
    }

    override fun onSaveClicked() {
        saveData()
    }

    override fun onLoadClicked() {
        loadData()
    }

    private fun storeRoundData(index: Int) {

        dataStopPointer = controller!!.currentDataPointer
        // System.out.println("Start: " + dataStartPointer + " / Stop: " + dataStopPointer);

        val roundData = controller!!.getSingleChannelDataSection(dataStartPointer, dataStopPointer, 0)
        data!!.addRoundData(roundData)
        form!!.onRoundDataAvailable(roundData, index)

        dataStartPointer = dataStopPointer
    }

    private fun saveData() {

        /*
        try {
            // TODO Detach UiUtils from ConconiTool
            String filename = UiUtils.showConconiSaveDialog();
            AppUtils.INSTANCE.serializeToFile(data, filename);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Cannot store data to file!");
        } */
    }

    private fun loadData() {

        /*
        try {
            // TODO Detach UiUtils from ConconiTool
            String filename = UiUtils.showConconiLoadDialog();
            data = AppUtils.INSTANCE.deserializeFromFile(filename);

            // Set the data to the form (simulate set)
            for (int i = 0; i < data.getRoundCount(); i++) {
                form.onRoundDataAvailable(data.getRoundData(i), i);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Cannot load data from file!");
        }
        */
    }

    companion object {

        private val TIMES = intArrayOf(72, 69, 65, 63, 60, 58, 55, 53, 51, 50, 48, 46, 45, 44, 42, 41, 40, 39, 38, 37, 36)

        internal var SPEEDS = doubleArrayOf(10.0, 10.5, 11.0, 11.5, 12.0, 12.5, 13.0, 13.5, 14.0, 14.5, 15.0, 15.5, 16.0, 16.5, 17.0, 17.5, 18.0, 18.5, 19.0, 19.5, 20.0)
    }

}
