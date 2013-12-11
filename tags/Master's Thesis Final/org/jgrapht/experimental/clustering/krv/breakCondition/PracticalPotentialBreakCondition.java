package org.jgrapht.experimental.clustering.krv.breakCondition;


/**
 * Implements a practical version of the break condition as defined in the Paper (Chapter 3.1.1.)
 * 
 * @author moritzfuchs
 * @date 07.10.2013
 *
 */
public class PracticalPotentialBreakCondition implements KRVBreakCondition {

	/**
	 * Bound we want to reach
	 */
	private Double bound;
	
	public PracticalPotentialBreakCondition(Integer n) {
		bound = 1.0/(16 * n);
		bound *= 50;
	}
	
	@Override
	public Boolean breakIteration(Double current_potential , Integer noDeletionStep) {
		return current_potential < bound;
	}

	public void reset() {}
}
