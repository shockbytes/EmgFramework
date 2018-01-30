package at.fhooe.mc.emg.desktop.util

import java.io.File
import javax.sound.sampled.AudioSystem

object DesktopUtils {

    @JvmStatic
    fun playSound(file: File) {

        Thread {
            try {
                val clip = AudioSystem.getClip()
                val inputStream = AudioSystem.getAudioInputStream(file)
                clip.open(inputStream)
                clip.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

}