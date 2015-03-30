package cz.cuni.mff.d3s.roadtrain.demo;

import java.security.KeyStoreException;

import cz.cuni.mff.d3s.deeco.annotations.processor.AnnotationProcessorException;
import cz.cuni.mff.d3s.deeco.simulation.matsim.MATSimRouter;
import cz.cuni.mff.d3s.deeco.simulation.matsim.MATSimSimulation;
import cz.cuni.mff.d3s.roadtrain.demo.components.AbstractVehicle;
import cz.cuni.mff.d3s.roadtrain.demo.environment.MATSimDataProviderReceiver;

public interface VehicleDeployer {
	public void deployVehicle(AbstractVehicle component) throws AnnotationProcessorException, KeyStoreException;
	public MATSimSimulation getSimulation();
	public MATSimRouter getRouter();
	public MATSimDataProviderReceiver getProviderReceiver();
	public void addDestination(String destination);
}
