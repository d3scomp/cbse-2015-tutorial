package cz.cuni.mff.d3s.roadtrain.demo;

public class MessageProbe {
	private long msgSentMANET;
	private long msgSentIP;
	
	public MessageProbe() {
		msgSentMANET = 0;
		msgSentIP = 0;
	}
	
	public long getMsgSentMANET() {
		return msgSentMANET;
	}

	public long getMsgSentIP() {
		return msgSentIP;
	}

	public void messageSentMANET() {
		msgSentMANET++;
	}
	
	public void messageSentIP() {
		msgSentIP++;
	}
	
	public String report() {
		return String.format("Total number of messages sent MANET: %d IP: %d", msgSentMANET, msgSentIP);
	}
}
