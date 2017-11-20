package at.fhooe.mc.emg.tools.conconi;

import at.fhooe.mc.emg.client.ChannelData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Author:  Mescht
 * Date:    08.07.2017
 */
class ConconiData implements Serializable {

    private List<ChannelData> roundData;

    ConconiData() {
        roundData = new ArrayList<>();
    }

    void addRoundData(ChannelData crd) {
        roundData.add(crd);
    }

    ChannelData getRoundData(int index) {
        return roundData.get(index);
    }

    int getRoundCount() {
        return roundData.size();
    }

}
