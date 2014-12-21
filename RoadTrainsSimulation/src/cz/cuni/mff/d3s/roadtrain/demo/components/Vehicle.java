package cz.cuni.mff.d3s.roadtrain.demo.components;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;

import cz.cuni.mff.d3s.deeco.annotations.Component;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.Local;
import cz.cuni.mff.d3s.deeco.annotations.Out;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.deeco.logging.Log;
import cz.cuni.mff.d3s.deeco.scheduler.CurrentTimeProvider;
import cz.cuni.mff.d3s.deeco.simulation.matsim.MATSimRouter;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.roadtrain.demo.Settings;
import cz.cuni.mff.d3s.roadtrain.demo.environment.Actuator;
import cz.cuni.mff.d3s.roadtrain.demo.environment.ActuatorProvider;
import cz.cuni.mff.d3s.roadtrain.demo.environment.ActuatorType;
import cz.cuni.mff.d3s.roadtrain.demo.environment.Sensor;
import cz.cuni.mff.d3s.roadtrain.demo.environment.SensorProvider;
import cz.cuni.mff.d3s.roadtrain.demo.environment.SensorType;
import cz.cuni.mff.d3s.roadtrain.demo.environment.VehicleMonitor;
import cz.cuni.mff.d3s.roadtrain.demo.utils.Navigator;
import cz.cuni.mff.d3s.roadtrain.demo.utils.VehicleInfo;
import cz.cuni.mff.d3s.roadtrain.demo.utils.VehicleLink;
import cz.cuni.mff.d3s.roadtrain.demo.utils.VehicleState;

@Component
public class Vehicle {
	/**
	 * Id of the vehicle component.
	 */
	public String id;
	
	public VehicleState state = VehicleState.SINGLE;

	/**
	 * Contains a list of link ids that lead to the destination. It is given to
	 * the MATSim to guide the vehicle which way it should go.
	 */
	@Local
	public List<Id> route;
	
	public Map<String, VehicleInfo> destGroup = new HashMap<String, VehicleInfo>();
	
	public Map<String, VehicleInfo> trainGroup = new HashMap<String, VehicleInfo>();

	/**
	 * Link where the vehicle is currently at.
	 */
	public Id currentLink;
	
	public Long trainIdTime;
	public String trainId;
	
	public VehicleLink leader;
	
	public Double nearestFollower = null;
	public Long nearestFollowerTime;
	
	public VehicleLink trainFollower;
	
	public Double speed;
	
	/**
	 * Destination city
	 */
	public String dstPlace;
	
	/**
	 * Position of the current link.
	 */
	public Coord position;
	
	public Coord destination;
	
	@Local
	public VehicleMonitor vehicleMonitor;
	
	@Local
	public Actuator<List<Id> > routeActuator;
	
	@Local
	public Actuator<Double> speedActuator;
	
	@Local
	public Sensor<Id> currentLinkSensor;

	@Local
	public MATSimRouter router;
	
	@Local
	public CurrentTimeProvider clock;
	
	public Long curTime;

	public Vehicle(String id, String dstPlace, Id currentLink,
			ActuatorProvider actuatorProvider, SensorProvider sensorProvider,
			MATSimRouter router, CurrentTimeProvider clock, VehicleMonitor vehicleMonitor) {
		this.id = id;
		this.trainId = id;
		this.dstPlace = dstPlace;
		this.currentLink = currentLink;
		this.routeActuator = actuatorProvider.createActuator(ActuatorType.ROUTE);
		this.speedActuator = actuatorProvider.createActuator(ActuatorType.SPEED);
		this.currentLinkSensor = sensorProvider.createSensor(SensorType.CURRENT_LINK);
		this.router = router;
		this.clock = clock;
		this.vehicleMonitor = vehicleMonitor;
	}

