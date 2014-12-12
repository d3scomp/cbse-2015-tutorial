package cz.cuni.mff.d3s.roadtrain.demo;

import java.io.File;
import java.util.Random;

import cz.cuni.mff.d3s.roadtrain.demo.environment.VehicleMonitor;

public class Demo {
	public static void main(String[] args) throws Exception {
		System.out.println("Launching default simulation");
				
		if(args.length < 2) {
			throw new Exception("Please provide gossip strategy as first param \"groupers\" or \"random\" and number of crash sites as second parameter \"crashes=10\"");
		}
		
		// Set random generator
		Random random = new Random(42);
		
		MessageProbe messageProbe = new MessageProbe();
		
		// Determine whenever to use groupers
		Object launcher = null;
		switch(args[0]) {
		case "groupers":
			System.out.println("Running simulation with groupers");
			launcher = new LauncherWithGroupers(3, random, messageProbe);
			break;
		case "random":
			System.out.println("Running simulation with random gossip");
			launcher = new LauncherWithRandomIPGossip(random, messageProbe);
			break;
		}
		
		// Determine crash site count
		int crashes = Integer.parseInt(args[1].replaceFirst("crashes=", ""));
		System.out.println(String.format("Running simulation with %d crash sites", crashes));
		
		// Determine output folder
		String outDir = "output" + File.separator;
		if(args.length >= 3) {
			outDir += args[2];
		} else {
			outDir += "default";
		}
		VehicleMonitor vehicleMonitor = new VehicleMonitor(outDir);
		
	/*	DemoDeployer demoDeployer = new EmergencyDemoDeployer(
				crashes,
				Settings.AMBULANCE_PER_CRASH,
				Settings.FIRE_PER_CRASH,
				Settings.POLICE_PER_CRASH,
				(VehicleDeployer) launcher,
				vehicleMonitor);*/
		
		DemoDeployer demoDeployer = new MilitaryTrainDemoDeployer(1, 10, (VehicleDeployer)launcher, vehicleMonitor);
		
		((Launcher) launcher).run(demoDeployer);
	}
}
