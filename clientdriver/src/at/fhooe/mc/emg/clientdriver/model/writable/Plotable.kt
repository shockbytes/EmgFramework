package at.fhooe.mc.emg.clientdriver.model.writable

import at.fhooe.mc.emg.clientdriver.model.EmgPoint

interface Plotable {

    fun plotData(channel: Int): List<EmgPoint>


}