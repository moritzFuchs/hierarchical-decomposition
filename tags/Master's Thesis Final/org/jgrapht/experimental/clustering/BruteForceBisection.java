package org.jgrapht.experimental.clustering;

import java.util.HashSet;
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
	 * @return Set<Set<V>> clusters (S and S_bar)
	 */
	public Set<Set<V>> computeBisection(Set<V> subgraph) {
		Double min = Double.POSITIVE_INFINITY;
		Set<V> min_cluster = null;
		for (Set<V> cluster : Sets.powerSet(subgraph)) {
			if (cutSize(cluster, subgraph) < min && cluster.size() > 0 && cluster.size() < subgraph.size()) {
				min = cutSize(cluster, subgraph);
				min_cluster = cluster;
			} 
		}
		Set<Set<V>> clusters = new HashSet<Set<V>>();
		Set<V> cluster_bar = new HashSet<V>(subgraph);
		cluster_bar.removeAll(min_cluster);
		clusters.add(min_cluster);
		clusters.add(cluster_bar);

		return clusters;
	}
	
	/**
	 * Computes the cut size of a given cluster in {@link g}
	 * 
	 * @param cluster : A cluster in the graph {@link g}
	 * @return : The cut size of the given cluster in {@link g}
	 */
	private Double cutSize(Set<V> cluster, Set<V> subgraph) {
		
		Double size = 0.0;
		
		for (V source : cluster) {
			for (E e : g.edgesOf(source)) {
				V target;
				if (source == g.getEdgeSource(e)) {
					target = g.getEdgeTarget(e);
				} else {
					target = g.getEdgeSource(e);
				}
				if (!cluster.contains(target) && subgraph.contains(target)) {
					size += g.getEdgeWeight(e);
				}
			}
		}		
		
		return size;
	}
	
}
