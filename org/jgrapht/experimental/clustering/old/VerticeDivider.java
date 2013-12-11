package org.jgrapht.experimental.clustering.old;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.experimental.clustering.SplitGraph;
import org.jgrapht.experimental.clustering.SplitVertex;
import org.jgrapht.experimental.clustering.Util;

import com.google.common.collect.Ordering;

//TODO: Class is kind of long, think of way to split it into 2 or maybe 3 classes (that we might be able to test separately)
/**
 * Takes a set A and divides it into 2 sets A_s and A_t such that the flow vector projections of edges in A_s are 'far away' from the median and |A_t| >= |A|/2 and |A_s| <= |A|/8 
 * 
 * @author moritzfuchs
 *
 * @param <V> : The type of vertices
 * @param <E> : The type of edges
 */
public class VerticeDivider<V extends Comparable<V>,E> {

	/**
	 * Divisions with potential less than DIVISION_POTENTIAL_BOUND times the potential of the whole set A are acceptable
	 */
	private static final double DIVISION_POTENTIAL_BOUND = 1.0/20.0;
	
	/**
	 * Max. fraction of A that can become source edges. (if |A| < 1/MAX_SOURCE_EDGES then this bound is ignored!) 
	 */
	private static final double MAX_SOURCE_EDGES  = 1.0/8.0;

	/**
	 * Flow vectors of edges in original graph G
	 */
	private Double[][] flowVectors;
	
	/**
	 * Number of edges in original graph G
	 */
	private Integer m;
	
	/**
	 * Unique Integer for each edge e in original Graph g
	 */
	private Map<E , Integer> edgeNum;
	
	/**
	 * Flag to indicate whether or not average flow vector is currently valid
	 */
	private boolean averageFlowVectorValid = false;
	
	/**
	 * The current average flow vector if {@link VerticeDivider:averageFlowVectorValid} is true, garbage else 
	 */
	private Double[] avergeFlowVector = null;
	
	/**
	 * The source edges (after division)
	 */
	private Set<SplitVertex<V,E>> A_s;
	
	/**
	 * The target edges (after division)
	 */
	private Set<SplitVertex<V,E>> A_t;

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
	
	public VerticeDivider(Double[][] flowVectors , Integer m , Map<E , Integer> edgeNum) {
		this.flowVectors = flowVectors;
		this.m = m;
		this.edgeNum = edgeNum;
		
		this.A_t = new HashSet<SplitVertex<V,E>>();
		this.A_s = new HashSet<SplitVertex<V,E>>();
	}
	
	/**
	 * Compute the set of active edge sources (big flow vector) and active edge targets (small flow vector)
	 * according to Lemma 3.5 (page 7).
	 * @param gPrime 
	 * @return A pair of sets with A_s being the first and A_t being the second set
	 */
	public void divideActiveVertices(SplitGraph<V, E> gPrime , Set<E> A) {

		//Generate a random direction
		Double[] r = Util.getRandomDirection(m);

		if (A.size() < 1.0 / MAX_SOURCE_EDGES) {
			smallSetSpecialCase(gPrime, A, r);
		} else {
			divideDefaultCase(gPrime, A, r);
		}
	}

