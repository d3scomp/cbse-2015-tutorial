package cz.cuni.mff.d3s.roadtrain.demo.environment;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.matsim.api.core.v01.Coord;

import cz.cuni.mff.d3s.roadtrain.demo.Settings;

public class VehicleMonitor {
	static Map<Long, StringBuilder> records = new HashMap<Long, StringBuilder>();
	
	static final int scale = 1; 

	public static synchronized void report(long timeMs, String id, Coord pos, String leader, int carNum, String dstCity, Coord dst) {
		if (!records.containsKey(timeMs)) {
			records.put(timeMs, new StringBuilder());
		}

		StringBuilder builder = records.get(timeMs);

		if(leader == null) {
			leader = dstCity;
		}
		
		builder.append(String.format("\n%s [\n\t pos = \"%s,%s!\"]", dstCity, dst.getX() / scale, dst.getY() / scale));
			
		builder.append(String.format("\n%s [\n\tlabel = \"%s\"\n\tpos = \"%s,%s!\"\n]\n%s -> %s [color=blue]",
				id,
				id + "(" + carNum + ")",
				pos.getX() / scale,
				pos.getY() / scale,
				id, leader
		));
	}

	public static void dump(String prefix) throws IOException {
		for (Entry<Long, StringBuilder> entry : records.entrySet()) {
			String filename = String.format("%s%08d.dot", prefix, entry.getKey());
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			writer.write("digraph " + entry.getKey() + " {\n");
			
			writer.write("node [shape=box, label=\"\\N\", pin=true, width=\"0.1\", height=\"0.1\"\n];\n");
			writer.write("graph [scale=\"1\", dpi=64, bb=\"0,0,2000,2000\"];\n");
		    
			writer.write("tl [ pos = \"0,0!\" ]\n");
			writer.write("tr [ pos = \"2000,0!\" ]\n");
			writer.write("bl [ pos = \"0,2000!\" ]\n");
			writer.write("br [ pos = \"2000,2000!\" ]\n");
	//		writer.write(String.format("size=\"%s,%s!\";", Settings.mapDimension / scale, Settings.mapDimension / scale));
			writer.write(entry.getValue().toString());
			writer.write("\n}");
			writer.close();
		}
	}
}
