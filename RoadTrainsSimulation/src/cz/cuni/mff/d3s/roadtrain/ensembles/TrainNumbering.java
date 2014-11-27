package cz.cuni.mff.d3s.roadtrain.ensembles;

import org.matsim.api.core.v01.Id;

import cz.cuni.mff.d3s.deeco.annotations.Ensemble;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.KnowledgeExchange;
import cz.cuni.mff.d3s.deeco.annotations.Membership;
import cz.cuni.mff.d3s.deeco.annotations.PartitionedBy;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.roadtrain.utils.Navigator;

@Ensemble
@PeriodicScheduling(period = 1000)
@PartitionedBy("destination")
public class TrainNumbering {
	@Membership
	public static boolean membership(
			@In("coord.id") String coordId,
			@In("member.leaderCar") String memeberLeaderCar) {
		// Member is following coordinator
		return memeberLeaderCar.equals(coordId);
	}

	@KnowledgeExchange
	public static void exchange(@In("coord.id") String coordId,
			@In("member.id") String memberId,

			@InOut("coord.nearestFollower") ParamHolder<Double> nearestFollower,
			@InOut("member.leaderDist") ParamHolder<Double> leaderDist,
			@In("coord.currentLink") Id coordLink,
			@In("member.currentLink") Id memberLink,
			@In("member.carNum") int carNum) {
		// TODO: map coord location, speed, ... for precious following

		// Leader - follower distance
		double dist = Navigator.getCarToCarDist(coordLink, memberLink);
		if (nearestFollower.value == null || nearestFollower.value > dist)
			nearestFollower.value = dist;
		leaderDist.value = dist;
	}
}
