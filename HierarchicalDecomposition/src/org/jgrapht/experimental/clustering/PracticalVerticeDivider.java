package org.jgrapht.experimental.clustering;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;

import cern.colt.matrix.tdouble.DoubleMatrix1D;

import com.google.common.collect.Ordering;

/**
 * Takes a set A and divides it into 2 sets A_s and A_t such that the flow vector projections of edges in A_s are 'far away' from the median and |A_t| >= |A|/2 and |A_s| <= |A|/8 
 * 
 * @author moritzfuchs
 *
 * @param <V> : The type of vertices
 * @param <E> : The type of edges
 */
public class PracticalVerticeDivider<V extends Comparable<V> , E> {

		/**
		 * Unique Integer for each edge e in original Graph g
		 */
		private Map<E , Integer> edgeNum;
		
		/**
		 * The source edges (after division)
		 */
		private Set<SplitVertex<V,E>> A_s;
		
		/**
		 * The target edges (after division)
		 */
		private Set<SplitVertex<V,E>> A_t;

		/**
		 * Projection of the flow vectors onto a random vecotor
		 */
		private DoubleMatrix1D projection;

		private VectorPotential<V, E> pot;
		
		/**
		 * The original graph G
		 */
		private Graph<V,E> g;

		//------------------------------ INNER CLASS -----------------------------/
		
		//Edge container to make edges comparable (needed for selection of smallest / greatest edges later on)
		class EdgeContainer implements Comparable<EdgeContainer> {

			private Double len = 0.0;
			private E e = null;
			
			public EdgeContainer(E e , Double len) {
				this.e = e;
				this.len = len;
			}
			
			public E getE(){
				return this.e;
			}
			
			public Double getLen() {
				return this.len;
			}
			
			@Override
			public int compareTo(EdgeContainer container) {
				return container.getLen().compareTo(this.len);
			}

		}
		
		//--------------------------------- Inner Class END --------------------------//
		
		public PracticalVerticeDivider(Graph<V,E> g, DoubleMatrix1D projection, Map<E , Integer> edgeNum) {
			this.projection = projection;
			this.edgeNum = edgeNum;
			
			this.g = g;
			
			this.A_t = new HashSet<SplitVertex<V,E>>();
			this.A_s = new HashSet<SplitVertex<V,E>>();
			
			this.pot = new VectorPotential<V,E>(g, edgeNum);
		}
		
		/**
		 * Compute the set of active edge sources (big flow vector projection) and active edge targets (small flow vector projection)
		 * 
		 * @param gPrime 
		 * @return A pair of sets with A_s being the first and A_t being the second set
		 */
		public void divideActiveVertices(SplitGraph<V, E> gPrime , Set<E> A , DoubleMatrix1D r) {

			if (A.size() < 1.0 / DecompositionConstants.MAX_SOURCE_EDGES) {
				smallSetSpecialCase(gPrime, A, r);
			} else {
				divideDefaultCase(gPrime, A, r);
			}
		}

		/**
		 * Returns the source edges (after division)
		 * 
		 * @return : The source edges
		 */
		public Set<SplitVertex<V,E>> getAs() {
			return A_s;
		}
		
		/**
		 * Returns the target edges (after division)
		 * @return : The target edges
		 */
		public Set<SplitVertex<V,E>> getAt() {
			return A_t;
		}
		
		/**
		 * Divides the set of edges A into 2 sets A_s and A_t. A_s <= |A|/8 , A_t >= |A|/2
		 * 
		 * @param gPrime : The subdivision graph G'
		 * @param A : The set to divide
		 * @param r : A random direction
		 * @return : the divided set A 
		 */
		private void divideDefaultCase(
				SplitGraph<V, E> gPrime, Set<E> A, DoubleMatrix1D r) {
			//Copy of R with EdgeContainers instead of edges
			
			Set<EdgeContainer> edgeContainersL = new HashSet<EdgeContainer>();
			Set<EdgeContainer> edgeContainersR = new HashSet<EdgeContainer>();
			Set<E> L = new HashSet<E>();
			Set<E> R = new HashSet<E>();

			Double average_length = pot.computeAverageProjection(projection, A);
			
			splitAt(A, edgeContainersL , edgeContainersR , L, R, average_length);
			
			if (R.size() > L.size()) {
				standardDivisionSmallest(gPrime, A, edgeContainersL, L, R);
			} else {
				standardDivisionGreatest(gPrime, A, edgeContainersR, L, R);
			}
		}

