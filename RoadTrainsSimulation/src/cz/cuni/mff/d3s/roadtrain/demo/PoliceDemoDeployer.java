package cz.cuni.mff.d3s.roadtrain.demo;

import java.security.KeyStoreException;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.basic.v01.IdImpl;

import cz.cuni.mff.d3s.deeco.annotations.processor.AnnotationProcessorException;
import cz.cuni.mff.d3s.roadtrain.demo.components.PoliceVehicle;
import cz.cuni.mff.d3s.roadtrain.demo.components.Vehicle;
import cz.cuni.mff.d3s.roadtrain.demo.environment.VehicleMonitor;
import cz.cuni.mff.d3s.roadtrain.demo.utils.Navigator;

public class PoliceDemoDeployer implements DemoDeployer {

	private int numberOfPolice = 0;
	private int numberOfOrdinary = 0;
	private int vehicleCounter = 0;
	private VehicleDeployer deployer;
	private VehicleMonitor vehicleMonitor;
	
	public PoliceDemoDeployer(int numberOfPolice,
			int numberOfOrdinary, VehicleDeployer launcher,
			VehicleMonitor monitor) {
		this.deployer = launcher;
		this.vehicleMonitor = monitor;
		this.numberOfPolice = numberOfPolice;
		this.numberOfOrdinary = numberOfOrdinary;
	}

	@Override
	public void deploy() throws AnnotationProcessorException, KeyStoreException {
		for (int i = 0; i < numberOfPolice; i++) {
			deployPolice(i + 5);
		}
		for (int i = 0; i < numberOfOrdinary; i++) {
			deployOrdinaryVehicle(i + 5);
		}
	}
	
	private void deployOrdinaryVehicle(int i) throws KeyStoreException, AnnotationProcessorException {
		int carId = getCarId();
 
		String policeStationName = Navigator.getMatchingPlaces("P").get(i);
		Link policeStationLink = Navigator.getPosition(policeStationName);
		
		Link randomDestinationLink = Navigator.getRandomLinkAroud(policeStationLink,  Settings.WIDTH * 0.1);
		String randomDestinationName = String.format("Dest%d", carId);		
		Navigator.putLink(randomDestinationName, randomDestinationLink);
		deployer.addDestination(randomDestinationName);
		
		Link randomPositionLink = Navigator.getRandomLinkAroud(policeStationLink,  Settings.WIDTH * 0.1);
		String randomPositionName = String.format("Start%d", carId);
		Navigator.putLink(randomPositionName, randomPositionLink);
		
		String compIdString = "V" + carId;
		Id compId = new IdImpl(compIdString);

		Vehicle vehicle = new Vehicle(compIdString, randomDestinationName, Navigator.getPosition(randomPositionName).getId(),
				deployer.getProviderReceiver().getActuatorProvider(compId),
				deployer.getProviderReceiver().getSensorProvider(compId),
				deployer.getRouter(),
				deployer.getSimulation(),
				vehicleMonitor);
		vehicle.ownerName = "Owner" + i;
		
		deployer.deployVehicle(vehicle);		
	}

	private void deployPolice(int i) throws KeyStoreException, AnnotationProcessorException {
		int carId = getCarId();
		
		String destinationName = Navigator.getMatchingPlaces("P").get(i);
		Link destinationLink = Navigator.getPosition(destinationName);
		
		Link randomPositionLink = Navigator.getRandomLinkAroud(destinationLink,  Settings.WIDTH * 0.1);
		String randomPositionName = String.format("Start%d", carId);
		Navigator.putLink(randomPositionName, randomPositionLink);
		
		String compIdString = "Police" + carId;
		Id compId = new IdImpl(compIdString);
			
		PoliceVehicle vehicle = new PoliceVehicle(compIdString, destinationName, Navigator.getPosition(randomPositionName).getId(),
				deployer.getProviderReceiver().getActuatorProvider(compId),
				deployer.getProviderReceiver().getSensorProvider(compId),
				deployer.getRouter(),
				deployer.getSimulation(),
				vehicleMonitor, 
				"Owner" + i);
		
		deployer.deployVehicle(vehicle);			
	}

	private int getCarId() {
		return vehicleCounter++;
	}

	
}
