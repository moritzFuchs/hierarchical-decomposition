package org.jgrapht.experimental.clustering.krv;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.experimental.clustering.Connectivity;
import org.jgrapht.experimental.clustering.DecompositionConstants;
import org.jgrapht.experimental.clustering.FlowPath;
import org.jgrapht.experimental.clustering.FlowProblem;
import org.jgrapht.experimental.clustering.SplitGraph;
import org.jgrapht.experimental.clustering.SplitVertex;
import org.jgrapht.experimental.clustering.old.DeletionStep;
import org.jgrapht.experimental.clustering.old.ModifiedKRVProcedure;
import org.jgrapht.graph.DefaultWeightedEdge;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
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
	 * The deletion matrix of the following form: x-axis: old flow vectors; y-axis: new flow vectors => new flow vector = sum of old flow vectors as described in the matrix (row sum = 1 for all active edges)
	 */
	private DeletionMatrix matrixContainer;
	
	/**
	 * Indicates whether a restart is neccessary or not
	 */
	private Boolean restart_needed = false;
	
	private Set<E> A_old;
	private Set<E> B_old;
	
	
	public DeletionStepNew(Graph<V,E> g,
			SplitGraph<V,E> gPrime,
			BiMap<E , Integer> edgeNum
			) {
		this.g = g;
		this.gPrime = gPrime;
		this.edgeNum = edgeNum;
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
			Set<FlowPath<SplitVertex<V,E>>> paths) {
		
		A_old = A;
		B_old = B;
		
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
		Set<DefaultWeightedEdge> cut = flow_problem.getMinCut();
		
		Set<E> C = gPrime.translateCut(cut);

		//Compute ((A + B) - A_t) + C
		Set<E> newClustering = new HashSet<E>(A_new);
		newClustering.addAll(B);
		newClustering.removeAll(gPrime.getOriginalEdges(A_t));
		newClustering.addAll(C);	
		
		matrixContainer = new DeletionMatrix(deletionMatrix);
		
		if (Connectivity.isBalancedClustering(g, newClustering)) {
			
			/*
			 * ((A + B) - A_t) + C induces a balanced clustering.
			 * This means that F will probably be significantly smaller than before.
			 */
			A_new.addAll(B);
			A_new.removeAll(gPrime.getOriginalEdges(A_t));
			A_new.addAll(C);
			
			B_new.clear();

			deleteOldFlowVectors(A_t);
			
			restart_needed = true;
			
		} else { // In this case ((A + B) - A_s) + C induces a balanced clustering and we must move the flow vectors of A_s to the cut C
			A_new.removeAll(gPrime.getOriginalEdges(A_s));
	
			//Compute the assignment of fractional flow vectors to cut edges
			Set<E> new_edges = computeFlowVectorMovement(paths, cut);
			
			//All flow vectors of source edges on flow paths were now moved and summed up. Now let's see which edges have flow vectors >= 1.
			assignEdges(new_edges);
			
			//Delete all old flow vectors (those for edges in A_s \ A_new)
			deleteOldFlowVectors(A_s);
		}
		
		if (A_new.size() + B_new.size() <= DecompositionConstants.KRV_RESTART_BOUND * (A_old.size() + B_old.size())) {
			restart_needed = true;
		}
		
		if (DecompositionConstants.DEBUG) {
			if (!B.isEmpty()) {
				System.out.println("B is not empty!");
			}
			
			if (!restart_needed) {
				DoubleMatrix2D matrix = matrixContainer.getMatrix();
				for (int i=0;i<matrix.rows();i++) {
					
					DoubleMatrix1D row = matrix.viewRow(i);
					Double sum = 0.0;
					for (int j=0;j<row.size();j++) {
						sum += row.getQuick(j) * g.getEdgeWeight(edgeNum.inverse().get(j));
					}
					if (sum != 0.0 && (sum < g.getEdgeWeight(edgeNum.inverse().get(i)) - DecompositionConstants.EPSILON || sum > g.getEdgeWeight(edgeNum.inverse().get(i)) + DecompositionConstants.EPSILON)) {
						System.out.println("Flow vector too large or too small!");
					}
				}
				
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
	 * Assigns edges to A or B depending on the amount of flow they received by {@link DeletionStep#computeFlowVectorMovement(FlowDecomposer, Map, Set)}.
	 * If an edge has flow vectors of total length >= capacity assigned to it, it will become an active edge. Additionally the received flow vector will be normalized to have a length of capacity of the edge.
	 * If the total length of the receiver's flow vectors is < capacity of the edge, it will be marked as inactive. (should not happen if an exact max-flow-algorithm is used.) 
	 * The assigned fractional flow vectors will be neglected.
	 * 
	 * @param sum : The assignment of sums of fractional flow vectors to cut edges
	 */
	private void assignEdges(Set<E> new_edges) {
		for (E e : new_edges) {
			
			DoubleMatrix1D row = matrixContainer.getMatrix().viewRow(edgeNum.get(e));
			
//			DenseDoubleAlgebra algebra = new DenseDoubleAlgebra();
//			Double len = algebra.norm2(row);
			
			Double len = 0.0;
			for (int i=0;i<row.size();i++) {
				len += row.getQuick(i) * g.getEdgeWeight(edgeNum.inverse().get(i));
			}
			
			// + EPSILON to deal with Double precision errors
			if (len + DecompositionConstants.EPSILON >= g.getEdgeWeight(e)) {
				//The flow vector assignment was big enough. Therefore the edge will get an flow vector.
				A_new.add(e);
				B_new.remove(e);

				if (len > g.getEdgeWeight(e)) {
					//We need to make sure that the row sums up to 1, therefore we normalize the row.

					Double factor = g.getEdgeWeight(e) / len;
					
					for (int i=0;i<row.size();i++) {
						row.setQuick(i, row.getQuick(i) * factor);
					}
					
//					System.out.println("### " + g.getEdgeWeight(e));
//					for (int i=0;i<row.size();i++) {
//						if (row.getQuick(i) != 0.0) {
//							System.out.println("" + row.getQuick(i) + " * " + g.getEdgeWeight(edgeNum.inverse().get(i)));
//						}
//					}
					
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
			Set<FlowPath<SplitVertex<V,E>>> paths,
			Set<DefaultWeightedEdge> cut) {
		
		Set<E> new_edges = new HashSet<E>();
		
		for (FlowPath<SplitVertex<V, E>> path : paths) {
			Double weight = path.getFlowPathWeight(); 
			
			Double cap = g.getEdgeWeight(gPrime.getOriginalEdge(path.getPath().get(1)));
			
			//FIXME: Is + EPSILON neccessary?
			if (weight <= cap + DecompositionConstants.EPSILON) {
				
				//Move flow vector to first cut edge on path

				DefaultWeightedEdge cutEdge = findCutEdge(cut, path.getPath());
				
				E to = gPrime.getOriginalEdge(cutEdge);
				E from = gPrime.getOriginalEdge(path.getPath().get(1));
				
				//if the original edge is in A, then it already has a flow vector of length 1. Hence we ignore this fraction of the flow vector
				if (!A_new.contains(to)) {
					//move fraction of flow vector of "from" to "to"
					Double current_weight = matrixContainer.getMatrix().getQuick(edgeNum.get(to) , edgeNum.get(from));
					matrixContainer.getMatrix().setQuick(edgeNum.get(to), edgeNum.get(from), current_weight + weight / cap);
					
					new_edges.add(to);
				}
			}
		}
		if (DecompositionConstants.DEBUG) {
			DoubleMatrix2D matrix = matrixContainer.getMatrix();
			for (int i=0;i<matrix.rows();i++) {
				
				DoubleMatrix1D row = matrix.viewRow(i);
				Double sum = 0.0;
				for (int j=0;j<row.size();j++) {
					sum += row.getQuick(j) * g.getEdgeWeight(edgeNum.inverse().get(j));
				}
				if (sum != 0.0 && sum < g.getEdgeWeight(edgeNum.inverse().get(i)) - DecompositionConstants.EPSILON) {
					System.out.println("Flow vector too large!");
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

	/**
	 * Apply the deletion step.
	 * 
	 * @param r : The random direction that this projection relies on.
	 * @param current_projection : The current projection of the flow vector (before this deletion step)
	 * 
	 * @return {@link DoubleMatrix1D} : The flow vector projection after the application of the deletion step
	 */
	@Override
	public DoubleMatrix1D applyStep(DoubleMatrix1D r,
			DoubleMatrix1D current_projection) {

		DoubleMatrix1D new_projection = current_projection.copy();
		new_projection = matrixContainer.getMatrix().zMult(current_projection, new_projection);
		
		return new_projection;
	}
	
	/**
	 * True if no progress was made; False is progress was made.
	 * 
	 * @return Boolean: True if no progress was made, False otherwise.
	 */
	public Boolean noProgress() {
		return A_old.containsAll(A_new) && A_new.containsAll(A_old) && B_old.containsAll(B_new) && B_new.containsAll(B_old);
	}
	
	/**
	 * True if a restart is neccessary, False otherwise
	 * @return
	 */
	public Boolean restartNeeded() {
		return restart_needed;
	}

}
