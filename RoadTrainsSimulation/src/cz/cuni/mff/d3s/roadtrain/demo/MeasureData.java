package cz.cuni.mff.d3s.roadtrain.demo;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

abstract class BaseConfig {
	
}

class ConfigEmergency extends BaseConfig {
	final int[] CRASH_SITES;
	final int POLICE_PER_CRASH;
	final int AMBULANCE_PER_CRASH;
	final int FIRE_PER_CRASH;
	final int[] RUNS;
	final String[] STRATEGY;
	
	public ConfigEmergency(int[] crashSites, int policePerCrash, int AmbulancePerCrash, int FirePerCrash, int[] runs, String[] strategy) {
		CRASH_SITES = crashSites;
		POLICE_PER_CRASH = policePerCrash;
		AMBULANCE_PER_CRASH = AmbulancePerCrash;
		FIRE_PER_CRASH = FirePerCrash;
		RUNS = runs;
		STRATEGY = strategy;
	}
}

class ConfigMilitary extends BaseConfig {
	final int[] VEHICLES;
	final String[] EVALS;
	final int[] RUNS;
	
	public ConfigMilitary(int[] vehicles, String[] evals, int[] runs) {
		VEHICLES = vehicles;
		EVALS = evals;
		RUNS = runs;
	}
}

public class MeasureData {
	static final int NUM_PROCESSES = 2;
		
	static final BaseConfig[] configs = {
		new ConfigEmergency(new int[]{1, 2, 3, 5, 10, 15, 20}, 1, 1, 1, new int[]{0}, new String[]{"groupers", "random"}),
		new ConfigEmergency(new int[]{1, 2, 3, 5, 10, 15, 20}, 1, 2, 2, new int[]{0}, new String[]{"groupers", "random"}),
		new ConfigMilitary(new int[]{3, 5, 10, 15, 20}, new String[]{"eval", "def"}, new int[]{0})
	};
		
	static final Queue<List<String>> runConfigs = new LinkedList<List<String>>();	
	static final Map<Process, List<String>> running = new HashMap<Process, List<String>>();
		
	public static void main(String[] args) throws Exception {
		for(BaseConfig config: configs) {
			if(config instanceof ConfigEmergency) {
				ConfigEmergency emergency = (ConfigEmergency) config;
				for(int crashes: emergency.CRASH_SITES) {
					for(int i: emergency.RUNS) {
						for(String strategy: emergency.STRATEGY) {
							runEmergency(
									crashes,
									emergency.POLICE_PER_CRASH,
									emergency.AMBULANCE_PER_CRASH,
									emergency.FIRE_PER_CRASH,
									i,
									strategy);
						}
					}
				}
			} else if(config instanceof ConfigMilitary) {
				ConfigMilitary military = (ConfigMilitary) config;
				for(int vehicles: military.VEHICLES) {
					for(int i: military.RUNS) {
						for(String eval: military.EVALS) {
							runMilitary(
									vehicles,
									eval,
									i
							);
						}
					}
				}
			} else {
				throw new Exception("Unknown config type");
			}
		}
		
		// Register process kill on shutdown
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
		    public void run() {
		    	for(Process process: running.keySet()) {
		    		process.destroy();
		    	/*	try {
						Thread.sleep(500);
					} catch (InterruptedException e) {}
		    		if(process.isAlive()) {
		    			process.destroyForcibly();
		    		}*/
		    	}
		    }
		}));
		
		runProcesses();
	}
	
	private static void updateRunningProcess() throws InterruptedException, IOException {
		for(Iterator<Entry<Process, List<String>>> it = running.entrySet().iterator(); it.hasNext(); ) {
			Entry<Process, List<String>> entry = it.next();
			Process process = entry.getKey();
			/*if(!process.isAlive()) {
				it.remove();
				
				// Re-add process if crashed
				if(process.exitValue() != 0) {
					runConfigs.add(entry.getValue());
				}
			}*/
			try {
				int exit = process.exitValue();
				it.remove();
				if(exit != 0) {
					System.out.println("Re-adding failed config: " + connectParams(entry.getValue()));
					runConfigs.add(entry.getValue());
				} else {
					System.out.println("Completed config: " + connectParams(entry.getValue()));
				}
			} catch (Exception e) {}
		}
	}
	
	private static void runEmergency(int crashSites, int police, int ambulance, int fire, int runId, String strategy) throws Exception {
		System.out.println(String.format("Adding emergency simulation with %s: %d police %d ambulance %d fire and %d crashes with run Id %d",
				strategy, police, ambulance, fire, crashSites, runId));

		addRun(new String[]{
				"emergency",
				strategy,
				String.valueOf(police),
				String.valueOf(ambulance),
				String.valueOf(fire),
				String.valueOf(crashSites), 
				String.valueOf(runId)}
		);
	}
	
	private static void runMilitary(int vehicles, String eval, int runId) throws Exception {
		System.out.println(String.format("Adding military simulation with %s: %d vehicles with run Id %d",
				eval, vehicles, runId));
		
		addRun(new String[]{
			"military",
			eval,
			String.valueOf(vehicles),
			String.valueOf(runId)
		});
	}
	
	private static void addRun(String[] args) throws Exception {
		final String java = System.getProperty("java.home") + "/bin/java";
		final String classPath = System.getProperty("java.class.path");
		final String jVMParams = "-Xmx4096m";
		final String className = SimulationRunner.class.getCanonicalName();
		
		List<String> command = new LinkedList<String>();
		command.add(java);
		command.add("-classpath");
		command.add(classPath);
		command.add(jVMParams);
		command.add(className);
		command.addAll(Arrays.asList(args));
		
		runConfigs.add(command);
	}
	
	private static String connectParams(List<String> params) {
		StringBuilder builder = new StringBuilder();
		
		int counter = 0;
		for(String param: params) {
			if(counter >= 5) {
				if(counter == 5) {
					builder.append(param);
				} else {
					builder.append("-");
					builder.append(param);
				}
			}
			counter++;
		}
				
		return builder.toString();
	}
	
	private static void runProcesses() throws Exception {
		while(!runConfigs.isEmpty() || !running.isEmpty()) {
			updateRunningProcess();
			
			if(running.size() <= NUM_PROCESSES) {
				List<String> command = runConfigs.poll();
				
				if(command != null) {				
					final String ident = connectParams(command);
					
					System.out.println("Running config: " + ident);
					
					ProcessBuilder builder = new ProcessBuilder(command);
					File log = new File(String.format(
									"output%slogs%s%d-%s",
									File.separator,
									File.separator,
									System.currentTimeMillis(),
									ident + ".txt"));
					log.getParentFile().mkdirs();
					log.createNewFile();
					
					builder.redirectOutput(log);
					builder.redirectError(log);
								
					Process process = builder.start();
					running.put(process, command);
				}
			}
			
			Thread.sleep(1000);
		}
	}
}
