package org.jgrapht.experimental.clustering;

/**
 * Parameters for the decomposition algorithm.
 * 
 * @author moritzfuchs
 * @date 12.09.2013
 *
 */
public class DecompositionConstants {

	//Prevent initialization
	private DecompositionConstants() {}
	
	/**
	 * In order to be a balanced clustering, the subset of the vertices defining the clustering must be smaller 
	 * than the size of the underlying graph times this constant. 
	 */
	public static final Double BALANCED_CLUSTERING_BOUND = 3.0 / 4.0;
	
	/**
	 * Used by the {@link FlowRescaler}. All flow paths that are bigger than the bound will be rescaled to have a weight of exactly 1.0
	 */
	public static final Double RESCALE_BOUND = 0.5;
	
	/**
	 * Used by {@link KRVPotential}. Number of iterations that are used to approximate the potential. (bigger => more space + time needed!)
	 */
	public static final Integer POTENTIAL_APPROXIMATION_ITERATIONS = 1000;
	
	/**
	 * User by {@link MatchingStepNew}. Fraction of the flow vector that is moved during a matching.
	 */
	public static final Double FLOW_MOVEMENT_FRACTION = 0.5;
	
	/**
	 * Used by {@link ModifiedEfficientKRVProcedure}. The procedure restarts when the set of edges F = A+B gets smaller than this constant times the original F. 
	 */
	public static final Double KRV_RESTART_BOUND = 7.0/8.0;
	
	/**
	 * Used by {@link PracticalVerticeDivider} and {@link TheoreticalVerticeDivider}. Maximal fraction of the edge set that can become source edges.
	 */
	public static final Double MAX_SOURCE_EDGES = 1.0/8.0;
	
	/**
	 * Fraction of the available CPU Cores that are used. #Cores * this constant = number of Threads in ThreadPool.
	 */
	public static final Integer MULTIPLE_OF_CORES = 2;
}
