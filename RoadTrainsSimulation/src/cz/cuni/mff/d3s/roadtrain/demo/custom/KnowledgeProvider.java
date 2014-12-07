/**
 * 
 */
package cz.cuni.mff.d3s.roadtrain.demo.custom;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.cuni.mff.d3s.deeco.network.DataReceiver;
import cz.cuni.mff.d3s.deeco.network.KnowledgeData;
import cz.cuni.mff.d3s.deeco.network.connector.KnowledgeDataStore;

/**
 * 
 * @author Ondrej Kováč <info@vajanko.me>
 */
@SuppressWarnings("rawtypes")
public class KnowledgeProvider extends DataReceiver<List> implements KnowledgeDataStore {

	// component id - knowledge data
	private Map<String, KnowledgeData> data;

	public Collection<KnowledgeData> getAllKnowledgeData() {
		return data.values();
	}

	public void remove(KnowledgeData kd) {
		data.remove(kd.getMetaData().componentId);
	}

	public void removeAll(Collection<KnowledgeData> kds) {
		for (KnowledgeData kd : kds) {
			data.remove(kd);
		}
	}
	
	/**
	 * 
	 */
	public KnowledgeProvider() {
		super(List.class);
		this.data = new HashMap<String, KnowledgeData>();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void receive(List data, double rssi) {
		List<? extends KnowledgeData> knowledgeData = (List<? extends KnowledgeData>) data;

		for (KnowledgeData kd : knowledgeData) {
			this.data.put(kd.getMetaData().componentId, kd);
		}
	}
}
