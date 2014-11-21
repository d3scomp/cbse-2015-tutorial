package cz.cuni.mff.d3s.demo;

import java.io.IOException;
import java.util.LinkedList;

import cz.cuni.mff.d3s.deeco.annotations.processor.AnnotationProcessorException;

public class SimpleLauncher {

	public static void main(String[] args) throws IOException, AnnotationProcessorException {
		// TODO Auto-generated method stub
	//	modifier = args[0] + "-" + density + "-full";
		Settings.MATSIM_CONFIG = Settings.MATSIM_INPUT + "/" + "config" + ".xml";
		MATSimConfigFileGenerator.getInstance().generateConfig(Settings.MATSIM_CONFIG_TEMPLATE, Settings.FULL_SIMULATION_DURATION, Settings.MATSIM_CONFIG, Settings.MATSIM_OUTPUT);
		DistributedSimulationWithOMNetLauncher.run(new LinkedList<String>());
	//	sb.append(modifier + ": " + MaxParkingTimeCollector.getInstance().getMaxTime() + ", " + MaxParkingTimeCollector.getInstance().allParked(Settings.VEHICLE_COUNT) + "\n");
		MaxParkingTimeCollector.getInstance().clear();
		
	}

}
