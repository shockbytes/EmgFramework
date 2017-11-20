package at.fhooe.mc.emg.util;

import java.util.List;

/**
 * Author:  Mescht
 * Date:    08.07.2017
 */
public class PeakDetector {


    public static int detectSimpleThresholdPeaks(double[] yVals, int threshold) {

        int peaks = 0;
        boolean isRising = true;
        for (double yVal : yVals) {

            if (yVal > threshold && isRising) {
                isRising = false;
            }
            if (yVal < threshold && !isRising) {
                peaks++;
                isRising = true;
            }
        }
        return peaks;
    }

    public static List<Integer> detectPeakLocations(double[] yVals) {
        return null;
    }

}
