package cz.cuni.mff.d3s.roadtrain.demo.environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;

import cz.cuni.mff.d3s.deeco.simulation.matsim.MATSimRouter;
import cz.cuni.mff.d3s.roadtrain.demo.Settings;
import cz.cuni.mff.d3s.roadtrain.demo.utils.Navigator;
import cz.cuni.mff.d3s.roadtrain.demo.utils.VehicleLink;
import cz.cuni.mff.d3s.roadtrain.demo.utils.VehicleState;

public class VehicleMonitor {
	private String dir;
	private StringBuilder record;
	private long time;
	
	private final double SCALE = 0.1;
	
	public VehicleMonitor(String dirName) {
		this.dir = dirName;
		time = 0;
		record = new StringBuilder();
	}

	/*
	 * static final String[] colors = {"aliceblue", "antiquewhite", "aqua", "aquamarine", "azure", "beige", "bisque",
	 * "black", "blanchedalmond", "blue", "blueviolet", "brown", "burlywood", "cadetblue", "chartreuse", "chocolate",
	 * "coral", "cornflowerblue", "cornsilk", "crimson", "cyan", "darkblue", "darkcyan", "darkgoldenrod", "darkgreen",
	 * "darkgrey", "darkkhaki", "darkmagenta", "darkolivegreen", "darkorange", "darkorchid", "darkred", "darksalmon",
	 * "darkseagreen", "darkslateblue", "darkslategray", "darkslategrey", "darkturquoise", "darkviolet", "deeppink",
	 * "deepskyblue", "dimgray", "dimgrey", "dodgerblue", "firebrick", "floralwhite", "forestgreen", "fuchsia",
	 * "gainsboro", "ghostwhite", "gold", "goldenrod", "green", "greenyellow", "honeydew", "hotpink", "indianred",
	 * "indigo", "ivory", "khaki", "lavender", "lavenderblush", "lawngreen", "lemonchiffon", "lightblue", "lightcoral",
	 * "lightcyan", "lightgoldenrodyellow", "lightgray", "lightgreen", "lightgrey", "lightpink", "lightsalmon",
	 * "lightseagreen", "lightskyblue", "lightslategray", "lightslategrey", "lightsteelblue", "lightyellow", "lime",
	 * "limegreen", "linen", "magenta", "maroon", "mediumaquamarine", "mediumblue", "mediumorchid", "mediumpurple",
	 * "mediumseagreen", "mediumslateblue", "mediumspringgreen", "mediumturquoise", "mediumvioletred", "midnightblue",
	 * "mintcream", "mistyrose", "moccasin", "navajowhite", "navy", "oldlace", "olive", "olivedrab", "orange",
	 * "orangered", "orchid", "palegoldenrod", "palegreen", "paleturquoise", "palevioletred", "papayawhip", "peachpuff",
	 * " peru", "pink", "plum", "powderblue", "purple", "red", "rosybrown", "royalblue", "saddlebrown", "salmon",
	 * "sandybrown", "seagreen", "seashell", "sienna", "silver", "skyblue", "slateblue", "slategray", "slategrey",
	 * "springgreen", "steelblue", "tan", "teal", "thistle", "tomato", "turquoise", "violet", "yellow", "yellowgreen" };
	 */

	static final Map<String, String> colorMap = new HashMap<String, String>();
	static {
		colorMap.put("V0", "green");
		colorMap.put("V1", "orange");
		colorMap.put("V2", "blue");
		colorMap.put("V3", "red");
		colorMap.put("V4", "blueviolet");
		colorMap.put("V5", "deeppink");
		colorMap.put("V6", "gold");
		colorMap.put("V7", "purple");
		colorMap.put("V8", "cyan");
		colorMap.put("V9", "khaki");
		colorMap.put("V10", "green");
		colorMap.put("V11", "orange");
		colorMap.put("V12", "blue");
		colorMap.put("V13", "red");
		colorMap.put("V14", "blueviolet");
		colorMap.put("V15", "deeppink");
		colorMap.put("V16", "gold");
		colorMap.put("V17", "purple");
		colorMap.put("V18", "cyan");
		colorMap.put("V19", "chartreuse");
		colorMap.put("V20", "brown");
		colorMap.put("V21", "yellowgreen");
		colorMap.put("V22", "steelblue");
		colorMap.put("V23", "springgreen");
		colorMap.put("V24", "plum");
		colorMap.put("V25", "chocolate");
	}

