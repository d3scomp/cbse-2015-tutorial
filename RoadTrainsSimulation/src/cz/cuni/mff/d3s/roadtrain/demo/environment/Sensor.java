package cz.cuni.mff.d3s.roadtrain.demo.environment;

public interface Sensor<T> {
	
	SensorType getSensorType();
	T read();
}
