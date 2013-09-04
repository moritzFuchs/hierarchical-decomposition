package org.jgrapht.experimental.decomposition;

import org.jgrapht.experimental.clustering.TreeVertex;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;

/**
 * Synchronized container for Decomposition tree
 * 
 * @author moritzfuchs
 *
 * @param <V>
 */
public class DecompositionTree<V> {

	/**
	 * The default weight of an edge in the decomposition tree (used if no weight is specified)
	 */
	public static final Double DEFAULT_WEIGHT = 1.0;

	/**
	 * The decomposition tree
	 */
	private SimpleGraph<TreeVertex<V>,DefaultWeightedEdge> tree;
	
	/**
	 * The root vertex of the decomposition tree
	 */
	private TreeVertex<V> root;
	
	public DecompositionTree() {
		tree = new SimpleGraph<TreeVertex<V> , DefaultWeightedEdge>(DefaultWeightedEdge.class);
		root = new TreeVertex<V>();
		tree.addVertex(root);
	}
	
	/**
	 * Add an inner tree vertex to the tree
	 * 
	 * @return : The newly added tree vertex
	 */
	public synchronized TreeVertex<V> addVertex() {
		TreeVertex<V> vertex = new TreeVertex<V>();
		tree.addVertex(vertex);
		
		return vertex;
	} 
	
	/**
	 * Add a leaf to the tree
	 * 
	 * @param v : Corresponding vertex in the decomposed graph G
	 * @return : The newly added leaf
	 */
	public synchronized TreeVertex<V> addVertex(V v) {
		TreeVertex<V> vertex = new TreeVertex<V>(v);
		tree.addVertex(vertex);
		
		return vertex;
	}
	
	/**
	 * Returns the root of the decomposition tree
	 * 
	 * @return : The root of the decomposition tree
	 */
	public TreeVertex<V> getRoot() {
		return root;
	}
	
	/**
	 * Adds an edge with default weight ({@link DecompositionTree.DEFAULT_WEIGHT}) to the tree.
	 * 
	 * @param source : The source of the edge
	 * @param target : The target of the edge
	 */
	public synchronized void addEdge(TreeVertex<V> source , TreeVertex<V> target) {
		addEdge(source , target , DEFAULT_WEIGHT);
	}
	
	/**
	 * Adds a new edge with specified weight to the tree.
	 * 
	 * @param source : The source vertex of the edge
	 * @param target : The target vertex of the edge
	 * @param weight : The weight of the new edge
	 */
	public synchronized void addEdge(TreeVertex<V> source , TreeVertex<V> target , Double weight) {
		DefaultWeightedEdge e = tree.addEdge(source, target);
		tree.setEdgeWeight(e, weight);
	}
}