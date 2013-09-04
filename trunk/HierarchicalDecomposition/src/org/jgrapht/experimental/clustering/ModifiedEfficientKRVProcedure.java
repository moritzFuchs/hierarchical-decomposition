package org.jgrapht.experimental.clustering;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.logging.Logger;

import org.jgrapht.Graph;
import org.jgrapht.experimental.clustering.util.MatchedPair;
import org.jgrapht.experimental.util.LoggerFactory;
import org.jgrapht.graph.DefaultWeightedEdge;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;

import com.google.common.collect.BiMap;

public class ModifiedEfficientKRVProcedure<V extends Comparable<V>,E> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ModifiedEfficientKRVProcedure.class.getName());
	
	private Graph<V,E> g;
	
	private List<KRVStep<V,E>> partitionMatrices;

	private SplitGraph<V, E> gPrime;
	
	private Set<E> A;
	
	private Set<E> B;

	private BiMap<E, Integer> edgeNum;

	private Double bound;
	
	private DoubleMatrix1D projection;
	
	private Integer originalClusterSize;
	
	private KRVPotential<V,E> krvpot;
	
	
	public ModifiedEfficientKRVProcedure (Graph<V,E> g , SplitGraph<V,E> gPrime , Set<E> F , BiMap<E , Integer> edgeNum) {
		this.g = g;
		this.gPrime = gPrime;
		
		this.A = new HashSet<E>(F);
		this.B = new HashSet<E>();

		this.edgeNum = edgeNum;

		this.bound = 1/(16 * Math.pow(g.vertexSet().size(),2)); 

		this.partitionMatrices = new LinkedList<KRVStep<V,E>>(); 
		
		this.originalClusterSize = F.size();
		
		this.krvpot = new KRVPotential<V,E>(partitionMatrices,A,edgeNum,g.edgeSet().size());
	}
	
	public Integer performKRV() {
		
		LOGGER.info("Starting modified KRV procedure.");
		
		DenseDoubleMatrix1D r = Util.getRandomDirection(g.edgeSet().size());
		projection = FlowVectorProjector.getFlowVectorProjection(partitionMatrices , r);
		Double current_potential = krvpot.getPotential();
		
		while (current_potential >= bound && A.size() > 1) {
		
			LOGGER.fine("A = " + A);
			LOGGER.fine("B = " + B);	
			
			r = Util.getRandomDirection(g.edgeSet().size());
			projection = FlowVectorProjector.getFlowVectorProjection(partitionMatrices , r);
						
			PracticalVerticeDivider<V,E> divider = new PracticalVerticeDivider<V,E>(projection , edgeNum);
			divider.divideActiveVertices(gPrime, A, projection);
			
			/////////////////////////////// START DEBUG //////////////////////////////////////////////
			
			KRVPotential<V,E> k = new KRVPotential<V, E>(partitionMatrices, A, edgeNum, g.edgeSet().size());
			
			System.out.println("Potential-Difference: " + (krvpot.getPotential()-k.getPotential()));
			
			Double sum = 0.0;
			for (SplitVertex<V, E> e : divider.getAs()) {
				sum += projection.getQuick(edgeNum.get(gPrime.getOriginalEdge(e)));
			}
			System.out.println("Average projection length A_s: " + sum/divider.getAs().size());

			sum = 0.0;
			for (SplitVertex<V, E> e : divider.getAt()) {
				sum += projection.getQuick(edgeNum.get(gPrime.getOriginalEdge(e)));
			}
			System.out.println("Average projection length A_t: " + sum/divider.getAs().size());
			
			/////////////////////////////// END DEBUG /////////////////////////////////////////////////
			
			//Skip this iteration if Source or Target set is empty (in this case there is no flow and therefore no matching / deletion)
			if (divider.getAs().isEmpty() || divider.getAt().isEmpty()) {
				continue;
			}
			
			gPrime.addSourceAndTarget(divider.getAs(), divider.getAt());

			long time1 = System.currentTimeMillis();
			
			//Create the flow problem
			FlowProblem<SplitVertex<V,E> , DefaultWeightedEdge> flow_problem = 
				new UndirectedFlowProblem<SplitVertex<V,E>, DefaultWeightedEdge>(gPrime, gPrime.getFlowSource(), gPrime.getFlowTarget());
			
			//Compute maxFlow
			Map <DefaultWeightedEdge , Double> maxFlow = flow_problem.getFlow();
			
			long time2 = System.currentTimeMillis();
			System.out.println("Time Edmonds-Karp Flow : " + (time2-time1));
			
			//Rescale flow
			FlowRescaler<V,E> rescaler = new FlowRescaler<V, E>();
			Set<FlowPath<SplitVertex<V, E>, DefaultWeightedEdge>> paths = rescaler.rescaleFlow(gPrime, maxFlow, flow_problem);
			
			
			// -------------------------------- CHECK IF DELETION OR MATCHING STEP PERFORMS BETTER ---------------------------------- //
			
			DeletionStepNew<V,E> deletionStep = new DeletionStepNew<V,E>(g,gPrime,edgeNum,originalClusterSize);
			deletionStep.computeDeletionMatrix(A, B, divider.getAs(), divider.getAt(), flow_problem,paths);
			deletionStep.getDeletionMatrix();
			
			MatchingStepNew<V,E> matchingStep = new MatchingStepNew<V,E>(edgeNum , g.edgeSet().size());
			Set<MatchedPair<SplitVertex<V, E>>> matching = flow_problem.getFractionalPartialMatching(paths);
			matchingStep.computeMatchingMatrix(gPrime, A, matching);
			
			current_potential = applyBetterStep(deletionStep , matchingStep , r , current_potential);
			
			gPrime.removeSourceAndTarget();
			gPrime.resetWeights();
		}

		return getResultCase();
	}
	
	/**
	 * Checks results of both KRVSteps and applies the better one
	 * 
	 * @param deletionStep : The deletionStep after the computation of the deletionMatrix
	 * @param matchingStep : The matchingStep after the computation of the matchingMatrix
	 * @param r : The current random direction
	 * @return Double : The potential of the applied step
	 */
	//TODO: Remove current potential
	private Double applyBetterStep(DeletionStepNew<V,E> deletionStep,
			MatchingStepNew<V,E> matchingStep,
			DoubleMatrix1D r,
			Double current_potential) {
		
		List<KRVStep<V,E>> partitionMatricesMatching = new LinkedList<KRVStep<V,E>>(partitionMatrices);
		partitionMatricesMatching.add(matchingStep);
		
		List<KRVStep<V,E>> partitionMatricesDeletion = new LinkedList<KRVStep<V,E>>(partitionMatrices);
		partitionMatricesDeletion.add(deletionStep);
		
		Double matchingPotential = krvpot.getPotentialAfterStep(A , matchingStep);
		Double deletionPotential = krvpot.getPotentialAfterStep(deletionStep.getA() , deletionStep);
		
//		System.out.println("Current projection: " + projection);
		System.out.println("Current potential: " + current_potential);
		System.out.println("Potential for matching: " + matchingPotential);
		System.out.println("Potential for deletion: " + deletionPotential);
		System.out.println("Bound : " + bound);
		
		Double potential;
		if (matchingPotential <= deletionPotential) {
			projection = matchingStep.applyStep(r , projection);
			potential = matchingPotential;
			
			krvpot.permanentlyAddKRVStep(matchingStep);
			
			partitionMatrices.add(matchingStep);
		} else {
			A = deletionStep.getA();
			B = deletionStep.getB();
			
			projection = deletionStep.applyStep(r , projection);
			potential = deletionPotential;
			
			//If the deletion step was 'big' we need to restart the KRV procedure with the new F = A_new + B_new
			//This is similar to resetting the list of partition matrices and setting A = A + B
			if (deletionStep.restartNeccessary()) {
				partitionMatrices.clear();
				
				krvpot.restart();
				
				A.addAll(B);
				B.clear();
				
				originalClusterSize = A.size();
				
				System.out.println("Restarted KRV");
			} else {
				partitionMatrices.add(deletionStep);
				krvpot.permanentlyAddKRVStep(deletionStep);
				
			}
		}
		
		return potential;
	}

	
	
	/**
	 * Returns the case in which the Lemma stopped
	 * 
	 * @return Integer : 1 if Case 1 holds, 2 if Case 2 holds
	 */
	public Integer getResultCase() {
		//log2 = log_2(n)
		Double log2 = Math.log(g.vertexSet().size())/Math.log(2);
		//Double log10 = Math.log10(n);
		
		if (B.size() <= 2 * A.size() / log2) {
			return 2; 
		}
		return 1;
	}
	
	/**
	 * Returns the current set A of active edges
	 * 
	 * @return Set<E>: The current set of active edges
	 */
	public Set<E> getA() {
		return A;
	}
	
	/**
	 * Returns the current set of inactive edges
	 * 
	 * @return Set<E>: The current set of inactive edges
	 */
	public Set<E> getB() {
		return B;
	}
	
}
