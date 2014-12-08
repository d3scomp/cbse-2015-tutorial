package cz.cuni.mff.d3s.roadtrain.demo.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.utils.geometry.CoordImpl;

import cz.cuni.mff.d3s.deeco.simulation.matsim.MATSimRouter;
import cz.cuni.mff.d3s.roadtrain.demo.Settings;

public class Navigator {
	private static Map<String, Link> places = new HashMap<String, Link>();
	private static MATSimRouter router;
	private static Random random = new Random(42);
			
	public static void init(MATSimRouter router) {
		Navigator.router = router;
	
		// Fire stations
		putLocation("F1", 50.0763769, 14.4292897);
		putLocation("F2", 50.0864694, 14.3401217);
		putLocation("F3", 50.1069967, 14.4441611);		
		putLocation("F4", 50.0327289, 14.5027281);
		putLocation("F5", 50.0655097, 14.5001003);
		putLocation("F6", 50.0411614, 14.4488708);		
		putLocation("F7", 50.0593000, 14.3730000);
		putLocation("F8", 49.9921206, 14.3449481);		
		putLocation("F9", 50.0905178, 14.3988442);
		putLocation("F10", 50.1262236, 14.5761058);
		
		// Police stations
		putLocation("P1", 50.0909269, 14.4701122);
		putLocation("P2", 50.0845331, 14.4543175);
		putLocation("P3", 50.1306867, 14.4169069);
		putLocation("P4", 50.0910656, 14.4455614);
		putLocation("P5", 50.1214317, 14.4548733);
		putLocation("P6", 50.1132217, 14.4765136);
		putLocation("P7", 50.1283167, 14.4685800);
		putLocation("P8", 50.1509764, 14.5132225);
		putLocation("P9", 50.1157528, 14.6140792);
		putLocation("P10", 50.1087483, 14.5793525);
		putLocation("P11", 50.1371403, 14.5153011);
		putLocation("P12", 50.1236794, 14.4902867);
		putLocation("P13", 50.0771944, 14.6607231);
		putLocation("P14", 50.1062861, 14.4941333);		
		putLocation("P15", 50.083118, 14.418075);
		putLocation("P16", 50.089835, 14.426287);
		putLocation("P17", 50.07831, 14.427848);
		putLocation("P18", 50.087279, 14.400439);
		putLocation("P19", 50.08379, 14.423276);
		putLocation("P20", 50.088176, 14.367307);
		putLocation("P21", 50.102159, 14.406705);
		putLocation("P22", 50.107997, 14.27328);
		putLocation("P23", 50.065403, 14.304121);
		putLocation("P24", 50.099038, 14.357888);
		putLocation("P25", 50.104298, 14.453574);
		putLocation("P26", 50.0997156, 14.4305794);
		putLocation("P27", 50.087461, 14.432691);
		putLocation("P28", 50.109987, 14.438529);
		putLocation("P29", 50.071223, 14.41904);
		putLocation("P30", 50.070616, 14.434737);
		putLocation("P31", 50.031125, 14.369612);
		putLocation("P32", 50.053653, 14.344457);
		putLocation("P33", 49.989072, 14.375424);
		putLocation("P34", 50.071899, 14.409052);
		putLocation("P35", 50.044867, 14.321921);
		putLocation("P36", 50.065173, 14.287977);
		putLocation("P37", 50.0807119, 14.4323572);
		putLocation("P38", 50.061423, 14.408728);
		putLocation("P39", 50.033836, 14.488656);
		putLocation("P40", 50.016953, 14.446253);
		putLocation("P41", 50.004976, 14.414889);
		putLocation("P42", 50.061728, 14.443934);
		putLocation("P43", 50.061308, 14.426165);
		putLocation("P44", 50.048575, 14.431816);
		putLocation("P45", 50.056693, 14.497606);
		
		// Ambulance
		putLocation("A1", 50.1294783, 14.4887108);
		putLocation("A2", 50.1146342, 14.4837219);
		putLocation("A3", 50.1093850, 14.5730603);
		putLocation("A4", 50.0961644, 14.2843769);
		putLocation("A5", 50.0862753, 14.3397678);
		putLocation("A6", 50.0917392, 14.3915872);
		putLocation("A7", 50.0989994, 14.4328828);
		putLocation("A8", 50.0844208, 14.4536303);
		putLocation("A9", 50.0710958, 14.3793275);
		putLocation("A10", 50.0726533, 14.4018903);
		putLocation("A11", 50.0773275, 14.4154728);
		putLocation("A12", 50.0716433, 14.4600361);
		putLocation("A13", 50.0556475, 14.3439878);
		putLocation("A14", 50.0362922, 14.4110558);
		putLocation("A15", 50.0423561, 14.4442761);
		putLocation("A16", 50.0351742, 14.5111614);
		putLocation("A17", 50.0337653, 14.5936147);
		putLocation("A18", 49.9922836, 14.3883783);
	};
	
