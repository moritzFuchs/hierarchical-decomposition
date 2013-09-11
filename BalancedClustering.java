package org.jgrapht.experimental.clustering;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.Map;

import org.jgrapht.Graph;

public class BalancedClustering<V,E> {

	/**
	 * Checks whether a given clustering is balanced or not. A clustering is balanced if every induced cluster has size at most 3/4  * V[G].
	 * 
	 * @param g
	 * @param edges
	 * @return
	 */
	public  Boolean isBalancedClustering(Graph<V,E> g , Set<E> edges) {
		
		Map<V, Boolean> visited = new HashMap<V, Boolean>();
		
		for (V v: g.vertexSet()) {
			if (visited.get(v) == null) {
				Integer reachable = bfs(g , v , visited , edges);
				if (reachable > g.vertexSet().size() * 3 / 4) {
					return false;
				}
			}
		}
		
		//If we did not return false after seeing all vertices the clustering has to be balanced
		return true;
	}
	
	/**
	 * Perform BFS, ignore edges that induce clustering (given by 'edges') and count reachable vertices that have not been seen before. 
	 * 
	 * @param g : graph to perform BFS on
	 * @param v : starting vertex from G
	 * @param visited : markers for vertices to indicate whether or not a given vertex has been visited before
	 * @param edges : edges that induce clustering
	 * @return
	 */
	private Integer bfs(Graph <V,E> g , V v , Map<V , Boolean> visited , Set<E> edges) {
		//Initially we have found ourself
		Integer nodesFound = 0;
		
		Queue<V> q = new LinkedList<V>();
		q.add(v);
		
		while (!q.isEmpty()) {
			V current_vertice = q.poll();
			if (visited.get(current_vertice) == null) {
				nodesFound++;
				for (E e : g.edgesOf(current_vertice)) {
					
					//Skip e \in edges (= edges that are deleted to induce clustering)
					if (edges.contains(e))
						continue;
					
					V target;
					if (g.getEdgeTarget(e) != current_vertice){
						target = g.getEdgeTarget(e);
					} else {
						target = g.getEdgeSource(e);
					}
					q.add(target);
				}
			}
			
			//Mark current vertice as seen
			visited.put(current_vertice, true);
		}
		
		return nodesFound;
	}
	
}
