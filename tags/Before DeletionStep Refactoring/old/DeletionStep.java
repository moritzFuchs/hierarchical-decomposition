package org.jgrapht.experimental.clustering.old;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.experimental.clustering.Connectivity;
import org.jgrapht.experimental.clustering.SplitGraph;
import org.jgrapht.experimental.clustering.SplitVertex;
import org.jgrapht.experimental.clustering.Util;
import org.jgrapht.graph.DefaultWeightedEdge;

import com.google.common.collect.BiMap;

/**
 * Performs a deletion step for the modified KRV procedure in {@link ModifiedKRVProcedure}  
 * 
 * @author moritzfuchs
 *
 * @param <V> : The type of vertices 
 * @param <E> : The type of edges
 */
public class DeletionStep<V extends Comparable<V>,E> {

	/**
	 * If F_new gets smaller than KRV_RESTART_BOUND * F_original we restart with A := A + B the KRV procudure
	 */
	private static final Double KRV_RESTART_BOUND = 7.0 / 8.0;

	/**
	 * The original graph G
	 */
	private Graph<V,E> g;
	
	/**
	 * The set A of active edges computed by the deletion step
	 */
	private Set<E> A_new;
	
	/**
	 * The set B of inactive edges computed by the deletion step
	 */
	private Set<E> B_new;
	
	/**
	 * The subdivision graph G'
	 */
	private SplitGraph<V,E> gPrime;
	
	/**
	 * Mapping from edges in G to Integers (unique number for each edge) 
	 */
	private BiMap<E,Integer> edgeNum;
	
	/**
	 * copy of flow vectors of edges in G
	 */
	private Double[][] flowVectors;
	
	/**
	 * Number of edges in G
	 */
	private Integer m;

	/**
	 * Contains the size of the original clustering - the number of edges in F at the start of the KRV procedure
	 */
	private Integer originalClusteringSize;
	
	public DeletionStep(Graph<V,E> g,
			SplitGraph<V,E> gPrime,
			BiMap<E , Integer> edgeNum,
			Double[][] flowVectors,
			Integer m,
			Integer originalClusteringSize
			) {
		this.g = g;
		this.gPrime = gPrime;
		this.edgeNum = edgeNum;
		this.flowVectors = Util.clone2DArray(flowVectors);
		this.m = m;
		this.originalClusteringSize = originalClusteringSize;
		
	}
	
