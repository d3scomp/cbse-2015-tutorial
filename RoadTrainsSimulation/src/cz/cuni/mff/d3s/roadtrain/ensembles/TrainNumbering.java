package cz.cuni.mff.d3s.roadtrain.ensembles;

import java.util.Map;

import org.matsim.api.core.v01.Coord;

import cz.cuni.mff.d3s.deeco.annotations.Ensemble;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.KnowledgeExchange;
import cz.cuni.mff.d3s.deeco.annotations.Membership;
import cz.cuni.mff.d3s.deeco.annotations.Out;
import cz.cuni.mff.d3s.deeco.annotations.PartitionedBy;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.roadtrain.utils.Navigator;
import cz.cuni.mff.d3s.roadtrain.utils.VehicleInfo;

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
	public static void exchange(
			@In("coord.id") String coordId,
			@In("member.id") String memberId,
			
			@In("coord.carNum") int coordCarNum,
			@Out("member.carNum") ParamHolder<Integer> memberCarNum,
			
			//@InOut("coord.followers") ParamHolder<Map<String, VehicleInfo> > followers,
			@InOut("coord.nearestFollower") ParamHolder<Double> nearestFollower,
			@InOut("member.leaderDist") ParamHolder<Double> leaderDist,
			@In("coord.position") Coord coordPosition,
			@In("member.position") Coord position,
			@In("member.carNum") int carNum) {
		// TODO: map coord location, speed, ... for precious following
		
		//followers.value.put(memberId, new VehicleInfo(memberId, position, carNum));
		
		double dist = Navigator.getEuclidDistance(coordPosition, position);
		if(nearestFollower.value == null || nearestFollower.value > dist)
			nearestFollower.value = dist;
		
		leaderDist.value = Navigator.getEuclidDistance(position, coordPosition);
		
		memberCarNum.value = coordCarNum + 1;
	}
}


