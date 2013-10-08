package org.jgrapht.experimental.clustering.krv.breakCondition;

/**
 * Interface for a break condition for the {@link ModifiedEfficientKRVProcedure}.
 * 
 * @author moritzfuchs
 * @date 07.10.2013
 *
 */
public interface KRVBreakCondition {
	public Boolean breakIteration(Double current_potential , Integer noDeletionStep); 
	public void reset();
}
