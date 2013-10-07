package org.jgrapht.experimental.clustering;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.logging.Logger;

import org.jgrapht.Graph;
import org.jgrapht.experimental.clustering.stats.KRVStats;
import org.jgrapht.experimental.clustering.util.MatchedPair;
import org.jgrapht.experimental.util.LoggerFactory;
import org.jgrapht.graph.DefaultWeightedEdge;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;

import com.google.common.collect.BiMap;

public class ModifiedEfficientKRVProcedure<V extends Comparable<V>,E> {

	/**
	 * Logger object for this class
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ModifiedEfficientKRVProcedure.class.getName());
	
	/**
	 * The graph G we want to decompose
	 */
	private Graph<V,E> g;
	
	/**
	 * The subdivision graph of G
	 */
	private SplitGraph<V, E> gPrime;
	
	/**
	 * All KRVSteps that have been taken so far. Contains all information about flow vectors that we have.
	 */
	private List<KRVStep<V,E>> partitionMatrices;

	/**
	 * The set of active edges (=F on startup)
	 */
	private Set<E> A;
	
	/**
	 * The set of inactive edges (= empty set on startup)
	 */
	private Set<E> B;

	/**
	 * Edge ID Map (unique ID for each edge)
	 */
	private BiMap<E, Integer> edgeNum;

	/**
	 * Potential bound we want to reach.
	 */
	private Double bound;
	
	/**
	 * Current projection of the flow vectors onto a random unit vector r
	 */
	private DoubleMatrix1D projection;
	
	/**
	 * The size of the initial F.
	 */
	private Integer originalClusterSize;
	
	/**
	 * Potential computation object
	 */
	private KRVPotential<V,E> krvpot;
	
	/**
	 * Projector for this KRV procedure (projects flow vectors onto a random vector)
	 */
	private FlowVectorProjector<V,E> projector;
	
	/************************************* STATISTICS ****************************************/
	
	/**
	 * ID of this run in the database.
	 */
	private Integer db_id;
	
	/**
	 * Object to write statistics to the database.
	 */
	private KRVStats stats;
	
	/**
	 * Number of iterations.
	 */
	private Integer iterations = 0;
	
	/**
	 * Number of iterations since last deletion step
	 */
	private Integer noDeletionStep = 0;
	
	/**
	 * Time spent in maxFlow computations.
	 */
	private Long timeInMaxFlow = 0L;
	
	/**
	 * Total time spent in the KRV procedure.
	 */
	private Long time = 0L; 
	
	public ModifiedEfficientKRVProcedure (Graph<V,E> g , SplitGraph<V,E> gPrime , Set<E> F , BiMap<E , Integer> edgeNum) {
		this.g = g;
		this.gPrime = gPrime;
		this.edgeNum = edgeNum;
		
		this.partitionMatrices = new LinkedList<KRVStep<V,E>>();
		
		this.originalClusterSize = F.size();
		
		this.A = new HashSet<E>(F);
		this.B = new HashSet<E>();

		//This is the theoretical bound
		this.bound = 1/(16 * Math.pow(g.vertexSet().size(),2));
		
		//Let's make it practical! (cut is very unlikely to change at such low potentials)
		this.bound = this.bound * 10;
	
		this.projector = new FlowVectorProjector<V,E>(g, edgeNum);
		
		this.krvpot = new KRVPotential<V,E>(g,partitionMatrices,projector,A,edgeNum,g.edgeSet().size());		
		
		if (DecompositionConstants.STATS) {
			this.stats = KRVStats.getInstance();
			this.db_id = stats.registerKRVRun(g.vertexSet().size(), g.edgeSet().size());
		}
	}
	
