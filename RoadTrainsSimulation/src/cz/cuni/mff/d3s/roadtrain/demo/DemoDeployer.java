package cz.cuni.mff.d3s.roadtrain.demo;

import java.security.KeyStoreException;

import cz.cuni.mff.d3s.deeco.annotations.processor.AnnotationProcessorException;

public interface DemoDeployer {
	public void deploy() throws AnnotationProcessorException, KeyStoreException;
}
