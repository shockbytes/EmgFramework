package at.fhooe.mc.emg.desktop.tools.conconi;

import at.fhooe.mc.emg.core.tools.conconi.ConconiRoundData;
import at.fhooe.mc.emg.core.tools.conconi.ConconiView;
import at.fhooe.mc.emg.core.tools.conconi.ConconiViewCallback;
import at.fhooe.mc.emg.desktop.ui.UiUtils;
import at.fhooe.mc.emg.desktop.util.DesktopUtils;
import io.reactivex.functions.Consumer;
import org.jetbrains.annotations.NotNull;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Author:  Martin Macheiner
 * Date:    08.07.2017
 */
public class SwingConconiView implements ActionListener, ConconiView {

    private JPanel panelMain;
    private JButton btnStart;
    private JButton btnStop;
    private JLabel labelTime;
    private JButton btnSave;
    private JTable table;
    private JPanel panelAverageVisual;
    private JButton btnLoad;

    private XYChart chartAverage;
    private XChartPanel<XYChart> chartAverageWrapper;

    private ConconiViewCallback viewCallback;

    private List<Double> xVals;
    private List<Double> yAvg;

    private Consumer<Throwable> errorHandler = new Consumer<Throwable>() {
        @Override
        public void accept(Throwable throwable) {
            JOptionPane.showMessageDialog(panelMain, throwable.getLocalizedMessage());
        }
    };

    public SwingConconiView() {
        btnStart.addActionListener(this);
        btnStop.addActionListener(this);
        btnSave.addActionListener(this);
        btnLoad.addActionListener(this);

        updateButtonStates(true);

        setupCharts();
    }

    private JFrame wrap() {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setTitle("Conconi Test");
        frame.setIconImage(Toolkit.getDefaultToolkit()
                .getImage(System.getProperty("user.dir") + "/desktop/icons/ic_tool_conconi.png"));
        frame.setContentPane(panelMain);
        frame.setBounds(650, 100, 450, 500);
        return frame;
    }

    @Override
    public void onCountdownTick(int seconds) {
        labelTime.setText(String.valueOf(seconds));
    }

    @Override
    public void onTick(int seconds, int goal) {
        String out = seconds + " / " + goal + " seconds";
        labelTime.setText(out);
    }

    @Override
    public void onRoundDataAvailable(@NotNull ConconiRoundData data, int round) {

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.addRow(new String[]{String.valueOf(data.getSpeed()), String.valueOf(data.getRms()),
                String.valueOf(data.getPeaks())});

        updateCharts(data.getRms(), data.getSpeed());
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == btnStart) {
            viewCallback.onStartClicked();
            updateButtonStates(false);
        } else if (e.getSource() == btnStop) {
            viewCallback.onStopClicked();
            labelTime.setText("Test finished!");
            updateButtonStates(true);
        } else if (e.getSource() == btnSave) {
            String saveFileName = UiUtils.INSTANCE.showConconiSaveDialog();
            viewCallback.onSaveClicked(saveFileName, errorHandler);
        } else if (e.getSource() == btnLoad) {
            String loadFileName = UiUtils.INSTANCE.showConconiLoadDialog();
            viewCallback.onLoadClicked(loadFileName, errorHandler);
        }
    }

    private void updateCharts(double avg, double speed) {

        if (xVals == null) {
            xVals = new ArrayList<>();
            yAvg = new ArrayList<>();
        }

        xVals.add(speed);
        yAvg.add(avg);

        if (chartAverage.getSeriesMap().size() == 0) {
            chartAverage.addSeries("Average", xVals, yAvg);
        } else {
            chartAverage.updateXYSeries("Average", xVals, yAvg, null);
        }
        chartAverageWrapper.repaint();

    }

    private void setupCharts() {

        chartAverage = new XYChartBuilder().width(150).height(200).theme(Styler.ChartTheme.GGPlot2).build();
        chartAverage.getStyler().setLegendVisible(false);
        chartAverage.getStyler().setPlotGridLinesVisible(false);
        chartAverage.getStyler().setPlotBackgroundColor(Color.WHITE);
        chartAverage.setXAxisTitle("km/h");

        chartAverageWrapper = new XChartPanel<>(chartAverage);
        panelAverageVisual.add(chartAverageWrapper);
    }

    private void updateButtonStates(boolean isEnabled) {

        btnStart.setEnabled(isEnabled);
        btnLoad.setEnabled(isEnabled);
        btnStop.setEnabled(!isEnabled);
    }

    private void createUIComponents() {
        table = new JTable(new DefaultTableModel(new String[0][3], new String[]{"km/h", "RMS", "Peaks"}));
    }

    @Override
    public void setup(@NotNull ConconiViewCallback viewCallback, boolean showViewImmediate) {
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

    @Override
    public void onPlayCountdownSound() {
        File file = new File(System.getProperty("user.dir") + "/data/sound/conconi_countdown.wav");
        DesktopUtils.INSTANCE.playSound(file);
    }
}
