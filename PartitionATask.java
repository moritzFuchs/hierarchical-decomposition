package org.jgrapht.experimental.clustering;

import org.jgrapht.Graph;
import org.jgrapht.experimental.decomposition.DecompositionTask;

public class PartitionATask<V,E> extends ClusteringTask<V, E> {

	public PartitionATask(Graph<V, E> g) {
		super(g);
	}

	public PartitionATask(DecompositionTask<V, E> task) {
		super(task.getSubGraph());
	}

}
