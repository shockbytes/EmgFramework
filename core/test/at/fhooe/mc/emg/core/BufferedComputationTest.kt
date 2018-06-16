package at.fhooe.mc.emg.core

import at.fhooe.mc.emg.core.computation.RegressionAnalysisBufferedComputation
import org.junit.Assert.assertEquals
import org.junit.Test

class BufferedComputationTest {


    @Test
    fun testRegressionAnalysis() {

        val r = RegressionAnalysisBufferedComputation(3)

        var counter = 0
        val results = doubleArrayOf(1.0, 2.0, 0.5)

        r.outputPort.subscribe {
            assertEquals(results[counter], it, 0.01)
            counter++
        }

        r.update(Pair(0.0, 0.0))
        r.update(Pair(1.0, 1.0))
        r.update(Pair(2.0, 2.0))

        Thread.sleep(1000)

        r.update(Pair(1.0, 2.0))
        r.update(Pair(2.0, 4.0))
        r.update(Pair(3.0, 6.0))

        Thread.sleep(1000)

        r.update(Pair(2.0, 1.0))
        r.update(Pair(4.0, 2.0))
        r.update(Pair(6.0, 3.0))


        Thread.sleep(1000)

    }

}