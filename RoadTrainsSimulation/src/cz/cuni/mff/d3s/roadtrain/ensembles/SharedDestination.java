package cz.cuni.mff.d3s.roadtrain.ensembles;

import java.util.Map;

import org.matsim.api.core.v01.Coord;

import cz.cuni.mff.d3s.deeco.annotations.Ensemble;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.KnowledgeExchange;
import cz.cuni.mff.d3s.deeco.annotations.Membership;
import cz.cuni.mff.d3s.deeco.annotations.PartitionedBy;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.roadtrain.utils.VehicleInfo;

@Ensemble
@PeriodicScheduling(period = 1000)
@PartitionedBy("destination")
public class SharedDestination {
	@Membership
	public static boolean membership(
			@In("coord.id") String coordId,
			@In("member.id") String memeberId,
			@In("coord.dstCity") String coordDstCity,
			@In("member.dstCity") String memeberDstCity) {
		// Same destination, not the same vehicle
		return coordDstCity.equals(memeberDstCity) && !coordId.equals(memeberId);
	}
	
	@KnowledgeExchange
	public static void exchange(
			@In("coord.id") String coordId,
			@In("member.id") String memberId,
			@In("coord.position") Coord coordPosition,
			@In("member.position") Coord memberPosition,
			@In("coord.carNum") int coordCarNum,
			@In("member.carNum") int memberCarNum,
			@InOut("coord.group") ParamHolder<Map<String, VehicleInfo> > coordGroup,
			@InOut("member.group") ParamHolder<Map<String, VehicleInfo> > memberGroup) {
		// Exchange information about the group sharing the same destination
		memberGroup.value.put(coordId, new VehicleInfo(coordId, coordPosition, coordCarNum));
		memberGroup.value.put(memberId, new VehicleInfo(memberId, memberPosition, memberCarNum));
	}
}
