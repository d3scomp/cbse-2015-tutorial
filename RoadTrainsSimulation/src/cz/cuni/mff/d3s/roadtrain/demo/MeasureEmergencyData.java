package cz.cuni.mff.d3s.roadtrain.demo;

import java.util.*;


public class MeasureEmergencyData {
	static final int NUM_PROCESSES = 2;
	
	static class Config {
		static final int[] CRASH_SITES = {1/*, 2, 3, 5, 10, 15, 20*/};
		static final int POLICE_PER_CRASH = 0;
		static final int AMBULANCE_PER_CRASH = 0;
		static final int FIRE_PER_CRASH = 1;
		static final int[] RUNS = {0};
	}
	
	static Collection<Process> liveProcesses = new HashSet<Process>();
	
	
	public static void main(String[] args) throws Exception {
		for(int crashes: Config.CRASH_SITES) {
			for(int i: Config.RUNS) {
				// Run groupers
				run(crashes, Config.POLICE_PER_CRASH, Config.AMBULANCE_PER_CRASH, Config.FIRE_PER_CRASH, i, true);
				
				// Run random
				run(crashes, Config.POLICE_PER_CRASH, Config.AMBULANCE_PER_CRASH, Config.FIRE_PER_CRASH, i, false);
			}
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
