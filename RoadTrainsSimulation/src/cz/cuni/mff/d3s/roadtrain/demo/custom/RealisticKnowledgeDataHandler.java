package cz.cuni.mff.d3s.roadtrain.demo.custom;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import cz.cuni.mff.d3s.deeco.network.AbstractHost;
import cz.cuni.mff.d3s.deeco.network.DataReceiver;
import cz.cuni.mff.d3s.deeco.simulation.DirectKnowledgeDataHandler;
import cz.cuni.mff.d3s.roadtrain.demo.MessageProbe;
import cz.cuni.mff.d3s.roadtrain.demo.components.Position;
import cz.cuni.mff.d3s.roadtrain.demo.components.PositionAware;

@SuppressWarnings({ "rawtypes" })
public class RealisticKnowledgeDataHandler extends
		DirectKnowledgeDataHandler {
	
	public static final double MANET_RANGE = 250.0;
	
	private final PositionAware positions;
	
	public RealisticKnowledgeDataHandler(PositionAware positions) {
		this.positions = positions;
	}
	
	@Override
	public void networkSend(AbstractHost from, Object data, AbstractHost recipientHost, Collection<DataReceiver> recipientReceivers) {
		for (DataReceiver receiver: recipientReceivers) {
			MessageProbe.messageSentIP();
			receiver.checkAndReceive(data, DEFAULT_IP_RSSI);
		}
	}
	
	@Override
	public void networkBroadcast(AbstractHost from, Object data, Map<AbstractHost, Collection<DataReceiver>> receivers) {
		MessageProbe.messageSentMANET();
		
		Position fromPosition = positions.getPosition(from.getHostId());
		Iterator<Map.Entry<AbstractHost, Collection<DataReceiver>>> entries = receivers.entrySet().iterator();
		Map.Entry<AbstractHost, Collection<DataReceiver>> entry;
		while (entries.hasNext()) {
			entry = entries.next();
			if (isInMANETRange(positions.getPosition(entry.getKey().getHostId()), fromPosition)) {
				for (DataReceiver receiver: entry.getValue()) {
					receiver.checkAndReceive(data, DEFAULT_MANET_RSSI);
				}
			}
		}
	}
	
	private boolean isInMANETRange(Position a, Position b) {
		return getEuclidDistance(a, b) <= MANET_RANGE;
	}
	
	private  double getEuclidDistance(Position a, Position b) {
		if (a == null || b == null) {
			return Double.POSITIVE_INFINITY;
		}
		double dx = a.x - b.x;
		double dy = a.y - b.y; 
		return Math.sqrt(dx*dx + dy*dy);
	}


}
