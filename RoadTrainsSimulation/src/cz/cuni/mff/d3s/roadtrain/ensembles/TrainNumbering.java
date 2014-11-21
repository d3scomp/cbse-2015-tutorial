package cz.cuni.mff.d3s.roadtrain.ensembles;

import cz.cuni.mff.d3s.deeco.annotations.Ensemble;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.KnowledgeExchange;
import cz.cuni.mff.d3s.deeco.annotations.Membership;
import cz.cuni.mff.d3s.deeco.annotations.Out;
import cz.cuni.mff.d3s.deeco.annotations.PartitionedBy;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;

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
			@In("coord.carNum") int coordCarNum,
			@Out("member.carNum") ParamHolder<Integer> memberCarNum) {
		// Update member car number
		memberCarNum.value = coordCarNum + 1;
	}
}
