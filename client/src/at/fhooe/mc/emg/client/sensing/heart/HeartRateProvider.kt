package at.fhooe.mc.emg.client.sensing.heart

interface HeartRateProvider {

    fun provideHeartRate(): Int

}