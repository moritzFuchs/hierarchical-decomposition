package org.jgrapht.experimental.clustering;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.jgrapht.Graph;
import org.jgrapht.experimental.util.LoggerFactory;
import org.jgrapht.graph.DefaultWeightedEdge;

import com.google.common.collect.Iterables;

/**
 * Performs a BiPartition depending on a previously computed clustering by {@link PartitionA} and the boundary edges of the subgraph G[S] of G.
 * WARNING: Since no approximate max-flow algorithm was available when writing this class, this class might need some additional work!
 * 
 * @author moritzfuchs
 * @date 19.09.2013
 *
 * @param <V> : The type of vertices
 * @param <E> : The type of edges
 */
public class ApproximatePartitionB<V extends Comparable<V>,E> extends Clustering<V,E> {

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
	
	/**
	 * Subdivision graph G[S]' of G[S] with additional {@link SplitVertex} for each boundary edge of G[S] (edge with exactly 1 endpoint in G[S] and one endpoint in G)
	 */
	private SplitGraph<V,E> gPrime;
	
	/****************** RESULTS *****************/
	/**
	 * Set of edges inducing the clustering
	 */
	private Set<E> F;
	
	/**
	 * Set of split vertices representing boundary edges (needed for approximate version, ignore if you are only interested in exact version)
	 */
	private Set<SplitVertex<V,E>> A;
	
	/**
	 * Set of split vertices representing the clustering edges
	 */
	private Set<SplitVertex<V,E>> B;
	
	/**
	 * The left side of the bisection (the side t is attached to)
	 */
	private Set<V> L;
	
	/**
	 * The right side of the bisection (the side s is attached to)
	 */
	private Set<V> R;