	/**
	 * Performs the modified KRV iteration. 
	 * 
	 * @return Integer : The case number which the iteration ended up in. (you can mostly ignore this since the iteration ends in Case 2 since KRV restarts in Case 1)
	 */
	public Integer performKRV() {
		
		Long startTime = System.currentTimeMillis();
		
		LOGGER.info("Starting modified KRV procedure.");
		
		DenseDoubleMatrix1D r = Util.getRandomDirection(g.edgeSet().size());
		Double current_potential = krvpot.getPotential();
		
		while (current_potential >= bound && A.size() > 1) {
			
			Long startTimeIteration = System.currentTimeMillis();
			iterations++;
			
			r = Util.getRandomDirection(g.edgeSet().size());
			projection = projector.getFlowVectorProjection(partitionMatrices , r);
						
			PracticalVerticeDivider<V,E> divider = new PracticalVerticeDivider<V,E>(g, projection , edgeNum);
			divider.divideActiveVertices(gPrime, A, projection);
			
			if (DecompositionConstants.DEBUG) {
				//Provide some debugging information
				debugInformation(divider);
			}
			
			//Skip this iteration if Source or Target set is empty (in this case there is no flow and therefore no matching / deletion)
			if (divider.getAs().isEmpty() || divider.getAt().isEmpty()) {
				continue;
			}
			
			gPrime.addSourceAndTarget(divider.getAs(), divider.getAt());

			Long startTimeMaxFlow = System.currentTimeMillis();
			
			//Create the flow problem
			FlowProblem<SplitVertex<V,E> , DefaultWeightedEdge> flow_problem = 
				new UndirectedFlowProblem<SplitVertex<V,E>, DefaultWeightedEdge>(gPrime, gPrime.getFlowSource(), gPrime.getFlowTarget());
			
			//Compute maxFlow
			Map <DefaultWeightedEdge , Double> maxFlow = flow_problem.getMaxFlow();
			
			timeInMaxFlow = System.currentTimeMillis() - startTimeMaxFlow;
			
			//Rescale flow
			FlowRescaler<V,E> rescaler = new FlowRescaler<V, E>();
			Set<FlowPath<SplitVertex<V, E>>> paths = rescaler.rescaleFlow(gPrime, maxFlow, flow_problem);
			
			if (DecompositionConstants.DEBUG) {
				for (FlowPath<SplitVertex<V, E>> path : flow_problem.getPaths()) {
					DefaultWeightedEdge e  = findCutEdge(flow_problem.getMinCut(), path.getPath());
					if (gPrime.getEdgeWeight(e) > Math.abs(flow_problem.getMaxFlow().get(e))) {
						System.out.println(" " + gPrime.getEdgeWeight(e) + " " + flow_problem.getMaxFlow().get(e));
					} 
				}
			}
			
			
			
			// -------------------------------- CHECK IF DELETION OR MATCHING STEP PERFORMS BETTER ---------------------------------- //
			
			DeletionStepNew<V,E> deletionStep = new DeletionStepNew<V,E>(g,gPrime,edgeNum);
			deletionStep.computeDeletionMatrix(A, B, divider.getAs(), divider.getAt(), flow_problem,paths);
			deletionStep.getDeletionMatrix();
			
			MatchingStepNew<V,E> matchingStep = new MatchingStepNew<V,E>(edgeNum , g.edgeSet().size());
			Set<MatchedPair<SplitVertex<V, E>>> matching = flow_problem.getFractionalPartialMatching(paths);
			matchingStep.computeMatchingMatrix(gPrime, A, matching);
			
			current_potential = applyBetterStep(deletionStep , matchingStep , r , current_potential);
			
			gPrime.removeSourceAndTarget();
			gPrime.resetWeights();
			
			if (A.size() + B.size() < DecompositionConstants.KRV_RESTART_BOUND * originalClusterSize) {
				System.out.println("Restarting KRV");
				current_potential = restart();
			}
			
			Long timeIteration = System.currentTimeMillis() - startTimeIteration;
			
			stats.addIteration(db_id, A.size()+B.size(), timeIteration,timeInMaxFlow, iterations, current_potential);
		}

		time = System.currentTimeMillis() - startTime;
		
		//Update stitistics on this KRV run
		if (DecompositionConstants.STATS) {
			stats.updateKRVRun(db_id, time);
		}
		
		return getResultCase();
	}

