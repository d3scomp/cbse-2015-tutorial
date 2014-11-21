package cz.cuni.mff.d3s.roadtrain.utils;

import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.utils.geometry.CoordImpl;

import cz.cuni.mff.d3s.deeco.simulation.matsim.MATSimRouter;

public class Navigator {
	private static Map<String, Link> places = new HashMap<String, Link>();
	private static MATSimRouter router;
			
	public static void init(MATSimRouter router, double dim) {
		Navigator.router = router;
		places.put("Praha", router.findNearestLink(new CoordImpl(dim * 0.25, dim * 0.25)));
		places.put("Brno", router.findNearestLink(new CoordImpl(dim * 0.25, dim * 0.75)));		
		places.put("Liberec", router.findNearestLink(new CoordImpl(dim * 0.25, dim * 0.75)));
		places.put("Ostrava", router.findNearestLink(new CoordImpl(dim * 0.75, dim * 0.75)));
	};
	
	public static Link getPosition(String name) {
		return places.get(name);
	}
	
	public static double getDesDist(String destination, Id currentLink) {
		return router.route(currentLink, getPosition(destination).getId()).size();
	}
	
	public static double getCarToCarDist(Id car1link, Id car2link) {
		return router.route(car1link, car2link).size();
	}
	
	public static double getDestDistUsingCar(String destination, Id currentLink, Id midCarLink) {
		return getCarToCarDist(currentLink, midCarLink) + getDesDist(destination, midCarLink);
	}
}
