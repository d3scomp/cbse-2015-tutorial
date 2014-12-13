package cz.cuni.mff.d3s.roadtrain.demo.utils;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public enum VehicleState {
	SINGLE,
	TRAIN_LEADER,
	TRAIN_MEMBER,
	TRAIN_TAIL,
	DONE;
	
	public boolean canLead() {
		return this == SINGLE || this == TRAIN_TAIL || this == TRAIN_LEADER;
	}
	
	public boolean canFollow() {
		return this == SINGLE || this == TRAIN_LEADER;
	}
	
	public boolean onTrain() {
		return this == TRAIN_LEADER || this == TRAIN_MEMBER || this == TRAIN_TAIL;
	}
	
	public boolean destinationExchange() {
		return canFollow() || canLead();
	}
	
	public String toShortString() {
		switch(this) {
		case SINGLE:
			return "S";
		case TRAIN_LEADER:
			return "L";
		case TRAIN_MEMBER:
			return "M";
		case TRAIN_TAIL:
			return "T";
		case DONE:
			return "D";
		default:
			throw new NotImplementedException();
		}
	}
}
