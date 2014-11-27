package cz.cuni.mff.d3s.roadtrain.utils;

import java.io.Serializable;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;

public class VehicleInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public String id;
	public Coord position;
	public int trainNum;
	public Id link;

	public VehicleInfo(String id, Coord position, int trainNum, Id link) {
		this.id = id;
		this.position = position;
		this.trainNum = trainNum;
		this.link = link;
	}
}
