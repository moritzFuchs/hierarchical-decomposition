package org.jgrapht.experimental.decomposition;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.jgrapht.Graph;
import org.jgrapht.experimental.clustering.DecompositionConstants;

//TODO: Document
public class RSTDecomposition<V extends Comparable<V>,E> extends Decomposition<V, E> {
	
	public RSTDecomposition(Graph<V,E> g) {
		this.originalGraph = g;
	
		this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * DecompositionConstants.MULTIPLE_OF_CORES);
		
		//Generate decomposition tree
		this.decomposition = new DecompositionTree<V>();
		
		task = new DecompositionTask<V,E>(g,g,decomposition.getRoot());
	}

	@Override
	protected DecompositionSubTreeGenerator<V, E> getDecompositionSubtreeGenerator(DecompositionTask<V,E> task) {
		return new RSTDecompositionSubTreeGenerator<>(this.decomposition, task, this.executor, this);
	}

}
