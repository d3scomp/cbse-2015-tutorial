package cz.cuni.mff.d3s.roadtrain.ensembles;

import java.util.Map;

import org.matsim.api.core.v01.Coord;

import cz.cuni.mff.d3s.deeco.annotations.Ensemble;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.KnowledgeExchange;
import cz.cuni.mff.d3s.deeco.annotations.Membership;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.roadtrain.utils.VehicleInfo;

@Ensemble
@PeriodicScheduling(period = 1000)
public class CarPair {
	@Membership
	public static boolean membership(
			@In("coord.id") String coordId,
			@In("member.prevCar") String memeberPrevCar) {
		// Member is following coordinator
		return memeberPrevCar.equals(coordId);
	}
	
	@KnowledgeExchange
	public static void exchange(
			@In("coord.id") String coordId,
			@In("member.id") String memberId,
			@InOut("coord.followers") ParamHolder<Map<String, VehicleInfo> > followers,
			@In("member.position") Coord position,
			@In("member.carNum") int carNum) {
		// TODO: map coord location, speed, ... for precious following
		
		followers.value.put(memberId, new VehicleInfo(memberId, position, carNum));
	}
}
