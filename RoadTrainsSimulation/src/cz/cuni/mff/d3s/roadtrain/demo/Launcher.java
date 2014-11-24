package cz.cuni.mff.d3s.roadtrain.demo;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

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
import cz.cuni.mff.d3s.deeco.simulation.SimulationRuntimeBuilder;
import cz.cuni.mff.d3s.deeco.simulation.TimerTaskListener;
import cz.cuni.mff.d3s.deeco.simulation.matsim.DefaultMATSimExtractor;
import cz.cuni.mff.d3s.deeco.simulation.matsim.DefaultMATSimUpdater;
import cz.cuni.mff.d3s.deeco.simulation.matsim.JDEECoAgent;
import cz.cuni.mff.d3s.deeco.simulation.matsim.JDEECoAgentSource;
import cz.cuni.mff.d3s.deeco.simulation.matsim.MATSimOMNetSimulation;
import cz.cuni.mff.d3s.deeco.simulation.matsim.MATSimRouter;
import cz.cuni.mff.d3s.deeco.simulation.omnet.OMNetSimulationHost;
import cz.cuni.mff.d3s.demo.AlwaysRebroadcastingKnowledgeDataManager;
import cz.cuni.mff.d3s.demo.environment.MATSimOMNetDataProviderReceiver;
import cz.cuni.mff.d3s.roadtrain.components.Vehicle;
import cz.cuni.mff.d3s.roadtrain.demo.environment.VehicleMonitor;
import cz.cuni.mff.d3s.roadtrain.ensembles.CarPair;
import cz.cuni.mff.d3s.roadtrain.ensembles.SharedDestination;
import cz.cuni.mff.d3s.roadtrain.ensembles.TrainNumbering;
import cz.cuni.mff.d3s.roadtrain.utils.Navigator;

public class Launcher {
	private static JDEECoAgentSource jdeecoAgentSource;
	private static MATSimOMNetSimulation simulation;
	private static MATSimRouter router;
	private static MATSimOMNetDataProviderReceiver matSimProviderReceiver;
	private static SimulationRuntimeBuilder builder;
	private static StringBuilder omnetConfig = new StringBuilder();
	
	private static int vehicleCounter = 0;
	
	private static Random random = new Random(0);
	
	public static void run() throws AnnotationProcessorException, IOException {
		// Setup simulation
		System.out.println("Preparing simulation");
		jdeecoAgentSource = new JDEECoAgentSource();
		matSimProviderReceiver = new MATSimOMNetDataProviderReceiver(new LinkedList<String>());
		simulation = new MATSimOMNetSimulation(matSimProviderReceiver,
				matSimProviderReceiver, new DefaultMATSimUpdater(),
				new DefaultMATSimExtractor(),
				Arrays.asList(jdeecoAgentSource), Settings.MATSIM_CONFIG);
		router = new MATSimRouter(simulation.getControler(),
				simulation.getTravelTime(), Settings.ROUTE_CALCULATION_OFFSET);
		matSimProviderReceiver.initialize(simulation, router);
		System.out.println("Creating components");
		builder = new SimulationRuntimeBuilder();
		
		// Setup navigator
		Navigator.init(router, Settings.mapDimension);
		
		// Deploy components
		System.out.println("Deploying components");
		deployCars();
		
		// Preparing OMNeT++ configuration
		String confName = "omnetpp" + vehicleCounter;
		String confFile = confName + ".ini";
		Scanner scanner = new Scanner(new File(Settings.OMNET_CONFIG_TEMPLATE));
		String template = scanner.useDelimiter("\\Z").next();
		template = template.replace("<<<configName>>>", confName);
		scanner.close();
		PrintWriter out = new PrintWriter(Files.newOutputStream(
				Paths.get(confFile), StandardOpenOption.CREATE,
				StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING));
		out.println(template);
		out.println();
		out.println(String.format("**.playgroundSizeX = %dm", new Double(Settings.mapDimension).longValue() + 100));
		out.println(String.format("**.playgroundSizeY = %dm", new Double(Settings.mapDimension).longValue() + 100));
		out.println();
		out.println(String.format("**.numNodes = %d", vehicleCounter));
		out.println();
		out.println("**.node[*].appl.packet802154ByteLength = 128B");
		out.println();
		out.println();
		out.println(String.format("sim-time-limit = %ds", simulation.getOMNetSimulationDuration()));
		out.println();
		out.println(omnetConfig.toString());
		out.close();
		

		System.out.println("Run the simulation");
		// Run the simulation
		simulation.run("Cmdenv", confFile);
		System.out.println("Simulation Finished");
		
		// Record simulation visualization
		VehicleMonitor.dump("visual" + File.separator + "time-");
	}
	
	private static void createAndDeployVehicleComponent(int idx,
			String sourceLinkIdString, String destination, StringBuilder omnetConfig)
			throws AnnotationProcessorException {
		String compIdString = "V" + idx;
		Id compId = new IdImpl(compIdString);
		Id sourceLinkId = new IdImpl(sourceLinkIdString);
		
		jdeecoAgentSource.addAgent(new JDEECoAgent(compId, sourceLinkId));

		Vehicle component = new Vehicle(compIdString, destination, sourceLinkId,
				matSimProviderReceiver.getActuatorProvider(compId),
				matSimProviderReceiver.getSensorProvider(compId), router,
				simulation);

		RuntimeMetadata model = RuntimeMetadataFactoryExt.eINSTANCE
				.createRuntimeMetadata();
		AnnotationProcessor processor = new AnnotationProcessor(
				RuntimeMetadataFactoryExt.eINSTANCE, model, new CloningKnowledgeManagerFactory());
		processor.process(component, SharedDestination.class, CarPair.class, TrainNumbering.class);
		
		Coord currentPosition = router.getLink(sourceLinkId).getCoord();
		omnetConfig.append(String.format("**.node[%d].mobility.initialX = %dm %n", idx, new Double(currentPosition.getX()).longValue()));			
		omnetConfig.append(String.format("**.node[%d].mobility.initialY = %dm %n", idx, new Double(currentPosition.getY()).longValue()));
		omnetConfig.append(String.format("**.node[%d].mobility.initialZ = 0m %n", idx));
		omnetConfig.append(String.format("**.node[%d].appl.id = \"%s\" %n%n", idx, component.id));
		
		Collection<? extends TimerTaskListener> listeners = null;
		if (idx == 0) {
			listeners = Arrays.asList(simulation);
		}
		
		OMNetSimulationHost host = simulation.getHost(compIdString, "node["+idx+"]");
		RuntimeFramework runtime = builder.build(host, simulation, listeners, model, new AlwaysRebroadcastingKnowledgeDataManager(model.getEnsembleDefinitions(), null), new CloningKnowledgeManagerFactory());
		runtime.start();
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
					getRandomLink(pos.getX(), pos.getY(), Settings.GROUP_A_RADIUS * Settings.mapDimension).getId().toString(),
					Settings.GROUP_A_DST,
					omnetConfig);
		}
			
		// Deploy group B
		for(int i = 0; i < Settings.GROUP_B_VEHICLE_COUNT; ++i) {
			Coord pos = Navigator.getPosition(Settings.GROUP_B_POS).getCoord();
			createAndDeployVehicleComponent(
					getCarId(),
					getRandomLink(pos.getX(), pos.getY(), Settings.GROUP_B_RADIUS * Settings.mapDimension).getId().toString(),
					Settings.GROUP_B_DST,
					omnetConfig);
		}
	}
}
