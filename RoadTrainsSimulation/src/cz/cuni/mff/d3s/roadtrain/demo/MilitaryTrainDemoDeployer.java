package cz.cuni.mff.d3s.roadtrain.demo;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.basic.v01.IdImpl;

import cz.cuni.mff.d3s.deeco.annotations.processor.AnnotationProcessorException;
import cz.cuni.mff.d3s.roadtrain.demo.components.Vehicle;
import cz.cuni.mff.d3s.roadtrain.demo.environment.VehicleMonitor;
import cz.cuni.mff.d3s.roadtrain.demo.utils.Navigator;

public class MilitaryTrainDemoDeployer implements DemoDeployer {
	private final int numTrains;
	private final int vehiclesPerTrain;
	private int vehicleCounter = 0;
	private final VehicleDeployer deployer;
	private final VehicleMonitor vehicleMonitor;
	
	public MilitaryTrainDemoDeployer(int numTrains, int vehiclesPerTrain, VehicleDeployer deployer,
			VehicleMonitor vehicleMonitor) {
		this.numTrains = numTrains;
		this.vehiclesPerTrain = vehiclesPerTrain;
		this.deployer = deployer;
		this.vehicleMonitor = vehicleMonitor;
	}
	
	@Override
	public void deploy() throws AnnotationProcessorException {
		for(int i = 0; i < numTrains; ++i) {
			deployTrain(i);
		}
	}
	
	private int getCarId() {
		return vehicleCounter++;
	}
	
	private void deployTrain(int i) throws AnnotationProcessorException {
		Navigator.getRandomLink();
		
		final String start = String.format("T%dStart", i); 
		final String dest = String.format("T%dDest", i);
		final Link startLink = Navigator.getRandomLinkFixedX(Settings.MIN_X);
				
		Navigator.putLink(start, startLink);
		Navigator.putLink(dest, Navigator.getRandomLinkFixedX(Settings.MAX_X));
		
		deployer.addDestination(dest);
		
		for(int j = 0; j < vehiclesPerTrain; ++j) {
			String compIdString = String.format("V%d", getCarId());
			Id compId = new IdImpl(compIdString);
			Vehicle vehicle = new Vehicle(compIdString, dest, Navigator.getRandomLinkAroud(startLink, Settings.WIDTH * 0.05).getId(),
					deployer.getProviderReceiver().getActuatorProvider(compId),
					deployer.getProviderReceiver().getSensorProvider(compId),
					deployer.getRouter(),
					deployer.getSimulation(),
					vehicleMonitor);
			
			deployer.deployVehicle(vehicle);
		}
		
	}
}