	public static void putLocation(String name, double lat , double lon) {
		places.put(name, router.findNearestLink(new CoordImpl(Settings.physToX(lon), Settings.physToY(lat))));
	}
	
	public static void putLink(String name, Link link) {
		places.put(name, link);
	}
	
	public static Collection<String> getPlaces() {
		return places.keySet();
	}
	
	public static Link getPosition(String name) {
		return places.get(name);
	}
	
	public static double getDesDist(String destination, Id currentLink) {
		return getLinkDistance(currentLink, getPosition(destination).getId());
	}
	
	public static double getLinkLinkDist(Id car1link, Id car2link) {
		return getLinkDistance(car1link, car2link);
	}
	
	public static double getDestDistUsingCar(String destination, Id currentLink, Id midCarLink) {
		return getLinkLinkDist(currentLink, midCarLink) + getDesDist(destination, midCarLink);
	}
	
	private static double getLinkDistance(Id link1, Id link2) {
		double dist = 0;
		for(Id id: router.route(link1, link2)) {
			dist += router.findLinkById(id).getLength();
		}		
		return dist;
	}
	
	public static double getEuclidDistance(Coord p1, Coord p2) {
		if (p1 == null || p2 == null) {
			return Double.POSITIVE_INFINITY;
		}

		double dx = p1.getX() - p2.getX();
		double dy = p1.getY() - p2.getY(); 

		return Math.sqrt(dx*dx + dy*dy);
	}
	
	public static String getRandomPlace(String prefix) {
		List<String> matching = getMatchingPlaces(prefix);
		return matching.remove(random.nextInt(matching.size() - 1));
	}
	
	public static String getNearestPlace(String prefix, String toPlace, Set<String> ommit) {
		String nearest = null;
		double nearestDist = 0;
		
		List<String> matching = getMatchingPlaces(prefix);
		matching.removeAll(ommit);
		
		for(String place: matching) {
			Link pos = Navigator.getPosition(place);
			double dist = Navigator.getDesDist(toPlace, pos.getId());
			
			if(nearest == null || dist < nearestDist) {
				nearest = place;
				nearestDist = dist;
			}
		}
		
		return nearest;
	}
	
	public static List<String> getMatchingPlaces(String prefix) {
		// Get matching places
		List<String> matching = new LinkedList<String>();
		for(String place: Navigator.getPlaces()) {
			if(place.startsWith(prefix)) {
				matching.add(place);
			}
		}
		return matching;
	}
	
	public static Link getRandomLinkAround(double x, double y, double radius) {
		double dx = (0.5 - random.nextDouble()) * 2 * radius;
		double dy = (0.5 - random.nextDouble()) * 2 * radius;
		return router.findNearestLink(new CoordImpl(x + dx, y + dy));
	}
	
	public static Link getRandomLink() {
		double x = Settings.MIN_X + random.nextDouble() * Settings.WIDTH;
		double y = Settings.MIN_Y + random.nextDouble() * Settings.HEIGHT;
		return router.findNearestLink(new CoordImpl(x, y));
	}
}
