package cz.cuni.mff.d3s.roadtrain.demo.utils;

import java.io.Serializable;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;

public class VehicleInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public String id;
	public Coord position;
	public Id link;
	public long time;
	public VehicleState state;

	public VehicleInfo(String id, Coord position, Id link, long time, VehicleState state) {
		this.id = id;
		this.position = position;
		this.link = link;
		this.time = time;
		this.state = state;
	}
}
