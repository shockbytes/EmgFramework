package at.fhooe.mc.emg.messaging

import at.fhooe.mc.emg.messaging.model.EmgPacket
import at.fhooe.mc.emg.messaging.model.experimental.EmgBurst
import org.junit.Test



class EmgBurstVsPacketTest {

    @Test
    fun testEmgPacketSize() {

        val packets = (0 until 1000).mapTo(mutableListOf()) {
            EmgPacket(listOf(128.0,127.0), System.currentTimeMillis(), 60)
        }

        //println(ObjectGraphMeasurer.measure(packets))
        //println(MemoryMeasurer.measureBytes(packets))
    }

    @Test
    fun testEmgBurstSize() {
        val burst = EmgBurst(System.currentTimeMillis(), 60,
                (0 until 1000).mapTo(mutableListOf()) { listOf(128.0f, 127.0f) })

        //println(ObjectGraphMeasurer.measure(burst))
        //println(MemoryMeasurer.measureBytes(burst))
    }

}