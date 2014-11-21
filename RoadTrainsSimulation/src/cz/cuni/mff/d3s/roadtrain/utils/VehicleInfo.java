package cz.cuni.mff.d3s.roadtrain.utils;

import java.io.Serializable;

import org.matsim.api.core.v01.Coord;

public class VehicleInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	public VehicleInfo(Coord position/*, String trainId*/) {
		this.position = position;
	//	this.trainId = trainId;
	}
	
	public Coord position;
//	public String trainId;
}
