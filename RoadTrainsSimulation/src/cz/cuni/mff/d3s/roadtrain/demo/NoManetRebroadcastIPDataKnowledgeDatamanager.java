package cz.cuni.mff.d3s.roadtrain.demo;

import java.util.List;

import cz.cuni.mff.d3s.deeco.logging.Log;
import cz.cuni.mff.d3s.deeco.model.runtime.api.EnsembleDefinition;
import cz.cuni.mff.d3s.deeco.network.DefaultKnowledgeDataManager;
import cz.cuni.mff.d3s.deeco.network.IPGossipStrategy;
import cz.cuni.mff.d3s.deeco.network.KnowledgeData;
import cz.cuni.mff.d3s.deeco.network.KnowledgeMetaData;
import cz.cuni.mff.d3s.deeco.network.NICType;
import cz.cuni.mff.d3s.deeco.network.RebroadcastTask;

public class NoManetRebroadcastIPDataKnowledgeDatamanager extends DefaultKnowledgeDataManager {

	public NoManetRebroadcastIPDataKnowledgeDatamanager(List<EnsembleDefinition> ensembleDefinitions,
			IPGossipStrategy ipGossipStrategy) {
		super(ensembleDefinitions, ipGossipStrategy);
	}
	
	@Override
	protected void queueForRebroadcast(KnowledgeData kd) {
		if (checkBoundaryCondition && !isInSomeBoundary(kd, getNodeKnowledge())) {
			if (Log.isDebugLoggable()) { 
				Log.d(String.format("Boundary failed (%d) at %s for %sv%d\n", 
					timeProvider.getCurrentMilliseconds(), host, kd.getMetaData().componentId, kd.getMetaData().versionId));
			}
			return;
		} 
		
		KnowledgeMetaData kmd = kd.getMetaData();
		int delay = getManetRebroadcastDelay(kmd);
		
		
		// delay < 0 indicates not rebroadcasting
		if (delay < 0) {			
			return;
		}
		
		if (Log.isDebugLoggable()) {
			Log.d(String.format(
					"Gossip rebroadcast (%d) at %s for %sv%d from %s with rssi %g with delay %d\n",
					timeProvider.getCurrentMilliseconds(), host,
					kmd.componentId,
					kmd.versionId, kmd.sender,
					kmd.rssi, delay));
		}
		
		// schedule a task for rebroadcast
		// Do not rebroadcast IP received data on manet
		if(kmd.rssi != -1) {
			dataToRebroadcastOverMANET.put(kmd.getSignatureWithRole(), kd);
			RebroadcastTask task = new RebroadcastTask(scheduler, this, delay, kmd, NICType.MANET);
			scheduler.addTask(task);
		}
		
		if (ipGossipStrategy != null && ipDelay > 0) {
			dataToRebroadcastOverIP.put(kmd.getSignatureWithRole(), kd);
			RebroadcastTask task = new RebroadcastTask(scheduler, this, ipDelay, kmd, NICType.IP);
			scheduler.addTask(task);
		}
	}
	
}