	/**
	 * Performs a deletion step and returns the resulting flow vectors. Also changes the sets A and B to reflect these changes.
	 * 
	 * @param A : The set of active edges e \in E[G]
	 * @param B : The set of inactive edges e \in E[G]
	 * @param A_s : The set of active source splitvertices e \in E[G']
	 * @param A_t : The set of active target splitvertices e \in E[G']
	 * @param dec : the Flow decomposition object
	 * @param flow : the flow in G'_st
	 * @return the resulting flowVectors
	 */
	public Double[][] performDeletionStep(Set<E> A, 
			Set<E> B , 
			Set<SplitVertex<V,E>> A_s , 
			Set<SplitVertex<V,E>> A_t , 
			FlowDecomposer<SplitVertex<V,E>, DefaultWeightedEdge> dec , 
			Map <DefaultWeightedEdge , Double> flow) {

		A_new = new HashSet<E>(A);
		B_new = new HashSet<E>(B);
		
		//Get cut induced by flow
		Set<DefaultWeightedEdge> cut = dec.getCut(gPrime, flow, gPrime.getFlowSource() , gPrime.getFlowTarget());
		
		Set<E> C = gPrime.translateCut(cut);

		//Compute ((A + B) - A_t) + C
		Set<E> newClustering = new HashSet<E>(A_new);
		newClustering.addAll(B);
		newClustering.removeAll(gPrime.getOriginalEdges(A_t));
		newClustering.addAll(C);
		
		
		if (Connectivity.isBalancedClustering(g, newClustering)) {
			//Iteration done. Lemma returns with Case 1
			
			/*
			 * If ((A + B) - A_t) + C induces a balanced clustering we want to 'restart' the KRV procedure. 
			 * Therefore we set A = F = ((A + B) - A_t) + C and reset the flow fectors to '1' for each e \in A = F
			 */
			A_new.addAll(B);
			A_new.removeAll(gPrime.getOriginalEdges(A_t));
			A_new.addAll(C);
			
			//B_new = C;
			B_new.clear();
			
			
			//TODO: Use new Flow Vectors
			flowVectors = Util.getInitialFlowVectorsForSet(A_new , edgeNum , m);
		} else { // In this case ((A + B) - A_s) + C induces a balanced clustering and we must move the flow vectors of A_s to the cut C
				
			A_new.removeAll(gPrime.getOriginalEdges(A_s));
			
			//Compute the assignment of fractional flow vectors to cut edges
			Map<E , Double[]> sum = computeFlowVectorMovement(dec, flow, cut);

			 //All flow vectors of source edges on flow paths were now moved and summed up. Now let's see which edges have flow vectors >= 1.
			assignEdges(sum);
			
			//Delete all old flow vectors (those for edges in A_s \ A_new)
			deleteOldFlowVectors(A_s);

			/* 
			 * If F = A + B gets too small we restart the KRV procedure. 
			 * This is similar to putting A = F and resetting the flow vectors for A to have a unit of a unique commodity for each edge in A
			 */ 
			if (A_new.size() + B_new.size() < KRV_RESTART_BOUND * originalClusteringSize) {
				A_new.addAll(B_new);
				B_new.clear();
				
				flowVectors = Util.getInitialFlowVectorsForSet(A_new, edgeNum, m);
			}
		}

		return flowVectors;
	}

	/**
	 * Deleted old flow vectors that have been moved to cut edges. 
	 * Deletes flow vectors for edges in the given set A if they have not been reassigned to A_new. (this can happen if the cut for an edge e=(u,v) is between s -- (u,v) and (u,v) -- u or v) 
	 * 
	 * @param A : Edges for which the flow vector is deleted if they were not reassigned as active edges (in this case they are in A_new)
	 */
	private void deleteOldFlowVectors(Set<SplitVertex<V, E>> A) {
		for (SplitVertex<V,E> v : A) {
			E originalEdge = gPrime.getOriginalEdge(v);
			if (!A_new.contains(originalEdge)) {
				flowVectors[edgeNum.get(originalEdge)] = new Double[m];
				Arrays.fill(flowVectors[edgeNum.get(originalEdge)] , 0.0);
			}
		}
	}

	/**
	 * Assigns edges to A or B depending on the amount of flow they received by the {@link DeletionStep#computeFlowVectorMovement(FlowDecomposer, Map, Set)} operation.
	 * If an edge has flow vectors of total length >= 1 assigned to it, it will become an active edge. Additionally the received flow vector will be normalized to have a length of 1.
	 * If the total length of the receiver flow vectors is < 1 the edge will be marked as inactive. The assigned fractional flow vectors will be neglected.
	 * 
	 * @param sum : The assignment of sums of fractional flow vectors to cut edges
	 */
	private void assignEdges(Map<E, Double[]> sum) {
		for (E e : sum.keySet()) {
			
			if (Util.vectorLength(sum.get(e)) >= 1) {
				
				Double[] newFlowVector = Util.normalizeVector(sum.get(e));
				
				//save normalized flow vector
				flowVectors[edgeNum.get(e)] = newFlowVector;
				
				A_new.add(e);
				B_new.remove(e);
			} else {
				B_new.add(e);
			}				
		}
	}

