package at.fhooe.mc.emg.client;

import java.io.Serializable;
import java.util.*;
import java.util.stream.IntStream;

public class ChannelData implements Serializable {

    private static final int DEFAULT_MAX_AMOUNT = 512;

	private volatile List<List<Double>> yChartValues;
	private volatile List<List<Double>> xChartValues;

	private int maxAmount;

	public ChannelData() {
	    this(DEFAULT_MAX_AMOUNT);
    }

	public ChannelData(int maxAmount) {
		this.maxAmount = maxAmount;
		
		xChartValues = new ArrayList<>();
		yChartValues = new ArrayList<>();
	}

	public ChannelData(List<List<Double>> xChartValues, List<List<Double>> yChartValues) {
	    this.xChartValues = xChartValues;
	    this.yChartValues = yChartValues;
	    this.maxAmount = DEFAULT_MAX_AMOUNT;
    }

	private void addSeries(int num) {
		for (int i = 0; i < num; i++) {
			addSeries();
		}
	}

	private void addSeries() {
		yChartValues.add(new ArrayList<>());
		xChartValues.add(new ArrayList<>());
	}

	public void updateXYSeries(int channel, double x, double y) {
		
		// Add channels if not available
		if (channel >= yChartValues.size()) {
			addSeries(channel - yChartValues.size() +1);
		}
		
		updateXSeries(channel, x);
		updateYSeries(channel, y);
	}

	private void updateYSeries(int channel, double value) {
		yChartValues.get(channel).add(value);
	}

	private void updateXSeries(int channel, double value) {
		xChartValues.get(channel).add(value);
	}

	public synchronized double[] getXSeries(int channel) {
		Double[] vals = xChartValues.get(channel).toArray(new Double[xChartValues.get(channel).size()]);
		return Arrays.stream(vals)
				.mapToDouble(Double::doubleValue)
				.skip(Math.max(0, vals.length - maxAmount))
				.toArray();
	}

	public synchronized double[] getYSeries(int channel) {
		Double[] vals = yChartValues.get(channel).toArray(new Double[yChartValues.get(channel).size()]);
		return Arrays.stream(vals)
				.mapToDouble(Double::doubleValue)
				.skip(Math.max(0, vals.length - maxAmount))
				.toArray();		
	}

	public int getChannelCount() {
		return yChartValues.size();
	}

	public synchronized String getCsvLogOutput() {

		String headerPrefix = "sep=,\n";
		
		StringBuilder sb = new StringBuilder();
		sb.append(headerPrefix);
		
		StringBuilder header = new StringBuilder("time,");
		for (int i = 0; i < yChartValues.size(); i++) {
			header.append("channel_").append(i);
			if (i < yChartValues.size()-1) {
				header.append(",");
			}
		}
		sb.append(header).append("\n");
		
		int minSize = Collections.min(yChartValues, Comparator.comparingInt(List::size)).size();
				
		for (int i = 0; i < minSize; i++) {
			sb.append(xChartValues.get(0).get(i));
			sb.append(",");
			for (int j = 0; j < yChartValues.size(); j++) {
				sb.append(yChartValues.get(j).get(i));
				if (i < yChartValues.size() - 1) {
					sb.append(", ");
				}
			}
			sb.append("\n");
		}
		
		return sb.toString();
	}

	public synchronized ChannelData getSingleChannelSection(int start, int stop, int channel) {

		if (channel <= 0 && channel >= yChartValues.size()) {
		    throw new IllegalArgumentException("Channel " + channel + " out of range!");
        }
        if (start < 0 || yChartValues.get(0) == null || stop > yChartValues.get(0).size()) {
		    throw new IllegalArgumentException("Start or stop out of range [" + start + ", " + stop + "]");
        }

        List<Double> subX = new ArrayList<>();
		List<Double> subY = new ArrayList<>();
        IntStream.range(start, stop).forEach(idx -> {
            subX.add(xChartValues.get(channel).get(idx));
            subY.add(yChartValues.get(channel).get(idx));
        });

        return new ChannelData(Collections.singletonList(subX),
                Collections.singletonList(subY));
        /*
        return new ChannelData(Collections.singletonList(new ArrayList<>(xChartValues.get(channel).subList(start, stop))),
                Collections.singletonList(new ArrayList<>(yChartValues.get(channel).subList(start, stop)))); */
	}
}
