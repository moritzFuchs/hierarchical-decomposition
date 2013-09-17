package org.jgrapht.experimental.clustering;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;

/**
 * Keeps an approximate potential for the KRV procedure.  
 * 
 * @author moritzfuchs
 *
 * @param <V> : The type of vertices
 * @param <E> : The type of edges
 */
public class KRVPotential<V,E> {
	
	private Map<E, Integer> edgeNum;
	private List<KRVStep<V,E>> krvsteps;
	
	/**
	 * The set of active edges. CAUTION: Will be changed by the KRVProcedure.
	 */
	private Set<E> A;
	
	/**
	 * Number of edges
	 */
	private Integer m;
	
	/**
	 * flow vector projections
	 */
	private DoubleMatrix1D[] projections;
	
	/**
	 * Random unit directions used for the flow vector projections in {@link KRVPotential.projections}.
	 */
	private DoubleMatrix1D[] randomDirections;
	
	/**
	 * Projector for the current KRV procedure (projects flow vectors onto a given vector)
	 */
	private FlowVectorProjector<V,E> projector;
	
	/**
	 * Graph we want to decompose
	 */
	private Graph<V,E> g;
	
	/**
	 * The goal vector which the flow vectors want to reach
	 */
	private DoubleMatrix1D goal;
	
	/**
	 * Projections of the goal vector onto the random directions
	 */
	private DoubleMatrix1D goal_projections;
	
	
	public KRVPotential(Graph<V,E> g, List<KRVStep<V,E>> matrices ,FlowVectorProjector<V,E> projector , Set<E> A,Map<E , Integer> edgeNum, Integer m) {
		
		this.g = g;
		this.krvsteps = matrices;
		this.projector = projector;
		this.A = A;
		this.edgeNum = edgeNum;
		this.m = m;
		
		//Compute the 'goal'
		goal = new DenseDoubleMatrix1D(m);
		Double cap_sum = 0.0;
		
		for (E e : g.edgeSet()) {
			cap_sum += g.getEdgeWeight(e);
		}
		
		for (E e : g.edgeSet()) {
			goal.setQuick(edgeNum.get(e), g.getEdgeWeight(e) / cap_sum);
		}
		
		projections = new DoubleMatrix1D[DecompositionConstants.POTENTIAL_APPROXIMATION_ITERATIONS];
		randomDirections = new DoubleMatrix1D[DecompositionConstants.POTENTIAL_APPROXIMATION_ITERATIONS];
		
		precomputeProjections();
		
		goal_projections = new DenseDoubleMatrix1D(m); 
		
		goal_projections.assign(0.0);
		for (int i=0;i<DecompositionConstants.POTENTIAL_APPROXIMATION_ITERATIONS;i++) {
			Double new_weight = 0.0;
			for (E e : A) {
				new_weight += goal.getQuick(i) * randomDirections[i].get(edgeNum.get(e));
			}
			goal_projections.setQuick(i, new_weight);
		}
	}

	/**
	 * KRVProcedure has restarted. Reset the projections
	 */
	public void restart() {
		for (int i=0;i<DecompositionConstants.POTENTIAL_APPROXIMATION_ITERATIONS;i++) {
			//Project onto capacity matrix (1-Matrix in unweighted case)
			projections[i] = projector.getFlowVectorProjection(new LinkedList<KRVStep<V,E>>(), randomDirections[i]);
		}
	}
	
	/**
	 * Precomputes projections of the flow vectors onto random directions
	 */
	private void precomputeProjections() {
		for (int i=0;i<DecompositionConstants.POTENTIAL_APPROXIMATION_ITERATIONS;i++) {
			DoubleMatrix1D r = Util.getRandomDirection(m);
			DoubleMatrix1D projection = projector.getFlowVectorProjection(krvsteps, r);
			
			projections[i] = projection;
			randomDirections[i] = r;
		}
		
		
	}
	
	/**
	 * Permanently applies a {@link KRVStep} to the potential
	 * 
	 * @param step : The KRVStep which will be applied
	 */
	public void permanentlyAddKRVStep(KRVStep<V,E> step) {
		for (int i=0;i<DecompositionConstants.POTENTIAL_APPROXIMATION_ITERATIONS;i++) {
			projections[i] = step.applyStep(randomDirections[i], projections[i]);
		}
		A = step.getA();
	}
	
	/**
	 * Approximate the potential p = SUM ( 2-Norm(f_e - average_flow_vector)) by using the 'Gaussian Behavior of Projections'-Lemma. ==> Paper page 8
	 * 
	 * @return : The approximate potential of the flow vectors
	 */
	public Double getPotential() {

		Double total = 0.0;
		
		for (int i=0;i<DecompositionConstants.POTENTIAL_APPROXIMATION_ITERATIONS;i++) {
			
//			Double average = computeAverageProjection(projections[i], A);

			Double sum = 0.0;
			for(E e : A) {
				Double weight = g.getEdgeWeight(e);
				Double u_e = projections[i].getQuick(edgeNum.get(e));
				sum += weight * Math.pow(u_e / weight - goal_projections.getQuick(i) , 2);
			}
			
			//Could also be m instead of A.size()
			total += A.size() * sum;
		}
		
		return total / DecompositionConstants.POTENTIAL_APPROXIMATION_ITERATIONS;
	}
	
	/**
	 * Applies one more KRVStep and returns the potential after the application. Use this to test KRVSteps.
	 * 
	 * @param step : The {@link KRVStep} that is about to be tested 
	 * @return : The potential after the application of the given {@link KRVStep} .
	 */
	public Double getPotentialAfterStep(Set<E> A_new , KRVStep<V,E> step) {
		
		Double total = 0.0;
		
		for (int i=0;i<DecompositionConstants.POTENTIAL_APPROXIMATION_ITERATIONS;i++) {
			
			DoubleMatrix1D after = step.applyStep(randomDirections[i], projections[i]);
			
			Double average = computeAverageProjection(after, A_new);
			Double sum = 0.0;
			for(E e : A_new) {
				sum += Math.pow(after.getQuick(edgeNum.get(e)) - average , 2);
			}
			total += A_new.size() * sum;
		}
		
		return total / DecompositionConstants.POTENTIAL_APPROXIMATION_ITERATIONS;
	}
	
	/**
	 * Get the average flow vector projection
	 * 
	 * @param projection : The current projection of flow vectors onto a random direction
	 * @param A : The set of active edges (only they have flow vectors)
	 * @return : The average flow vector projection
	 */
	public Double computeAverageProjection(DoubleMatrix1D projection , Set<E> A) {
		Double avg = 0.0;
		for (E e : A) {
			avg += projection.getQuick(edgeNum.get(e));
		}
		return avg / A.size();
	}
}