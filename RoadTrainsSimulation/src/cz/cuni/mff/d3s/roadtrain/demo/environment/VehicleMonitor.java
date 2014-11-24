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

	public static synchronized void report(long timeMs, String id, Coord pos, String leader, int carNum, String dstCity, Coord dst) {
		if (!records.containsKey(timeMs)) {
			records.put(timeMs, new StringBuilder());
		}

		StringBuilder builder = records.get(timeMs);

		if(leader == null) {
			leader = dstCity;
		}
		
		builder.append(String.format("\n%s [\n\t pos = \"%s,%s!\"]", dstCity, dst.getX() / 100, dst.getY() / 100));
			
		builder.append(String.format("\n%s [\n\tlabel = \"%s\"\n\tpos = \"%s,%s!\"\nwidth=\"0.1\", height=\"0.1\"\n]\n%s -> %s [color=blue]",
				id,
				id,
				pos.getX() / 100,
				pos.getY() / 100,
				id, leader
		));
	}

	public static void dump(String prefix) throws IOException {
		for (Entry<Long, StringBuilder> entry : records.entrySet()) {
			String filename = prefix + entry.getKey() + ".dot";
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			writer.write("digraph " + entry.getKey() + " {\n");
			
			writer.write("node [label=\"\\N\", pin=true];\n");
			writer.write("graph [bb=\"0,0,20,20\"];\n");
		    
			writer.write("tl [ pos = \"0,0!\" ]\n");
			writer.write("tr [ pos = \"20,0!\" ]\n");
			writer.write("bl [ pos = \"0,20!\" ]\n");
			writer.write("br [ pos = \"20,20!\" ]\n");
			writer.write(String.format("size=\"%s,%s!\";", Settings.mapDimension / 100, Settings.mapDimension / 100));
			writer.write(entry.getValue().toString());
			writer.write("\n}");
			writer.close();
		}
	}
}
