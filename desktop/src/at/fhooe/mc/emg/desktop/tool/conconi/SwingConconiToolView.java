package at.fhooe.mc.emg.desktop.tool.conconi;

import at.fhooe.mc.emg.core.tool.conconi.ConconiRoundData;
import at.fhooe.mc.emg.core.tool.conconi.ConconiToolView;
import at.fhooe.mc.emg.core.tool.conconi.ConconiToolViewCallback;
import at.fhooe.mc.emg.desktop.util.UiUtils;
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
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Author:  Martin Macheiner
 * Date:    08.07.2017
 */
public class SwingConconiToolView implements ConconiToolView {

    private JPanel panelMain;
    private JButton btnStart;
    private JButton btnStop;
    private JLabel labelTime;
    private JButton btnSave;
    private JTable table;
    private JPanel panelAverageVisual;
    private JButton btnLoad;
    private JCheckBox runningAverageFiterCheckBox;

    private XYChart chartAverage;
    private XChartPanel<XYChart> chartAverageWrapper;

    private ConconiToolViewCallback viewCallback;

    private List<Double> xVals;
    private List<Double> yAvg;
    private List<Integer> yHr;

    private Consumer<Throwable> errorHandler = new Consumer<Throwable>() {
        @Override
        public void accept(Throwable throwable) {
            JOptionPane.showMessageDialog(panelMain, throwable.getLocalizedMessage());
        }
    };

    private ActionListener actionListener = new ActionListener() {

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
    };


    public SwingConconiToolView() {
        btnStart.addActionListener(actionListener);
        btnStop.addActionListener(actionListener);
        btnSave.addActionListener(actionListener);
        btnLoad.addActionListener(actionListener);
        runningAverageFiterCheckBox.addItemListener(e -> {
            boolean isSelected = e.getStateChange() == ItemEvent.SELECTED;
            viewCallback.setUseRunningAverageFilter(isSelected);
        });

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

        ((DefaultTableModel) table.getModel()).addRow(
                new String[]{String.valueOf(data.getSpeed()), String.valueOf(data.getEmg()),
                        String.valueOf(data.getHr())});

        updateCharts(data.getEmg(), data.getSpeed(), data.getHr());
    }

    private void updateCharts(double avg, double speed, int heartRate) {

        if (xVals == null) {
            xVals = new ArrayList<Double>();
            yAvg = new ArrayList<Double>();
            yHr = new ArrayList<Integer>();
        }

        xVals.add(speed);
        yAvg.add(avg);
        yHr.add(heartRate);

        if (chartAverage.getSeriesMap().size() == 0) {
            chartAverage.addSeries("EMG", xVals, yAvg);
            chartAverage.addSeries("Heart rate", xVals, yHr);
        } else {
            chartAverage.updateXYSeries("EMG", xVals, yAvg, null);
            chartAverage.updateXYSeries("Heart rate", xVals, yHr, null);
        }
        chartAverageWrapper.repaint();
    }

    private void setupCharts() {
        chartAverage = new XYChartBuilder().width(150).height(200).theme(Styler.ChartTheme.GGPlot2).build();
        chartAverage.getStyler().setLegendVisible(true);
        chartAverage.getStyler().setPlotGridLinesVisible(false);
        chartAverage.getStyler().setPlotBackgroundColor(Color.WHITE);
        chartAverage.getStyler().setLegendPosition(Styler.LegendPosition.OutsideS);
        chartAverage.setXAxisTitle("km/h");
        chartAverage.setYAxisTitle("HR / EMG");

        chartAverageWrapper = new XChartPanel<XYChart>(chartAverage);
        panelAverageVisual.add(chartAverageWrapper);
    }

    private void updateButtonStates(boolean isEnabled) {
        btnStart.setEnabled(isEnabled);
        btnLoad.setEnabled(isEnabled);
        btnStop.setEnabled(!isEnabled);
    }

    private void createUIComponents() {
        table = new JTable(new DefaultTableModel(new String[0][3], new String[]{"km/h", "EMG", "Heart Rate"}));
    }

    @Override
    public void setup(@NotNull ConconiToolViewCallback viewCallback, boolean showViewImmediate) {
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
        DesktopUtils.playSound(file);
    }
}