		/**
		 * Gets L, R and the edgeContainer corresponding to L and computes the division.
		 * A_t corresponds to R and stays pretty much untouched.
		 * A_s corresponds to the |A|/8 edges in L with the smallest flow vector projection 
		 * 
		 * @param gPrime : The subdivision graph G'
		 * @param A : The set to be divided
		 * @param edgeContainers : The edge containers corresponding to L
		 * @param L : The edges in A with below average flow vector projection
		 * @param R : The edges in A with above average flow vector projection
		 * @return A division of A
		 */
		private void standardDivisionSmallest(SplitGraph<V, E> gPrime, Set<E> A,Set<EdgeContainer> edgeContainersL, Set<E> L, Set<E> R) {
			
			//Get A/8 edges with smallest projection and return them as A_s
			Ordering<EdgeContainer> o = Ordering.natural();
			
			List<EdgeContainer> smallest = o.leastOf(edgeContainersL, (int)((double)A.size() * DecompositionConstants.MAX_SOURCE_EDGES));
			
			L.clear();
			for (EdgeContainer container : smallest)
				L.add(container.getE());
			
			A_s = gPrime.getSplitVertices(L);
			A_t = gPrime.getSplitVertices(R);
		}
		
		/**
		 * Gets L, R and the edgeContainer corresponding to L and computes the division.
		 * A_t corresponds to R and stays pretty much untouched.
		 * A_s corresponds to the |A|/8 edges in L with the smallest flow vector projection 
		 * 
		 * @param gPrime : The subdivision graph G'
		 * @param A : The set to be divided
		 * @param edgeContainers : The edge containers corresponding to L
		 * @param L : The edges in A with below average flow vector projection
		 * @param R : The edges in A with above average flow vector projection
		 * @return A division of A
		 */
		private void standardDivisionGreatest(SplitGraph<V, E> gPrime, Set<E> A,Set<EdgeContainer> edgeContainersR, Set<E> L, Set<E> R) {
			
			//Get A/8 edges with smallest projetion and return them as A_s
			Ordering<EdgeContainer> o = Ordering.natural();
			
			List<EdgeContainer> greatest = o.greatestOf(edgeContainersR, (int)((double)A.size() * DecompositionConstants.MAX_SOURCE_EDGES));
			
			R.clear();
			for (EdgeContainer container : greatest)
				R.add(container.getE());
			
			A_s = gPrime.getSplitVertices(R);
			A_t = gPrime.getSplitVertices(L);
		}
	
		/**
		 * Splits the set A at the given split value. All vertices with flowvectors >= split_value are put into R, all vertices with flowvectors < split_value are put into L 
		 * 
		 * @param A : The set to be divided
		 * @param r : A random direction
		 * @param edgeContainers : A set where L is duplicated including its flow projections for future use (to pick the smallest)
		 * @param L : Subset of A containing edges whos projected flowvectors are < split_value
		 * @param R : Subset of A containing edges whos projected flowvectors are >= split_value
		 * @param split_value : The split value
		 */
		private void splitAt(Set<E> A,
				Set<EdgeContainer> edgeContainersL,
				Set<EdgeContainer> edgeContainersR, 
				Set<E> L, 
				Set<E> R,
				Double split_value) {
			//Split A into 2 sets:
			// * L = {e \in A | u_e < avg_len}
			// * R = {e \in A | u_e >= avg_len}
			for (E e: A ){
				Double p = projection.getQuick(edgeNum.get(e));
				p = p / g.getEdgeWeight(e);
				if (p < split_value) {
					L.add(e);
					//All edges in L need to be comparable => add an edge container for them
					edgeContainersL.add(new EdgeContainer(e, p));
				} else {
					R.add(e);
					edgeContainersR.add(new EdgeContainer(e, p));
				}
			}
		}
		
		/**
		 * Handles the special case where |A| < 8. Here edges are matched evenly. If |A| is odd the edge nearest to the median is neglected (as discussed with Chintan)
		 * 
		 * @param gPrime : The subdivision Graph of G
		 * @param A : The set to divide
		 * @param r : A random direction
		 * @return : A Pair containing the divided set A
		 */
		private void smallSetSpecialCase(SplitGraph<V,E> gPrime , Set<E> A , DoubleMatrix1D r) {
			Set<EdgeContainer> edgeContainers = new HashSet<EdgeContainer>(); 

			Set<E> L = new HashSet<E>();
			Set<E> R = new HashSet<E>();
			
			for (E e:A) {
				Double p = projection.getQuick(edgeNum.get(e));

				//Gather all elements in container
				edgeContainers.add(new EdgeContainer(e, p));
			}
			
			Integer take = 0;
			if (A.size() % 2 == 0) { //even
				take = A.size() / 2;
			} else { //
				take = (A.size() -1) /2;
			}
			
			//take |A|/2 elements for L and R
			Ordering<EdgeContainer> o = Ordering.natural();
			
			List<EdgeContainer> smallest = o.leastOf(edgeContainers, take);
			for (EdgeContainer container : smallest) {
				L.add(container.getE());
			}
			

			List<EdgeContainer> greatest = o.greatestOf(edgeContainers, take);
			for (EdgeContainer container : greatest) {
				R.add(container.getE());
			}
			
			A_s = gPrime.getSplitVertices(L);
			A_t = gPrime.getSplitVertices(R);
		}
	}
