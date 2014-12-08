package cz.cuni.mff.d3s.roadtrain.demo;

public class MessageProbe {
	private static long msgSentMANET = 0;
	private static long msgSentIP = 0;
	
	public static void messageSentMANET() {
		msgSentMANET++;
	}
	
	public static void messageSentIP() {
		msgSentIP++;
	}
	
	public static String report() {
		return String.format("Total number of messages sent MANET: %d IP: %d", msgSentMANET, msgSentIP);
	}
}
