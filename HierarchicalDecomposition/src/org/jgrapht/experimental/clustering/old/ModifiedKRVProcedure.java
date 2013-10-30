package org.jgrapht.experimental.clustering.old;

import org.jgrapht.Graph;
import org.jgrapht.experimental.clustering.FlowRescaler;
import org.jgrapht.experimental.clustering.SplitGraph;
import org.jgrapht.experimental.clustering.SplitVertex;
import org.jgrapht.experimental.clustering.exception.StalemateException;
import org.jgrapht.experimental.clustering.util.MatchedPair;
import org.jgrapht.experimental.util.LoggerFactory;
import org.jgrapht.graph.DefaultWeightedEdge;

import com.google.common.collect.BiMap;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Implements Lemma 3.2 of 'Computing Cut-Based Hierarchical Decompositions in Almost linear time' (refered to as 'paper') by R�cke et al.
 * 
 * @author moritzfuchs
 *
 * @param <V> : The type of vertices
 * @param <E> : The type of edges
 */
public class ModifiedKRVProcedure<V extends Comparable<V>,E> {
 
	/**
	 * Logger for this class
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ModifiedKRVProcedure.class.getName());
	
	/**
	 * Potential bound (return if potential < bound)
	 */
	private Double bound = 0.0;
	
	//FIXME: Get rid of flow vectors	
	/**
	 * The current flow vectors
	 */
	private Double[][] flowVectors = null;
	
	/**
	 * The original graph G
	 */
	private Graph<V,E >g;
	
	/**
	 * The subdivision graph G'
	 */
	private SplitGraph<V,E> gPrime;
	
	/**
	 * The set of active edges in F
	 */
	private Set<E> A;
		
	/**
	 * The set of inactive edges in F 
	 */
	private Set<E> B;
	
	/**
	 * A BiMap from edges to Integer (used to access flow vector matrix)
	 */
	private BiMap<E , Integer> edgeNum;
	
	/**
	 * Number of edges in G
	 */
	private Integer m;
	
	/**
	 * Number of vertices in G
	 */
	private Integer n;

	/**
	 * Size of the clustering at the beginning of the KRV procedure (the number of edges in F)
	 */
	private int originalClusteringSize;
	
	
	/**
	 * Sets up the flow vectors for all edges
	 * 
	 * @param g : The original graph G
	 * @param gPrime : The SplitVertexGraph G'  with one additional vertex per edge in G
	 * @param A : The set of active edges in F (= A + B)
	 * @param B : The set of inactive edges in F (= A + B)
	 * @param edgeNum : A Mapping from edges in E to Integer
	 */
	public ModifiedKRVProcedure (Graph<V,E> g , SplitGraph<V,E> gPrime , Set<E> F , BiMap<E , Integer> edgeNum) {
		
		this.g = g;
		this.gPrime = gPrime;
		
		this.m = g.edgeSet().size();
		this.n = g.vertexSet().size();
		
		this.A = new HashSet<E>(F);
		this.B = new HashSet<E>();
		
		this.originalClusteringSize = F.size();
		
		this.edgeNum = edgeNum;
		
		this.flowVectors = new Double[m][m];
		
		for (int i = 0;i<m;i++) {
			for (int j = 0;j<m;j++) {
				this.flowVectors[i][j] = 0.0;
			}
		}
		
		//Init flow vectors. Each e \in A receives a unique flow vector
		for (E e: A) {
			//If edge number i is contained in A it is active and therefore receives a flow vectors with commodity i
			flowVectors[edgeNum.get(e)][edgeNum.get(e)] = 1.0;
		}
		
		this.bound = 1/(16 * Math.pow(n,2)); 
	}
	
	/**
	 * performs the modified KRV procedure as described in the proof for Lemma 3.2 and returns the case in which the Lemma ended up in
	 * 
	 * @return
	 * @throws StalemateException : Thrown if algorithm reaches stalemate
	 */
	public Integer performModifiedKRVProcedure() throws StalemateException {
		return iterate(g, gPrime);
	}
		
