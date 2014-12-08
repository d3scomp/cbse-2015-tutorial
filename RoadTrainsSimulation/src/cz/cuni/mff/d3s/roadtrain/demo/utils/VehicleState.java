package cz.cuni.mff.d3s.roadtrain.demo.utils;

public enum VehicleState {
	SINGLE,
	TRAIN_LEADER,
	TRAIN_MEMBER,
	TRAIN_TAIL,
	DONE;
	
	public boolean canLead() {
		return this == SINGLE || this == TRAIN_TAIL;
	}
	
	public boolean canFollow() {
		return this == SINGLE;
	}
	
	public boolean onTrain() {
		return this == TRAIN_LEADER || this == TRAIN_MEMBER || this == TRAIN_TAIL;
	}
	
	public boolean destinationExchange() {
		return this == SINGLE || canFollow() || canLead();
	}
}
