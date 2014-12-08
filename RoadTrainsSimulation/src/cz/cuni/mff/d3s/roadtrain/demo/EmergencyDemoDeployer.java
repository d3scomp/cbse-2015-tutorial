package cz.cuni.mff.d3s.roadtrain.demo;

import java.util.HashSet;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.basic.v01.IdImpl;

import cz.cuni.mff.d3s.deeco.annotations.processor.AnnotationProcessorException;
import cz.cuni.mff.d3s.roadtrain.demo.components.Vehicle;
import cz.cuni.mff.d3s.roadtrain.demo.utils.Navigator;

public class EmergencyDemoDeployer implements DemoDeployer {
	private int numCrashSites;
	private int numAmbulancePerCrashSite;
	private int numFirePerCrashSite;
	private int numPolicePerCrashSite;
	private int vehicleCounter = 0;
	private VehicleDeployer deployer;
	
	public EmergencyDemoDeployer(int crashSites, int numAmbulance, int numFire, int numPolice, VehicleDeployer deployer) {
		numCrashSites = crashSites;
		numAmbulancePerCrashSite = numAmbulance;
		numFirePerCrashSite = numFire;
		numPolicePerCrashSite = numPolice;
		this.deployer = deployer;
	}
	
	@Override
	public void deploy() throws AnnotationProcessorException {
		for(int i = 0; i < numCrashSites; ++i) {
			deployAccidentSite(i, numPolicePerCrashSite, numAmbulancePerCrashSite, numFirePerCrashSite);
		}
	}
	
	private int getCarId() {
		return vehicleCounter++;
	}
	
	private void deployAccidentSite(int siteNum, int policeCount, int ambulanceCount, int fireCount)
			throws AnnotationProcessorException {
		Link crashSite = Navigator.getRandomLink();
		String crashSiteName = String.format("C%d", siteNum);
		
		Navigator.putLink(crashSiteName, crashSite);
		deployer.addDestination(crashSiteName);
		
		// Deploy police vehicles
		deploySquad("P", crashSiteName, policeCount);
		
		// Deploy ambulance vehicles
		deploySquad("A", crashSiteName, ambulanceCount);
				
		// Deploy fire vehicles
		deploySquad("F", crashSiteName, fireCount);
	}
	
	private void deploySquad(String prefix, String crashSite, int count) throws AnnotationProcessorException {
		// Deploy squad vehicles
		Set<String> places = new HashSet<String>();
		for(int i = 0; i < count; ++i) {
			String start = Navigator.getNearestPlace(prefix, crashSite, places);
			places.add(start);
		}
		
		for(String p: places) {
			String compIdString = "V" + getCarId();
			Id compId = new IdImpl(compIdString);

			Vehicle vehicle = new Vehicle(compIdString, crashSite, Navigator.getPosition(p).getId(),
					deployer.getProviderReceiver().getActuatorProvider(compId),
					deployer.getProviderReceiver().getSensorProvider(compId),
					deployer.getRouter(),
					deployer.getSimulation());
			
			deployer.deployVehicle(vehicle);
		}
	}

	
	
}
