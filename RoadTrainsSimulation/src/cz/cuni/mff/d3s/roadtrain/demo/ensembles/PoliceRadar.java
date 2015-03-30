
package cz.cuni.mff.d3s.roadtrain.demo.ensembles;

import java.util.Map;

import org.matsim.api.core.v01.Coord;

import cz.cuni.mff.d3s.deeco.annotations.Ensemble;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.KnowledgeExchange;
import cz.cuni.mff.d3s.deeco.annotations.Membership;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.roadtrain.demo.Settings;
import cz.cuni.mff.d3s.roadtrain.demo.components.VehicleKind;
import cz.cuni.mff.d3s.roadtrain.demo.utils.Navigator;

@Ensemble
@PeriodicScheduling(period = 1000)
public class PoliceRadar {
	
	@Membership
	public static boolean membership(
			@In("member.position") Coord memberPosition,
			@In("member.vehicleKind") VehicleKind memberKind,
			@In("coord.position") Coord coordPosition,
			@In("coord.vehicleKind") VehicleKind coordKind) {
		return Navigator.getEuclidDistance(memberPosition, coordPosition) <= Settings.POLICE_RADAR_RANGE &&
				memberKind == VehicleKind.ORDINARY && coordKind == VehicleKind.POLICE;
	}

	
	@KnowledgeExchange
	public static void exchange(			
			@In("member.id") String id,
			@In("member.ownerName") String ownerId,
			@In("member.dstPlace") String dstPlace,
			@InOut("coord.vehiclesOwnersNearby") ParamHolder<Map<String, String>> vehiclesOwnersNearby,
			@InOut("coord.vehicleIdsNearby") ParamHolder<Map<String, String>> vehicleIdsNearby) {
		vehiclesOwnersNearby.value.put(ownerId, dstPlace);
		vehicleIdsNearby.value.put(ownerId, id);
	}
}
