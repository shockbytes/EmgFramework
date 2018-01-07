package at.fhooe.mc.emg.desktop.tools.peaks;

import at.fhooe.mc.emg.core.tools.peaks.PeakDetectionView;
import kotlin.Unit;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class SwingPeakDetectionView implements PeakDetectionView {

    private JPanel mainPanel;

    private JFrame wrap() {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setTitle("Peak detection");
        frame.setIconImage(Toolkit.getDefaultToolkit()
                .getImage(System.getProperty("user.dir") + "/desktop/icons/ic_tool_peak_detection.png"));
        frame.setContentPane(mainPanel);
        frame.setBounds(650, 100, 450, 500);
        return frame;
    }

    @Override
    public void showData(@NotNull int[] xValues, @NotNull double[] yValues, @NotNull int[] peaks) {

    }

    @Override
    public void setup(Unit viewCallback) {
        JFrame frame = wrap();
        frame.setVisible(true);
    }
}
