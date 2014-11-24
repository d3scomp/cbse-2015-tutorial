package cz.cuni.mff.d3s.roadtrain.utils;

import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.utils.geometry.CoordImpl;

import cz.cuni.mff.d3s.deeco.simulation.matsim.MATSimRouter;

public class Navigator {
	private static Map<String, Link> places = new HashMap<String, Link>();
	private static MATSimRouter router;
			
	public static void init(MATSimRouter router, double dim) {
		Navigator.router = router;
		places.put("Praha", router.findNearestLink(new CoordImpl(dim * 0.10, dim * 0.10)));
		places.put("Brno", router.findNearestLink(new CoordImpl(dim * 0.10, dim * 0.90)));		
		places.put("Liberec", router.findNearestLink(new CoordImpl(dim * 0.75, dim * 0.25)));
		places.put("Ostrava", router.findNearestLink(new CoordImpl(dim * 0.75, dim * 0.75)));
	};
	
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
		return router.route(link1, link2).size();
	/*	Coord coord1 = router.findLinkById(link1).getCoord();
		Coord coord2 = router.findLinkById(link2).getCoord();
		return getEuclidDistance(coord1, coord2);*/
	}
	
	private static double getEuclidDistance(Coord p1, Coord p2) {
		if (p1 == null || p2 == null) {
			return Double.POSITIVE_INFINITY;
		}

		double dx = p1.getX() - p2.getX();
		double dy = p1.getY() - p2.getY(); 

		return Math.sqrt(dx*dx + dy*dy);
	}
}
