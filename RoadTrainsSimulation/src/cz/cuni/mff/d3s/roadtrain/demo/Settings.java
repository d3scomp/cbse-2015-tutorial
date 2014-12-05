package cz.cuni.mff.d3s.roadtrain.demo;

public class Settings {
	//public static double mapDimension = Settings.CELL_COUNT*Settings.LINKS_PER_EDGE*Settings.LINK_LENGTH;
	
	public static final String MATSIM_CONFIG_TEMPLATE = "input/config.xml";
	public static final String ANALYSES_DIRECTORY = "measurements";
	
	// Map parameters
	public static final double MIN_X = 1090633.9990942501;
	public static final double MAX_X = 1108409.9502133469;
	public static final double MIN_Y = 564064.6541308606;
	public static final double MAX_Y = 577009.0102368896;
	public static final double WIDTH = MAX_X - MIN_X;
	public static final double HEIGHT = MAX_Y - MIN_Y;
	
	public static final double PHYS_MIN_X = 14.2819905; 
	public static final double PHYS_MAX_X = 14.5509804; 
	public static final double PHYS_MIN_Y = 50.0248755; 
	public static final double PHYS_MAX_Y = 50.1307352;
	public static final double PHYS_WIDTH = PHYS_MAX_X - PHYS_MIN_X;
	public static final double PHYS_HEIGHT = PHYS_MAX_Y - PHYS_MIN_Y;
	
	public static double physToX(double physX) {
		return MIN_X + ((physX - PHYS_MIN_X) / (PHYS_WIDTH)) * WIDTH;
	}
	public static double physToY(double physY) {
		return MIN_Y + ((physY - PHYS_MIN_Y) / (PHYS_HEIGHT)) * HEIGHT;
	}
	
	// Train parameters
	public static final double TRAIN_FORM_DISTANCE = 1000; // m
	
	public static final double LINK_FORM_DISTANCE = 5000; // m
	public static final double LINK_MAX_CAR_DIST = 2000; // m
	public static final double LINK_MIN_CAR_DIST = 500; // m
	
	public static final double VEHICLE_FULL_SPEED = 20; // m/s
	public static final double VEHICLE_WAIT_SPEED = 10; // m/s
	
	// Car deployment parameters
	public static final int GROUP_A_VEHICLE_COUNT = 3;
	public static final String GROUP_A_POS = "Liberec";
	public static final double GROUP_A_RADIUS = 0.05 * (WIDTH);
	public static final String GROUP_A_DST = "Praha";
	
	public static final int GROUP_B_VEHICLE_COUNT = 0;
	public static final String GROUP_B_POS = "Ostrava";
	public static final double GROUP_B_RADIUS = 0.05 * (WIDTH);
	public static final String GROUP_B_DST = "Brno";
	
	// Router settings
	public static final int ROUTE_CALCULATION_OFFSET = 0;
	
	// ENSEMBLE
	public static final double ENSEMBLE_RADIUS = 700.0; // sqrt(2) * 500
	
	// Network generation
	public static final int CELL_COUNT = 10;
	public static final int LINKS_PER_EDGE = 20; // Each street is 200m long assuming 10m long segment
	public static final double LINK_LENGTH = 10.0; // IN METERS
	public static final String NETWORK_OUTPUT = "input/network.xml";
	public static final boolean BIDIRECTIONAL_STREETS = false;
	
	// Link parameters
	public final static double FREE_SPEED = 10.0; // m/s
	public final static double CAPACITY = 5000.0; // v/s
	public final static double PERMLANES = 3.0;
	public final static int ONEWAY = 1;
	public final static String MODES = "car";
	
	// OMNetMATSim simulation parameters
	public static String OMNET_CONFIG_TEMPLATE = "omnetpp.ini.templ";
	
	// MATSim
	public static final long FULL_SIMULATION_DURATION = 400000; // IN MILLIS
	public static final String MATSIM_INPUT = "input";
	public static final String MATSIM_OUTPUT = "matsim";
	public static String MATSIM_CONFIG = Settings.MATSIM_INPUT + "/" + "config" + ".xml";
}
