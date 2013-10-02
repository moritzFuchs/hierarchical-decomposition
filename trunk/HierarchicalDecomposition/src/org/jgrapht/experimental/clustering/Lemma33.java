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

//TODO: This name SUCKS! Think of better name!!!

public class Lemma33<V extends Comparable<V>,E> {

	/**
	 * Local logger
	 */
	private final static Logger LOGGER = LoggerFactory.getLogger(Lemma33.class
		      .getName());
	
	/**
	 * The set of active edges in G
	 */
	private Set<E> A;
	
	/**
	 * The set of inactive edges in G
	 */
	private Set<E> B;
	
	/**
	 * The current set of SplitGraphVertices (after last iteration: vertices that separate A and B)
	 */
	private Set<SplitVertex<V,E>> X_Y;
	
	/**
	 * The original graph G
	 */
	private Graph<V,E> g;
	
	/**
	 * The subdivision graph G' with one additional vertex for each edge in G
	 */
	private SplitGraph<V,E> gPrime;
	
	/**
	 * The result of performing Lemma 3.3, the new edge set
	 */
	private Set<E> F;
	
	/**
	 * Case in which the lemma ended up in
	 */
	private Integer caseNum = -1;
	
	//TODO : Document
	public Lemma33(Graph<V,E> g , Set<E> A , Set<E> B) {
		LOGGER.fine("Setting up variables.");
		
		if (B.size() > 2 * A.size() / Math.log10(g.vertexSet().size())) {
			LOGGER.warning("|B| <= |A| / log n does not hold!");
			throw new IllegalArgumentException("|B| <= |A| / log n does not hold!");
		}
		
		this.A = A;
		this.B = B;
		
		this.F = new HashSet<E>(A);
		F.addAll(B);
		
		this.g = g;
		this.gPrime = new SplitGraph<V,E>(g);
		
		this.X_Y = new HashSet<SplitVertex<V,E>>();

		this.gPrime.addSourceAndTargetForLemma33(gPrime.getSplitVertices(A),gPrime.getSplitVertices(B));			
	}
	
	/**
	 * Returns the resulting set of edges (or A+B if Lemma 3.3 hat not been performed yet)
	 * 
	 * @return : The resulting set of edges after performing Lemma 3.3 (before that is returns A+B)
	 */
	public Set<E> getF() {
		return F;
	}
	
	/**
	 * Returns the case in which the lemma ended up in (or -1 before the lemma is applied) 
	 * 
	 * @return : The case number for Lemma 3.3 (or -1 if the lemma has not been applied yet)
	 */
	public Integer getCaseNum() {
		return caseNum;		
	}
	
	/**
	 * Performs Lemma 3.3; Changes the Sets A and B and returns an Integer indicating in which Case the Lemma ended up.
	 * 
	 *  @return The number of the case the Lemma ended up in.
	 */
	public Integer performLemma33() {
		
		//If either A or B is empty, we are done.
		if (B.size() == 0 || A.size() == 0) {
			this.caseNum = 2;
			return this.caseNum;
		}
		
		LOGGER.info("Performing Lemma 3.3");
		while (Connectivity.<SplitVertex<V,E> , DefaultWeightedEdge>hasFlowPath(gPrime, gPrime.getFlowSource(), gPrime.getFlowTarget())) {
			LemmaA2();
		}
		
		LOGGER.info("Graph no longer connected. Retrieving edge set F.");
		
		Set<E> C = gPrime.getOriginalEdges(X_Y);
		
		Set<E> F_new = new HashSet<E>(A);
		F_new.addAll(C);
		
		LOGGER.info("F = A+C = " + F);
		
		if (Connectivity.isBalancedClustering(g, F_new) ) {
			LOGGER.info("Good new everyone! F = A+C induces a balanced clustering. Hence we are in Lemma 3.3 Case 2.");
			F = F_new;
			
			this.caseNum = 2;
			return this.caseNum;
		} else {
			LOGGER.info("Bad new everyone ... F = A+C does not induce a balanced clustering. Hence F = B+C and we are in Case 1 of Lemma 3.3.");
			
			//B + C must be a balanced clustering
			F = new HashSet<E>(B);
			F.addAll(C);
			
			this.caseNum = 1;
			return this.caseNum;
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
		for(SplitVertex<V, E> v : gPrime.getSplitVertices(A)) {
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
		
		for(SplitVertex<V, E> v : gPrime.getSplitVertices(B)) {
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
