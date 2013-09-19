package org.jgrapht.experimental.clustering;


import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.jgrapht.Graph;
import org.jgrapht.experimental.util.LoggerFactory;
import org.jgrapht.graph.DefaultWeightedEdge;

/**
 * Performs a BiPartition depending on a previously computed clustering by {@link PartitionA} and the boundary edges of the subgraph subG
 * 
 * @author moritzfuchs
 *
 * @param <V> : The type of vertices
 * @param <E> : The type of edges
 */
public class PartitionB<V extends Comparable<V>,E> extends Clustering<V,E> {

	/**
	 * Logger object for this class
	 */
	private final static Logger LOGGER = LoggerFactory.getLogger(PartitionB.class.getName());
	
	/**
	 * The set of split vertices in subGprime the represents boundary edges (edges between the subgraph G[S] and G)
	 */
	private Set<SplitVertex<V,E>> boundaryVertices;
	
	/**
	 * The precomputed clustering 
	 */
	private Set<E> clustering;
		
	/**
	 * Original graph G
	 */
	private Graph<V,E> g;
	
	/**
	 * Subgraph of G that we will bisect
	 */
	private Graph<V,E> subG;
	
	
	/****************** RESULTS *****************/
	/**
	 * Set of edges inducing the clustering
	 */
	private Set<E> F;
	
	/**
	 * The left side of the bisection (the side t is attached to)
	 */
	private Set<V> L;
	
	/**
	 * The right side of the bisection (the side s is attached to)
	 */
	private Set<V> R;

	
	
	/**
	 * Setup all variables
	 * 
	 * @param task : {@link PartitionBTask}; contains the graph G, the subgraph G' we want to bisect and a pre-computed clustering of the subgraph computed by PartitionA
	 */
	public PartitionB(PartitionBTask<V,E> task) {
		super(task);
		this.g = task.getGraph();
		this.subG = task.getSubGraph();
		this.clustering = task.getClustering();
		
		this.L = new HashSet<V>();
		this.R = new HashSet<V>();
	}
	
	/**
	 * Get the boundary of a subgraph (edges with 1 endpoint in the subgraph and 1 endpoint outside of it. 
	 * 
	 * @param subG : A subgraph G' of G
	 * @param g : A graph
	 * @return : The set of edges with one endpoint in G' and one endpoint in G 
	 */
	public Set<E> getBoundary(Graph<V,E> subG , Graph<V,E>g) {
		
		Set<E> boundary = new HashSet<E>();
		
		for (V source : subG.vertexSet()) {
			for (E e : g.edgesOf(source)) {
				V target = Util.getEdgeTarget(g, source, e);
				if (!subG.vertexSet().contains(target)) {
					boundary.add(e);
				}
			}
		}
		
		return boundary;
	}
	
