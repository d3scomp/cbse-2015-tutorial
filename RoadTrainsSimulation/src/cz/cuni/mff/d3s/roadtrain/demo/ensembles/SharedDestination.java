package cz.cuni.mff.d3s.roadtrain.demo.ensembles;

import java.util.Map;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;

import cz.cuni.mff.d3s.deeco.annotations.Ensemble;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.KnowledgeExchange;
import cz.cuni.mff.d3s.deeco.annotations.Membership;
import cz.cuni.mff.d3s.deeco.annotations.PartitionedBy;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.scheduler.CurrentTimeProvider;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.roadtrain.demo.utils.VehicleInfo;
import cz.cuni.mff.d3s.roadtrain.demo.utils.VehicleState;

@Ensemble
@PeriodicScheduling(period = 1000)
@PartitionedBy("dstPlace")
public class SharedDestination {
	@Membership
	public static boolean membership(
			@In("coord.id") String coordId,
			@In("coord.dstPlace") String coordDstPlace,
			@In("coord.state") VehicleState coordState,
			@In("member.id") String memberId,
			@In("member.dstPlace") String memberDstPlace,
			@In("member.state") VehicleState memberState) {
		// Same destination, not the same vehicle, not part of the train
		return coordDstPlace.equals(memberDstPlace) && !coordId.equals(memberId) && memberState.destinationExchange() && coordState.destinationExchange();
	}

	@KnowledgeExchange
	public static void exchange(
			@In("member.id") String memberId,
			@In("member.position") Coord memberPosition,
			@In("member.currentLink") Id memberLink,
			@InOut("coord.destGroup") ParamHolder<Map<String, VehicleInfo>> coordGroup,
			@In("coord.clock") CurrentTimeProvider clock) {
		// Exchange information about the group sharing the same destination
		coordGroup.value.put(memberId, new VehicleInfo(memberId, memberPosition, memberLink, clock.getCurrentMilliseconds()));
	}
}
