package at.fhooe.mc.emg.client.simulation;

public class SimulationSource {

	private String name;
	private String path;
	
	SimulationSource(String name, String path) {
		this.name = name;
		this.path = path;
	}
	
	public String getName() {
		return name;
	}
	
	String getFilepath() {
		return path;
	}
	
}
