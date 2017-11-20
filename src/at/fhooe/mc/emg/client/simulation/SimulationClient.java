package at.fhooe.mc.emg.client.simulation;

import at.fhooe.mc.emg.client.ChannelData;
import at.fhooe.mc.emg.client.EmgClient;
import at.fhooe.mc.emg.util.AppUtils;
import at.fhooe.mc.emg.client.ClientDataCallback;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimulationClient extends EmgClient {

	private final String SIMULATION_FOLDER = System.getProperty("user.dir") + "/data/simulation";

	private int millis;
	private boolean isEndlessLoopEnabled;
	
	private SimulationSource source;

	private Timer t;
	private Thread thread;
	private List<Double> simulationData;

	private int timestamp;

	public SimulationClient(double sampleFrequency, int maxAmount, boolean isEndlessLoopEnabled) {
	    super();
		setSamplingFrequency(sampleFrequency);
		setEndlessLoopPlayback(isEndlessLoopEnabled);
		channelData = new ChannelData(maxAmount);
	}
	
	@Override
	public void connect(ClientDataCallback callback) throws Exception {
        setClientCallback(callback);
		if (source == null || callback == null) {
			throw new IllegalStateException("Source or listener cannot be null for simulation!");
		}
		
		disconnect();
		
		simulationData = prepareSimulationData();

		thread = new Thread(() -> {

            t = new Timer();
            TimerTask tt = new TimerTask() {

                int index = 0;

                @Override
                public void run() {

                    double data = simulationData.get(index);
                    callback.onRawDataAvailable(String.valueOf(data));

                    channelData.updateXYSeries(0, timestamp, data);
                    callback.onChanneledDataAvailable(channelData);

                    timestamp++;
                    index++;

                    if (index >= simulationData.size()) {
                        if (isEndlessLoopEnabled) {
                            index = 0;
                        } else {
                            disconnect();
                        }
                    }
                }
            };
            t.schedule(tt, 0, millis);
        });
		thread.start();
	}

	@Override
	public void disconnect() {

		synchronized (this) {
			
			if (t != null) {
				t.cancel();
				t.purge();
				t = null;
			}
			if (thread != null) {
				try {
					thread.join();
					thread = null;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

    @Override
    public String getName() {
        return (source == null) ? getShortName() : "Simulator /w " + source.getName();
    }

    @Override
    public String getShortName() {
        return "Simulator";
    }

	@Override
	public int getCurrentDataPointer() {
		return timestamp;
	}

	@Override
    public boolean isDataStorageEnabled() {
        return false;
    }

    @Override
	public void setSamplingFrequency(double sampleFrequency) {
	    super.setSamplingFrequency(sampleFrequency);
		millis = (int) ((1d / sampleFrequency) * 1000);
	}

	public void setSimulationSource(SimulationSource source) {
		this.source = source;
	}

	public void addFileAsSimulationSource(String srcPath) {

		File srcFile = new File(srcPath);
		File destFile = new File (SIMULATION_FOLDER + "/" + srcFile.getName());
		
		try {
			
			FileUtils.copyFile(srcFile, destFile);
			
			String modified = Files.readAllLines(Paths.get(destFile.getAbsolutePath()))
								.stream()
								.filter(s -> !s.isEmpty() && Character.isDigit(s.charAt(0)))
								.collect(Collectors.joining(System.lineSeparator()));
			AppUtils.writeFile(destFile, modified);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<SimulationSource> loadSimulationSources() {

		File[] simulationFiles = new File(SIMULATION_FOLDER).listFiles();
		return Arrays.stream(Optional.ofNullable(simulationFiles).orElse(new File[] {}))
				.map(f -> new SimulationSource(f.getName().substring(0, f.getName().lastIndexOf(".")),
						f.getAbsolutePath()))
				.collect(Collectors.toList());
	}

	public void setEndlessLoopPlayback(boolean isEndlessLoopEnabled) {
		this.isEndlessLoopEnabled = isEndlessLoopEnabled;
	}

    private List<Double> prepareSimulationData() {

        List<Double> list = new ArrayList<>();

        try (Stream<String> stream = Files.lines(Paths.get(source.getFilepath()))) {

            list = stream
                    .map(s -> {
                        String number = s.substring(s.indexOf(",")+1, s.length());
                        return number.isEmpty() ? 0 : Double.parseDouble(number);
                    })
                    .collect(Collectors.toList());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

}
