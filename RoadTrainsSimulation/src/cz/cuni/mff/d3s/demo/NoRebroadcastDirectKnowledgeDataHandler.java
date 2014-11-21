package cz.cuni.mff.d3s.demo;

import java.util.Collection;
import java.util.List;

import cz.cuni.mff.d3s.deeco.network.AbstractHost;
import cz.cuni.mff.d3s.deeco.network.KnowledgeData;
import cz.cuni.mff.d3s.deeco.network.KnowledgeDataReceiver;
import cz.cuni.mff.d3s.deeco.simulation.DirectKnowledgeDataHandler;

public class NoRebroadcastDirectKnowledgeDataHandler extends
		DirectKnowledgeDataHandler {
	
	@Override
	public void networkBroadcast(AbstractHost from, List<? extends KnowledgeData> knowledgeData, Collection<KnowledgeDataReceiver> receivers) {
		for (KnowledgeData kd: knowledgeData) {
			kd.getMetaData().rssi = -1.0;
		}
		for (KnowledgeDataReceiver receiver: receivers) {
			receiver.receive(knowledgeData);
		}
	}


}