	/**
	 * Prints some random debug information (changes every now and then..)
	 * 
	 * @param divider
	 */
	private void debugInformation(PracticalVerticeDivider<V, E> divider) {
//		KRVPotential<V,E> k = new KRVPotential<V, E>(g,partitionMatrices, projector,A, edgeNum, g.edgeSet().size());
//		
//		System.out.println("Potential-Difference: " + (krvpot.getPotential()-k.getPotential()));

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
	}
	
	/**
	 * Checks results of both KRVSteps and applies the better one
	 * 
	 * @param deletionStep : The deletionStep after the computation of the deletionMatrix
	 * @param matchingStep : The matchingStep after the computation of the matchingMatrix
	 * @param r : The current random direction
	 * @return Double : The potential of the applied step
	 */
	private Double applyBetterStep(DeletionStepNew<V,E> deletionStep,
			MatchingStepNew<V,E> matchingStep,
			DoubleMatrix1D r,
			Double current_potential) {
		
		Double matchingPotential = krvpot.getPotentialAfterStep(A , matchingStep);
		Double deletionPotential = krvpot.getPotentialAfterStep(deletionStep.getA() , deletionStep);
		
		/****************** INFO ***********/
		Double sum = 0.0;
		for (E e : A) {
			sum += g.getEdgeWeight(e);
		}
		
		LOGGER.fine("Current summed up edge weight: " + sum);
		LOGGER.fine("Mean of edge weights: " + sum / A.size());
		LOGGER.info("Current potential: " + current_potential);
		LOGGER.info("Potential for matching: " + matchingPotential);
		LOGGER.info("Potential for deletion: " + deletionPotential + " No Progress?: " + deletionStep.noProgress() + " Restart:" + deletionStep.restartNeeded());
		LOGGER.info("Bound : " + bound);
		/************************************/
		
		
		Double potential;
		if (deletionStep.noProgress() || (!deletionStep.restartNeeded() && matchingPotential <= deletionPotential)) {
			projection = matchingStep.applyStep(r , projection);
			potential = matchingPotential;
			
			krvpot.permanentlyAddKRVStep(matchingStep);
			
			partitionMatrices.add(matchingStep);
			
			noDeletionStep = 0;
		} else {
			A = deletionStep.getA();
			B = deletionStep.getB();
			
			projection = deletionStep.applyStep(r , projection);
			potential = deletionPotential;
			
			partitionMatrices.add(deletionStep);
			krvpot.permanentlyAddKRVStep(deletionStep);
			
			noDeletionStep++;
		}
		
		return potential;
	}

	/**
	 * Restarts the KRV procedure and returns new current potential
	 * 
	 * @return Double: The new current potential 
	 */
	private Double restart() {
		LOGGER.fine("Restarting KRV.");
		
		partitionMatrices.clear();
		
		krvpot.restart();
		
		//F = A+B, therefore A = A+B after a restart.
		A.addAll(B);
		B.clear();
			
		originalClusterSize = A.size();
		
		return krvpot.getPotential();
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
	
	/**
	 * Returns the number of iterations that were needed for this modified KRV procedure 
	 * 
	 * @return Integer : The number of iterations that were needed.
	 */
	public Integer getIterations() {
		return iterations;
	}
	
	/**
	 * Returns time spent on max flow computations [ms]
	 * 
	 * @return Long: Time spent on max flow computations in ms 
	 */
	public Long getTimeInMaxFlow() {
		return timeInMaxFlow;
	}
	
	/**
	 * Returns time in ms that the algorithm needed. Returns 0 while the procedure is still running.
	 * 
	 * @return Long : Time needed for this modified KRV procedure [ms] 
	 */
	public Long getCompleteTime() {
		return time;
	}
	
	/**
	 * Finds the first cut edge on a given path
	 * 
	 * @param cut : The cut
	 * @param path : The path
	 * @return DefaultWeightedEdge : The first cut edge on the given path. 
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
	
	
}
