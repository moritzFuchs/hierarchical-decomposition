package org.jgrapht.experimental.clustering.krv.breakCondition;

/**
 * Break condition exactly as defined in the paper (Chapter 3.1.1):
 * Potential < 1/(16 n^2) => Stop KRV procedure
 * WARNING: DO NOT USE THIS IN PRACTICE! (there will be lots of unneccessary iterations)
 * 
 * @author moritzfuchs
 * @date 07.10.2013
 *
 */
public class TheoreticalPotentialBreakCondition implements KRVBreakCondition {

	/**
	 * Potential bound we want to reach
	 */
	private Double bound;
	
	public TheoreticalPotentialBreakCondition(Integer nodes) {
		bound = 1.0/(16 * nodes^2);
	}
	
	@Override
	public Boolean breakIteration(Double current_potential , Integer noDeletionStep) {
		return current_potential < bound;
	}

	public void reset() {}
}
