package at.fhooe.mc.emg.util;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class AppUtils {

    public static double roundDouble(double value, int digits) {

        if (value == 0) {
            return 0.00;
        }

        if (digits < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(digits, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
    
    public static void writeFile(File file, String text) throws IOException {
    	
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			writer.write(text);
		}
    }

    public static void playSound(File file) {

        new Thread(() -> {
            try {
                Clip clip = AudioSystem.getClip();
                AudioInputStream inputStream = AudioSystem.getAudioInputStream(file);
                clip.open(inputStream);
                clip.start();
            } catch (Exception e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    public static <T extends Serializable> void serializeToFile(T obj, String filename) throws IOException {

        // To avoid null pointers
        if (obj == null || filename == null) {
            return;
        }

        // Create output streams
        FileOutputStream fos = new FileOutputStream(filename);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        // Write object
        oos.writeObject(obj);
        // Close streams
        oos.close();
        fos.close();
    }

    public static <T> T deserializeFromFile(String filename) throws Exception{

        if (filename == null) {
            return null;
        }

        // Create output streams
        FileInputStream fis = new FileInputStream(filename);
        ObjectInputStream ois = new ObjectInputStream(fis);
        // Write object
        T obj = (T) ois.readObject();
        // Close streams
        ois.close();
        fis.close();

        return obj;
    }

}
