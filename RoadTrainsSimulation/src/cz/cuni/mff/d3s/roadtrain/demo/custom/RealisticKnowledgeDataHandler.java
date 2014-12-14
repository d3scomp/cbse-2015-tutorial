package cz.cuni.mff.d3s.roadtrain.demo.custom;

import java.util.*;

import cz.cuni.mff.d3s.deeco.model.runtime.api.KnowledgePath;
import cz.cuni.mff.d3s.deeco.network.AbstractHost;
import cz.cuni.mff.d3s.deeco.network.DataReceiver;
import cz.cuni.mff.d3s.deeco.network.KnowledgeData;
import cz.cuni.mff.d3s.deeco.simulation.DirectKnowledgeDataHandler;
import cz.cuni.mff.d3s.roadtrain.demo.MessageProbe;
import cz.cuni.mff.d3s.roadtrain.demo.components.Position;
import cz.cuni.mff.d3s.roadtrain.demo.components.PositionAware;
import cz.cuni.mff.d3s.roadtrain.demo.utils.VehicleState;

@SuppressWarnings({ "rawtypes" })
public class RealisticKnowledgeDataHandler extends
		DirectKnowledgeDataHandler {
	
	public static final double MANET_RANGE = 250.0;
	public static boolean OPTIMIZE_DESTINATION_ENSEMBLE = false;
	
	private final PositionAware positions;
	private MessageProbe messageProbe;
	
	public RealisticKnowledgeDataHandler(PositionAware positions, MessageProbe messageProbe) {
		this.positions = positions;
		this.messageProbe = messageProbe;
	}
	
	@Override
	public void networkSend(AbstractHost from, Object data, AbstractHost recipientHost, Collection<DataReceiver> recipientReceivers) {
		// FIXME: This is a hack which skips sending of knowledge of vehicles which are not in the destination exchange state
		if(data instanceof  List<?> && OPTIMIZE_DESTINATION_ENSEMBLE) {
			for(Object o: (List<?>)data) {
				if(o instanceof KnowledgeData) {
					KnowledgeData kData = (KnowledgeData) o;
					for(KnowledgePath p: kData.getKnowledge().getKnowledgePaths()) {
						if(p.toString().equals("state")) {
							VehicleState state = (VehicleState) kData.getKnowledge().getValue(p);
							if(!state.destinationExchange()) {
								//System.out.println("Not sending knowledge as component state does not allow destiantion exchange");
								return;
							}
						}
					}
				}
			}
		}
		
		for (DataReceiver receiver: recipientReceivers) {
			messageProbe.messageSentIP();
			receiver.checkAndReceive(data, DEFAULT_IP_RSSI);
		}
	}
	
	@Override
	public void networkBroadcast(AbstractHost from, Object data, Map<AbstractHost, Collection<DataReceiver>> receivers) {
		messageProbe.messageSentMANET();
		
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