	/**
	 * Adds a node for each boundary edge to the subdivision graph subGprime, adds a source connected to the boundary vertices, 
	 * adds a target connected to all edges in the precomputed clustering. Then computes a cut between s and t. This cut is the 
	 * desired bisection. Apart from that 2 sets L and R are computes. L is the set of vertices in the subgraph that are between
	 * t and the cut, R is the set of vertices between s and the cut.   
	 * 
	 * @param g : The original graph G
	 * @param subG : The subgraph we want to bisect
	 * @param clustering : The clustering computed by {@link PartitionA}
	 */
	public void performClustering(Graph<V,E> g , Graph<V,E> subG , Set<E> clustering) {
		LOGGER.info("Performing PartitionB.");
		
		this.boundaryVertices = new HashSet<SplitVertex<V,E>>();
		this.clustering = clustering;
		
		Double log2n = (Math.log10(g.vertexSet().size()) / Math.log10(2));
		
		SplitGraph<V,E> subGprime = new SplitGraph<V,E>(subG);
		
		SplitVertex<V,E> s = new SplitVertex<V,E>();
		SplitVertex<V,E> t = new SplitVertex<V,E>();
		
		subGprime.addVertex(s);
		subGprime.addVertex(t);

		subGprime.setFlowSource(s);
		subGprime.setFlowTarget(t);
		
		Set<E> boundary = getBoundary(subG, g);
		
		for (E e : boundary) {
			SplitVertex<V,E> target;
			if (subG.vertexSet().contains(g.getEdgeSource(e))) {
				target = subGprime.getSplitVertexFromVertex(g.getEdgeSource(e));
			} else {
				target = subGprime.getSplitVertexFromVertex(g.getEdgeTarget(e));
			}
			
			SplitVertex<V,E> splitVertex = new SplitVertex<V,E>();
			splitVertex.setEdge(e);
			
			//Remember the split vertices for boundary edges
			boundaryVertices.add(splitVertex);
			
			subGprime.addVertex(splitVertex);
			
			//Connect boundary split vertex to endpoint in subG
			DefaultWeightedEdge new_edge = subGprime.addEdge(target, splitVertex);
			subGprime.setEdgeWeight(new_edge, g.getEdgeWeight(e) / log2n);
			
			//Connect boundary split vertex to s
			DefaultWeightedEdge new_source_edge = subGprime.addEdge(s , splitVertex);
			subGprime.setEdgeWeight(new_source_edge, g.getEdgeWeight(e) / log2n );
		}
		
		//Connect all edges of the clustering to a target vertex t
		for (E e : clustering) {
			SplitVertex<V,E> source = subGprime.getSplitVertexFromEdge(e);
			
			DefaultWeightedEdge new_target_edge = subGprime.addEdge(source, t);
			subGprime.setEdgeWeight(new_target_edge, g.getEdgeWeight(e));
		}
		
		
		FlowProblem<SplitVertex<V,E> , DefaultWeightedEdge> flow_problem = new UndirectedFlowProblem<SplitVertex<V,E> , DefaultWeightedEdge>(subGprime,s,t);
		Set<DefaultWeightedEdge> cut = flow_problem.getMinCut();

		if (F == null) {
			F = new HashSet<E>();
		}
		
		for (DefaultWeightedEdge cutEdge : cut) {
			E e = subGprime.getOriginalEdge(cutEdge);
			if ( subG.containsEdge(e)) {
				F.add(e);
			}
		}
		
		//Compute sets L (= vertices on the 't-side' of the cut) and R (= vertices on the 's-side' of the cut)
		
		Set<SplitVertex<V, E>> reachable = Connectivity.bfs(subGprime , s , new HashSet<SplitVertex<V,E>>() , cut);
		for (SplitVertex<V,E> splitVertex : reachable ) {
			if (splitVertex.isVertexContainer()) {
				R.add(splitVertex.getVertex());
			}
		}
		
		for (V v : subG.vertexSet()) {
			if (!R.contains(v)) {
				L.add(v);
			}
		}
		
		LOGGER.info("PartitionB finished. Notifying observers.");
		
		setChanged();
		notifyObservers(L);
	}
	
	/**
	 * Runs the Thread
	 */
	@Override
	public void run() {
		performClustering(g , subG , clustering);
	}
	
	/**
	 * Returns the bisection of the subgraph or null if the computation is not done yet
	 * 
	 * @return : The bisection of the given subgraph G' or null if the computation is not done yet (all observers are notified once the computation is done)
	 */
	@Override
	public Set<E> getClustering() {
		return F;
	}
	
	
	/**
	 * Returns the 'left' side of the bisection (= the side t is attached to)
	 * 
	 * @return The left side of the bisection 
	 */
	public Set<V> getL() {
		return L;
	}
	
	/**
	 * Returns the 'right' side of the bisection (= the side s is attached to)
	 * 
	 * @return : The right side of the bisection
	 */
	public Set<V> getR() {
		return R;
	}
	
	
}
