/**
 * Shared destination ensemble
 * 
 * This is used to distribute information about vehicles
 * sharing the same destination to them self. 
 */

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
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.roadtrain.demo.utils.VehicleInfo;
import cz.cuni.mff.d3s.roadtrain.demo.utils.VehicleState;

@Ensemble
@PeriodicScheduling(period = 4500)
@PartitionedBy("dstPlace")
public class SharedDestination {
	/**
	 * Members are not the same vehicles, but with the shared destination.
	 * Destination is exchanged only for the vehicles that are single or
	 * the train leaders as the members do not have to be considered
	 * in planning. 
	 */
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
			@In("member.state") VehicleState memeberState,
			@InOut("coord.destGroup") ParamHolder<Map<String, VehicleInfo>> coordGroup,
			@In("member.curTime") long time) {
		// Exchange information about the group sharing the same destination
		coordGroup.value.put(memberId, new VehicleInfo(memberId, memberPosition, memberLink, time, memeberState));
	}
}
