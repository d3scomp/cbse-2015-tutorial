package cz.cuni.mff.d3s.roadtrain.demo;

import java.util.HashSet;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.basic.v01.IdImpl;

import cz.cuni.mff.d3s.deeco.annotations.processor.AnnotationProcessorException;
import cz.cuni.mff.d3s.roadtrain.demo.components.Vehicle;
import cz.cuni.mff.d3s.roadtrain.demo.environment.VehicleMonitor;

public class EmergencyDemoDeployer implements DemoDeployer {
	private int numCrashSites;
	private int numAmbulancePerCrashSite;
	private int numFirePerCrashSite;
	private int numPolicePerCrashSite;
	private int vehicleCounter = 0;
	private VehicleDeployer deployer;
	private VehicleMonitor vehicleMonitor;
	
	public EmergencyDemoDeployer(int crashSites, int numAmbulance, int numFire, int numPolice, VehicleDeployer deployer,
			VehicleMonitor vehicleMonitor) {
		numCrashSites = crashSites;
		numAmbulancePerCrashSite = numAmbulance;
		numFirePerCrashSite = numFire;
		numPolicePerCrashSite = numPolice;
		this.deployer = deployer;
		this.vehicleMonitor = vehicleMonitor;
	}
	
	@Override
	public void deploy() throws AnnotationProcessorException {
		for(int i = 0; i < numCrashSites; ++i) {
			deployAccidentSite(i);
		}
	}
	
	private int getCarId() {
		return vehicleCounter++;
	}
	
	private void deployAccidentSite(int siteNum)
			throws AnnotationProcessorException {
		Link crashSite = deployer.getNavigator().getRandomLink();
		String crashSiteName = String.format("C%d", siteNum);
		
		deployer.getNavigator().putLink(crashSiteName, crashSite);
		deployer.addDestination(crashSiteName);
		
		// Deploy police vehicles
		deploySquad("P", crashSiteName, numPolicePerCrashSite);
		
		// Deploy ambulance vehicles
		deploySquad("A", crashSiteName, numAmbulancePerCrashSite);
				
		// Deploy fire vehicles
		deploySquad("F", crashSiteName, numFirePerCrashSite);
	}
	
	private void deploySquad(String prefix, String crashSite, int count) throws AnnotationProcessorException {
		// Deploy squad vehicles
		Set<String> places = new HashSet<String>();
		for(int i = 0; i < count; ++i) {
			String start = deployer.getNavigator().getNearestPlace(prefix, crashSite, places);
			places.add(start);
		}
		
		for(String p: places) {
			String compIdString = "V" + getCarId();
			Id compId = new IdImpl(compIdString);

			Vehicle vehicle = new Vehicle(compIdString, crashSite, deployer.getNavigator().getPosition(p).getId(),
					deployer.getProviderReceiver().getActuatorProvider(compId),
					deployer.getProviderReceiver().getSensorProvider(compId),
					deployer.getRouter(),
					deployer.getSimulation(),
					vehicleMonitor,
					deployer.getNavigator());
			
			deployer.deployVehicle(vehicle);
		}
	}	
}