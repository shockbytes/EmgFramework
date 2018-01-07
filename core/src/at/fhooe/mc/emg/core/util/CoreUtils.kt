package at.fhooe.mc.emg.core.util

import java.io.*
import java.math.BigDecimal
import java.math.RoundingMode
import javax.sound.sampled.AudioSystem

object CoreUtils {

    fun roundDouble(value: Double, digits: Int): Double {

        if (value == 0.0 || digits < 0) {
            return 0.00
        }
        return BigDecimal(value).setScale(digits, RoundingMode.HALF_UP).toDouble()
    }

    @Throws(IOException::class)
    fun writeFile(file: File, text: String) {
        BufferedWriter(FileWriter(file)).use { writer -> writer.write(text) }
    }

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

    @Throws(IOException::class)
    fun <T : Serializable> serializeToFile(obj: T?, filename: String): Boolean {
        return if (obj != null) {
            val oos = ObjectOutputStream(FileOutputStream(filename))
            oos.writeObject(obj)
            oos.close()
            true
        } else {
            false
        }
    }

    @Throws(Exception::class)
    fun <T> deserializeFromFile(filename: String): T {

        val ois = ObjectInputStream(FileInputStream(filename))
        val obj = ois.readObject() as T
        ois.close()
        return obj
    }

}
