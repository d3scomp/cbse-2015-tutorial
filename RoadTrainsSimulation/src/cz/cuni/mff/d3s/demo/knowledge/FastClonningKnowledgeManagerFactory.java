package cz.cuni.mff.d3s.demo.knowledge;

import java.util.Collection;

import com.rits.cloning.Cloner;

import cz.cuni.mff.d3s.deeco.knowledge.BaseKnowledgeManager;
import cz.cuni.mff.d3s.deeco.knowledge.KnowledgeManager;
import cz.cuni.mff.d3s.deeco.knowledge.KnowledgeManagerFactory;
import cz.cuni.mff.d3s.deeco.knowledge.KnowledgeNotFoundException;
import cz.cuni.mff.d3s.deeco.knowledge.ValueSet;
import cz.cuni.mff.d3s.deeco.model.runtime.api.KnowledgePath;

public class FastClonningKnowledgeManagerFactory implements
		KnowledgeManagerFactory {

	public KnowledgeManager create(String id) {
		return new FastCloningKnowledgeManager(id);
	}
	
	public static class FastCloningKnowledgeManager extends BaseKnowledgeManager {

		private final Cloner c;
		
		public FastCloningKnowledgeManager(String id) {
			super(id);
			c = new Cloner();
		}
		
		@Override
		public ValueSet get(Collection<KnowledgePath> knowledgePaths)
				throws KnowledgeNotFoundException {		
			ValueSet values = super.get(knowledgePaths);
			ValueSet result = new ValueSet();
			for (KnowledgePath p: values.getKnowledgePaths()) {
				if (isLocal(p) || !knowledgePaths.isEmpty()) {
					result.setValue(p, values.getValue(p));
				} else {
					result.setValue(p, c.deepClone(values.getValue(p)));
				}
			}
			return result;
		}
	}

}
