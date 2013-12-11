package org.jgrapht.experimental.clustering.krv;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.jgrapht.Graph;
import org.jgrapht.experimental.clustering.Clustering;
import org.jgrapht.experimental.clustering.SplitGraph;
import org.jgrapht.experimental.util.LoggerFactory;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Implements PartitionA from 'Computing Cut-Based Hierarchical Decompositions in Almost linear time' by R�cke, Shah, T�ubig (pages 4 - 10)
 * The Algorithm receives a Graph g and partitions it into sets Z_i s.t. for every Z_i: |Z_i| <= 3/4 |E|. Apart form that inter-cluster-edges
 * are a-flow-linked with a \in \Omega (1 / log^2 n)
 * 
 * @author moritzfuchs
 *
 * @param <V> : The type of vertices
 * @param <E> : The type of edges
 */
public class PartitionA<V extends Comparable<V>,E> extends Clustering<V,E>{	

	/**
	 * Local logger
	 */
	private final static Logger LOGGER = LoggerFactory.getLogger(PartitionA.class
		      .getName());
	
	/**
	 * The resulting set of edges that induce the clustering
	 */
	private Set<E> F = null;
	
	/**
	 * BiMap from Edges to Integers. Every edge e \in E[G] has a unique number (needed especially for the flow vectors)
	 */
	private BiMap<E , Integer> edgeNum = HashBiMap.create();
	
	public PartitionA(PartitionATask<V,E> task) {
		super(task);
	}
	
	/**
	 * Returns a set of edges F that induces a clustering as described in Theorem 3.1 
	 * 
	 * @return A set of edges that induces a clustering as described in Theorem 3.1
	 */
	public Set<E> getF() {
		return F;
	}

	/**
	 * Performs the clustering of the graph g
	 * 
	 * @param g : The graph to be partitioned
	 */
	private void performClustering(Graph<V,E> g) {
		Integer step = 1;
		
		Set<E> A = g.edgeSet();
		Set<E> B = new HashSet<E>();
		
		Boolean done = false;
		
		//Produce a Mapping from edges to Integers (needed for FlowVectors in Lemma 3.2)
		int i = 0;
		for (E e : g.edgeSet()) {
			edgeNum.put(e, i++);
		}
		
		SplitGraph<V,E> gPrime = new SplitGraph<V, E>(g);
		
		while (!done) {
			if ( step == 1 ) {
				LOGGER.info("Performing Lemma 3.2.");
				Set<E> F = new HashSet<E>(A);
				F.addAll(B);
				//Apply Lemma 3.2 until Case 2 is reached (O(log^2 n) iterations)
				ModifiedEfficientKRVProcedure<V, E> krv = new ModifiedEfficientKRVProcedure<V, E>(g, gPrime, F, edgeNum);
				Integer result;
				
				result = krv.performKRV();
				//result = krv.performModifiedKRVProcedure();
				
				A = krv.getA();
				B = krv.getB();
					
				if (result == 1 ) 
					step = 1;
				if (result == 2 ) 
					step = 2;
				
			} else {
				LOGGER.info("Performing Lemma 3.3.");
				Lemma33<V,E> l = new Lemma33<V,E>(g, A, B);
				Integer result = l.performLemma33();
				if (result == 1) {
					step = 1;
				} else {
					done = true;
				}
			}
		}
		
		F = A;
		F.addAll(B);
		
		LOGGER.info("Modified KRV procedure done.");
		LOGGER.info("Resulting edges set F= " + F);
		LOGGER.info("Waking observers now.");
		
		setChanged();
		notifyObservers();
	}

	@Override
	public void run() {
		performClustering(task.getGraph());	
	}

	/**
	 * Returns the computed clustering (or null if the computation is not done yet)
	 * 
	 * @return The computed clustering or null
	 */
	@Override
	public Set<E> getClustering() {
		return F;
	}
	
}
