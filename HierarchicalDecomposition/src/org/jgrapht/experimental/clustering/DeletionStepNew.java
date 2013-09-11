package org.jgrapht.experimental.clustering;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.experimental.clustering.old.DeletionStep;
import org.jgrapht.experimental.clustering.old.ModifiedKRVProcedure;
import org.jgrapht.graph.DefaultWeightedEdge;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;

import com.google.common.collect.BiMap;

/**
 * Performs a deletion step for the modified KRV procedure in {@link ModifiedKRVProcedure}  
 * 
 * @author moritzfuchs
 *
 * @param <V> : The type of vertices 
 * @param <E> : The type of edges
 */
public class DeletionStepNew<V extends Comparable<V>,E> implements KRVStep<V,E> {

	/**
	 * If F_new gets smaller than KRV_RESTART_BOUND * F_original we restart with A := A + B the KRV procudure
	 */
	private static final Double KRV_RESTART_BOUND = 7.0 / 8.0;

	/**
	 * The original graph G
	 */
	private Graph<V,E> g;
	
	/**
	 * The subdivision graph G' of G
	 */
	private SplitGraph<V,E> gPrime;
	
	/**
	 * The set A of active edges computed by the deletion step
	 */
	private Set<E> A_new;
	
	/**
	 * The set B of inactive edges computed by the deletion step
	 */
	private Set<E> B_new;
	
	/**
	 * Mapping from edges in G to Integers (unique number for each edge) 
	 */
	private BiMap<E,Integer> edgeNum;

	/**
	 * Contains the size of the original clustering - the number of edges in F at the start of the KRV procedure
	 */
	private Integer originalClusteringSize;

	/**
	 * Indicator whether the KRV procedure will have to be restarted or not
	 */
	private Boolean restart_neccessary;

	/**
	 * The deletion matrix of the following form: x-axis: old flow vectors; y-axis: new flow vectors => new flow vector = sum of old flow vectors as described in the matrix (row sum = 1 for all active edges)
	 */
	private DeletionMatrix matrixContainer;
	
	
	public DeletionStepNew(Graph<V,E> g,
			SplitGraph<V,E> gPrime,
			BiMap<E , Integer> edgeNum,
			Integer originalClusteringSize
			) {
		this.g = g;
		this.gPrime = gPrime;
		this.edgeNum = edgeNum;
		this.originalClusteringSize = originalClusteringSize;
		this.restart_neccessary = false;
		
	}
	
	/**
	 * Get the deletion matrix for the deletion step
	 * 
	 * @return : The DeletionMatrix for this deletion step (containing the flow vector movement)
	 */
	public DeletionMatrix computeDeletionMatrix(Set<E> A, 
			Set<E> B , 
			Set<SplitVertex<V,E>> A_s , 
			Set<SplitVertex<V,E>> A_t , 
			FlowProblem<SplitVertex<V,E>, DefaultWeightedEdge> flow_problem , 
			Set<FlowPath<SplitVertex<V,E>,DefaultWeightedEdge>> paths) {
		
		SparseDoubleMatrix2D deletionMatrix = new SparseDoubleMatrix2D(g.edgeSet().size(),g.edgeSet().size());
		
		//Before doing anything every edge in A should keep its current projected flow vector (=> The submatrix with only A is the identity matrix)
		for (E e : A) {
			Integer index = edgeNum.get(e);
			deletionMatrix.setQuick(index, index, 1.0);
		}
		
		A_new = new HashSet<E>();
		A_new.addAll(A);
		B_new = new HashSet<E>(B);
		
		//Get cut induced by flow
		Set<DefaultWeightedEdge> cut = flow_problem.getCut();
		
		Set<E> C = gPrime.translateCut(cut);

		//Compute ((A + B) - A_t) + C
		Set<E> newClustering = new HashSet<E>(A_new);
		newClustering.addAll(B);
		newClustering.removeAll(gPrime.getOriginalEdges(A_t));
		newClustering.addAll(C);	
		
		if (Connectivity.isBalancedClustering(g, newClustering)) {
			//Iteration done. Lemma returns with Case 1 and we need to restart the KRV procedure.
			
			/*
			 * If ((A + B) - A_t) + C induces a balanced clustering we want to 'restart' the KRV procedure. 
			 * Therefore we set A = F = ((A + B) - A_t) + C and reset the flow fectors to '1' for each e \in A = F
			 */
			A_new.addAll(B);
			A_new.removeAll(gPrime.getOriginalEdges(A_t));
			A_new.addAll(C);
			
			B_new.clear();

			/////////////////////////// RESTART //////////////////////////////////////
			restart_neccessary = true;
			
		} else { // In this case ((A + B) - A_s) + C induces a balanced clustering and we must move the flow vectors of A_s to the cut C
		
			matrixContainer = new DeletionMatrix(deletionMatrix);
			
			A_new.removeAll(gPrime.getOriginalEdges(A_s));
	
			//Compute the assignment of fractional flow vectors to cut edges
			Set<E> new_edges = computeFlowVectorMovement(paths, cut);
			
			//All flow vectors of source edges on flow paths were now moved and summed up. Now let's see which edges have flow vectors >= 1.
			assignEdges(new_edges);
			
			//Delete all old flow vectors (those for edges in A_s \ A_new)
			deleteOldFlowVectors(A_s);

			/* 
			 * If F = A + B gets too small we restart the KRV procedure. 
			 * This is similar to putting A = F and resetting the flow vectors for A to have a unit of a unique commodity for each edge in A
			 */ 
			if (A_new.size() + B_new.size() < KRV_RESTART_BOUND * originalClusteringSize) {
				A_new.addAll(B_new);
				B_new.clear();
				
				/////////////////////////// RESTART //////////////////////////////////////
				restart_neccessary = true;
			}
		}
		
		return matrixContainer;
	}
	
