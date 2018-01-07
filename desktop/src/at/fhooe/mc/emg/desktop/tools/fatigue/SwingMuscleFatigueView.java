package at.fhooe.mc.emg.desktop.tools.fatigue;

import at.fhooe.mc.emg.core.tools.fatigue.MuscleFatigueView;
import at.fhooe.mc.emg.core.tools.fatigue.MuscleFatigueViewCallback;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SwingMuscleFatigueView implements MuscleFatigueView {

    private MuscleFatigueViewCallback viewCallback;

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
    public void setup(MuscleFatigueViewCallback viewCallback) {
        this.viewCallback = viewCallback;

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
