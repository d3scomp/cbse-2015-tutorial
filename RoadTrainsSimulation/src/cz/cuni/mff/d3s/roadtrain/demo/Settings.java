package cz.cuni.mff.d3s.roadtrain.demo;

public class Settings {
	
	public static final String MATSIM_CONFIG_TEMPLATE = "input/config.xml";
	public static final String ANALYSES_DIRECTORY = "measurements";
	
	// Train parameters
	public static final double TRAIN_FORM_DISTANCE = 300; // m
	
	// Car parameters
	public static final int GROUP_A_VEHICLE_COUNT = 5;
	public static final String GROUP_A_POS = "Liberec";
	public static final double GROUP_A_RADIUS = 0.10; // %
	public static final String GROUP_A_DST = "Praha";
	
	public static final int GROUP_B_VEHICLE_COUNT = 5;
	public static final String GROUP_B_POS = "Ostrava";
	public static final double GROUP_B_RADIUS = 0.10; // %
	public static final String GROUP_B_DST = "Brno";
	
	// Router settings
	public static final int ROUTE_CALCULATION_OFFSET = 5;
	
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
	public static final long FULL_SIMULATION_DURATION = 480000; // IN MILLIS
	public static final long PARTIAL_SIMULATION_DURATION = 600000; // IN MILLIS
	public static final long NONE_SIMULATION_DURATION = 1200000; // IN MILLIS
	public static final String MATSIM_INPUT = "input";
	public static final String MATSIM_OUTPUT = "matsim";
	public static String MATSIM_CONFIG = Settings.MATSIM_INPUT + "/" + "config" + ".xml";
}
