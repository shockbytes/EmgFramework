package at.fhooe.mc.emg.clientdriver.model.writable

import at.fhooe.mc.emg.clientdriver.model.EmgPoint

/**
 * Implementing this interface indicates that the content data can be converted into a representation, which
 * then can be plotted.
 */
interface Plotable {

    /**
     * Returns a list of EmgPoints with the size of the utilized window siz.
     *
     * @param channel Indicates which channel (if multi channels are utilized) should be transformed into plot data.
     * @return List of {@link EmgPoint} of size of utilized window size.
     */
    fun plotData(channel: Int): List<EmgPoint>


}