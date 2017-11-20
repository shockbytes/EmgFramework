package at.fhooe.mc.emg.tools.peak;

import at.fhooe.mc.emg.core.EmgController;
import at.fhooe.mc.emg.tools.Tool;

import javax.swing.*;

/**
 * Author:  Mescht
 * Date:    04.07.2017
 */
public class PeakDetectionTool implements Tool {

    public PeakDetectionTool() {

    }

    @Override
    public String getName() {
        return "Peak detection";
    }

    @Override
    public void start(EmgController controller) {

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setTitle(getName());
        frame.setContentPane(new JPanel());
        frame.setBounds(600, 200, 400, 300);
        frame.setVisible(true);
    }
}
