package cz.cuni.mff.d3s.roadtrain.demo;

import java.util.*;

import cz.cuni.mff.d3s.deeco.knowledge.ReadOnlyKnowledgeManager;
import cz.cuni.mff.d3s.deeco.network.DirectRecipientSelector;
import cz.cuni.mff.d3s.deeco.network.KnowledgeData;

public class DummyRecipientSelector implements DirectRecipientSelector {
	private Collection<String> all = new HashSet<String>();
	private final int numPeers;
	private Random random = new Random(42);
	
	public DummyRecipientSelector(int numPeers) {
		this.numPeers = numPeers;
	}
	
	public void add(String peer) {
		all.add(peer);
	}
				
	public Collection<String> getRecipients(KnowledgeData data,
			ReadOnlyKnowledgeManager sender) {
		if(numPeers == 0) {
			return all;
		} else {
			Vector<String> allVector = new Vector<String>(all);
			
			List<String> result = new LinkedList<String>();
			
			for(int i = 0; i < numPeers; ++i) {
				result.add(allVector.get(random.nextInt(allVector.size())));
			}
			
			return result;
		}
	}
}
