package at.fhooe.mc.emg.client;

/**
 * Author:  Martin Macheiner
 * Date:    03.07.2017
 */
public interface ClientDataCallback {

    void onChanneledDataAvailable(ChannelData channelData);

    void onRawDataAvailable(String line);
}