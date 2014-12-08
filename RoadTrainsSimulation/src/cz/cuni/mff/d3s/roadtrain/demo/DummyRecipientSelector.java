package cz.cuni.mff.d3s.roadtrain.demo;

import java.util.Collection;
import java.util.HashSet;

import cz.cuni.mff.d3s.deeco.knowledge.ReadOnlyKnowledgeManager;
import cz.cuni.mff.d3s.deeco.network.DirectRecipientSelector;
import cz.cuni.mff.d3s.deeco.network.KnowledgeData;

public class DummyRecipientSelector implements DirectRecipientSelector {
	private Collection<String> all = new HashSet<String>();
	
	public void add(String peer) {
		all.add(peer);
	}
				
	public Collection<String> getRecipients(KnowledgeData data,
			ReadOnlyKnowledgeManager sender) {
		return all;
	}
}
