package cz.cuni.mff.d3s.roadtrain.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.utils.geometry.CoordImpl;

import cz.cuni.mff.d3s.deeco.simulation.matsim.MATSimRouter;
import cz.cuni.mff.d3s.roadtrain.demo.Settings;

public class Navigator {
	private static Map<String, Link> places = new HashMap<String, Link>();
	private static MATSimRouter router;
			
	public static void init(MATSimRouter router) {
		Navigator.router = router;
		places.put("Praha", router.findNearestLink(new CoordImpl(Settings.MIN_X + Settings.WIDTH * 0.10, Settings.MIN_Y + Settings.HEIGHT * 0.10)));
		places.put("Brno", router.findNearestLink(new CoordImpl(Settings.MIN_X + Settings.WIDTH * 0.10, Settings.MIN_Y + Settings.HEIGHT * 0.90)));		
		places.put("Liberec", router.findNearestLink(new CoordImpl(Settings.MIN_X + Settings.WIDTH * 0.75, Settings.MIN_Y + Settings.HEIGHT * 0.25)));
		places.put("Ostrava", router.findNearestLink(new CoordImpl(Settings.MIN_X + Settings.WIDTH * 0.75, Settings.MIN_Y + Settings.HEIGHT * 0.75)));
		
		// Fire stations
		putLocation("HS_1", 50.0763769, 14.4292897);
		putLocation("HS_2", 50.0864694, 14.3401217);
		putLocation("HS_3", 50.1069967, 14.4441611);		
		putLocation("HS_4", 50.0327289, 14.5027281);
		putLocation("HS_5", 50.0655097, 14.5001003);
		putLocation("HS_6", 50.0411614, 14.4488708);		
		putLocation("HS_7", 50.0593000, 14.3730000);
		putLocation("HS_8", 49.9921206, 14.3449481);		
		putLocation("HS_9", 50.0905178, 14.3988442);
		putLocation("HS_10", 50.1262236, 14.5761058);
	};
	
	private static void putLocation(String name, double lat , double lon) {
		places.put(name, router.findNearestLink(new CoordImpl(Settings.physToX(lon), Settings.physToY(lat))));
	}
	
	public static Collection<String> getCities() {
		return places.keySet();
	}
	
	public static Link getPosition(String name) {
		return places.get(name);
	}
	
	public static double getDesDist(String destination, Id currentLink) {
		return getLinkDistance(currentLink, getPosition(destination).getId());
	}
	
	public static double getCarToCarDist(Id car1link, Id car2link) {
		return getLinkDistance(car1link, car2link);
	}
	
	public static double getDestDistUsingCar(String destination, Id currentLink, Id midCarLink) {
		return getCarToCarDist(currentLink, midCarLink) + getDesDist(destination, midCarLink);
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
}