	/**
	 * Returns the source edges (after division)
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
			SplitGraph<V, E> gPrime, Set<E> A, Double[] r) {
		//Copy of R with EdgeContainers instead of edges
		
		Set<EdgeContainer> edgeContainersL = new HashSet<EdgeContainer>();
		Set<EdgeContainer> edgeContainersR = new HashSet<EdgeContainer>();
		Set<E> L = new HashSet<E>();
		Set<E> R = new HashSet<E>();

		Double[] avg = computeAverageFlowVector(A);
		Double average_length = projectFlowVector(avg , r);
		
		splitAt(A, r, edgeContainersL , edgeContainersR , L, R, average_length);
		
		Double l = computeDistanceFrom(r, L, average_length);
		
		if (R.size() > L.size()) {

			//Compute potential of L and A according to paper pages 7, 17
			Double P_L = getDivisionPotential(L , average_length , r);
			Double P_A = getDivisionPotential(A , average_length , r);
			
			if (P_L >= DIVISION_POTENTIAL_BOUND * P_A) {
				standardDivisionSmallest(gPrime, A, edgeContainersL, L, R);
			} else {
				//TODO: Test
				recomputeDivision(gPrime, A, r, average_length, l);
			}		
		} else {
			//R is the smaller set, therefore we want to take L as A_t and the GREATEST edges of R as A_t
			
			//Compute potential of L and A according to paper pages 7, 17
			Double P_R = getDivisionPotential(R , average_length , r);
			Double P_A = getDivisionPotential(A , average_length , r);
			
			if (P_R >= DIVISION_POTENTIAL_BOUND * P_A) {
				standardDivisionGreatest(gPrime, A, edgeContainersR, L, R);
			} else {
				//TODO: Test
				recomputeReverseDivision(gPrime, A, r, average_length, l);
			}	
			
		}
	}

	/**
	 * Computes the sum of distances of flow vector projections of edges in L to the average length of a flow vector projection. 
	 * 
	 * @param r : A random direction
	 * @param L : All edges in A with below average flow vector projection
	 * @param average_length : Average length of a flow vector projection
	 * @return sum of distances of flow vector projections of L to the average flow vector projection
	 */
	private Double computeDistanceFrom(Double[] r, Set<E> L, Double average_length) {
		//l = sum of distances of edges in L to the average length ( = sum of distances of edges in R to the average length)
		Double distance = 0.0;
		for (E e : L) {
			distance += Math.abs(projectFlowVector(flowVectors[edgeNum.get(e)], r) - average_length);
		}
		return distance;
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
		
		List<EdgeContainer> smallest = o.leastOf(edgeContainersL, (int)((double)A.size() * MAX_SOURCE_EDGES));
		
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
		
		List<EdgeContainer> greatest = o.greatestOf(edgeContainersR, (int)((double)A.size() * MAX_SOURCE_EDGES));
		
		R.clear();
		for (EdgeContainer container : greatest)
			R.add(container.getE());
		
		A_s = gPrime.getSplitVertices(R);
		A_t = gPrime.getSplitVertices(L);
	}

	/**
	 * Backup case in the potential of L is less than 1/20 times the potential of A
	 * 
	 * @param gPrime : The subdivision graph G'
	 * @param A : The set to be divided
	 * @param r : A random direction 
	 * @param average : The average length of a flow vector projection onto r
	 * @param l : Sum of distances of vectors larger than average
	 * @return : A division of A 
	 */
	private void recomputeDivision(
			SplitGraph<V, E> gPrime, Set<E> A, Double[] r, Double average, Double l) {
		
		Set<E> L = new HashSet<E>();
		Set<E> R = new HashSet<E>();

		Set<EdgeContainer> edgeContainers = new HashSet<EdgeContainer>(); 
		
		for (E e:A) {
			Double projection = projectFlowVector(flowVectors[edgeNum.get(e)], r);
			
			//Conditions as defined on page 17 (Proof of Lemma 3.5)
			if (projection <= average  + 4*l / A.size()) {
				L.add(e);
			}
			if (projection >= average  + 6*l / A.size()) {
				R.add(e);
				edgeContainers.add(new EdgeContainer(e , projection));
			}
		}
			
		//A_s = A/8 e with largest u_e
		Ordering<EdgeContainer> o = Ordering.natural();
		List<EdgeContainer> greatest = o.greatestOf(edgeContainers, (int)((double)A.size() * MAX_SOURCE_EDGES));
		
		R.clear();
		for (EdgeContainer container : greatest) {
			R.add(container.getE());
		}
		
		A_s = gPrime.getSplitVertices(R);
		A_t = gPrime.getSplitVertices(L);
	}
	
