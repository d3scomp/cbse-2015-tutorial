package cz.cuni.mff.d3s.roadtrain.demo;

import java.io.IOException;
import java.util.*;

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
import cz.cuni.mff.d3s.deeco.simulation.NetworkDataHandler;
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
import cz.cuni.mff.d3s.roadtrain.ensembles.LeaderFollower;
import cz.cuni.mff.d3s.roadtrain.ensembles.SharedDestination;
import cz.cuni.mff.d3s.roadtrain.ensembles.Train;
import cz.cuni.mff.d3s.roadtrain.ensembles.TrainLeaderFollower;
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
		NetworkDataHandler networkHandler = new DirectKnowledgeDataHandler();
		matSimProviderReceiver = new MATSimDataProviderReceiver(new LinkedList<String>());
		simulation = new MATSimSimulation(matSimProviderReceiver,
				matSimProviderReceiver, new DefaultMATSimUpdater(),
				new DefaultMATSimExtractor(), networkHandler,
				Arrays.asList(jdeecoAgentSource), Settings.MATSIM_CONFIG);
		router = new MATSimRouter(simulation.getControler(),
				simulation.getTravelTime(), Settings.ROUTE_CALCULATION_OFFSET);
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
		//deployCarGroups();
		deployAccidentSites(
				Settings.CRASH_SITES,
				Settings.POLICE_PER_CRASH,
				Settings.AMBULANCE_PER_CRASH,
				Settings.FIRE_PER_CRASH);
		
		DirectSimulationHost host = simulation.getHost("Host");
		RuntimeFramework runtime = builder.build(host, simulation, null, model, new AlwaysRebroadcastingKnowledgeDataManager(model.getEnsembleDefinitions(), null), new CloningKnowledgeManagerFactory());
		runtime.start();
		
		System.out.println("Run the simulation");
		// Run the simulation
		long startTime = System.currentTimeMillis();
		simulation.run();
		long diffTime = System.currentTimeMillis() - startTime;
		System.out.println(String.format("Simulation Finished in: %s.%ss", diffTime / 1000, diffTime % 1000));
	}
	
	private static void createAndDeployVehicleComponent(int idx,
			Id sourceLinkId, String destination)
			throws AnnotationProcessorException {
		String compIdString = "V" + idx;
		Id compId = new IdImpl(compIdString);
				
		jdeecoAgentSource.addAgent(new JDEECoAgent(compId, sourceLinkId));

		Vehicle component = new Vehicle(compIdString, destination, sourceLinkId,
				matSimProviderReceiver.getActuatorProvider(compId),
				matSimProviderReceiver.getSensorProvider(compId), router,
				simulation);

		processor.process(component, SharedDestination.class, LeaderFollower.class, Train.class, TrainLeaderFollower.class);
	}
	
	private static int getCarId() {
		return vehicleCounter++;
	}
	
	private static Link getRandomLinkAround(double x, double y, double radius) {
		double dx = (0.5 - random.nextDouble()) * 2 * radius;
		double dy = (0.5 - random.nextDouble()) * 2 * radius;
		return router.findNearestLink(new CoordImpl(x + dx, y + dy));
	}
	
	private static Link getRandomLink() {
		double x = Settings.MIN_X + random.nextDouble() * Settings.WIDTH;
		double y = Settings.MIN_Y + random.nextDouble() * Settings.HEIGHT;
		return router.findNearestLink(new CoordImpl(x, y));
	}
	
	private static void deployCarGroups() throws AnnotationProcessorException {
		// Deploy group A
		for(int i = 0; i < Settings.GROUP_A_VEHICLE_COUNT; ++i) {
			Coord pos = Navigator.getPosition(Settings.GROUP_A_POS).getCoord();
			createAndDeployVehicleComponent(
					getCarId(),
					getRandomLinkAround(pos.getX(), pos.getY(), Settings.GROUP_A_RADIUS).getId(),
					Settings.GROUP_A_DST);
		}
			
		// Deploy group B
		for(int i = 0; i < Settings.GROUP_B_VEHICLE_COUNT; ++i) {
			Coord pos = Navigator.getPosition(Settings.GROUP_B_POS).getCoord();
			createAndDeployVehicleComponent(
					getCarId(),
					getRandomLinkAround(pos.getX(), pos.getY(), Settings.GROUP_B_RADIUS).getId(),
					Settings.GROUP_B_DST);
		}
	}
	
	private static void deployAccidentSites(int sites, int policeCount, int ambulanceCount, int fireCount) throws AnnotationProcessorException {
		for(int i = 0; i < sites; ++i)
			deployAccidentSite(i, policeCount, ambulanceCount, fireCount);
	}
	
	private static void deployAccidentSite(int siteNum, int policeCount, int ambulanceCount, int fireCount) throws AnnotationProcessorException {
		Link crashSite = getRandomLink();
		String crashSiteName = String.format("C%d", siteNum);
		
		Navigator.putLink(crashSiteName, crashSite);
		
		// Deploy police vehicles
		deploySquad("P", crashSiteName, policeCount);
		
		// Deploy ambulance vehicles
		deploySquad("A", crashSiteName, ambulanceCount);
				
		// Deploy fire vehicles
		deploySquad("F", crashSiteName, fireCount);
	}
	
	private static void deploySquad(String prefix, String crashSite, int count) throws AnnotationProcessorException {
		// Deploy squad vehicles
		Set<String> places = new HashSet<String>();
		for(int i = 0; i < count; ++i) {
			String start = getNearestPlace(prefix, crashSite, places);
			places.add(start);
		}
		
		for(String p: places) {
			createAndDeployVehicleComponent(
					getCarId(),
					Navigator.getPosition(p).getId(),
					crashSite);
		}
	}
	
	private static String getRandomPlace(String prefix) {
		List<String> matching = getMatchingPlaces(prefix);
		return matching.remove(random.nextInt(matching.size() - 1));
	}
	
	private static String getNearestPlace(String prefix, String toPlace, Set<String> ommit) {
		String nearest = null;
		double nearestDist = 0;
		
		List<String> matching = getMatchingPlaces(prefix);
		matching.removeAll(ommit);
		
		for(String place: matching) {
			Link pos = Navigator.getPosition(place);
			double dist = Navigator.getDesDist(toPlace, pos.getId());
			
			if(nearest == null || dist < nearestDist) {
				nearest = place;
				nearestDist = dist;
			}
		}
		
		return nearest;
	}
	
	private static List<String> getMatchingPlaces(String prefix) {
		// Get matching places
		List<String> matching = new LinkedList<String>();
		for(String place: Navigator.getPlaces()) {
			if(place.startsWith(prefix)) {
				matching.add(place);
			}
		}
		return matching;
	}
}
