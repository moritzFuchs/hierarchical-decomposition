package org.jgrapht.experimental.clustering.krv.breakCondition;

/**
 * Implementation of a {@link KRVBreakConditionComposition}. Implements AND; All conditions have to be true in order to break the iteration
 * 
 * @author moritzfuchs
 * @date 15.10.2013
 *
 */
public class KRVBreakConditionAND extends KRVBreakConditionComposition {

	@Override
	protected Boolean compose(Boolean a, Boolean b) {
		return (a && b);
	}

}
