package cz.cuni.mff.d3s.roadtrain.demo;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.basic.v01.IdImpl;

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
import cz.cuni.mff.d3s.roadtrain.demo.components.Vehicle;
import cz.cuni.mff.d3s.roadtrain.demo.custom.AlwaysRebroadcastingKnowledgeDataManager;
import cz.cuni.mff.d3s.roadtrain.demo.ensembles.LeaderFollower;
import cz.cuni.mff.d3s.roadtrain.demo.ensembles.SharedDestination;
import cz.cuni.mff.d3s.roadtrain.demo.ensembles.Train;
import cz.cuni.mff.d3s.roadtrain.demo.ensembles.TrainLeaderFollower;
import cz.cuni.mff.d3s.roadtrain.demo.environment.MATSimDataProviderReceiver;
import cz.cuni.mff.d3s.roadtrain.demo.utils.Navigator;

public class Launcher {
	private static JDEECoAgentSource agentSource;
	private static MATSimSimulation sim;
	private static MATSimRouter router;
	private static MATSimDataProviderReceiver matSimProviderReceiver;
	private static SimulationRuntimeBuilder builder;	
	private static AnnotationProcessor processor;
	private static int vehicleCounter = 0;
		
	public static void run() throws AnnotationProcessorException, IOException {
		// Setup simulation
		System.out.println("Preparing simulation");
		agentSource = new JDEECoAgentSource();
		NetworkDataHandler networkHandler = new DirectKnowledgeDataHandler();
		matSimProviderReceiver = new MATSimDataProviderReceiver(new LinkedList<String>());
		sim = new MATSimSimulation(matSimProviderReceiver,
				matSimProviderReceiver, new DefaultMATSimUpdater(),
				new DefaultMATSimExtractor(), networkHandler,
				Arrays.asList(agentSource), Settings.MATSIM_CONFIG);
		router = new MATSimRouter(sim.getControler(), sim.getTravelTime(), Settings.ROUTE_CALCULATION_OFFSET);
		builder = new SimulationRuntimeBuilder();
		
		// Setup navigator
		Navigator.init(router);
	
		RuntimeMetadata model = RuntimeMetadataFactoryExt.eINSTANCE.createRuntimeMetadata();
		processor = new AnnotationProcessor(RuntimeMetadataFactoryExt.eINSTANCE, model, new CloningKnowledgeManagerFactory());
		
		
		// Deploy components
		System.out.println("Deploying components");
		//deployCarGroups();
		deployAccidentSites(
				Settings.CRASH_SITES,
				Settings.POLICE_PER_CRASH,
				Settings.AMBULANCE_PER_CRASH,
				Settings.FIRE_PER_CRASH);
		
		
		DirectSimulationHost host = sim.getHost("Host");
		RuntimeFramework runtime = builder.build(host, sim, null, model, new AlwaysRebroadcastingKnowledgeDataManager(model.getEnsembleDefinitions(), null), new CloningKnowledgeManagerFactory());
		runtime.start();
		
		// Run the simulation
		System.out.println("Running simulation");
		long startTime = System.currentTimeMillis();
		sim.run();
		long diffTime = System.currentTimeMillis() - startTime;
		System.out.println(String.format("Simulation Finished in: %s.%ss", diffTime / 1000, diffTime % 1000));
	}
	
	private static void deployVehicle(int idx, Id sourceLinkId, String destination)
			throws AnnotationProcessorException {
		String compIdString = "V" + idx;
		Id compId = new IdImpl(compIdString);
				
		agentSource.addAgent(new JDEECoAgent(compId, sourceLinkId));

		Vehicle component = new Vehicle(compIdString, destination, sourceLinkId,
				matSimProviderReceiver.getActuatorProvider(compId),
				matSimProviderReceiver.getSensorProvider(compId), router,
				sim);

		processor.process(component, SharedDestination.class, LeaderFollower.class, Train.class, TrainLeaderFollower.class);
	}
	
	private static int getCarId() {
		return vehicleCounter++;
	}
	
	private static void deployAccidentSites(int sites, int policeCount, int ambulanceCount, int fireCount) throws AnnotationProcessorException {
		for(int i = 0; i < sites; ++i)
			deployAccidentSite(i, policeCount, ambulanceCount, fireCount);
	}
	
	private static void deployAccidentSite(int siteNum, int policeCount, int ambulanceCount, int fireCount) throws AnnotationProcessorException {
		Link crashSite = Navigator.getRandomLink();
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
			String start = Navigator.getNearestPlace(prefix, crashSite, places);
			places.add(start);
		}
		
		for(String p: places) {
			deployVehicle(
					getCarId(),
					Navigator.getPosition(p).getId(),
					crashSite);
		}
	}
	
	
}
