package cz.cuni.mff.d3s.roadtrain.demo;

public class Demo {

	public static void main(String[] args) throws Exception {
		System.out.println("Launching default simulation");
		
		LauncherWithGroupers launcher = new LauncherWithGroupers(3);
		
		DemoDeployer demoDeployer = new EmergencyDemoDeployer(
				Settings.CRASH_SITES,
				Settings.AMBULANCE_PER_CRASH,
				Settings.FIRE_PER_CRASH,
				Settings.POLICE_PER_CRASH,
				launcher);
				
		launcher.run(demoDeployer);
		
		System.out.println("Simulation finished");
	}

}