	/**
	 * Returns the computed deletion matrix (or null if {@link DeletionStepNew.computeDeletionMatrix} was not called before)
	 * 
	 * @return : The computed deletion matrix or null if {@link DeletionStepNew.computeDeletionMatrix} was not called before
	 */
	public DeletionMatrix getDeletionMatrix() {
		return matrixContainer;
	}
	
	/**
	 * Indicator whether the KRV procedure has to be restarted or not.
	 * 
	 * @return : True if the KRV procedure has to be restarted, false if not.
	 */
	public Boolean restartNeccessary() {
		return restart_neccessary;
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
				
				//The edge does not get a flow vector and is therefore set of 0
				DoubleMatrix1D row = matrixContainer.getMatrix().viewRow(edgeNum.get(originalEdge));
				row.assign(0.0);
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
	private void assignEdges(Set<E> new_edges) {
		for (E e : new_edges) {
			
			DoubleMatrix1D row = matrixContainer.getMatrix().viewRow(edgeNum.get(e));
			
//			DenseDoubleAlgebra algebra = new DenseDoubleAlgebra();
//			Double len = algebra.norm2(row);
			
			Double len = row.zSum();
			
			if (len >= 1.0) {
				//The flow vector assignment was big enough. Therefore the edge will get an flow vector.
				A_new.add(e);
				B_new.remove(e);

				if (len > 1.0) {
					//We need to make sure that the row sums up to 1, therefore we normalize the row.
					row.normalize();
				} 
				
			} else {
				//The flow vector assignment was not big enough. Therefore the edge will not get any flow vector
				B_new.add(e);
				row.assign(0.0);
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
	 * @param cut : The cut edges induced by the flow
	 * @return An assignment of sums of fractional flow vectors to cut edges in G
	 */
	private Set<E> computeFlowVectorMovement(
			Set<FlowPath<SplitVertex<V,E>, DefaultWeightedEdge>> paths,
			Set<DefaultWeightedEdge> cut) {
		
		Set<E> new_edges = new HashSet<E>();
		
		for (FlowPath<SplitVertex<V, E>, DefaultWeightedEdge> path : paths) {
			Double weight = path.getFlowPathWeight(); 
			if (weight <= 1) {
				
				//Move flow vector to first cut edge on path

				DefaultWeightedEdge cutEdge = findCutEdge(cut, path.getPath());
				E originalCutEdge = gPrime.getOriginalEdge(cutEdge);
				
				E to = gPrime.getOriginalEdge(cutEdge);
				E from = gPrime.getOriginalEdge(path.getPath().get(1));
				
				//if the original edge is in A, then it already has a flow vector of length 1. Hence we ignore this fraction of the flow vector
				if (!A_new.contains(originalCutEdge)) {
					//put movement of dec.getFlowPathWeight(path) from edge "from" to edge "to"
					Double current_weight = matrixContainer.getMatrix().getQuick(edgeNum.get(from) , edgeNum.get(to));
					matrixContainer.getMatrix().setQuick(edgeNum.get(from), edgeNum.get(to), current_weight + weight);
					new_edges.add(originalCutEdge);
				}
			}
		}
		return new_edges;
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

	@Override
	public DoubleMatrix1D applyStep(DoubleMatrix1D r,
			DoubleMatrix1D current_projection) {
		DoubleMatrix1D new_projection;
		if (!restart_neccessary) {
			new_projection = current_projection.copy();
			new_projection = matrixContainer.getMatrix().zMult(current_projection, new_projection);
		} else {
			new_projection = new DenseDoubleMatrix1D((int) current_projection.size());
			for (E e : A_new) {
				new_projection.setQuick(edgeNum.get(e), r.getQuick(edgeNum.get(e)));
			}
		}
		
		return new_projection;
	}

}
