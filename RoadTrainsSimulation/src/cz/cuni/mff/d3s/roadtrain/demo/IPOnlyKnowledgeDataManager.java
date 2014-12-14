package cz.cuni.mff.d3s.roadtrain.demo;

import java.util.List;

import cz.cuni.mff.d3s.deeco.model.runtime.api.EnsembleDefinition;
import cz.cuni.mff.d3s.deeco.network.DefaultKnowledgeDataManager;
import cz.cuni.mff.d3s.deeco.network.IPGossipStrategy;
import cz.cuni.mff.d3s.deeco.network.KnowledgeData;

public class IPOnlyKnowledgeDataManager extends DefaultKnowledgeDataManager {

	public IPOnlyKnowledgeDataManager(List<EnsembleDefinition> ensembleDefinitions, IPGossipStrategy ipGossipStrategy) {
		super(ensembleDefinitions, ipGossipStrategy);
	}
	
	@Override
	public void publish() {
		// we re-publish periodically only local data
		List<KnowledgeData> data = prepareLocalKnowledgeData();
		
		if (!data.isEmpty()) {
			
			logPublish(data);
			
			
			if (ipGossipStrategy != null) {
				sendDirect(data);
			}
			localVersion++;
		}
	}
}