	/**
	 * Periodically prints out the values of sensors and important values of the
	 * knowledge.
	 */
	@Process
	@PeriodicScheduling(period = 5000, order = 10)
	public static void reportStatus(
			@In("id") String id,
			@In("state") VehicleState state,
 			@In("currentLinkSensor") Sensor<Id> currentLinkSensor,
			@In("position") Coord position,
			@In("dstPlace") String dstPlace,
			@In("leader") VehicleLink leader,
			@In("destGroup") Map<String, VehicleInfo> destGroup,
			@In("trainGroup") Map<String, VehicleInfo> trainGroup,
			@In("route") List<Id> route,
			@In("clock") CurrentTimeProvider clock,
			@In("nearestFollower") Double nearestFollower,
			@In("router") MATSimRouter router,
			@In("trainId") String trainId,
			@In("trainFollower") VehicleLink trainFollower,
			@In("speed") Double speed,
			@In("vehicleMonitor") VehicleMonitor vehicleMonitor) {

		Log.d("Entry [" + id + "]:reportStatus");

		System.out.format("%s [%s] state: %s, pos: %s(%.0f, %.0f), GDest: %s, GTrain: %s, dist: %.0f, leader: %s, dst: %s(%s), train: %s, tFollower: %s, speed: %.0f\n",
				formatTime(clock.getCurrentMilliseconds()),
				id,
				state,
				currentLinkSensor.read(),
				position.getX(),
				position.getY(),
				groupToString(destGroup),
				groupToString(trainGroup),
				Navigator.getDesDist(dstPlace, currentLinkSensor.read()),
				leader,
				Navigator.getPosition(dstPlace).getId(),
				dstPlace,
				trainId,
				trainFollower,
				speed);
		
		// Report information about vehicle
		vehicleMonitor.report(
				clock.getCurrentMilliseconds(),
				id,
				state,
				position,
				leader,
				dstPlace,
				route,
				router,
				nearestFollower,
				trainId);
	}

	/**
	 * Periodically updates knowledge based on sensor readings. These knowledge
	 * fields are updated: currentLink.
	 */
	@Process
	@PeriodicScheduling(period = 200, order = 1)
	public static void updateSensors(
			@In("id") String id,
			@Out("currentLink") ParamHolder<Id> currentLinkHolder,
			@Out("position") ParamHolder<Coord> position,
			@In("currentLinkSensor") Sensor<Id> currentLinkSensor,
			@In("router") MATSimRouter router,
			@In("clock") CurrentTimeProvider clock,
			@Out("curTime") ParamHolder<Long> curTime) {
		Log.d("Entry [" + id + "]:updateCurrentLink");
		currentLinkHolder.value = currentLinkSensor.read();
		position.value = router.getLink(currentLinkHolder.value).getCoord();
		curTime.value = clock.getCurrentMilliseconds();
	}
	
	@Process
	@PeriodicScheduling(period = 200)
	public static void updateState(
			@In("id") String id,
			@In("dstPlace") String dstPlace,
			@In("currentLink") Id currentLink,
			@In("trainId") String trainId,
			@In("leader") VehicleLink leader,
			@In("trainFollower") VehicleLink trainFollower,
			@Out("state") ParamHolder<VehicleState> state) {
		// Decide vehicle state
		
		// Done
		if(Navigator.getDesDist(dstPlace, currentLink) == 0) {
			state.value = VehicleState.DONE;
			return;
		}
		
		// Single
		if(id.equals(trainId) && trainFollower == null) {
			state.value = VehicleState.SINGLE;
			return;
		}
		
		// Train leader
		if(id.equals(trainId) && trainFollower != null) {
			state.value = VehicleState.TRAIN_LEADER;
			return;
		}
		
		// Train member
		if(leader != null && trainFollower != null) {
			state.value = VehicleState.TRAIN_MEMBER;
			return;
		}
		
		// Train tail
		if(leader != null && trainFollower == null) {
			state.value = VehicleState.TRAIN_TAIL;
			return;
		}
		
		throw new RuntimeException(String.format("Vehicle %s is in invalid state", id));
	}
	
