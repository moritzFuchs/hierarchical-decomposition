package org.jgrapht.experimental.clustering.krv.breakCondition;

/**
 * Break condition for the {@link ModifiedEfficientKRVProcedure} that relies on the number of iterations since the last {@link DeletionStepNew}.
 * 
 * @author moritzfuchs
 * @date 07.10.2013
 *
 */
public class NoDeletionBreakCondition implements KRVBreakCondition {

	/**
	 * Number of consecutive iterations without {@link DeletionStepNew} that are tolerated.
	 */
	private Double bound;
	
	public NoDeletionBreakCondition(Integer n) {
		bound = 5* Math.log(n) / Math.log(2);
	}
	
	@Override
	public Boolean breakIteration(Double current_potential,
			Integer noDeletionStep) {
		return (noDeletionStep > bound);
	}
	
	public void reset() {}
}
