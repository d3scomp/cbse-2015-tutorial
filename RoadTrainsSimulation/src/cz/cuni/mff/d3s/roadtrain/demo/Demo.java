package cz.cuni.mff.d3s.roadtrain.demo;

public class Demo {

	public static void main(String[] args) throws Exception {
		System.out.println("Launching default simulation");
		
		Launcher launcher = new LauncherWithGroupers();
		launcher.run();
		
		System.out.println("Simulation finished");
	}

}
