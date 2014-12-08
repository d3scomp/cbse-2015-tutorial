package cz.cuni.mff.d3s.roadtrain.demo;

import cz.cuni.mff.d3s.deeco.annotations.processor.AnnotationProcessorException;
import cz.cuni.mff.d3s.deeco.simulation.matsim.MATSimRouter;
import cz.cuni.mff.d3s.deeco.simulation.matsim.MATSimSimulation;
import cz.cuni.mff.d3s.roadtrain.demo.components.Vehicle;
import cz.cuni.mff.d3s.roadtrain.demo.environment.MATSimDataProviderReceiver;

public interface VehicleDeployer {
	public void deployVehicle(Vehicle component) throws AnnotationProcessorException;
	public MATSimSimulation getSimulation();
	public MATSimRouter getRouter();
	public MATSimDataProviderReceiver getProviderReceiver();
	public void addDestination(String destination);
}
