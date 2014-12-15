package cz.cuni.mff.d3s.roadtrain.demo;

import java.io.*;
import java.util.*;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import cz.cuni.mff.d3s.roadtrain.demo.custom.RealisticKnowledgeDataHandler;
import cz.cuni.mff.d3s.roadtrain.demo.environment.VehicleMonitor;

public class SimulationRunner {
	static final long baseSeed = 42;
	
	static final int GROUPER_COUNT = 3;
	
	static final String OUTPUT = "output";
				
	/*
	 * usage: 
	 * 
	 * emergency groupers PolicePerCrash AmbulancePerCrash FirePerCrash Crashes RunNum
	 * 
	 * or
	 * 
	 * military eval Vehicles RunNum
	 * 
	 */	
	@SuppressWarnings("static-access")
	public static void main(String[] args) throws Exception {
		// Get sim scenario
		Config conf = null;
		if(args[0].equals("emergency")) {
			EmergencyConfig emergency = new EmergencyConfig();
			emergency.USE_GROUPERS = args[1].equals("groupers");
			emergency.POLICE_PER_CRASH = Integer.valueOf(args[2]);
			emergency.AMBULANCE_PER_CRASH = Integer.valueOf(args[3]);
			emergency.FIRE_PER_CRASH = Integer.valueOf(args[4]);
			emergency.CRASH_SITES = Integer.valueOf(args[5]);
			emergency.RUN = Integer.valueOf(args[6]);
			conf = emergency;
		} else if(args[0].equals("military")) {
			MilitaryConfig military = new MilitaryConfig();
			military.EVAL_ENSEMBLE = args[1].equals("eval");
			military.VEHICLES = Integer.valueOf(args[2]);
			military.RUN = Integer.valueOf(args[3]);
			conf = military;
		} else {
			throw new NotImplementedException();
		}
		
		// Setup random
		Random random = new Random(baseSeed + conf.RUN);
		
		// Prepare probe
		MessageProbe probe = new MessageProbe();
		
		// Create launcher
		Object launcher = null;
		if(conf instanceof MilitaryConfig || (conf instanceof EmergencyConfig && ((EmergencyConfig) conf).USE_GROUPERS)) {
			launcher = new LauncherWithGroupers(GROUPER_COUNT, random, probe);
		} else {
			launcher = new LauncherWithRandomIPGossip(random, probe);
		}
		
		// Prepare vehicle monitor
		VehicleMonitor monitor = new VehicleMonitor(
				String.format("%s%s%s%s%s", OUTPUT, File.separator, conf.getDir(), File.separator, conf.getIdent()));
		
		// Deploy demo
		DemoDeployer demo = null;
		if(conf instanceof MilitaryConfig) {
			MilitaryConfig military = (MilitaryConfig) conf;
			demo = new MilitaryTrainDemoDeployer(
					military.NUM_TRAINS,
					military.VEHICLES,
					(VehicleDeployer) launcher,
					monitor);
			
			// Setup faked ensemble evaluation
			if(military.EVAL_ENSEMBLE) {
				RealisticKnowledgeDataHandler.OPTIMIZE_DESTINATION_ENSEMBLE = true;
			}
		}
		if(conf instanceof EmergencyConfig) {
			EmergencyConfig emergency = (EmergencyConfig) conf;
			demo = new EmergencyDemoDeployer(
					emergency.CRASH_SITES,
					emergency.AMBULANCE_PER_CRASH,
					emergency.FIRE_PER_CRASH,
					emergency.POLICE_PER_CRASH,
					(VehicleDeployer) launcher,
					monitor);
		}
		
		// Deploy and run simulation
		((Launcher) launcher).run(demo);
		
		// Store results in shared results
		final String fileName = OUTPUT + File.separator + conf.getDir() + File.separator + conf.getResult();
		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));
		try {
			writer.println(String.format("%d %d", probe.getMsgSentMANET(), probe.getMsgSentIP()));
		} catch (Exception e) {
			System.out.println("Failed to write results to file:" + conf.getResult() + " " + probe.report());
		} finally{
			writer.close();
		}
		
		// Store results in independent file
		final String fileNameInd = OUTPUT + File.separator + conf.getDir() + File.separator + conf.getIdent() + File.separator + "results.txt";
		PrintWriter writerInd = new PrintWriter(new BufferedWriter(new FileWriter(fileNameInd)));
		writerInd.println(String.format("%d %d", probe.getMsgSentMANET(), probe.getMsgSentIP()));
		writerInd.close();
	}
}

abstract class Config {
	public abstract String getIdent();
	public abstract String getResult();
	public abstract String getDir();
	public static final int NUM_TRAINS = 1;
	public int RUN;
}

class MilitaryConfig extends Config {
	public int VEHICLES;
	public boolean EVAL_ENSEMBLE;
	
	@Override
	public String getIdent() {
		return String.format("%s-num%d-%d", EVAL_ENSEMBLE?"eval":"def", VEHICLES, RUN);
	}

	@Override
	public String getDir() {
		return "Military";
	}

	@Override
	public String getResult() {
		return String.format("%s-%d", EVAL_ENSEMBLE?"eval":"def", VEHICLES);
	}
}

class EmergencyConfig extends Config {
	public boolean USE_GROUPERS;
	public int CRASH_SITES;
	public int POLICE_PER_CRASH;
	public int AMBULANCE_PER_CRASH;
	public int FIRE_PER_CRASH;
	
	@Override
	public String getIdent() {
		return String.format("%s-Crash%d-%d", USE_GROUPERS?"groupers":"random", CRASH_SITES, RUN);
	}

	@Override
	public String getDir() {
		return String.format("%d%d%d", POLICE_PER_CRASH, AMBULANCE_PER_CRASH, FIRE_PER_CRASH);
	}

	@Override
	public String getResult() {
		return String.format("%s-%d", USE_GROUPERS?"groupers":"random", CRASH_SITES);
	}
}
