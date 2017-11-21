package at.fhooe.mc.emg.util

import org.jtransforms.fft.DoubleFFT_1D

object Analysis {

    fun fft(input: DoubleArray): DoubleArray {

        // TODO Class not found exception
        val fftDo = DoubleFFT_1D(input.size.toLong())
        val fft = DoubleArray(input.size * 2)
        System.arraycopy(input, 0, fft, 0, input.size)
        fftDo.realForwardFull(fft)
        return fft
    }

}