	/**
	 * Iteration of Lemma 3.2: Computes Flow between active vertices and performs a matching- or a deletion-step depending on a coin flip. 
	 * 
	 * @param g : The original graph
	 * @param gPrime : The SplitGraph of G
	 * @param A : The set of active edges (= edges with flowVector)
	 * @param B : The set of inactive edges (= edges without flowVector)
	 * @param edgeNum : BiMapping of edges to Numbers s.t. every edge has a unique number
	 * @return The Case in which the Lemma ended up in. 1 for Case 1 of Lemma 3.2, 2 for Case 2 of Lemma 3.2
	 * @throws StalemateException : Thrown if algorithm reaches stalemate
	 */
	private Integer iterate(Graph<V,E> g , SplitGraph<V,E> gPrime) throws StalemateException {

		VectorPotential<V,E> potentialComputation = new VectorPotential<V,E>(edgeNum);
		Double current_potential = potentialComputation.getPotential(flowVectors , A);

		while (current_potential >= bound && A.size() > 1) {
			
			LOGGER.fine("Current potential = " + current_potential + "; Potential bound = " + bound);
			
			// -------------------------------- SETUP START --------------------------------------------------------------------- //
			VerticeDivider<V,E> divider = new VerticeDivider<V,E>(flowVectors , m , edgeNum);
			
			//Divide set of active edges into sets A_s (first element in pair) and A_t (second element in pair)
			divider.divideActiveVertices( gPrime, A );

			//Add source s and target t to G' and connect them to A_s and A_t respectively
			gPrime.addSourceAndTarget(divider.getAs(), divider.getAt());
			
			//Create the flow object
			DummyFlow<SplitVertex<V, E>, DefaultWeightedEdge> maxFlowComputation = 
				new DummyFlow<SplitVertex<V,E>,DefaultWeightedEdge> (
					(Graph<SplitVertex<V, E>, DefaultWeightedEdge>) gPrime , 
					gPrime.getFlowSource() , 
					gPrime.getFlowTarget()
				);
			
			//Compute maxFlow
			Map <DefaultWeightedEdge , Double> maxFlow = maxFlowComputation.getFlow();
			
			//Get the flow decomposer (computes flow paths , a cut or a partial fractional matching)
			FlowDecomposer<SplitVertex<V,E>, DefaultWeightedEdge> dec = new FlowDecomposer<SplitVertex<V,E>, DefaultWeightedEdge>();
			
			//Rescale flow
			FlowRescaler<V,E> rescaler = new FlowRescaler<V, E>();
			maxFlow = rescaler.rescaleFlow(gPrime, maxFlow, dec);
			// -------------------------------- SETUP END ----------------------------------------------------------------------- //
			
			//Try Deletion + Matching step, choose the one with less potential
			Double[][] newFlow;

			//Perform a deletion step
			DeletionStep<V,E> deletionStep = new DeletionStep<V,E>(g,gPrime,edgeNum,flowVectors,m,originalClusteringSize);
			newFlow = deletionStep.performDeletionStep(A, B, divider.getAs(), divider.getAt(), dec, maxFlow);
			Set<E> A_new = deletionStep.getA();
			
			Double deletionPotential = potentialComputation.getPotential(newFlow , A_new);
			
			//Perform a matching step
			Set<MatchedPair<SplitVertex<V, E>>> matching = dec.getFractionalPartialMatching(gPrime, divider.getAs(), divider.getAt(), maxFlow, gPrime.getFlowSource(), gPrime.getFlowTarget());
			MatchingStep<V,E> matchingStep = new MatchingStep<V,E>(flowVectors , edgeNum, m);
			newFlow = matchingStep.performMatchingStepV2(gPrime, matching);
			
			Double matchingPotential = potentialComputation.getPotential(newFlow, A);
			
			LOGGER.fine("Deletion potential = " + deletionPotential + "; Matching potential =  " + matchingPotential);

			//Compare deletion- and matching-step and execute the one with less potential
			if (deletionPotential < matchingPotential) {
				LOGGER.fine("Applying deletion step.");
				flowVectors = deletionStep.getFlowVectors();
				A = deletionStep.getA();
				B = deletionStep.getB();
				LOGGER.finer("After deletion step: " + "|F| = " + ((double)A.size() + (double)B.size()));
				
				current_potential = deletionPotential;
				
			} else {
				
				LOGGER.fine("Applying matching step.");
				flowVectors = matchingStep.getFlowVectors();
				
				current_potential = matchingPotential;
			}
			
			//remove the source and the target vertice from the graph
			gPrime.removeSourceAndTarget();

		}
		
		if (A.size() == 1) {
			throw new StalemateException("|A| = 1");
		}
		
		LOGGER.info("Modified KRV procedure done.");
		
		return getResultCase();
	}
	
	/**
	 * Returns the case in which the Lemma stopped
	 * 
	 * @return 1 if Case 1 holds, 2 if Case 2 holds
	 */
	public Integer getResultCase() {
		Double log2 = Math.log(n)/Math.log(2);
		//Double log10 = Math.log10(n);
		
		if (B.size() <= 2 * A.size() / log2) {
			return 2; 
		}
		return 1;
	}
	
	/**
	 * Get the computed edges set A
	 * 
	 * @return The set of active edges A
	 */
	public Set<E> getA() {
		return A;
	}
	
	/**
	 * Get the computed set B 
	 * 
	 * @return The set of inactive edges B
	 */
	public Set<E> getB() {
		return B;
	}

	/**
	 * Returns the current set of edges F = A + B
	 * @return : The current set of edges F = A + B
	 */
	public Set<E> getF() {
		Set<E> F = new HashSet<E>(A);
		F.addAll(B);
		return F;
	}
}
