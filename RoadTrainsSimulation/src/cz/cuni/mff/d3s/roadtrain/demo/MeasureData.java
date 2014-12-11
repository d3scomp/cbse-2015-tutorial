package cz.cuni.mff.d3s.roadtrain.demo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import cz.cuni.mff.d3s.deeco.annotations.processor.AnnotationProcessorException;
import cz.cuni.mff.d3s.roadtrain.demo.environment.VehicleMonitor;

public class MeasureData {
	static final long seed = 42;
	
	static final Random gRandom = new Random(seed);
	static final Random rRandom = new Random(seed);
	
	//static final int[] CRASH_SITES = {1, 2, 3, 5, 10, 15, 20};
	
	
	static class Config {
		static final int[] CRASH_SITES = {20};
		static final int POLICE_PER_CRASH = 1;
		static final int AMBULANCE_PER_CRASH = 2;
		static final int FIRE_PER_CRASH = 2;
		static final int RUNS = 1;
		
		static final String ident = String.format("%d%d%d", POLICE_PER_CRASH, AMBULANCE_PER_CRASH, FIRE_PER_CRASH);
	}
	
	
	public static void main(String[] args) throws Exception {
		for(int sites: Config.CRASH_SITES) {
			
			final File gMessages = new File(String.format("output%s%s%sgroupers-%d", File.separator, Config.ident, File.separator, sites));
			gMessages.getParentFile().mkdirs();
			BufferedWriter gWriter = new BufferedWriter(new FileWriter(gMessages));
			
			final File rMessages = new File(String.format("output%s%s%srandom-%d", File.separator, Config.ident, File.separator, sites));
			rMessages.getParentFile().mkdirs();
			BufferedWriter rWriter = new BufferedWriter(new FileWriter(rMessages));
			
			
			for(int i = 0; i < Config.RUNS; ++i) {
				final int currentI = i;
				
				MessageProbe gProbe = new MessageProbe();
				runSimulationWithGroupers(sites, currentI, gProbe);
				gWriter.write(String.format("%d %d\n", gProbe.getMsgSentMANET(), gProbe.getMsgSentIP()));
				gWriter.flush();
				
				MessageProbe rProbe = new MessageProbe();
				runSimulationWithRandom(sites, currentI, rProbe);
				rWriter.write(String.format("%d %d\n", rProbe.getMsgSentMANET(), rProbe.getMsgSentIP()));
				rWriter.flush();
			}
			
			gWriter.close();
			rWriter.close();
		}
		
	}
	
	private static void runSimulationWithGroupers(int crashes, int series, MessageProbe messageProbe)
			throws AnnotationProcessorException, IOException {
		System.out.println("Launching simulation WITH groupers and " + crashes + " crash sites #" + series);
				
		LauncherWithGroupers launcher = new LauncherWithGroupers(3, gRandom, messageProbe);

		final String ident = String.format("%s%sgroupers-Crash%d-%d", Config.ident, File.separator, crashes, series);

		final String outDir = "output" + File.separator + ident;
		VehicleMonitor vehicleMonitor = new VehicleMonitor(outDir);
		
		DemoDeployer demoDeployer = new EmergencyDemoDeployer(
				crashes,
				Config.AMBULANCE_PER_CRASH,
				Config.FIRE_PER_CRASH,
				Config.POLICE_PER_CRASH,
				(VehicleDeployer) launcher,
				vehicleMonitor);
		
		launcher.run(demoDeployer);
	}
	
	private static void runSimulationWithRandom(int crashes, int series, MessageProbe messageProbe) throws AnnotationProcessorException, IOException {
		System.out.println("Launching simulation WITHOUT groupers and " + crashes + " crash sites #" + series);
				
		LauncherWithRandomIPGossip launcher = new LauncherWithRandomIPGossip(rRandom, messageProbe);

		final String ident = String.format("%s%srandom-Crash%d-%d", Config.ident, File.separator, crashes, series);

		final String outDir = "output" + File.separator + ident;
		VehicleMonitor vehicleMonitor = new VehicleMonitor(outDir);
		
		DemoDeployer demoDeployer = new EmergencyDemoDeployer(
				crashes,
				Config.AMBULANCE_PER_CRASH,
				Config.FIRE_PER_CRASH,
				Config.POLICE_PER_CRASH,
				(VehicleDeployer) launcher,
				vehicleMonitor);
		
		launcher.run(demoDeployer);
	}
}
