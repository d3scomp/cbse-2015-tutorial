package cz.cuni.mff.d3s.roadtrain.components;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

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
import cz.cuni.mff.d3s.demo.environment.Actuator;
import cz.cuni.mff.d3s.demo.environment.ActuatorProvider;
import cz.cuni.mff.d3s.demo.environment.ActuatorType;
import cz.cuni.mff.d3s.demo.environment.Sensor;
import cz.cuni.mff.d3s.demo.environment.SensorProvider;
import cz.cuni.mff.d3s.demo.environment.SensorType;
import cz.cuni.mff.d3s.roadtrain.utils.Navigator;

@Component
public class Vehicle {
	/**
	 * Id of the vehicle component.
	 */
	public String id;

	/**
	 * Contains a list of link ids that lead to the destination. It is given to
	 * the MATSim to guide the vehicle which way it should go.
	 */
	@Local
	public List<Id> route;
	
	public Map<String, Coord> groupPos = new HashMap<String, Coord>();

	/**
	 * Link where the vehicle is currently at.
	 */
	public Id currentLink;
	
	public String leaderCar;
	
	public int carNum;
	
	/**
	 * Destination city
	 */
	public String dstCity;
	
	/**
	 * Position of the current link.
	 */
	public Coord position;
	
	public Coord destination;

	@Local
	public Actuator<List<Id>> routeActuator;
	
	@Local
	public Sensor<Id> currentLinkSensor;

	@Local
	public MATSimRouter router;
	
	@Local
	public CurrentTimeProvider clock;

	public Vehicle(String id, String dstCity, Id currentLink,
			ActuatorProvider actuatorProvider, SensorProvider sensorProvider,
			MATSimRouter router, CurrentTimeProvider clock) {
		this.id = id;
		this.dstCity = dstCity;
		this.routeActuator = actuatorProvider.createActuator(ActuatorType.ROUTE);
		this.currentLinkSensor = sensorProvider.createSensor(SensorType.CURRENT_LINK);
		this.router = router;
		this.clock = clock;
	}

	/**
	 * Periodically prints out the values of sensors and important values of the
	 * knowledge.
	 */
	@Process
	@PeriodicScheduling(period = 5000, order = 10)
	public static void reportStatus(
			@In("id") String id,
			@In("currentLinkSensor") Sensor<Id> currentLinkSensor,
			@In("dstCity") String dstCity,
			@In("carNum") int carNum,
			@In("leaderCar") String leaderCar,
			@In("groupPos") Map<String, Coord> groupPos,
			@In("route") List<Id> route, @In("clock") CurrentTimeProvider clock) {

		Log.d("Entry [" + id + "]:reportStatus");

		System.out.format("%s [%s] pos: %s, group: %s, dist: %s, prevCar: %s, trainNum: %s, dst: %s(%s), route: %s\n",
				formatTime(clock.getCurrentMilliseconds()),
				id, currentLinkSensor.read(),
				groupToString(groupPos),
				Navigator.getDesDist(dstCity, currentLinkSensor.read()),
				leaderCar,
				carNum,
				getDstLinkId(dstCity),
				dstCity,
				route);
	}

	/**
	 * Periodically updates knowledge based on sensor readings. These knowledge
	 * fields are updated: currentLink.
	 */
	@Process
	@PeriodicScheduling(period = 200, order = 1)
	public static void updateSensors(@In("id") String id,
			@Out("currentLink") ParamHolder<Id> currentLinkHolder,
			@Out("position") ParamHolder<Coord> position,
			@In("currentLinkSensor") Sensor<Id> currentLinkSensor,
			@In("router") MATSimRouter router) {

		Log.d("Entry [" + id + "]:updateCurrentLink");

		currentLinkHolder.value = currentLinkSensor.read();
		position.value = router.getLink(currentLinkHolder.value).getCoord();
	}
	
	@Process
	@PeriodicScheduling(period = 5000)
	public static void organizeRoadTrains(
			@In("id") String id,
			@In("groupPos") Map<String, Coord> groupPos,
			@In("dstCity") String dstCity,
			@In("currentLinkSensor") Sensor<Id> currentLinkSensor,
			@In("router") MATSimRouter router,
			@InOut("leaderCar") ParamHolder<String> leaderCar,
			@InOut("carNum") ParamHolder<Integer> carNum) {
		
		Id currentLink = currentLinkSensor.read();
		
		double myTargetDist = router.route(currentLink, getDstLinkId(dstCity)).size();
		
		// Do nothing when already at destination
		if(myTargetDist == 0)
			return;
		
		// Try to find a car to follow		
		String nearestCar = null;
		Double nearestDist = null;
		for(Entry<String, Coord> entry: groupPos.entrySet()) {
			Id carLink = router.findNearestLink(entry.getValue()).getId();
			double distUsingCar = Navigator.getDestDistUsingCar(dstCity, currentLink, carLink);
			double distToCar = Navigator.getCarToCarDist(currentLink, carLink);
			double catToDestDist = Navigator.getDesDist(dstCity, carLink);
			
			// Skip ourself
			if(entry.getKey().equals(id))
				continue;
						
			// Skip cars already at destination
			if(catToDestDist == 0)
				continue;
			
			// Route using car position is beneficial (length using the car is the same as without)
			if(myTargetDist == distUsingCar && (nearestDist == null || nearestDist > distToCar)) {
				nearestCar = entry.getKey();
				nearestDist = distToCar;
			}
		}
		
		// There is car that is in front of us on the path to destination -> follow it
		if(nearestCar != null) {
			leaderCar.value = nearestCar;
		} else {
			leaderCar.value = null;
			carNum.value = 0;
		}
	}
	
	/**
	 * Plans the route to the destination.
	 */
	@Process
	@PeriodicScheduling(period = 2000, order = 4)
	public static void planRouteAndDrive(
			@In("id") String id,
			@In("currentLink") Id currentLink,
			@In("leaderCar") String leaderCar,
			@In("groupPos") Map<String, Coord> groupPos,
			@In("dstCity") String dstCity,
			@InOut("route") ParamHolder<List<Id>> route,
			@In("routeActuator") Actuator<List<Id>> routeActuator,
			@In("router") MATSimRouter router) throws Exception {
		
		// Already at the destination -> stop
		if(currentLink.equals(Navigator.getPosition(dstCity).getId())) {
			route.value = new LinkedList<Id>();
		}
		
		// No car in front of us -> drive directly to destination
		if(leaderCar == null) {
			route.value = router.route(currentLink, getDstLinkId(dstCity), route.value);
		}
		
		// Car in front of us -> follow it
		if(leaderCar != null) {
			Coord carPos = groupPos.get(leaderCar);
			Link carLink = router.findNearestLink(carPos);
			route.value = router.route(currentLink, carLink.getId(), route.value);
		}
		
		routeActuator.set(route.value);
	}
	
	private static Id getDstLinkId(String dstCity) {
		return Navigator.getPosition(dstCity).getId();
	}
	
	private static String groupToString(Map<String, Coord> groupPos) {
		StringBuilder builder = new StringBuilder();
		
		boolean first = true;
		builder.append("[");
		for(Entry<String, Coord> entry: groupPos.entrySet()) {
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
