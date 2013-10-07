package org.jgrapht.experimental.clustering.krv.breakCondition;

import org.jgrapht.Graph;

public class TheoreticalPotentialBreakCondition implements KRVBreakCondition {

	private Double bound;
	
	public TheoreticalPotentialBreakCondition(Graph g) {
		bound = 1.0/(16 * g.vertexSet().size());
		System.out.println(bound);
	}
	
	
	@Override
	public Boolean breakIteration(Double current_potential , Integer noDeletionStep) {
		return current_potential < bound;
	}

}