	public synchronized void report(long timeMs, String id, VehicleState state, Coord pos, VehicleLink leader, String dstCity,
			List<Id> route, MATSimRouter router, Double nearestFollower, String train, String pursuedVehicleId) {
		// Start new frame if needed
		if (time != timeMs) {
			try {
				dump();
			} catch (IOException e) {
				System.err.println(VehicleMonitor.class + " failed to dump graph data:" + e.getMessage());
			}
			time = timeMs;
		}

		// Decide color
		String nodeColor = colorMap.get(train);		
		if (nodeColor == null) {
			nodeColor = "gray";
		}
		if (id.startsWith("Police")) {
			nodeColor = "black";
		}
		
		// Add places
		for (String place : Navigator.getPlaces()) {
			Coord coord = Navigator.getPosition(place).getCoord();
			String color = "grey";
			if(place.matches("A[0-9]*")) {
				color = "green";
			}
			if(place.matches("F[0-9]*")) {
				color = "red";
			}
			if(place.matches("P[0-9]*")) {
				color = "blue";
			}
			record.append(String.format("\n%s [pos = \"%s,%s!\", color=%s, width=\"0.0002\", height=\"0.002\", fontsize=7]", place, coord.getX() * SCALE, coord.getY()
					* SCALE, color));
		}

		// Add route
		String last = id;
		if(route != null) {
			for (Id i : route) {
				Coord linkPos = router.findLinkById(i).getCoord();
				String linkName = String.format("%s_%s", id, i);
				record.append(String.format(
						"\n%s [pos = \"%s,%s!\", style=\"\", label=\"\", width=\"0.001\", height=\"0.01\"]", linkName,
						linkPos.getX() * SCALE, linkPos.getY() * SCALE));
				record.append(String.format("\n%s -> %s [color=%s, arrowsize=\"0.1\", label=\"\"]", last, linkName,
						nodeColor));
				last = linkName;
			}
		}

		// Vehicle
		String leaderId = dstCity;
		Double leaderDist = 0.0;
		
		if(leader != null) {
			leaderId = leader.id;
			leaderDist = leader.dist;
		}
		
		record.append(String
				.format("\n%s [label = \"%s\", pos = \"%s,%s!\", color=%s, shape=ellipse, fontsize=8, fontcolor=\"%s\", width=\"0.01\", height=\"0.01\"]",
						id, id + state.toShortString(), pos.getX() * SCALE, pos.getY() * SCALE, nodeColor, nodeColor));
		
		if (pursuedVehicleId != null) {
			record.append(String.format("\n%s -> %s [color=%s]", id, pursuedVehicleId, nodeColor));
		} else {
			record.append(String.format("\n%s -> %s [color=%s, label=\"%s\"]", id, leaderId, nodeColor, leaderDist.intValue()));
		}
	}

	public void dump() throws IOException {
		String fileName = String.format("%s%stime-%08d.dot", dir, File.separator, time);
		File file = new File(fileName);
		file.getParentFile().mkdirs();
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.write("digraph " + time + " {\n");

		writer.write("node [shape=box, label=\"\\N\", pin=true\n];\n");

		writer.write(String.format("tl [ pos = \"%s,%s!\", style=invis ]\n", Settings.MIN_X * SCALE, Settings.MIN_Y
				* SCALE));
		writer.write(String.format("tr [ pos = \"%s,%s!\", style=invis ]\n", Settings.MIN_X * SCALE, Settings.MAX_Y
				* SCALE));
		writer.write(String.format("bl [ pos = \"%s,%s!\", style=invis ]\n", Settings.MAX_X * SCALE, Settings.MIN_Y
				* SCALE));
		writer.write(String.format("br [ pos = \"%s,%s!\", style=invis ]\n", Settings.MAX_X * SCALE, Settings.MAX_Y
				* SCALE));
		writer.write(record.toString());
		writer.write("\n}");
		writer.close();

		record = new StringBuilder();
	}
}
