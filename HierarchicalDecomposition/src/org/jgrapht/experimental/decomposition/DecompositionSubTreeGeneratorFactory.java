package org.jgrapht.experimental.decomposition;

import java.util.concurrent.ExecutorService;

public interface DecompositionSubTreeGeneratorFactory<V extends Comparable<V>,E> {
	public DecompositionSubTreeGenerator<V,E> getInstance(DecompositionTree<V> tree, DecompositionTask<V, E> task, ExecutorService exec, Decomposition<V, E> d); 
}
