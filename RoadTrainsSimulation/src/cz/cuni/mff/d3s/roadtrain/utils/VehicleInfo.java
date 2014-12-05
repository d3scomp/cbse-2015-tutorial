package cz.cuni.mff.d3s.roadtrain.utils;

import java.io.Serializable;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;

public class VehicleInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public String id;
	public Coord position;
	public Id link;
	public long time;

	public VehicleInfo(String id, Coord position, Id link, long time) {
		this.id = id;
		this.position = position;
		this.link = link;
		this.time = time;
	}
}
