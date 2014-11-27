package cz.cuni.mff.d3s.roadtrain.demo;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.utils.geometry.CoordImpl;

import cz.cuni.mff.d3s.deeco.annotations.processor.AnnotationProcessor;
import cz.cuni.mff.d3s.deeco.annotations.processor.AnnotationProcessorException;
import cz.cuni.mff.d3s.deeco.knowledge.CloningKnowledgeManagerFactory;
import cz.cuni.mff.d3s.deeco.model.runtime.api.RuntimeMetadata;
import cz.cuni.mff.d3s.deeco.model.runtime.custom.RuntimeMetadataFactoryExt;
import cz.cuni.mff.d3s.deeco.runtime.RuntimeFramework;
import cz.cuni.mff.d3s.deeco.simulation.DirectKnowledgeDataHandler;
import cz.cuni.mff.d3s.deeco.simulation.DirectSimulationHost;
import cz.cuni.mff.d3s.deeco.simulation.NetworkKnowledgeDataHandler;
import cz.cuni.mff.d3s.deeco.simulation.SimulationRuntimeBuilder;
import cz.cuni.mff.d3s.deeco.simulation.matsim.DefaultMATSimExtractor;
import cz.cuni.mff.d3s.deeco.simulation.matsim.DefaultMATSimUpdater;
import cz.cuni.mff.d3s.deeco.simulation.matsim.JDEECoAgent;
import cz.cuni.mff.d3s.deeco.simulation.matsim.JDEECoAgentSource;
import cz.cuni.mff.d3s.deeco.simulation.matsim.MATSimRouter;
import cz.cuni.mff.d3s.deeco.simulation.matsim.MATSimSimulation;
import cz.cuni.mff.d3s.demo.AlwaysRebroadcastingKnowledgeDataManager;
import cz.cuni.mff.d3s.demo.environment.MATSimDataProviderReceiver;
import cz.cuni.mff.d3s.roadtrain.components.Vehicle;
import cz.cuni.mff.d3s.roadtrain.ensembles.CarPair;
import cz.cuni.mff.d3s.roadtrain.ensembles.SharedDestination;
import cz.cuni.mff.d3s.roadtrain.ensembles.TrainNumbering;
import cz.cuni.mff.d3s.roadtrain.utils.Navigator;

public class Launcher {
	private static JDEECoAgentSource jdeecoAgentSource;
	private static MATSimSimulation simulation;
	private static MATSimRouter router;
	private static MATSimDataProviderReceiver matSimProviderReceiver;
	private static SimulationRuntimeBuilder builder;
	
	private static AnnotationProcessor processor;
		
	private static int vehicleCounter = 0;
	
	private static Random random = new Random(42);
	
	public static void run() throws AnnotationProcessorException, IOException {
		// Setup simulation
		System.out.println("Preparing simulation");
		jdeecoAgentSource = new JDEECoAgentSource();
		NetworkKnowledgeDataHandler networkHandler = new DirectKnowledgeDataHandler();
		matSimProviderReceiver = new MATSimDataProviderReceiver(new LinkedList<String>());
		simulation = new MATSimSimulation(matSimProviderReceiver,
				matSimProviderReceiver, new DefaultMATSimUpdater(),
				new DefaultMATSimExtractor(), networkHandler,
				Arrays.asList(jdeecoAgentSource), Settings.MATSIM_CONFIG);
		router = new MATSimRouter(simulation.getControler(),
				simulation.getTravelTime(), Settings.ROUTE_CALCULATION_OFFSET);
		//matSimProviderReceiver..initialize(simulation, router);
		System.out.println("Creating components");
		builder = new SimulationRuntimeBuilder();
		
		// Setup navigator
		Navigator.init(router);
	
		RuntimeMetadata model = RuntimeMetadataFactoryExt.eINSTANCE
				.createRuntimeMetadata();
		processor = new AnnotationProcessor(
				RuntimeMetadataFactoryExt.eINSTANCE, model, new CloningKnowledgeManagerFactory());
		
		// Deploy components
		System.out.println("Deploying components");
		deployCars();
		
		DirectSimulationHost host = simulation.getHost("Host");
		RuntimeFramework runtime = builder.build(host, simulation, null, model, new AlwaysRebroadcastingKnowledgeDataManager(model.getEnsembleDefinitions(), null), new CloningKnowledgeManagerFactory());
		runtime.start();
		
		System.out.println("Run the simulation");
		// Run the simulation
		simulation.run();
		System.out.println("Simulation Finished");
	}
	
	private static void createAndDeployVehicleComponent(int idx,
			String sourceLinkIdString, String destination)
			throws AnnotationProcessorException {
		String compIdString = "V" + idx;
		Id compId = new IdImpl(compIdString);
		Id sourceLinkId = new IdImpl(sourceLinkIdString);
		
		jdeecoAgentSource.addAgent(new JDEECoAgent(compId, sourceLinkId));

		Vehicle component = new Vehicle(compIdString, destination, sourceLinkId,
				matSimProviderReceiver.getActuatorProvider(compId),
				matSimProviderReceiver.getSensorProvider(compId), router,
				simulation);

		processor.process(component, SharedDestination.class, CarPair.class, TrainNumbering.class);
	}
	
	private static int getCarId() {
		return vehicleCounter++;
	}
	
	private static Link getRandomLink(double x, double y, double radius) {
		double dx = random.nextDouble() * radius;
		double dy = random.nextDouble() * radius;
		return router.findNearestLink(new CoordImpl(x + dx, y + dy));
	}
	
	private static void deployCars() throws AnnotationProcessorException {
		// Deploy group A
		for(int i = 0; i < Settings.GROUP_A_VEHICLE_COUNT; ++i) {
			Coord pos = Navigator.getPosition(Settings.GROUP_A_POS).getCoord();
			createAndDeployVehicleComponent(
					getCarId(),
					getRandomLink(pos.getX(), pos.getY(), Settings.GROUP_A_RADIUS).getId().toString(),
					Settings.GROUP_A_DST);
		}
			
		// Deploy group B
		for(int i = 0; i < Settings.GROUP_B_VEHICLE_COUNT; ++i) {
			Coord pos = Navigator.getPosition(Settings.GROUP_B_POS).getCoord();
			createAndDeployVehicleComponent(
					getCarId(),
					getRandomLink(pos.getX(), pos.getY(), Settings.GROUP_B_RADIUS).getId().toString(),
					Settings.GROUP_B_DST);
		}
	}
}
