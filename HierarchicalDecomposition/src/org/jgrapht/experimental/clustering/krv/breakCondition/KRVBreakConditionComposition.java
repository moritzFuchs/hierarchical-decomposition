package org.jgrapht.experimental.clustering.krv.breakCondition;

import java.util.LinkedList;
import java.util.List;

/**
 * Bundles multiple {@link KRVBreakCondition} to a single {@link KRVBreakCondition}. 
 * 
 * @author moritzfuchs
 * @date 07.10.2013
 *
 */
public class KRVBreakConditionComposition implements KRVBreakCondition {

	/**
	 * List of {@link KRVBreakCondition} that will be applied
	 */
	private List<KRVBreakCondition> conditions;
	
	public KRVBreakConditionComposition() {
		conditions = new LinkedList<KRVBreakCondition>();
	}
	
	/**
	 * Adds a {@link KRVBreakCondition} to the list
	 * 
	 * @param cond : The new {@link KRVBreakCondition}
	 */
	public void addBreakCondition(KRVBreakCondition cond) {
		conditions.add(cond);
	}
	
	/**
	 * Removes a {@link KRVBreakCondition} from the list
	 * 
	 * @param cond : The {@link KRVBreakCondition} that will be removed from the list
	 */
	public void removeBreakCondition(KRVBreakCondition cond) {
		conditions.remove(cond);
	}
	
	@Override
	public Boolean breakIteration(Double current_potential, Integer noDeletionStep) {
		Boolean breakIteration = false;
		
		for (KRVBreakCondition cond : conditions) {
			breakIteration = breakIteration || cond.breakIteration(current_potential, noDeletionStep);
		}
		
		return breakIteration;
	}

}