	@Process
	@PeriodicScheduling(period = 5000)
	public static void organizeLeaderFollowerLinks(
			@In("id") String id,
			@In("state") VehicleState state,
			@In("destGroup") Map<String, VehicleInfo> destGroup,
			@In("dstPlace") String dstPlace,
			@In("currentLink") Id currentLink,
			@In("clock") CurrentTimeProvider clock,
			@InOut("leader") ParamHolder<VehicleLink> leader) {
		double myTargetDist = Navigator.getDesDist(dstPlace, currentLink);
		
	//	Double leaderDist = null;
	//	if(leader.value != null) {
	//		leaderDist = Navigator.getLinkLinkDist(currentLink, leader.value.link);
	//	}
				
		// Do nothing when not single vehicle
		if(!state.canFollow()) {
			return;
		}
		
		// Try to find a car to follow
		String nearestCarId = null;
		Double nearestDist = null;
		Id nearestCarLink = null;
		for(VehicleInfo info: destGroup.values()) {
			Id carLink = info.link;
			double distToCar = Navigator.getLinkLinkDist(currentLink, carLink);
			double carToDestDist = Navigator.getDesDist(dstPlace, carLink);
			double distUsingCar = distToCar + carToDestDist;
			
			// Skip cars already at destination
			if(carToDestDist == 0) continue;
			
			// Skip cars which are too far
//			System.out.println(String.format("Dist: %s", distToCar));
			if(distToCar > Settings.LINK_FORM_DISTANCE) continue;
			
			// Follow only car on the route to destination
			//boolean distCond = myTargetDist - distUsingCar < distToCar / 4;
			boolean distCond = myTargetDist - distUsingCar > -1;// || (leader.value != null && leader.value.id.equals(info.id));
//			if(distCond)
//				System.out.println(String.format("%s -> %s = %s", myTargetDist, distUsingCar, myTargetDist - distUsingCar));
			
			boolean leaderDistCond = true; //leaderDist == null || distToCar < leaderDist / 4;
			
			
			// Do not follow car on the same link if it was not followed before
			boolean sameLinkCheck = !carLink.equals(currentLink) || (info.id.equals(leader.value.id));
			
			if((distCond && sameLinkCheck && leaderDistCond) && (nearestDist == null || nearestDist > distToCar)) {
				nearestCarId = info.id;
				nearestDist = distToCar;
				nearestCarLink = info.link;
			}
		}
		
		// Follow the car or lead new road train
		if(nearestCarId != null) {
			// There is car that is in front of us on the path to destination
			// and the road train is short enough -> follow it
			leader.value = new VehicleLink(nearestCarId, nearestCarLink, nearestDist, clock.getCurrentMilliseconds());
		} else {
			// There is no car in front of us on the path to destination,
			// or road train is too long -> lead the new train
			leader.value = null;
		}
	}
	
	@Process
	@PeriodicScheduling(period = 1000)
	public static void organizeTrain(
			@In("state") VehicleState state,
			@In("trainGroup") Map<String, VehicleInfo> train,
			@In("trainId") String trainId,
			@InOut("trainIdTime") ParamHolder<Long> trainIdTime,
			@In("currentLink") Id currentLink,
			@In("curTime") Long curTime,
			@InOut("leader") ParamHolder<VehicleLink> leader) {
		// Do nothing when not on train
		if(!state.onTrain()) {
			return;
		}
		
		// Get train leader
		VehicleInfo trainLeader = train.get(trainId);
		
		if(trainLeader == null) {
			return;
		}
		
		trainIdTime.value = curTime;
		
		// Get car to follow
		Double nearestDist = null;
		for(VehicleInfo info: train.values()) {
			double distToCar = Navigator.getLinkLinkDist(currentLink, info.link);
			double carToDestDist = Navigator.getLinkLinkDist(info.link, trainLeader.link);
			double distUsingCar = distToCar + carToDestDist;
			
			boolean sameLinkCheck = !info.link.equals(currentLink) || (info.id.equals(leader.value.id));
			
			if(sameLinkCheck && (nearestDist == null || nearestDist > distUsingCar)) {
				nearestDist = distUsingCar;
				leader.value = new VehicleLink(info.id, info.link, distUsingCar, curTime);
			}
		}
	}
	
