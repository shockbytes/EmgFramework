package at.fhooe.mc.emg.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Configuration {

	private static Configuration instance;

	private static final String KEY_WRITE_LOG = "write_log";
	private static final String KEY_COPY_TO_SIM = "copy_to_simulation";
	private static final String KEY_SIM_ENDLESS_LOOP = "simulation_endless_loop";
	private static final String KEY_RAVG_WS = "ravg_window_size";
	private static final String KEY_SG_WIDTH = "savitzky_golay_width";

	private static final String CONFIG_PATH = System.getProperty("user.dir") + "/data/config.json";

	private boolean isWriteToLogEnabled;
	private boolean isCopyToSimulationEnabled;
	private boolean isSimEndlessLoopEnabled;
	
	private int runningAverageWindowSize;
	private int savitzkyGolayFilterWidth;

	public static Configuration getInstance() {

		if (instance != null) {
			return instance;
		}
		instance = new Configuration();
		return instance;
	}

	public Configuration() {
		load();
	}

	private void load() {

		File file = new File(CONFIG_PATH);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

			String line;
			StringBuilder buffer = new StringBuilder();
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}

			JsonObject object = new JsonParser().parse(buffer.toString()).getAsJsonObject();
			isWriteToLogEnabled = (object.has(KEY_WRITE_LOG)) && object.get(KEY_WRITE_LOG).getAsBoolean();
			isCopyToSimulationEnabled = (object.has(KEY_COPY_TO_SIM)) && object.get(KEY_COPY_TO_SIM).getAsBoolean();
			isSimEndlessLoopEnabled = (object.has(KEY_SIM_ENDLESS_LOOP)) && object.get(KEY_SIM_ENDLESS_LOOP).getAsBoolean();
			runningAverageWindowSize = (object.has(KEY_RAVG_WS))
					? object.get(KEY_RAVG_WS).getAsInt() : 30;
			savitzkyGolayFilterWidth = (object.has(KEY_SG_WIDTH))
					? object.get(KEY_SG_WIDTH).getAsInt() : 10;

		} catch (Exception e) {
			e.printStackTrace();
			isWriteToLogEnabled = false;
			isCopyToSimulationEnabled = false;
			isSimEndlessLoopEnabled = false;
			runningAverageWindowSize = 30;
			savitzkyGolayFilterWidth = 10;
		}

	}

	public void save() {

		JsonObject object = new JsonObject();
		object.addProperty(KEY_WRITE_LOG, isWriteToLogEnabled);
		object.addProperty(KEY_COPY_TO_SIM, isCopyToSimulationEnabled);
		object.addProperty(KEY_SIM_ENDLESS_LOOP, isSimEndlessLoopEnabled);
		object.addProperty(KEY_RAVG_WS, runningAverageWindowSize);
		object.addProperty(KEY_SG_WIDTH, savitzkyGolayFilterWidth);

		try {
			AppUtils.writeFile(new File(CONFIG_PATH), object.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public boolean isWriteToLogEnabled() {
		return isWriteToLogEnabled;
	}

	public void setWriteToLogEnabled(boolean isWriteToLogEnabled) {
		this.isWriteToLogEnabled = isWriteToLogEnabled;
	}

	public boolean isCopyToSimulationEnabled() {
		return isCopyToSimulationEnabled;
	}

	public void setCopyToSimulationEnabled(boolean isCopyToSimulationEnabled) {
		this.isCopyToSimulationEnabled = isCopyToSimulationEnabled;
	}
	
	public boolean isSimulationEndlessLoopEnabled() {
		return isSimEndlessLoopEnabled;
	}
	
	public void setSimulationEndlessLoopEnabled(boolean isSimEndlessLoopEnabled) {
		this.isSimEndlessLoopEnabled = isSimEndlessLoopEnabled;
	}

	public int getRunningAverageWindowSize() {
		return runningAverageWindowSize;
	}

	public void setRunningAverageWindowSize(int runningAverageWindowSize) {
		this.runningAverageWindowSize = runningAverageWindowSize;
	}

	public int getSavitzkyGolayFilterWidth() {
		return savitzkyGolayFilterWidth;
	}

	public void setSavitzkyGolayFilterWidth(int savitzkyGolayFilterWidth) {
		this.savitzkyGolayFilterWidth = savitzkyGolayFilterWidth;
	}

}
