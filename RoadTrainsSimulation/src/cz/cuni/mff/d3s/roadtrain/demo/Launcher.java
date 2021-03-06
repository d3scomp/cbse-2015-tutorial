package cz.cuni.mff.d3s.roadtrain.demo;

import java.io.IOException;
import java.security.KeyStoreException;

import cz.cuni.mff.d3s.deeco.annotations.processor.AnnotationProcessorException;

public interface Launcher {
	void run(DemoDeployer demoDeployer) throws AnnotationProcessorException, IOException, KeyStoreException;
}