	/**
	 * Backup case in which the potential of L is less than 1/20 times the potential of A and |R| < |L|
	 * 
	 * @param gPrime : The subdivision graph G'
	 * @param A : The set to be divided
	 * @param r : A random direction 
	 * @param average : The average length of a flow vector projection onto r
	 * @param l : Sum of distances of vectors larger than average
	 * @return : A division of A 
	 */
	private void recomputeReverseDivision(
			SplitGraph<V, E> gPrime, Set<E> A, Double[] r, Double average, Double l) {
		
		Set<E> L = new HashSet<E>();
		Set<E> R = new HashSet<E>();

		Set<EdgeContainer> edgeContainers = new HashSet<EdgeContainer>(); 
		
		for (E e : A) {
			Double projection = projectFlowVector(flowVectors[edgeNum.get(e)], r);
			
			//Conditions as defined on page 17 (Proof of Lemma 3.5)
			if (projection <= average  + 4*l / A.size()) {
				R.add(e);
			}
			if (projection >= average  + 6*l / A.size()) {
				L.add(e);
				edgeContainers.add(new EdgeContainer(e , projection));
			}
		}
			
		//A_s = A/8 e with largest u_e
		Ordering<EdgeContainer> o = Ordering.natural();
		List<EdgeContainer> smallest = o.leastOf(edgeContainers, (int)((double)A.size() * MAX_SOURCE_EDGES));
		
		R.clear();
		for (EdgeContainer container : smallest) {
			L.add(container.getE());
		}
		
		A_s = gPrime.getSplitVertices(L);
		A_t = gPrime.getSplitVertices(R);
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
	private void splitAt(Set<E> A, Double[] r,
			Set<EdgeContainer> edgeContainersL,Set<EdgeContainer> edgeContainersR, Set<E> L, Set<E> R,
			Double split_value) {
		//Split A into 2 sets:
		// * L = {e \in A | u_e < avg_len}
		// * R = {e \in A | u_e >= avg_len}
		for (E e: A ){
			Double projection = projectFlowVector(flowVectors[edgeNum.get(e)] , r);
			if (projection < split_value) {
				L.add(e);
				//All edges in L need to be comparable => add an edge container for them
				edgeContainersL.add(new EdgeContainer(e, projection));
			} else {
				R.add(e);
				edgeContainersR.add(new EdgeContainer(e, projection));
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
	private void smallSetSpecialCase(SplitGraph<V,E> gPrime , Set<E> A , Double[] r) {
		Set<EdgeContainer> edgeContainers = new HashSet<EdgeContainer>(); 

		Set<E> L = new HashSet<E>();
		Set<E> R = new HashSet<E>();
		
		for (E e:A) {
			Double projection = projectFlowVector(flowVectors[edgeNum.get(e)] , r);

			//Gather all elements in container
			edgeContainers.add(new EdgeContainer(e, projection));
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
	
	/**
	 * Computes the average over all flow vectors
	 *
	 * @return
	 */
	private Double[] computeAverageFlowVector(Set<E> A) {
		if (this.averageFlowVectorValid)
			return this.avergeFlowVector;
		
		Double[] avg = new Double[m];
		
		Arrays.fill(avg, 0.0);
		
		//Sum up flow vectors
		for (E e: A) {
			for (int i= 0; i<m; i++) {				
				avg[i] += flowVectors[edgeNum.get(e)][i];
			} 
		}
		
		//average flow vectors
		for (int i = 0;i<m;i++) {
			avg[i] = avg[i] / A.size();
		}
		
		this.avergeFlowVector = avg;
		this.averageFlowVectorValid = true;
		
		return avg;
	}
	
	/**
	 * Projects vector u onto vector r. If vector dimensions are different, the smaller vector is assumed to be filled with 0 to match the larger vector's dimension.
	 * 
	 * @param u : vector that will be projected onto r
	 * @param r : vector that u will be projected onto
	 * @return : Projection of u onto r
	 */
	private Double projectFlowVector(Double[] u, Double[] r) {

		Double projection = 0.0;
		
		Integer len = (Integer)Math.min(u.length, r.length);
		
		for (int i=0;i<len;i++) {
			projection += u[i] * r[i];
		}
		
		return projection;
	}
	
	/**
	 * Compute the potential P_S of a set of edges S, as defined in the paper on page 7
	 * 
	 * @param L : A set of edges of G
	 * @param avg_len : The average length of a flow vector projection onto r
	 * @param r : A random direction (unit vector)
	 * @return : The current division potential
	 */
	private Double getDivisionPotential(Set<E> L , Double avg_len , Double[] r) {
		Double potential = 0.0;
		
		for (E e : L) {
			potential += Math.pow(projectFlowVector(flowVectors[edgeNum.get(e)], r) - avg_len, 2);
		}
		
		return potential;
	}
	
}
