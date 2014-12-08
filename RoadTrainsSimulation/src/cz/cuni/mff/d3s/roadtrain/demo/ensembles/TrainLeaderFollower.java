package cz.cuni.mff.d3s.roadtrain.demo.ensembles;

import org.matsim.api.core.v01.Id;

import cz.cuni.mff.d3s.deeco.annotations.Ensemble;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.KnowledgeExchange;
import cz.cuni.mff.d3s.deeco.annotations.Membership;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.roadtrain.demo.utils.Navigator;

@Ensemble
@PeriodicScheduling(period = 1000)
public class TrainLeaderFollower {
	@Membership
	public static boolean membership(
			@In("coord.id") String coordId,
			@In("member.id") String memberId,
			@In("member.leaderId") String memeberLeaderId,
			@In("coord.trainId") String coordTrainId,
			@In("member.trainId") String memberTrainId) {
		// Member is following coordinator in the road train
		return memeberLeaderId.equals(coordId) && memberTrainId.equals(coordTrainId);
	}

	@KnowledgeExchange
	public static void exchange(
			@In("coord.id") String coordId,
			@In("member.id") String memberId,
			@In("coord.currentLink") Id coordLink,
			@In("member.currentLink") Id memberLink,
			@InOut("coord.trainFollowerId") ParamHolder<String> followerId,
			@InOut("coord.trainFollowerDist") ParamHolder<Double> followerDist,
			@In("coord.navigator") Navigator navigator) {
		Double dist = navigator.getLinkLinkDist(coordLink, memberLink);
		
		// TODO: maybe timed rest is needed when some follower gets out of range
		if(followerDist.value == null || followerDist.value > dist || followerId.equals(memberId)) {
			followerId.value = memberId;
			followerDist.value = dist;
		}
	}
}
