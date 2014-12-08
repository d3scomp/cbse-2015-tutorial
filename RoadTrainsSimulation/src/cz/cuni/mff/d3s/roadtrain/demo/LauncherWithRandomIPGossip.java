package cz.cuni.mff.d3s.roadtrain.demo;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.matsim.core.basic.v01.IdImpl;

import cz.cuni.mff.d3s.deeco.annotations.processor.AnnotationProcessor;
import cz.cuni.mff.d3s.deeco.annotations.processor.AnnotationProcessorException;
import cz.cuni.mff.d3s.deeco.knowledge.CloningKnowledgeManagerFactory;
import cz.cuni.mff.d3s.deeco.model.runtime.api.RuntimeMetadata;
import cz.cuni.mff.d3s.deeco.model.runtime.custom.RuntimeMetadataFactoryExt;
import cz.cuni.mff.d3s.deeco.network.DefaultKnowledgeDataManager;
import cz.cuni.mff.d3s.deeco.network.DirectGossipStrategy;
import cz.cuni.mff.d3s.deeco.network.DirectRecipientSelector;
import cz.cuni.mff.d3s.deeco.network.IPGossipStrategy;
import cz.cuni.mff.d3s.deeco.network.KnowledgeDataManager;
import cz.cuni.mff.d3s.deeco.network.RandomIPGossip;
import cz.cuni.mff.d3s.deeco.runtime.RuntimeFramework;
import cz.cuni.mff.d3s.deeco.simulation.DirectSimulationHost;
import cz.cuni.mff.d3s.deeco.simulation.NetworkDataHandler;
import cz.cuni.mff.d3s.deeco.simulation.SimulationRuntimeBuilder;
import cz.cuni.mff.d3s.deeco.simulation.matsim.DefaultMATSimExtractor;
import cz.cuni.mff.d3s.deeco.simulation.matsim.DefaultMATSimUpdater;
import cz.cuni.mff.d3s.deeco.simulation.matsim.JDEECoAgent;
import cz.cuni.mff.d3s.deeco.simulation.matsim.JDEECoAgentSource;
import cz.cuni.mff.d3s.deeco.simulation.matsim.MATSimRouter;
import cz.cuni.mff.d3s.deeco.simulation.matsim.MATSimSimulation;
import cz.cuni.mff.d3s.roadtrain.demo.components.AgentSourceBasedPosition;
import cz.cuni.mff.d3s.roadtrain.demo.components.Vehicle;
import cz.cuni.mff.d3s.roadtrain.demo.custom.RealisticKnowledgeDataHandler;
import cz.cuni.mff.d3s.roadtrain.demo.ensembles.LeaderFollower;
import cz.cuni.mff.d3s.roadtrain.demo.ensembles.SharedDestination;
import cz.cuni.mff.d3s.roadtrain.demo.ensembles.Train;
import cz.cuni.mff.d3s.roadtrain.demo.ensembles.TrainLeaderFollower;
import cz.cuni.mff.d3s.roadtrain.demo.environment.MATSimDataProviderReceiver;
import cz.cuni.mff.d3s.roadtrain.demo.utils.Navigator;

public class LauncherWithRandomIPGossip implements Launcher, VehicleDeployer {
	private JDEECoAgentSource agentSource;
	private MATSimSimulation sim;
	private MATSimRouter router;
	private MATSimDataProviderReceiver matSimProviderReceiver;
	private SimulationRuntimeBuilder builder;	
	private AgentSourceBasedPosition positionProvider;
	private CloningKnowledgeManagerFactory kmFactory;
	private Set<String> destinations = new HashSet<String>();
	
	// For dummy gossip
	private final DummyRecipientSelector dummyRecipientSelector = new DummyRecipientSelector(5);
	private final DirectGossipStrategy directGossipStrategy = new DirectGossipStrategy() {
		public Collection<String> filterRecipients(Collection<String> recipients) {
			return recipients;
		}
	};
		
	public void run(DemoDeployer demoDeployer) throws AnnotationProcessorException, IOException {
		// Setup simulation
		System.out.println("Preparing simulation");
		agentSource = new JDEECoAgentSource();
		positionProvider = new AgentSourceBasedPosition(agentSource);
		NetworkDataHandler networkHandler = new RealisticKnowledgeDataHandler(positionProvider);
		matSimProviderReceiver = new MATSimDataProviderReceiver(new LinkedList<String>());
		sim = new MATSimSimulation(matSimProviderReceiver,
				matSimProviderReceiver, new DefaultMATSimUpdater(),
				new DefaultMATSimExtractor(), networkHandler,
				Arrays.asList(agentSource), Settings.MATSIM_CONFIG);
		router = new MATSimRouter(sim.getControler(), sim.getTravelTime(), Settings.ROUTE_CALCULATION_OFFSET);
		positionProvider.setRouter(router);
		builder = new SimulationRuntimeBuilder();
		kmFactory = new CloningKnowledgeManagerFactory();
		
		// Setup navigator
		Navigator.init(router);
	
		// Deploy components
		System.out.println("Deploying components");
		demoDeployer.deploy();
		
		// Run the simulation
		System.out.println("Running simulation");
		long startTime = System.currentTimeMillis();
		sim.run();
		long diffTime = System.currentTimeMillis() - startTime;
		System.out.println(String.format("Simulation Finished in: %s.%ss", diffTime / 1000, diffTime % 1000));
		System.out.println(MessageProbe.report());
	}
	
	
	public IPGossipStrategy getStrategy(Vehicle component, Object commGroup, RuntimeMetadata model, DirectSimulationHost host) {
		dummyRecipientSelector.add(component.id);
		return new RandomIPGossip(Arrays.asList((DirectRecipientSelector)dummyRecipientSelector), directGossipStrategy);
	}
	
	
	public void deployVehicle(Vehicle component) throws AnnotationProcessorException {
		agentSource.addAgent(new JDEECoAgent(new IdImpl(component.id), component.currentLink));
				
		RuntimeMetadata model = RuntimeMetadataFactoryExt.eINSTANCE.createRuntimeMetadata();
		AnnotationProcessor processor = new AnnotationProcessor(RuntimeMetadataFactoryExt.eINSTANCE, model,
				kmFactory, new PartitionedByProcessor());
		processor.process(
				component,
				SharedDestination.class,
				LeaderFollower.class,
				Train.class,
				TrainLeaderFollower.class);

		DirectSimulationHost host = sim.getHost(component.id);
		IPGossipStrategy strategy = getStrategy(component, component.dstPlace, model, host);
		KnowledgeDataManager kdm = new DefaultKnowledgeDataManager(model.getEnsembleDefinitions(), strategy);
		
		RuntimeFramework runtime = builder.build(host, sim, null, model, kdm, new CloningKnowledgeManagerFactory());
		runtime.start();
	}
		
	@Override
	public MATSimSimulation getSimulation() {
		return sim;
	}

	@Override
	public MATSimRouter getRouter() {
		return router;
	}

	@Override
	public MATSimDataProviderReceiver getProviderReceiver() {
		return matSimProviderReceiver;
	}

	@Override
	public void addDestination(String destination) {
		destinations.add(destination);
	}
}
