package cz.cuni.mff.d3s.roadtrain.demo.ensembles;

import java.util.Map;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;

import cz.cuni.mff.d3s.deeco.annotations.Ensemble;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.KnowledgeExchange;
import cz.cuni.mff.d3s.deeco.annotations.Membership;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.roadtrain.demo.utils.VehicleInfo;
import cz.cuni.mff.d3s.roadtrain.demo.utils.VehicleState;

@Ensemble
@PeriodicScheduling(period = 1000)
public class Train {
	@Membership
	public static boolean membership(
			@In("coord.id") String coordId,
			@In("coord.trainId") String coordTrainId,
			@In("coord.state") VehicleState coordState,
			@In("member.id") String memberId,
			@In("member.trainId") String memberTrainId,
			@In("member.state") VehicleState memberState) {
		// Not the same cars and the same train
		return !memberId.equals(coordId) && memberTrainId.equals(coordTrainId) && memberState != VehicleState.DONE;
	}

	@KnowledgeExchange
	public static void exchange(
			@In("member.id") String memberId,
			@In("member.position") Coord memberPosition,
			@In("member.currentLink") Id memberLink,
			@In("member.state") VehicleState memberState,
			@InOut("coord.trainGroup") ParamHolder<Map<String, VehicleInfo> > coordGroup,
			@In("coord.trainId") String coordTrainId,
			@InOut("coord.trainIdTime") ParamHolder<Long> coordTrainIdTime,
			@In("coord.curTime") long curTime) {
		// Exchange information about the road train
		coordGroup.value.put(memberId, new VehicleInfo(memberId, memberPosition, memberLink, curTime, memberState));
		
		if(memberId.equals(coordTrainId)) {
			coordTrainIdTime.value = curTime;
		}
	}
}
