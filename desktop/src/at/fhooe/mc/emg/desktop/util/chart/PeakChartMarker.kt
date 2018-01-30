package at.fhooe.mc.emg.desktop.util.chart

import org.knowm.xchart.style.markers.Diamond
import java.awt.Graphics2D

class PeakChartMarker(private val markerSize: Int) : Diamond() {

    override fun paint(g: Graphics2D?, xOffset: Double, yOffset: Double, markerSize: Int) {
        super.paint(g, xOffset, yOffset, this.markerSize)
    }

}