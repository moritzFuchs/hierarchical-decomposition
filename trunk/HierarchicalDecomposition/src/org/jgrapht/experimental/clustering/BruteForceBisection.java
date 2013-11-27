package org.jgrapht.experimental.clustering;

import java.util.Set;

import org.jgrapht.Graph;

import com.google.common.collect.Sets;


/**
 * Computes an optimal bisection using a brute force approach. WARNING: Do not use this for big graphs! 
 * 
 * @author: moritzfuchs
 * @date: 27.11.2013
 */
public class BruteForceBisection<V,E> {

	/**
	 * Graph to be bisected
	 */
	private Graph<V,E> g;
	
	public BruteForceBisection(Graph<V,E> g) {
		this.g = g;
	}
	
	/**
	 * Computes the optimal bisection and returns one side of the bisection (S or S_bar)
	 * 
	 * @return Set<V> cluster
	 */
	public Set<V> computeBisection() {
		Double min = Double.POSITIVE_INFINITY;
		Set<V> min_cluster = null;
		for (Set<V> cluster : Sets.powerSet(g.vertexSet())) {
			if (cutSize(cluster) < min && cluster.size() > 0 && cluster.size() < g.vertexSet().size()) {
				min = cutSize(cluster);
				min_cluster = cluster;
			} 
		}
		
		return min_cluster;
	}
	
	/**
	 * Computes the cut size of a given cluster in {@link g}
	 * 
	 * @param cluster : A cluster in the graph {@link g}
	 * @return : The cut size of the given cluster in {@link g}
	 */
	private Double cutSize(Set<V> cluster) {
		
		Double size = 0.0;
		
		for (V source : cluster) {
			for (E e : g.edgesOf(source)) {
				V target;
				if (source == g.getEdgeSource(e)) {
					target = g.getEdgeTarget(e);
				} else {
					target = g.getEdgeSource(e);
				}
				if (!cluster.contains(target)) {
					size += g.getEdgeWeight(e);
				}
			}
		}		
		
		return size;
	}
	
}
