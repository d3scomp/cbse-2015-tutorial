package cz.cuni.mff.d3s.roadtrain.demo.environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.matsim.api.core.v01.Coord;

import cz.cuni.mff.d3s.roadtrain.demo.Settings;

public class VehicleMonitor {
	// TODO: remove static methods, move prefix settings to constructor
	final static String prefix = "visual" + File.separator + "time-";
	static StringBuilder record = new StringBuilder();
	static long time = 0;
	
	public static synchronized void report(long timeMs, String id, Coord pos, String leader, int carNum, String dstCity, Coord dst) {
		// Start new frame if needed
		if(time != timeMs) {
			try {
				dump();
			} catch (IOException e) {
				System.err.println(VehicleMonitor.class + " failed to dump graph data:" + e.getMessage());
			}
			time = timeMs;
		}

		// Node
		record.append(String.format("\n%s [\n\t pos = \"%s,%s!\"]", dstCity, dst.getX(), dst.getY()));
		
		// Edge
		String color = "blue";
		if(leader == null) {
			leader = dstCity;
			color = "red";
		}
		record.append(String.format("\n%s [\n\tlabel = \"%s\"\n\tpos = \"%s,%s!\"\n]\n%s -> %s [color=%s]",
				id,
				id + "(" + carNum + ")",
				pos.getX(),
				pos.getY(),
				id, leader, color
		));
	}

	public static void dump() throws IOException {
		String filename = String.format("%s%08d.dot", prefix, time);
		BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
		writer.write("digraph " + time + " {\n");
		
		writer.write("node [shape=box, label=\"\\N\", pin=true, width=\"0.1\", height=\"0.1\"\n];\n");
		  
		writer.write(String.format("tl [ pos = \"0,0!\", style=invis ]\n"));
		writer.write(String.format("tr [ pos = \"%s,0!\", style=invis ]\n", Settings.mapDimension));
		writer.write(String.format("bl [ pos = \"0,%s!\", style=invis ]\n", Settings.mapDimension));
		writer.write(String.format("br [ pos = \"%s,%s!\", style=invis ]\n", Settings.mapDimension, Settings.mapDimension));
		writer.write(record.toString());
		writer.write("\n}");
		writer.close();
		
		record = new StringBuilder();
	}
}
