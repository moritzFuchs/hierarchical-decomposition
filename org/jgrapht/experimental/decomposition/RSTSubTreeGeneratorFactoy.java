package org.jgrapht.experimental.decomposition;

import java.util.concurrent.ExecutorService;

public class RSTSubTreeGeneratorFactoy<V extends Comparable<V>,E> implements
		DecompositionSubTreeGeneratorFactory<V, E> {
	
	@Override
	public DecompositionSubTreeGenerator<V, E> getInstance( DecompositionTree<V> tree, DecompositionTask<V, E> task, ExecutorService exec, Decomposition<V, E> d) {
		return new RSTDecompositionSubTreeGenerator<>(tree, task, exec, d);
	}

}
