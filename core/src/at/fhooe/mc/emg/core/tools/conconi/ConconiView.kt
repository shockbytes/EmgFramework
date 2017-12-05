package at.fhooe.mc.emg.core.tools.conconi

import at.fhooe.mc.emg.clientdriver.model.EmgData
import at.fhooe.mc.emg.core.tools.ToolView

/**
 * Author:  Mescht
 * Date:    20.11.2017
 */

interface ConconiView : ToolView<ConconiViewCallback> {

    fun onCountdownTick(seconds: Int)

    fun onTick(seconds: Int, goal: Int)

    fun onRoundDataAvailable(data: EmgData, round: Int)

}