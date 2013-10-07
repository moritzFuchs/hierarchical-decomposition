package org.jgrapht.experimental.clustering.krv.breakCondition;

import org.jgrapht.Graph;

public class PracticalPotentialBreakCondition implements KRVBreakCondition {

	/**
	 * Bound we want to reach
	 */
	private Double bound;
	
	public PracticalPotentialBreakCondition(Graph g) {
		bound = 1.0/(16 * g.vertexSet().size());
		bound *= 100;
	}
	
	
	@Override
	public Boolean breakIteration(Double current_potential , Integer noDeletionStep) {
		return current_potential < bound;
	}

}
