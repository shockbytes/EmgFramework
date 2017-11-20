package at.fhooe.mc.emg.tools.conconi;

import at.fhooe.mc.emg.client.ChannelData;
import at.fhooe.mc.emg.core.EmgController;
import at.fhooe.mc.emg.tools.Tool;
import at.fhooe.mc.emg.ui.UiUtils;
import at.fhooe.mc.emg.util.AppUtils;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Author:  Martin Macheiner
 * Date:    04.07.2017
 */
public class ConconiTool implements Tool, ConconiViewListener {

    private static int[] TIMES = new int[]{
            72, 69, 65, 63, 60, 58, 55, 53,
            51, 50, 48, 46, 45, 44, 42, 41,
            40, 39, 38, 37, 36};

    static double[] SPEEDS = new double[] {
            10, 10.5, 11, 11.5, 12, 12.5, 13,
            13.5, 14, 14.5, 15, 15.5, 16, 16.5,
            17, 17.5, 18, 18.5, 19, 19.5, 20};

    private Timer timer;
    private ConconiToolListener form;

    private EmgController controller;
    private ConconiData data;

    private int dataStartPointer;
    private int dataStopPointer;

    public ConconiTool() {
    }

    @Override
    public String getName() {
        return "Conconi Test";
    }

    @Override
    public void start(EmgController controller) {
        this.controller = controller;

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

        data = new ConconiData();
        dataStartPointer = 0;
        dataStopPointer = 0;
    }

    private void playCountdownSound() {

        File file = new File(System.getProperty("user.dir") + "/data/sound/conconi_countdown.wav");
        AppUtils.playSound(file);
    }

    @Override
    public void onStartClicked() {

        timer = new Timer();
        TimerTask timerTask = new TimerTask() {

            int i = 0;
            int idx = 0;

            @Override
            public void run() {

                i++;
                form.onTick(i, TIMES[idx]);

                if (i + 5 == TIMES[idx]) {
                    playCountdownSound();
                }

                if (i == TIMES[idx]) {
                    storeRoundData(idx);

                    i = 0;
                    idx++;
                }

            }
        };

        TimerTask countdownTask = new TimerTask() {

            int countdown = 6;

            @Override
            public void run() {

                countdown--;
                form.onCountdownTick(countdown);

                if (countdown == 0) {
                    controller.connect();
                    timer.schedule(timerTask, 0, 1000);
                    cancel();
                }
            }
        };
        timer.schedule(countdownTask, 0, 1000);
        playCountdownSound();

    }

    @Override
    public void onStopClicked() {

        if (timer != null) {
            timer.cancel();
            controller.disconnect();
        }
    }

    @Override
    public void onSaveClicked() {
        saveData();
    }

    @Override
    public void onLoadClicked() {
        loadData();
    }

    private void storeRoundData(int index) {

        dataStopPointer = controller.getCurrentDataPointer();
        // System.out.println("Start: " + dataStartPointer + " / Stop: " + dataStopPointer);

        ChannelData roundData = controller.getSingleChannelDataSection(dataStartPointer, dataStopPointer, 0);
        data.addRoundData(roundData);
        form.onRoundDataAvailable(roundData, index);

        dataStartPointer = dataStopPointer;
    }

    private void saveData() {

        try {
            String filename = UiUtils.showConconiSaveDialog();
            AppUtils.serializeToFile(data, filename);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Cannot store data to file!");
        }
    }

    private void loadData() {

        try {

            String filename = UiUtils.showConconiLoadDialog();
            data = AppUtils.deserializeFromFile(filename);

            // Set the data to the form (simulate set)
            for (int i = 0; i < data.getRoundCount(); i++) {
                form.onRoundDataAvailable(data.getRoundData(i), i);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Cannot load data from file!");
        }

    }

}
