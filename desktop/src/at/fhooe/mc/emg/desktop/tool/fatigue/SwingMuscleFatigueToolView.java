package at.fhooe.mc.emg.desktop.tool.fatigue;

import at.fhooe.mc.emg.core.tool.fatigue.MuscleFatigueToolView;
import at.fhooe.mc.emg.core.tool.fatigue.MuscleFatigueToolViewCallback;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SwingMuscleFatigueToolView implements MuscleFatigueToolView {

    private MuscleFatigueToolViewCallback viewCallback;

    private JPanel mainPanel;

    private JFrame wrap() {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setTitle("Muscle Fatigue detection");
        frame.setIconImage(Toolkit.getDefaultToolkit()
                .getImage(System.getProperty("user.dir") + "/desktop/icons/ic_tool_muscle_fatigue.png"));
        frame.setContentPane(mainPanel);
        frame.setBounds(650, 100, 450, 500);
        return frame;
    }

    @Override
    public void setup(MuscleFatigueToolViewCallback viewCallback, boolean showViewImmediate) {
        this.viewCallback = viewCallback;

        if (showViewImmediate) {
            showView();
        }
    }

    @Override
    public void showView() {
        JFrame frame = wrap();
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                viewCallback.onViewClosed();
            }
        });
        frame.setVisible(true);
    }
}