	/**
	 * Moves fractions of flow vectors according to the given s-t-flow and its flow path decomposition. 
	 * Each flow vector of a source edge (must be on index 1 on path due to the nature of the flow problem) 
	 * is moved to the nearest cut edge on its flow path. If the source edge has several flow paths, we move 
	 * fractions of the flow vector according to each path's weight.
	 * 
	 * @param dec : The decomposition algorithm
	 * @param flow : The s-t-flow on G'_st
	 * @param cut : The cut edges induced by the flow
	 * @return An assignment of sums of fractional flow vectors to cut edges in G
	 */
	private Map<E , Double[]> computeFlowVectorMovement(
			FlowDecomposer<SplitVertex<V, E>, DefaultWeightedEdge> dec,
			Map<DefaultWeightedEdge, Double> flow,
			Set<DefaultWeightedEdge> cut) {
		
		Map<E , Double[]> sum = new HashMap<E , Double[]>();
		
		for (List<SplitVertex<V, E>> path : dec.getFlowPaths(gPrime, flow, gPrime.getFlowSource(), gPrime.getFlowTarget())) {
			if (dec.getFlowPathWeight(path) <= 1) {
				//Move flow vector to first cut edge on path

				DefaultWeightedEdge cutEdge = findCutEdge(cut, path);
				E originalCutEdge = gPrime.getOriginalEdge(cutEdge);
				
				//if the original edge is in A, then it already has a flow vector of length 1. Hence we ignore this fraction of the flow vector
				if (!A_new.contains(originalCutEdge)) {
					
					//If we did not see the originalCutEdge before, generate a 0-vector for it
					if (sum.get(originalCutEdge) == null) {
						Double[] n = new Double[m];
						Arrays.fill(n, 0.0);
						sum.put(originalCutEdge, n);
					}
					
					//Assign flow vector of first source edge(= SplitVertex in G' !) (must be on index 1) to the first cut edge on the path (as previously computed and saved in cutEdge)
					Double[] sumElement = sumFlowVectors(sum.get(originalCutEdge) , flowVectors[edgeNum.get(gPrime.getOriginalEdge(path.get(1)))] , dec.getFlowPathWeight(path));
					
					sum.put(originalCutEdge, sumElement);
				}
			}
		}
		return sum;
	}

	/**
	 * Finds the first cut edge on a given path
	 * 
	 * @param cut : The set of cut edges
	 * @param path : The path that we want to search
	 * @return : The first cut edge on the given path
	 */
	private DefaultWeightedEdge findCutEdge(Set<DefaultWeightedEdge> cut, List<SplitVertex<V, E>> path) {
		//find the first cut edge on the path
		Integer index = 0;
		DefaultWeightedEdge cutEdge = null;
		while (cutEdge == null) {
			DefaultWeightedEdge currentEdge = gPrime.getEdge(path.get(index), path.get(index+1));
			if (cut.contains(currentEdge)) {
				cutEdge = currentEdge;
			}
			index++;
		}
		return cutEdge;
	}

	/**
	 * Returns the resulting set of active edges A
	 * 
	 * @return : the resulting set of active edges
	 */
	public Set<E> getA() {
		return A_new;
	}
	
	/**
	 * Returns the resulting set of inactive edges B
	 * @return : The resulting set of inactive edges
	 */
	public Set<E> getB() {
		return B_new;
	}
	
	/**
	 * For 2 Vectors flow1 and flow2 and a given weight w returns flow1 + w * flow2
	 * 
	 * @param flow1 : First vector
	 * @param flow2 : Second vector
	 * @param weight : Fraction of flow2 that is added to flow1
	 * @return flow1 + w * flow2
	 */
	private Double[] sumFlowVectors(Double[] flow1, Double[] flow2 , Double weight) {
		for (int i=0;i<m;i++) {
			flow1[i] += weight * flow2[i];
		}
		return flow1;
	}

	/**
	 * Returns the resulting flow vectors
	 * 
	 * @return The resulting flow vectors
	 */
	public Double[][] getFlowVectors() {
		return this.flowVectors;
	}
	
}
