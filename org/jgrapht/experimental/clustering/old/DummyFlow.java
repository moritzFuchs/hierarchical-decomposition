package org.jgrapht.experimental.clustering.old;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.jgrapht.Graph;

/**
 * Implements Edmond-Karp-Algorithm for Undirected Graphs
 * WARNING: This is not an efficient implementation at all! It should be used for TESTING ONLY!
 * (implementation inspired by Wikipedia)
 * 
 * @author moritzfuchs
 *
 * @param <V>
 * @param <E>
 */
public class DummyFlow<V,E> {

	private Map <E , Double> flow = new HashMap<E , Double>();
	
	public DummyFlow(Graph<V,E> g , V s , V t) {
		
		
		
		//Init flow to 0
		for (E e: g.edgeSet()) {
			flow.put(e, 0.0);
		}
		
		while(true) {
			//Generate parent map and let s point to itself (indicating that it is the source)
			Map<V , V> parent = new HashMap<V,V>();
			parent.put(s, s);
			
			Map<E , Double> new_flow = new HashMap<E , Double>();
			
			Queue<V> q = new LinkedList<V>();
			
			q.add(s);
			
			//Let's get dirty!
			LOOP:
			while (!q.isEmpty()) {
				V current_vertex = q.poll();
				//If current vertex is t, we have found a path from s to t with capacity > 0 => add that capacity to the path and start over.
				if (current_vertex == t) {
					V v = t;
					Double add_flow = new_flow.get(g.getEdge(t, parent.get(t)));

					//backtrack until we reach s
					while(parent.get(v) != v) {
						//Get edge between v and its parent and add the flow to it
						E current_edge = g.getEdge(v, parent.get(v));
						if (current_edge != null) {							
							flow.put(current_edge,  flow.get(current_edge) + add_flow);
						}
						
						v = parent.get(v);
					}
					break LOOP;
				}
				
				
				E current_edge = g.getEdge(parent.get(current_vertex), current_vertex);
				
				for (E e: g.edgesOf(current_vertex)) {
					V target;
					if (g.getEdgeTarget(e) != current_vertex) {
						target = g.getEdgeTarget(e);
					} else {
						target = g.getEdgeSource(e);
					}
					
					//If vertex has not been visited before
					if (parent.get(target) == null) {
						Double target_capacity = g.getEdgeWeight(e);
						Double target_flow = flow.get(e);
						
						Double target_remaining_capacity = target_capacity - target_flow;
						if (target_remaining_capacity > 0) {
							parent.put(target, current_vertex);
							
							Double flow_from = Double.POSITIVE_INFINITY;
							if (current_edge != null)
								flow_from = new_flow.get(current_edge);
							
							//new flow along this edge = min of remaining capacity of path to target and remaining capacity between current_vertex and target
							new_flow.put(e, Math.min(target_remaining_capacity,flow_from));
							q.add(target);
						}
					}
				}
			}

			//If the current BFS did not reach t, then there is no path from s to t => STOP!
			if (parent.get(t) == null) {
				return;
			}
		}
		
	}
	
	public Map<E , Double> getFlow() {
		return flow;
	}
	
	
	
}
