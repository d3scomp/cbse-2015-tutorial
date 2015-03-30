package cz.cuni.mff.d3s.roadtrain.demo.components;

import org.matsim.api.core.v01.Id;

public abstract class AbstractVehicle {

	public abstract String getId();
	public abstract String getDstPlace();
	public abstract Id getCurrentLink();
}
