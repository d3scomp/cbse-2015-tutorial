package cz.cuni.mff.d3s.roadtrain.demo.components;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.basic.v01.IdImpl;

import cz.cuni.mff.d3s.deeco.simulation.matsim.JDEECoAgent;
import cz.cuni.mff.d3s.deeco.simulation.matsim.JDEECoAgentSource;
import cz.cuni.mff.d3s.deeco.simulation.matsim.MATSimRouter;

public class AgentSourceBasedPosition implements PositionAware {
	private JDEECoAgentSource source;
	private MATSimRouter router;
	
	public AgentSourceBasedPosition(JDEECoAgentSource source) {
		this.source = source;
	}
	
	public void setRouter(MATSimRouter router) {
		this.router = router;
	}

	@Override
	public Position getPosition(String id) {
		for(JDEECoAgent agent: source.getAgents()) {
			if(agent.getId().equals(new IdImpl(id))) {
				Link currentLink = router.findLinkById(agent.getCurrentLinkId());
				Coord current = currentLink.getCoord();
				
				return new Position(current.getX(), current.getY());
			}
		}
		
		throw new RuntimeException("No usch agent: " + id);
	}
}