	@Process
	@PeriodicScheduling(period = 1500)
	public static void resetOldData(
			@InOut("nearestFollower") ParamHolder<Double> nearestFollower,
			@In("nearestFollowerTime") Long nearestFollowerTime,			
			@InOut("trainFollower") ParamHolder<VehicleLink> trainFollower,
			@InOut("destGroup") ParamHolder<Map<String, VehicleInfo> > destGroup,
			@InOut("trainGroup") ParamHolder<Map<String, VehicleInfo> > trainGroup,
			@InOut("leader") ParamHolder<VehicleLink> leader,
			@In("trainIdTime") Long trainIdTime,
			@InOut("trainId") ParamHolder<String> trainId,
			@In("id") String id,
			@In("curTime") Long curTime) {
		// Reset followers
		if(nearestFollowerTime == null || curTime - nearestFollowerTime > 10000) {
			nearestFollower.value = null;
		}
		
		if(trainIdTime == null || curTime - trainIdTime > 3000) {
			trainId.value = id;
		}
		
		// Reset train follower value when too old
		if(trainFollower.value != null && curTime - trainFollower.value.time > 3000) {
			trainFollower.value = null;
		}
		
		// Reset leader when value is too old
		if(leader.value != null && curTime - leader.value.time > 10000) {
			leader.value = null;
		}
		
		// Clear shared destination group old data
		Iterator<VehicleInfo> it = destGroup.value.values().iterator();
		while(it.hasNext()) {
			VehicleInfo info = it.next();
			if(curTime - info.time > 10000) {
				it.remove();
			}
		}
		
		// Clear train group old data
		it = trainGroup.value.values().iterator();
		while(it.hasNext()) {
			VehicleInfo info = it.next();
			if(curTime - info.time > 10000) {
				it.remove();
			}
		}
		
		// Reset leader link when leader not present in groups
		if(leader.value != null) {
			if(!destGroup.value.containsKey(leader.value.id) && !trainGroup.value.containsKey(leader.value.id)) {
				leader.value = null;
			}
		}
	}
	
	/**
	 * Plans the route to the destination.
	 */
	@Process
	@PeriodicScheduling(period = 2000, order = 4)
	public static void planRouteAndDrive(
			@In("id") String id,
			@In("state") VehicleState state,
			@In("currentLink") Id currentLink,
			@In("leader") VehicleLink leader,
			@In("dstPlace") String dstPlace,
			@InOut("route") ParamHolder<List<Id> > route,
			@In("routeActuator") Actuator<List<Id> > routeActuator,
			@In("speedActuator") Actuator<Double> speedActuator,
			@In("router") MATSimRouter router,
			@In("nearestFollower") Double nearestFollower,
			@In("trainFollower") VehicleLink trainFollower,
			@Out("speed") ParamHolder<Double> speed) throws Exception {
		
		boolean wait = false;
		
		// Wait for follower
		if(nearestFollower != null) {
			System.out.println(id + " waiting for non train followers");
			wait = true;
		}
		
		// Wait for train follower
/*		if(trainFollower != null && trainFollower.dist > Settings.LINK_MAX_CAR_DIST) {
			System.out.println(id + " waiting for train followers: " + trainFollower.dist);
			wait = true;
		}*/
		
		// Wait for train leaders
		if(state.onTrain() && state != VehicleState.TRAIN_LEADER && leader != null && leader.dist < Settings.TRAIN_MIN_CAR_DIST) {
			System.out.println(id + " waiting to let train leader lead: " + leader.dist);
			wait = true;
		}
		
		if(!wait) {
			if(state == VehicleState.TRAIN_LEADER)
				speed.value = Settings.VEHICLE_LEADER_SPEED;
			else
				speed.value = Settings.VEHICLE_FULL_SPEED;
		} else {
			speed.value = Settings.VEHICLE_WAIT_SPEED;
		}
		speedActuator.set(speed.value);
		
		// No car in front of us -> drive directly to destination
		if(leader == null) {
			route.value = router.route(currentLink, Navigator.getPosition(dstPlace).getId(), route.value);
		}
		
		// Car in front of us -> follow it
		if(leader != null) {
			route.value = router.route(currentLink, leader.link, route.value);
		}
		
		// Already at the destination -> stop
		if(state == VehicleState.DONE) {
			route.value = new LinkedList<Id>();
		}
		
		routeActuator.set(route.value);
	}
	
	private static String groupToString(Map<String, VehicleInfo> destGroup) {
		StringBuilder builder = new StringBuilder();
		
		boolean first = true;
		builder.append("[");
		for(Entry<String, VehicleInfo> entry: destGroup.entrySet()) {
			if(!first)
				builder.append(", ");
			first = false;
			builder.append(entry.getKey());
		}
		builder.append("]");
		
		return builder.toString();
	}
	
	private static String formatTime(long ts) {
		int msec = (int) (ts % 1000);
		ts = ts / 1000;
		int sec = (int) (ts % 60);
		ts = ts / 60;
		int min = (int) (ts % 60);
		ts = ts / 60;
		int hour = (int) ts;
		
		return String.format("<%02d:%02d:%02d.%03d>", hour, min, sec, msec);
	}
}
