package cz.cuni.mff.d3s.roadtrain.demo;

import java.util.*;

class ConfigBase {
	final int[] CRASH_SITES;
	final int POLICE_PER_CRASH;
	final int AMBULANCE_PER_CRASH;
	final int FIRE_PER_CRASH;
	final int[] RUNS;
	
	public ConfigBase(int[] crashSites, int policePerCrash, int AmbulancePerCrash, int FirePerCrash, int[] runs) {
		CRASH_SITES = crashSites;
		POLICE_PER_CRASH = policePerCrash;
		AMBULANCE_PER_CRASH = AmbulancePerCrash;
		FIRE_PER_CRASH = FirePerCrash;
		RUNS = runs;
	}
}

public class MeasureEmergencyData {
	static final int NUM_PROCESSES = 3;
		
	static final ConfigBase[] configs = {
		new ConfigBase(new int[]{1, 2, 3, 5, 10, 15, 20}, 1, 1, 1, new int[]{0}),
		new ConfigBase(new int[]{1, 2, 3, 5, 10, 15, 20}, 1, 2, 2, new int[]{0}),
	};
	
	static Collection<Process> liveProcesses = new HashSet<Process>();
	
	
	public static void main(String[] args) throws Exception {
		for(ConfigBase config: configs) {
			for(int crashes: config.CRASH_SITES) {
				for(int i: config.RUNS) {
					// Run groupers
					run(crashes, config.POLICE_PER_CRASH, config.AMBULANCE_PER_CRASH, config.FIRE_PER_CRASH, i, true);
					
					// Run random
					run(crashes, config.POLICE_PER_CRASH, config.AMBULANCE_PER_CRASH, config.FIRE_PER_CRASH, i, false);
				}
			}
		}
		
		for(Process process: liveProcesses) {
			process.waitFor();
		}
	}
	
	private static void waitForProcess() throws InterruptedException {
		while(liveProcesses.size() >= NUM_PROCESSES) {
			for(Iterator<Process> it = liveProcesses.iterator(); it.hasNext(); ) {
				Process process = it.next();
				if(!process.isAlive())
					it.remove();
			}
			
			Thread.sleep(5000);
		}	
	}
	
	private static void run(int crashSites, int police, int ambulance, int fire, int runId, boolean groupers) throws Exception {
		waitForProcess();
		
		System.out.println(String.format("Running emergency simulation with %s: %d police %d ambulance %d fire and %d crash-sites with run Id %d",
				groupers?"groupers":"gossip", police, ambulance, fire, crashSites, runId));
		final String java = System.getProperty("java.home") + "/bin/java";
		final String classPath = System.getProperty("java.class.path");
		final String className = SimulationRunner.class.getCanonicalName();
		
		ProcessBuilder builder = new ProcessBuilder(
				java,
				"-classpath", classPath,
				className,
				"emergency", groupers?"groupers":"gossip",
				String.valueOf(police),
				String.valueOf(ambulance),
				String.valueOf(fire),
				String.valueOf(crashSites), 
				String.valueOf(runId));
		
		builder.inheritIO();		
		
		Process process = builder.start();
		liveProcesses.add(process);
	}
}
