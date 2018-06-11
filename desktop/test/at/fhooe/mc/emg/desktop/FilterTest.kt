package at.fhooe.mc.emg.desktop

import at.fhooe.mc.emg.core.filter.RunningAverageFilter
import org.junit.Assert
import org.junit.Test

class FilterTest {


    @Test
    fun testRunningAverageFilterMap() {

        val data = generateSequence(0.toDouble()) { it + 1 }.take(500).toList()
        val filter = RunningAverageFilter()
        val iteratedResult = mutableListOf<Double>()

        data.forEach { iteratedResult.add(filter.step(it)) }
        filter.reset()
        val mappedResult = data.map { filter.step(it) }

        Assert.assertArrayEquals(iteratedResult.toTypedArray(), mappedResult.toTypedArray())
    }

}