package cz.cuni.mff.d3s.roadtrain.demo.utils;

import org.matsim.api.core.v01.Id;

public class VehicleLink {
	public String id;
	public Id link;
	public double dist;
	
	public VehicleLink(String id, Id link, double dist) {
		this.id = id;
		this.link = link;
		this.dist = dist;
	}
	
	@Override
	public String toString() {
		return id;
	}
}
