package cz.cuni.mff.d3s.roadtrain.demo;

import java.lang.annotation.Annotation;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import cz.cuni.mff.d3s.deeco.annotations.PartitionedBy;
import cz.cuni.mff.d3s.deeco.annotations.processor.AnnotationProcessorExtensionPoint;
import cz.cuni.mff.d3s.deeco.model.runtime.api.EnsembleDefinition;

public class PartitionedByProcessor extends AnnotationProcessorExtensionPoint {
	public void onEnsembleDefinitionCreation(EnsembleDefinition ensembleDefinition, Annotation unknownAnnotation) {
		if(unknownAnnotation instanceof PartitionedBy) {
			PartitionedBy annotation = (PartitionedBy)unknownAnnotation;
			
			if(ensembleDefinition.getPartitionedBy() != null) {
				throw new NotImplementedException();
			}
			
			ensembleDefinition.setPartitionedBy(annotation.value());
		}
	}
}
