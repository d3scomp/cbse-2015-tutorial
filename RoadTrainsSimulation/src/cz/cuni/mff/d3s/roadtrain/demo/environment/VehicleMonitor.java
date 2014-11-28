package cz.cuni.mff.d3s.roadtrain.demo.environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.*;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;

import cz.cuni.mff.d3s.deeco.simulation.matsim.MATSimRouter;
import cz.cuni.mff.d3s.roadtrain.demo.Settings;
import cz.cuni.mff.d3s.roadtrain.utils.Navigator;

public class VehicleMonitor {
	// TODO: remove static methods, move prefix settings to constructor
	final static String prefix = "visual" + File.separator + "time-";
	static StringBuilder record = new StringBuilder();
	static long time = 0;
	
	static final double SCALE = 0.1;
	
	static final Map<String, String> colorMap = new HashMap<String, String>();
	static {
		colorMap.put("V0", "green");
		colorMap.put("V1", "orange");
		colorMap.put("V2", "blue");
	}
	
	
	public static synchronized void report(long timeMs, String id, Coord pos, String leader, String dstCity, List<Id> route, MATSimRouter router, Double leaderDist, Double nearestFollower) {
		// Start new frame if needed
		if(time != timeMs) {
			try {
				dump();
			} catch (IOException e) {
				System.err.println(VehicleMonitor.class + " failed to dump graph data:" + e.getMessage());
			}
			time = timeMs;
		}
		
		// Decide color
		String nodeColor = colorMap.get(id);
		if(nodeColor == null) {
			nodeColor = "gray";
		}
		
		// Add cities
		for(String place: Navigator.getCities()) {
			Coord coord = Navigator.getPosition(place).getCoord();
			record.append(String.format("\n%s [\n\t pos = \"%s,%s!\"]", place, coord.getX() * SCALE, coord.getY() * SCALE));
		}
		
		// Add route
		String last = id;
		for(Id i: route) {
			Coord linkPos = router.findLinkById(i).getCoord();
			String linkName = String.format("%s_%s", id, i);
			record.append(String.format("\n%s [pos = \"%s,%s!\", style=\"\", label=\"\", width=\"0.001\", height=\"0.01\"]",
					linkName,
					linkPos.getX() * SCALE,
					linkPos.getY() * SCALE
			));
			record.append(String.format("\n%s -> %s [color=%s, arrowsize=\"0.1\", label=\"\"]",
					last,
					linkName,
					nodeColor
			));
			last = linkName;
		}
		
		// Vehicle
		if(leader == null) {
			leader = dstCity;
		}
		record.append(String.format("\n%s [label = \"%s\", pos = \"%s,%s!\", color=%s, shape=ellipse, fontsize=8, fontcolor=\"%s\", width=\"0.01\", height=\"0.01\"]",
				id,
				id,
				pos.getX() * SCALE,
				pos.getY() * SCALE,
				nodeColor,
				nodeColor
		));
		record.append(String.format("\n%s -> %s [color=%s, label=\"%s\"]",
				id, leader, nodeColor, leaderDist
		));
	}

	public static void dump() throws IOException {
		String filename = String.format("%s%08d.dot", prefix, time);
		BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
		writer.write("digraph " + time + " {\n");
		
		writer.write("node [shape=box, label=\"\\N\", pin=true\n];\n");
		  
		writer.write(String.format("tl [ pos = \"%s,%s!\", style=invis ]\n", Settings.MIN_X * SCALE, Settings.MIN_Y * SCALE));
		writer.write(String.format("tr [ pos = \"%s,%s!\", style=invis ]\n", Settings.MIN_X * SCALE, Settings.MAX_Y * SCALE));
		writer.write(String.format("bl [ pos = \"%s,%s!\", style=invis ]\n", Settings.MAX_X * SCALE, Settings.MIN_Y * SCALE));
		writer.write(String.format("br [ pos = \"%s,%s!\", style=invis ]\n", Settings.MAX_X * SCALE, Settings.MAX_Y * SCALE));
		writer.write(record.toString());
		writer.write("\n}");
		writer.close();
		
		record = new StringBuilder();
	}
}
