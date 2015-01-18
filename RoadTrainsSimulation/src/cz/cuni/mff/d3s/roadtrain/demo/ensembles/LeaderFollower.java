/**
 * Ensemble for maintaining leader-follower links
 * 
 * These are used to wait for followers and, when the
 * followers are close enough to road-train formation.
 */

package cz.cuni.mff.d3s.roadtrain.demo.ensembles;

import org.matsim.api.core.v01.Id;

import cz.cuni.mff.d3s.deeco.annotations.Ensemble;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.KnowledgeExchange;
import cz.cuni.mff.d3s.deeco.annotations.Membership;
import cz.cuni.mff.d3s.deeco.annotations.Out;
import cz.cuni.mff.d3s.deeco.annotations.PartitionedBy;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.roadtrain.demo.Settings;
import cz.cuni.mff.d3s.roadtrain.demo.utils.Navigator;
import cz.cuni.mff.d3s.roadtrain.demo.utils.VehicleLink;
import cz.cuni.mff.d3s.roadtrain.demo.utils.VehicleState;

@Ensemble
@PeriodicScheduling(period = 1000)
@PartitionedBy("dstPlace")
public class LeaderFollower {
	/**
	 * Membership condition
	 * 
	 * Ensemble is created according to the leader link set by a process in
	 * the member. The coordinator of the ensemble is the leader while
	 * the member is the follower. Member and the coordinator have to be
	 * different vehicles and the follower needs to be in the state when
	 * it can follow (not on the train or train leader)
	 */
	@Membership
	public static boolean membership(
			@In("coord.id") String coordId,
			@In("coord.state") VehicleState coordState,
			@In("member.state") VehicleState memberState,
			@In("member.leader") VehicleLink memberLeader,
			@In("member.trainId") String memberTrainId) {
		// Member is following coordinator and they are not part of the road train
		return memberLeader != null && memberLeader.id.equals(coordId) && coordState.canLead() && memberState.canFollow();
	}

	/**
	 * Knowledge exchange
	 * 
	 * Nearest follower knowledge is updated.
	 * Leader value is set
	 * Member can be assigned a train id.
	 * 
	 * The whole idea is obfuscated by time-stamps which will be hopefully
	 * handled by the jDEECo runtime. 
	 */
	@KnowledgeExchange
	public static void exchange(
			@In("coord.id") String coordId,
			@In("coord.currentLink") Id coordLink,
			@In("coord.trainId") String coordTrainId,
			@In("coord.curTime") Long coordTime,
			@In("member.curTime") Long memberTime,
			@InOut("coord.nearestFollower") ParamHolder<Double> nearestFollower,
			@In("coord.nearestFollowerTime") Long nearestFollowerTime,
			@In("member.id") String memberId,
			@In("member.currentLink") Id memberLink,
			@InOut("member.trainId") ParamHolder<String> memeberTrainId,
			@InOut("member.trainIdTime") ParamHolder<Long> memeberTrainIdTime,
			@Out("member.leader") ParamHolder<VehicleLink> leader,
			@In("member.state") VehicleState memberState) {
		double distance = Navigator.getLinkLinkDist(coordLink, memberLink);
				
		// Leader - follower distance		
		if (nearestFollower.value == null || nearestFollower.value > distance) {
			nearestFollower.value = distance;
			nearestFollowerTime = memberTime;
		}
		
		leader.value = new VehicleLink(coordId, coordLink, distance, coordTime);
		
		// Assign vehicle to train
		if(distance < Settings.TRAIN_FORM_DISTANCE && memberState == VehicleState.SINGLE || memberState == VehicleState.TRAIN_LEADER) {
			memeberTrainId.value = coordTrainId;
			memeberTrainIdTime.value = coordTime;
		}
	}
}
