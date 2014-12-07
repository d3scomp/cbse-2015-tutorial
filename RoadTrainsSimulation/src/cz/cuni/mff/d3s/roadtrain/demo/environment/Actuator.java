package cz.cuni.mff.d3s.roadtrain.demo.environment;

public interface Actuator<T> {
	public void set(T value);
	public ActuatorType getActuatorType();
}
