package at.fhooe.mc.emg.tools.conconi;

import at.fhooe.mc.emg.client.ChannelData;

/**
 * Author:  Mescht
 * Date:    08.07.2017
 */
public interface ConconiToolListener {

    void onCountdownTick(int seconds);

    void onTick(int seconds, int goal);

    void onRoundDataAvailable(ChannelData data, int round);

}
