package at.fhooe.mc.emg.desktop

import org.junit.Test

class MoodMetricsTest {

    @Test
    fun testMoodMetrics() {

        MoodMetrics("at.fhooe.mc.emg").metrics().blockingGet().let { metrics ->
            println(metrics)
        }
    }

}