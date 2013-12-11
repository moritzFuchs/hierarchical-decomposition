package org.jgrapht.experimental.clustering.old;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jgrapht.experimental.clustering.SplitGraph;
import org.jgrapht.experimental.clustering.SplitVertex;
import org.jgrapht.experimental.clustering.Util;
import org.jgrapht.experimental.clustering.krv.MatchingMatrix;
import org.jgrapht.experimental.clustering.util.MatchedPair;

import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;

/**
 * Contains the algorithm to perform a matching step in the modified KRV procedure.
 * Gets fractional, partion matching and exchanges fractions of flow vectors between pairs of matched vertices 
 * (the vertices are split vertices in G' and are therefore edges in the original graph G)
 * 
 * @author moritzfuchs
 *
 * @param <V>
 * @param <E>
 */
public class MatchingStep<V extends Comparable<V>,E> {

	/**
	 * Fraction of weight that is moved with each matching
	 */
	private static final Double FLOW_MOVEMENT_FRACTION = 0.5;
	
	/**
	 * The old flow vectors
	 */
	private Double[][] flowVectors;
	
	/**
	 * Mapping of edges to a unique integer \in [0 ... m-1] 
	 */
	private Map<E , Integer> edgeNum;
	
	/**
	 * Number of edges in G 
	 */
	private Integer m;
	
	/**
	 * Contains the resulting flow vectors
	 */
	private Double[][] newFlowVectors;
	
	public MatchingStep(Double[][] flowVectors , Map<E , Integer> edgeNum , Integer m) {
		this.flowVectors = flowVectors;
		this.newFlowVectors = Util.clone2DArray(flowVectors);
		this.edgeNum = edgeNum;
		this.m = m;
	}
	
	
	//TODO: better documentation
	/**
	 * Get the Matching Matrix 
	 * 
	 * @param gPrime : The subdivision graph G' of G
	 * @param activeEdges : The set of currently active edges
	 * @param matching : The set of matched pairs
	 * @return : The sparse matrix corresponding to the given matching
	 */
	public MatchingMatrix getMatchingMatrix(SplitGraph<V,E> gPrime,
			Set<E> activeEdges,
			Set<MatchedPair<SplitVertex<V, E>>> matching) {
		
		SparseDoubleMatrix2D matrix = new SparseDoubleMatrix2D(m,m);
		
		for (E e : activeEdges) {
			matrix.setQuick(edgeNum.get(e), edgeNum.get(e), 1.0);
		}
		
		for (MatchedPair<SplitVertex<V,E>> pair : matching) {
			SplitVertex<V,E> from = pair.getV();
			SplitVertex<V,E> to = pair.getW();
			Double weight = pair.getWeight();
			
			E fromEdge = gPrime.getOriginalEdge(from);
			E toEdge = gPrime.getOriginalEdge(to);
			
			Integer toNum = edgeNum.get(toEdge);
			Integer fromNum = edgeNum.get(fromEdge);
			
			matrix.setQuick(fromNum, toNum, weight * FLOW_MOVEMENT_FRACTION);
			matrix.setQuick(toNum, fromNum, weight * FLOW_MOVEMENT_FRACTION);
			
			matrix.setQuick(toNum, toNum, matrix.getQuick(toNum,toNum) - weight * FLOW_MOVEMENT_FRACTION);
			matrix.setQuick(fromNum, fromNum, matrix.getQuick(fromNum,fromNum) - weight * FLOW_MOVEMENT_FRACTION);
		}
		
		MatchingMatrix m = new MatchingMatrix(matrix);
		
		return m;
	}
	
	/**
	 * Gets a fractional, partial matching and performs an averaging step (move flow vectors depending on the weight between a pair of matched vertices)
	 * ('right' computation: for 2 Matchings (a,c,w_1) , (b,c,w_2) f_c = (1 - w_1 - w_2) * f_c + w_1 * f_a + w_2 * f_b )
	 * 
	 * @param matching : The matching we want to apply
	 * @return The modified flowVector matrix
	 */
	public Double[][] performMatchingStepV2(SplitGraph<V,E> gPrime,
			Set<MatchedPair<SplitVertex<V, E>>> matching) {
		
		Map<E , Double> collectiveWeight = new HashMap<E , Double>();
		
		//Compute how much of its flow vector each edge is going to lose
		for(MatchedPair<SplitVertex<V,E>> match : matching) {
			E v = gPrime.getOriginalEdge(match.getV());
			E w = gPrime.getOriginalEdge(match.getW());
			
			if (!collectiveWeight.containsKey(v)) {
				collectiveWeight.put(v, 0.0);
			}
			if (!collectiveWeight.containsKey(w)) {
				collectiveWeight.put(w, 0.0);
			}
			
			//Add additional weight for current matching
			collectiveWeight.put(v, collectiveWeight.get(v) + match.getWeight() * FLOW_MOVEMENT_FRACTION);
			collectiveWeight.put(w, collectiveWeight.get(w) + match.getWeight() * FLOW_MOVEMENT_FRACTION);
		}
		
		//Now we know what fraction of its flow vector it will lose.
		
		 //decrease new flow vectors be the factor specified in collectiveWeights map (equals amount of the flow vector that will be distributed among other edges/Splitvertices)
		 for (E e : collectiveWeight.keySet()) {
			 for (int i=0;i<m;i++) {
				 newFlowVectors[edgeNum.get(e)][i] = (1 - collectiveWeight.get(e)) * flowVectors[edgeNum.get(e)][i];
			 }
		 }
		 
		 //The new flow vectors have now been reduced by the factor in collectiveWeights
		 
		 //Now, add fractions of flow vectors to new flow vectors as specified by the matching
		 for(MatchedPair<SplitVertex<V,E>> match : matching) {

			E v = gPrime.getOriginalEdge(match.getV());
			E w = gPrime.getOriginalEdge(match.getW());
			//For each entry v_i exchange the specified amount of flow between the matched edges / splitvertices
			for(int i=0; i<m; i++) {
				newFlowVectors[edgeNum.get(v)][i] += flowVectors[edgeNum.get(w)][i] * match.getWeight() * FLOW_MOVEMENT_FRACTION;
				newFlowVectors[edgeNum.get(w)][i] += flowVectors[edgeNum.get(v)][i] * match.getWeight() * FLOW_MOVEMENT_FRACTION;
			}
		}
		
		//Averaging is done now.
		
		//return the new set of flow vectors
		return newFlowVectors;
	}
	
	/**
	 * Returns the resulting flow vectors
	 * 
	 * @return The resulting flow vectors
	 */
	public Double[][] getFlowVectors() {
		return newFlowVectors;
	}
}
