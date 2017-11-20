package at.fhooe.mc.emg.ui;

import at.fhooe.mc.emg.util.Analysis;
import at.fhooe.mc.emg.util.AppUtils;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.CategorySeries.CategorySeriesRenderStyle;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.style.Styler.ChartTheme;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.knowm.xchart.style.Styler.ToolTipType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;

class FrequencyAnalysisFrame extends JFrame {
	
	private static final long serialVersionUID = 1L;

	public enum AnalysisType {FFT, SPECTRUM}
	
	private JPanel contentPane;
	private CategoryChart chart;

	FrequencyAnalysisFrame(AnalysisType type, double[] input,
						   double sampleFrequency, JFrame parent) {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setIconImage(Toolkit.getDefaultToolkit().getImage(System.getProperty("user.dir") + "/icons/ic_analysis.png"));
		setTitle("Frequency analysis");
		setBoundsRelativeToParent(parent, type);
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(8, 8, 8, 8));
		contentPane.setBackground(Color.WHITE);
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		initializeChart();		
		doCalculation(type, input, sampleFrequency);
	}
	
	private void setBoundsRelativeToParent(JFrame parent, AnalysisType type) {
		
		Rectangle b = parent.getBounds();
		
		switch(type) {
		
		case FFT:
			
			setBounds(b.x + b.width + 10, b.y - b.height/2 + 30, 450, 300);
			break;
			
		case SPECTRUM:
			
			setBounds(b.x + b.width + 10, b.y + b.height/2 + 30, 450, 300);
			break;
		}
	}
	
	private void initializeChart() {
		
		chart = new CategoryChartBuilder().width(800).height(600).theme(ChartTheme.GGPlot2).build();
		chart.getStyler().setLegendPosition(LegendPosition.InsideN);
	    chart.getStyler().setDefaultSeriesRenderStyle(CategorySeriesRenderStyle.Stick);
	    chart.getStyler().setXAxisLabelRotation(90);
	    chart.getStyler().setToolTipsEnabled(true);
	    chart.getStyler().setToolTipType(ToolTipType.xAndYLabels);
	    chart.getStyler().setXAxisTicksVisible(false);
	    chart.getStyler().setPlotGridLinesVisible(false);
	    chart.getStyler().setPlotBackgroundColor(Color.WHITE);
	    chart.getStyler().setChartBackgroundColor(Color.WHITE);
	    chart.getStyler().setXAxisDecimalPattern("#0.00");

		XChartPanel<CategoryChart> chartWrapper = new XChartPanel<>(chart);
		contentPane.add(chartWrapper);
	}
	
	private void doCalculation(AnalysisType type, double[] input, double fs) {
				
		//new Thread(() -> {
	        double[] fft = Analysis.INSTANCE.fft(input);
	        
	        switch (type) {

	        case FFT:
	        	
				showFFTPlot(fft);
	        	break;
			
	        case SPECTRUM:
			
	        	showPowerSpectrumPlot(fft, fs);
	        	break;	        
	        }
 
		//}).start();
	}
	
	private void showFFTPlot(double[] fft) {
		
        double[] xData = new double[fft.length];
        Arrays.setAll(xData, i -> i);
        
        Color c = Color. decode("#0091EA");
        chart.addSeries("FFT", xData, fft)
        			.setMarkerColor(c)
        			.setLineColor(c);
	}
	
	private void showPowerSpectrumPlot(double[] fft, double fs) {
		
		int N = fft.length;
		double resolution = fs / N;
        double[] spectrum = new double[N/2 -1];
        double[] xData = new double[N/2 -1];
        Arrays.setAll(xData, i -> AppUtils.INSTANCE.roundDouble(i * resolution, 2));
        for (int k = 2; k < N/2 - 1; k++) {
	        spectrum[k] = AppUtils.INSTANCE.roundDouble(Math.sqrt(Math.pow(fft[2*k],2) + Math.pow(fft[2*k+1],2)), 2);
        }
        
        //setFrequency(xData);
        
        Color c = Color. decode("#8BC34A");
        chart.addSeries("Power spectrum", xData, spectrum)
					.setMarkerColor(c)
					.setLineColor(c);
	}
	
	/*
	private void setFrequency(double[] xData) {
		OptionalDouble max = Arrays.stream(xData).max();		
	    chart.setTitle("Frequency: " + max.orElse(-1) + "Hz");
	} */

}
