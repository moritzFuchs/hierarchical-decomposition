package org.jgrapht.experimental.clustering.krv.breakCondition;

/**
 * Implementation of a {@link KRVBreakConditionComposition}. Implements OR; At least one condition has to be true in order to break the iteration
 * 
 * @author moritzfuchs
 * @date 15.10.2013
 *
 */
public class KRVBreakConditionOR extends KRVBreakConditionComposition{

	@Override
	protected Boolean compose(Boolean a, Boolean b) {
		return (a || b);
	}

}
