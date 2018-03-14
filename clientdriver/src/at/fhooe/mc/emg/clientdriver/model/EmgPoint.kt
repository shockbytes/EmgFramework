package at.fhooe.mc.emg.clientdriver.model

import java.io.Serializable

/**
 * Data class for holding points. This class is only used to convert channels into {@link .writable.Plotable} data.
 */
data class EmgPoint(val x: Double, val y: Double) : Serializable