package cz.cuni.mff.d3s.roadtrain.ensembles;

import cz.cuni.mff.d3s.deeco.annotations.Ensemble;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.KnowledgeExchange;
import cz.cuni.mff.d3s.deeco.annotations.Membership;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;

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
			@In("member.id") String memberId) {
		// TODO: map coord location, speed, ... for precious following
	}
}
