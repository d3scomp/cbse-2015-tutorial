package cz.cuni.mff.d3s.roadtrain.demo.utils;

import org.matsim.api.core.v01.Id;

public class VehicleLink {
	public String id;
	public Id link;
	public double dist;
	public long time;
	
	public VehicleLink(String id, Id link, double dist, long time) {
		this.id = id;
		this.link = link;
		this.dist = dist;
		this.time = time;
	}
	
	@Override
	public String toString() {
		return id;
	}
}
