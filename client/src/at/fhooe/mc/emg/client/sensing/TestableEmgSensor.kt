package at.fhooe.mc.emg.client.things.sensing

import at.fhooe.mc.emg.client.sensing.EmgSensor

/**
 * @author Martin Macheiner
 * Date: 24.01.2018.
 */

class TestableEmgSensor: EmgSensor {

    var data = 0.0

    override fun provideEmgValues(): List<Double> {
        if (data > 1000) data = 0.0
        return listOf(data++)
    }

    override fun setup() {
        // Do nothing
    }

    override fun tearDown() {
        // Do nothing
    }

}