package org.jgrapht.experimental.clustering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import org.jgrapht.Graph;

import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;

import com.google.common.collect.BiMap;


/**
 * Collects some utilities
 * 
 * @author moritzfuchs
 *
 */
public class Util {

	/**
	 * Turns a unsorted collection into a sorted list. Taken from http://stackoverflow.com/questions/740299/how-do-i-sort-a-set-to-a-list-in-java
	 * 
	 * @param <T>
	 * @param c
	 * @return
	 */
	public static
	<T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
	  List<T> list = new ArrayList<T>(c);
	  java.util.Collections.sort(list);
	  return list;
	}
	
	/**
	 * Generates a random direction in a given number of dimensions
	 * 
	 * @param dimensions : The dimension of the direction
	 * @return : A random normalized direction
	 */
	public static DenseDoubleMatrix1D getRandomDirection(Integer dimensions) {
		
		DenseDoubleMatrix1D r = new DenseDoubleMatrix1D(dimensions); 
		
		Random rand = new Random();
		rand.setSeed(System.nanoTime());
		
		//generate random vector
		for (int i=0;i<dimensions;i++) {
			r.setQuick(i, -1.0 + 2.0 * rand.nextGaussian());
		}
		
		Double len = 0.0;
		
		//normalize
		for (int i=0;i<dimensions;i++) {
			len += Math.pow(r.getQuick(i) , 2);
		}
		
		len = Math.sqrt(len);
		
		for (int i=0;i<dimensions;i++) {
			r.setQuick(i, r.getQuick(i) / len);
		}
	
		return r;
	}
	
	/**
	 * Computes the 2-Norm of a given vector
	 * 
	 * @param vector
	 * @return The 2-Norm of the given vector
	 */
	public static Double vectorLength(Double[] vector) {
		
		Double sum = 0.0;
		for (Double d : vector) {
			sum += Math.pow(d, 2);
		}
		sum = Math.sqrt(sum);
		
		return sum;
		
	}

	/**
	 * Takes a vector and returns it normal
	 * 
	 * @param a arbitrary vector in \mathbb{R}^n
	 * @return The normal of the given vector
	 */
	public static Double[] normalizeVector(Double[] vector) {
		Double norm = vectorLength(vector);
		for (int i=0;i<vector.length;i++) {
			vector[i] = vector[i] / norm;
		}
		
		return vector;
	}
	
	/**
	 * Checks whether vertices s and t are connected in G or not
	 * 
	 * @param <V> : Type of vertices of G
	 * @param <E> : Type of edges of G
	 * @param g : Graph G
	 * @param s : Starting vertex s
	 * @param t : target vertex t
	 * @return : true is t is reachable from s, false otherwise
	 */
	public static <V,E> Boolean connected(Graph<V,E> g , V s, V t) {
		return connected(g,s,t,new HashSet<E>());
	}
	
	/**
	 * Checks whether t is reachable from s without using a set of forbidden edges
	 * 
	 * @param <V> : Type of vertices of G
	 * @param <E> : Type of edges of G
	 * @param g : Graph G
	 * @param s : Starting vertex s
	 * @param t : target vertex t
	 * @param edges : forbidden edges
	 * @return : true is t is reachable from s without using forbidden edges, false otherwise
	 */
	public static <V,E> Boolean connected(Graph<V,E> g , V s , V t , Set<E> edges) {
		Queue<V> q = new LinkedList<V>();
		q.add(s);
		
		Set<V> seen = new HashSet<V>();
		seen.add(s);
		
		while(!q.isEmpty()) {
			V current_vertex = q.poll();
			//We have found t => s and t must be connected
			if (current_vertex == t)
				return true;
			
			for (E e : g.edgesOf(current_vertex) ) {
				if (edges.contains(e))
					continue;
				
				V target;
				if (g.getEdgeTarget(e) != current_vertex) {
					target = g.getEdgeTarget(e);
				} else {
					target = g.getEdgeSource(e);
				}
				if (!seen.contains(target)) {
					seen.add(target);
					q.add(target);
				}
			}
			
		}
		
		return false;
	}
	
	/**
	 * Returns the target of a given edge starting from a given vertex
	 * 
	 * @param <V> : The type of vertices
	 * @param <E> : The type of edges
	 * @param g : The graph
	 * @param source : The source vertex
	 * @param edge : The traversed edge
	 * @return : The target of the given edge starting from the given vertex
	 */
	public static <V extends Comparable<V>,E> V getEdgeTarget( Graph<V,E> g , V source , E edge) {		
		
		if (g.getEdgeTarget(edge).compareTo(source) == 0) {
			return g.getEdgeSource(edge);
		}
		if (g.getEdgeSource(edge).compareTo(source) == 0) {
			return g.getEdgeTarget(edge);
		}
		
		return null;
	}

	/**
	 * Clones a 2D Double Array
	 * 
	 * @param array : 2D Double Array 
	 * @return : exact copy of the Array
	 */
	public static Double[][] clone2DArray(Double[][] array) {
		Double[][] new_array = new Double[array.length][array[0].length];
		for (int i=0; i<array.length; i++) {
			for (int j=0; j<array.length; j++) {
				new_array[i][j] = new Double(array[i][j]);
			}
		}

		return new_array;
	}
	
	/**
	 * Returns the initial set of flow vectors for a given set A. (unique unit commodity for each edge)
	 * 
	 * @param <V> : The type of vertices
	 * @param <E> : The type of edges
	 * @param A : The set of edges for which initial flow vectors shall be computed
	 * @param edgeNum : A BiMap from edges to Integers (unique Integer for each edge in [0 ... m-1])
	 * @param m : Dimension of the array ( = number of edges in graph G)
	 * @return : The initial set of flow vectors for the given set A. All edges in E \ A receive a 0-vector
	 */
	public static <V,E> Double[][] getInitialFlowVectorsForSet(Set<E> A , BiMap<E , Integer> edgeNum , Integer m) {
		
		Double[][] flowVectors = new Double[m][m];
		
		for (int i=0;i<m;i++) {
			Arrays.fill(flowVectors[i] , 0.0);
		}
		
		for (E e : A) {
			flowVectors[edgeNum.get(e)][edgeNum.get(e)] = 1.0;
		}
		return flowVectors;
	}
	
	
}
