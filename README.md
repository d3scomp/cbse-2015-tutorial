# Employing Domain Knowledge for Optimizing Component Communication (CBSE 2015)
## Road trains simulation

This repository contains sources of the road trains simulation. The application is a MATSIM simulation of emergency vehicle movements in the streets of Prague. The vehicle movements are described by DEECo components and ensembles while the simulation is using jDEECo runtime to implement these. The simulated network communication uses novel idea of Communication groups provided by groupers.

The simulation consists of separate experiments belonging to three experiment groups. Each experiment can be run separately or all experiments can be executed automatically. Keep in mind that running time of single experiments is relatively short (minutes - hours), but the whole simulation encompasses many experiments and thus can take days to complete on average hardware.

### Pre-requirements

- Eclipse
- Git
- Maven
- 64bit Java 1.8

### Project setup (for Eclipse)
1. Clone application repository https://github.com/d3scomp/cbse-2015-tutorial.git
2. Clone jDEECo repository https://github.com/d3scomp/JDEECo.git
3. Import the application, jDEECo.core, and jDEECo.simulation into one 
workspace in Eclipse
4. Add jDEECo.core and jDEECo.simulation projects on the build path of the application
5. Run Maven "update project" on application and jDEECo
6. Clean all projects

### Running simulation

1. Whole simulation (all experiments)

	The whole simulation is executed using the "cz.cuni.mff.d3s.roadtrain.demo.MeasureData" class. It is designed to be executed as Java application without parameters. Number of parallel experiments can is defined by constant ("NUM_PROCESSES") in the class sources and defaults to 8. Note that the whole simulation is expected to be run on the server with appropriate CPU power and RAM capacity. Every experiment takes up to 8GiB of RAM thus default setup of 8 parallel experiments may use up to 32 GiB of RAM.

2. Single experiment
	
	Single experiments are executed using the "cz.cuni.mff.d3s.roadtrain.demo.SimulationRunner" class. The parameters supplied to the class define experiment group and particular settings.

	1. Emergency vehicle road trains (3 vehicles per accident)

		+ params: emergency {groupers|random} #POLICE #FIRE #AMBULANCE #CRASHES RUNID
		+ example: emergency groupers 1 1 1 5 0
	
	2. Emergency vehicle road trains (5 vehicles per accident)

		+ params: emergency {groupers|random} #POLICE #FIRE #AMBULANCE #CRASHES RUNID
		+ example: emergency groupers 1 2 2 5 0
		
	3. Vehicle convoy (long road train)  
	
		+ params: military {eval|def} #VEHCILES RUNID
		+ example: military def 10 0

### Processing results
Each experiment produces two kinds of result. The first is the total number of messages sent during the experiment, while the second is the visualization of vehicle movements.

The message numbers can be processed into plots by scripts written in R. These are "output/processEmergency.R" and "output/processMilitary.R". Path to "output" directory needs to be configured in the scripts. Note that results from full simulation execution (all experiments) are needed to produce the plots as experiment setups are hardcoded in the scripts.  

The visualization of vehicle movements can be processed into series of images (snapshots each 5 simulation seconds). Sources for these images are placed in the subdirectory of "output" directory defined by experiment parameters. These are Graphviz source files with extension ".dot". In order to turn these sources into actual images the graphviz is needed together with Python script located in "output/processPNG.py". The script has to be copied into directory containing dot files and the path to neato executable has to be fixed in the script. Then the script outputs series of images visualizing vehicle locations and routes during the particular experiment.

### Note
In order to be consistent with the paper this the simulation is part of the jDEECO and the cbse-2015-tutorial repositories have tags "simulation-cbse2015" that mark the version of the sources used for the paper. As the topic covered by the paper is still work in progress these repositories will be updated with bug fixes and new features. Thus recent versions may be slightly different in terms of functionality, but in general should work better.

More information about DEECo and jDEECo can be found at:

http://d3s.mff.cuni.cz/projects/components_and_services/deeco/
and
https://github.com/d3scomp/JDEECo