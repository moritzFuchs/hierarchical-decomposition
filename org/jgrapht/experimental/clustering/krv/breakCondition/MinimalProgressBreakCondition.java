package org.jgrapht.experimental.clustering.krv.breakCondition;

import java.util.LinkedList;

import org.jgrapht.experimental.clustering.DecompositionConstants;

/**
 * This break condition uses the progress over the last few iterations. If the progress is less than a certain bound, the KRV procedure stops. 
 * 
 * @author moritzfuchs
 * @date 08.10.2013
 *
 */
public class MinimalProgressBreakCondition implements KRVBreakCondition {

	/**
	 * Potentials of the last few iterations
	 */
	private LinkedList<Double> progress;
	
	/**
	 * The bound at which the condition fires
	 */
	private Double bound;
	
	/**
	 * Amount of potentials saved so far (counts up to {@link MinimalProgressBreakCondition.iterations}
	 */
	private Integer saved = 0;
	
	public MinimalProgressBreakCondition(Integer n) {
		progress = new LinkedList<Double>();
		bound = 1.0/(4*Math.pow(n,2));
	}
	
	@Override
	public Boolean breakIteration(Double current_potential, Integer noDeletionStep) {
		if (saved < DecompositionConstants.NO_PROGRESS_BREAK_CONDITION_ITERATIONS) {
			saved++;
			progress.add(current_potential);
			return false;
		} else {
			//Remove first element (oldest) and add new last element (newest)
			progress.remove(0);
			progress.add(current_potential);			
			return (progress.get(0) - progress.get(saved-1) < bound);
			
		}
	}
	
	/**
	 * Resets the break condition state: All currently saved potentials of the last few iterations will be deleted.
	 */
	public void reset() {
		saved = 0;
		progress.clear();
	}
}