	/**
	 * Set of {@link SplitVertex} representing the bisection of G[S]
	 */
	private Set<SplitVertex<V,E>> X_Y;
	
	
	/**
	 * Setup all variables
	 * 
	 * @param task : {@link PartitionBTask}; contains the graph G, the subgraph G' we want to bisect and a pre-computed clustering of the subgraph computed by PartitionA
	 */
	public ApproximatePartitionB(PartitionBTask<V,E> task) {
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
	
	/**
	 * Computes the second partition using an approximate max-flow algorithm. 
	 * WARNING: Might need some work since no approximate algorithm was available for testing.  
	 * 
	 * @param g : The original (complete) graph G
	 * @param subG : The sub graph G[S] of G we want to bisect
	 * @param clustering : Decomposition of G[S] (computed by {@link PartitionA}) 
	 */
	public void performClustering(Graph<V,E> g , Graph<V,E> subG , Set<E> clustering) {
		
		LOGGER.info("Performing PartitionB.");
		
		this.boundaryVertices = new HashSet<SplitVertex<V,E>>();
		
		Double log2n = (Math.log10(g.vertexSet().size()) / Math.log10(2));
		
		gPrime = new SplitGraph<V,E>(subG);
		
		SplitVertex<V,E> s = new SplitVertex<V,E>();
		SplitVertex<V,E> t = new SplitVertex<V,E>();
		
		gPrime.addVertex(s);
		gPrime.addVertex(t);

		gPrime.setFlowSource(s);
		gPrime.setFlowTarget(t);
		
		Set<E> boundary = getBoundary(subG, g);
		
		for (E e : boundary) {
			SplitVertex<V,E> target;
			if (subG.vertexSet().contains(g.getEdgeSource(e))) {
				target = gPrime.getSplitVertexFromVertex(g.getEdgeSource(e));
			} else {
				target = gPrime.getSplitVertexFromVertex(g.getEdgeTarget(e));
			}
			
			SplitVertex<V,E> splitVertex = new SplitVertex<V,E>();
			splitVertex.setEdge(e);
			
			//Remember the split vertices for boundary edges
			boundaryVertices.add(splitVertex);
			
			gPrime.addVertex(splitVertex);
			A.add(splitVertex);
			
			//Connect boundary split vertex to endpoint in subG
			DefaultWeightedEdge new_edge = gPrime.addEdge(target, splitVertex);
			gPrime.setEdgeWeight(new_edge, g.getEdgeWeight(e) / log2n);
			
			//Connect boundary split vertex to s
			DefaultWeightedEdge new_source_edge = gPrime.addEdge(s , splitVertex);
			gPrime.setEdgeWeight(new_source_edge, g.getEdgeWeight(e) / log2n );
		}
		
		//Connect all edges of the clustering to a target vertex t
		for (E e : clustering) {
			SplitVertex<V,E> source = gPrime.getSplitVertexFromEdge(e);
			
			B.add(source);
			
			DefaultWeightedEdge new_target_edge = gPrime.addEdge(source, t);
			gPrime.setEdgeWeight(new_target_edge, g.getEdgeWeight(e));
		}
		
		
		while (Connectivity.<SplitVertex<V,E> , DefaultWeightedEdge>hasFlowPath(gPrime, gPrime.getFlowSource(), gPrime.getFlowTarget())) {
			LemmaA2();
		}

		F = new HashSet<E>();
		
		for (SplitVertex<V,E> v : X_Y) {
			if (!boundaryVertices.contains(v)) {
				F.add(gPrime.getOriginalEdge(v));
			}
		}
		
		//Compute sets L (= vertices on the 't-side' of the cut) and R (= vertices on the 's-side' of the cut)
		Set<V> reachable = Connectivity.bfs(subG , Iterables.get(subG.vertexSet(), 0) , new HashSet<V>() , F);
		for (V v : reachable ) {
				R.add(v);
		}
		
		for (V v : subG.vertexSet()) {
			if (!R.contains(v)) {
				L.add(v);
			}
		}
		
	}
	
	/**
	 * Computes flow from s to t and removes cut vertices x_e that handle more than cap(e) / 2 of flow
	 */
	private void LemmaA2(){
		
		FlowProblem<SplitVertex<V,E> , DefaultWeightedEdge> flow_problem = new UndirectedFlowProblem<SplitVertex<V,E>,DefaultWeightedEdge>(gPrime, gPrime.getFlowSource() , gPrime.getFlowTarget());
	
		Map<DefaultWeightedEdge , Double> maxFlow = flow_problem.getMaxFlow();	
		Set<FlowPath<SplitVertex<V, E>>> pathSet = flow_problem.getPaths();
		Set<DefaultWeightedEdge> cut = flow_problem.getMinCut();
		
		//Set of Split vertices that represent edges in G and are incident to the cut.
		Set<SplitVertex<V,E>> cutVertices = new HashSet<SplitVertex<V,E>>();
		
		//Get SplitGraphVertices that represent the edges cut in G 
		for (DefaultWeightedEdge e : cut) {
			cutVertices.add(gPrime.getIncidentSplitVertex(e));
		}
		
		Map<SplitVertex<V,E> , Double> flowAssignment = new HashMap<SplitVertex<V,E> , Double>();

		//Assign each flow path (its weight) to the first cut vertex representing an edge in G on the path
		for (FlowPath<SplitVertex<V,E>> path : pathSet) {
			
			SplitVertex<V,E> first = getFirstSplitVertexOnPath(path.getPath() , cutVertices);
			if (flowAssignment.get(first) == null) {
				flowAssignment.put(first, 0.0);
			}
			
			//Assign flow path weight to first cut vertex on path as computed by getFirstSplitVertexOnPath
			flowAssignment.put(first, flowAssignment.get(first) + path.getFlowPathWeight());
		}
		
		//remove vertices with more than cap(e) / 2 of flow path weights assigned to it
		for(SplitVertex<V,E> v : flowAssignment.keySet()) {
			if (flowAssignment.get(v) >= g.getEdgeWeight(gPrime.getOriginalEdge(v)) / 2) {
				X_Y.add(v);
				gPrime.removeVertex(v);
			}
		} 

		adjustEdgeCapacities(maxFlow);
	}

	/**
	 * Adjusts the edge capcities of edges incident to X_A or X_B. 
	 * Let v \in X_A and e = (s,v) where s is the source vertex. Then we reduce all edges incident to v by flow(e).
	 * X_B is handled in a similar fashion.
	 * 
	 * @param maxFlow : The max flow between s and t in G'
	 */
	private void adjustEdgeCapacities(Map<DefaultWeightedEdge, Double> maxFlow) {
		for(SplitVertex<V, E> v : A) {
			DefaultWeightedEdge e = gPrime.getEdge(gPrime.getFlowSource(), v);
			
			////e might have been deleted already (then there is no flow)
			if (e != null) {
				Double localFlow = maxFlow.get(e);
				//reduce capacity of vertices incident to s by the flow between source s and v
				for (DefaultWeightedEdge incident : gPrime.edgesOf(v)) {
					gPrime.setEdgeWeight(incident, Math.max(0.0, gPrime.getEdgeWeight(incident) - localFlow));
				}
			}
		}
		
		for(SplitVertex<V, E> v : B) {
			DefaultWeightedEdge e = gPrime.getEdge(v , gPrime.getFlowTarget());
			
			//e might have been deleted already (then there is no flow)
			if (e != null) {
				Double localFlow = maxFlow.get(e);
				//reduce capacity of vertices incident to t by the flow between t and v
				for (DefaultWeightedEdge incident : gPrime.edgesOf(v)) {
					gPrime.setEdgeWeight(incident, Math.max(0.0 , gPrime.getEdgeWeight(incident) - localFlow));
				}
			}
		}
	}

	/**
	 * Takes a path p and a set of vertices A and returns the first occurrence of any vertex v \in A on p  
	 * 
	 * @param path : A path in G'
	 * @param cutVertices : A set of vertices A of G'
	 * @return : First occurence of a vertex v \in A on p (or null in there is no vertex v s.t. v \in A and v \in p)
	 */
	private SplitVertex<V,E> getFirstSplitVertexOnPath(List<SplitVertex<V, E>> path , Set<SplitVertex<V, E>> cutVertices) {
		for (int i=1; i<path.size();i++) {
			if (cutVertices.contains(path.get(i)))
				return path.get(i);
		}
		
		return null;
	}
}
