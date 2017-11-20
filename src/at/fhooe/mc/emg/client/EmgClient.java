package at.fhooe.mc.emg.client;

/**
 * Author:  Martin Macheiner
 * Date:    03.07.2017
 */
public abstract class EmgClient {

    protected int dataRate;
    protected double fs;

    protected ChannelData channelData;
    protected ClientDataCallback callback;

    public EmgClient() {
        channelData = null;
    }

    public abstract void connect(ClientDataCallback callback) throws Exception;

    public abstract void disconnect();

    public abstract String getName();

    public abstract String getShortName();

    public abstract int getCurrentDataPointer();

    public abstract boolean isDataStorageEnabled();

    public int getDataRate() {
        return dataRate;
    }

    public void setDataRate(int dataRate) {
        this.dataRate = dataRate;
    }

    public double getSamplingFrequency() {
        return fs;
    }

    public void setSamplingFrequency(double fs) {
        this.fs = fs;
    }

    public void setClientCallback(ClientDataCallback callback) {
        this.callback = callback;
    }

    public ChannelData getChannelData() {
        return channelData;
    }

}
