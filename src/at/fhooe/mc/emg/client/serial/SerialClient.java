package at.fhooe.mc.emg.client.serial;

import at.fhooe.mc.emg.client.ChannelData;
import at.fhooe.mc.emg.client.EmgClient;
import at.fhooe.mc.emg.client.ClientDataCallback;
import gnu.io.*;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class SerialClient extends EmgClient implements SerialPortEventListener {

	private static final int TIMEOUT = 5000;
	public static final int DEFAULT_DATA_RATE = 115200;

	public static final int[] SUPPORTED_DATA_RATES = new int[] { 4800, 9600, 19200, 57600, 115200, 230400 };
	
	private List<CommPortIdentifier> ports;

	private BufferedReader inputReader;
	private BufferedWriter outputWriter;
	private SerialPort connectionPort;

	private String portName;

	private int timestamp;

	public SerialClient(int maxAmount) {
	    super();
		initializePorts();
		channelData = new ChannelData(maxAmount);
		fs = 100;
	}

	@SuppressWarnings("unchecked")
	private void initializePorts() {
		ports = Collections.list(CommPortIdentifier.getPortIdentifiers());
	}

	public List<String> getAvailablePortNames(boolean forceUpdate) {
		
		if (forceUpdate) {
			initializePorts();
		}
		
		return ports.stream().map(CommPortIdentifier::getName).collect(Collectors.toList());
	}

	private CommPortIdentifier getPortByName(String name) throws NoSuchPortException {
		return CommPortIdentifier.getPortIdentifier(name);
	}

	@Override
	public void connect(ClientDataCallback callback) throws Exception {
        setClientCallback(callback);

		connectionPort = (SerialPort) getPortByName(portName).open(getClass().getName(), TIMEOUT);
		dataRate = DEFAULT_DATA_RATE;
		setupConnectionParams();
		
		inputReader = new BufferedReader(new InputStreamReader(connectionPort.getInputStream()));
		outputWriter = new BufferedWriter(new OutputStreamWriter(connectionPort.getOutputStream()));
		connectionPort.addEventListener(this);
		connectionPort.notifyOnDataAvailable(true);
	}
	
	private void setupConnectionParams() throws UnsupportedCommOperationException {
		connectionPort.setSerialPortParams(dataRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
	}

	@Override
	public void disconnect() {

		if (connectionPort != null) {

			synchronized (this) {

				connectionPort.removeEventListener();
				connectionPort.close();

				try {
					inputReader.close();
					outputWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				connectionPort = null;
			}
		}
	}

    @Override
    public String getName() {
        return (portName == null) ? getShortName() : "Serial device @ " + portName;
    }

    @Override
    public String getShortName() {
        return "Serial device";
    }

	@Override
	public int getCurrentDataPointer() {
		return timestamp;
	}

	@Override
    public boolean isDataStorageEnabled() {
        return true;
    }

    @Override
	public void setSamplingFrequency(double fs) {
        super.setSamplingFrequency(fs);
        sendSamplingFrequencyToDevice();
	}

	@Override
	public void serialEvent(SerialPortEvent event) {

		if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {

			try {

				String inputLine = inputReader.readLine();
				if (inputLine != null && !inputLine.isEmpty()) {
					if (processLine(inputLine)) {
						callback.onRawDataAvailable(inputLine);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void setPortName(String portName) {
	    this.portName = portName;
    }

    private boolean processLine(String inputLine) {

        // Always increment x counter value
        timestamp++;

        // Okay, there are more than 1 channel
        if (inputLine.contains(",")) {

            List<Float> values = Arrays.stream(inputLine.split(","))
                    .map(s -> s.trim().isEmpty() ? 0 : Float.parseFloat(s.trim())).collect(Collectors.toList());

            if (values.size() <= 1) {
                return false; // Do not process damaged packages
            }

            IntStream.range(0, values.size()).forEach(idx -> channelData.updateXYSeries(idx, timestamp, values.get(idx)));

        } else {

            float value = Character.isDigit(inputLine.charAt(0)) ? Float.parseFloat(inputLine) : Float.MIN_VALUE;
            if (value != Float.MIN_VALUE) {
                channelData.updateXYSeries(0, timestamp, value);
            }
        }

        callback.onChanneledDataAvailable(channelData);
        return true;
    }

    private void sendSamplingFrequencyToDevice() {
        try {
            int millis = (int) ((1d/ fs) * 1000);
            String command = "delay="+millis+"\r\n";
            outputWriter.write(command);
            outputWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
