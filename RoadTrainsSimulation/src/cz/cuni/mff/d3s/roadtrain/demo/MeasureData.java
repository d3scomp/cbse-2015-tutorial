package cz.cuni.mff.d3s.roadtrain.demo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import cz.cuni.mff.d3s.deeco.annotations.processor.AnnotationProcessorException;
import cz.cuni.mff.d3s.roadtrain.demo.environment.VehicleMonitor;

public class MeasureData {
	static final Random random = new Random(42);
	
	//static final int[] CRASH_SITES = {1, 2, 3, 5, 10, 15, 20};
	static final int[] CRASH_SITES = {1};
	
	static final int RUNS = 5;
	
	public static void main(String[] args) throws Exception {
		for(int sites: CRASH_SITES) {
			
			final File gMessages = new File("output" + File.separator + "groupers-" + sites);
			gMessages.getParentFile().mkdirs();
			BufferedWriter gWriter = new BufferedWriter(new FileWriter(gMessages));
			
			final File rMessages = new File("output" + File.separator + "random-" + sites);
			rMessages.getParentFile().mkdirs();
			BufferedWriter rWriter = new BufferedWriter(new FileWriter(rMessages));
			
			
			for(int i = 0; i < RUNS; ++i) {
				final int currentI = i;
				
				Thread gThread = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							MessageProbe gProbe = new MessageProbe();
							runSimulationWithGroupers(sites, currentI, gProbe);
							gWriter.write(String.format("%d %d\n", gProbe.getMsgSentMANET(), gProbe.getMsgSentIP()));
							gWriter.flush();
						} catch(Exception e) {
							System.out.println("Measurement endded with exception !!!!!");
						}
					}
				});
				
				Thread rThread = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							MessageProbe rProbe = new MessageProbe();
							runSimulationWithRandom(sites, currentI, rProbe);
							rWriter.write(String.format("%d %d\n", rProbe.getMsgSentMANET(), rProbe.getMsgSentIP()));
							rWriter.flush();
						} catch(Exception e) {
							System.out.println("Measurement endded with exception !!!!!");
						}
					}
				});
				
				gThread.start();
				rThread.start();
				
				gThread.join();
				rThread.join();
			}
			
			gWriter.close();
			rWriter.close();
		}
		
	}
	
	private static void runSimulationWithGroupers(int crashes, int series, MessageProbe messageProbe) throws AnnotationProcessorException, IOException {
		System.out.println("Launching simulation WITH groupers and " + crashes + " crash sites #" + series);
				
		LauncherWithGroupers launcher = new LauncherWithGroupers(3, random, messageProbe);

		final String ident = String.format("groupers-Crash%d-%d", crashes, series);

		final String outDir = "output" + File.separator + ident;
		VehicleMonitor vehicleMonitor = new VehicleMonitor(outDir, ((VehicleDeployer)launcher).getNavigator());
		
		DemoDeployer demoDeployer = new EmergencyDemoDeployer(
				crashes,
				Settings.AMBULANCE_PER_CRASH,
				Settings.FIRE_PER_CRASH,
				Settings.POLICE_PER_CRASH,
				(VehicleDeployer) launcher,
				vehicleMonitor);
		
		launcher.run(demoDeployer);
	}
	
	private static void runSimulationWithRandom(int crashes, int series, MessageProbe messageProbe) throws AnnotationProcessorException, IOException {
		System.out.println("Launching simulation WITHOUT groupers and " + crashes + " crash sites #" + series);
				
		LauncherWithRandomIPGossip launcher = new LauncherWithRandomIPGossip(random, messageProbe);

		final String ident = String.format("random-Crash%d-%d", crashes, series);

		final String outDir = "output" + File.separator + ident;
		VehicleMonitor vehicleMonitor = new VehicleMonitor(outDir, ((VehicleDeployer)launcher).getNavigator());
		
		DemoDeployer demoDeployer = new EmergencyDemoDeployer(
				crashes,
				Settings.AMBULANCE_PER_CRASH,
				Settings.FIRE_PER_CRASH,
				Settings.POLICE_PER_CRASH,
				(VehicleDeployer) launcher,
				vehicleMonitor);
		
		launcher.run(demoDeployer);
	}
}
