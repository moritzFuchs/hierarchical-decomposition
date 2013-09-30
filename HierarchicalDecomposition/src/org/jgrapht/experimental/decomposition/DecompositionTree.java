package org.jgrapht.experimental.decomposition;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.experimental.clustering.TreeVertex;
import org.jgrapht.experimental.clustering.TreeVertexType;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

/**
 * Synchronized container for Decomposition tree
 * 
 * @author moritzfuchs
 *
 * @param <V>
 */
public class DecompositionTree<V> implements Serializable{

	private static final long serialVersionUID = 8279948299862555420L;

	/**
	 * The default weight of an edge in the decomposition tree (used if no weight is specified)
	 */
	public static final Double DEFAULT_WEIGHT = 1.0;

	/**
	 * The decomposition tree
	 */
	private SimpleDirectedGraph<TreeVertex<V>,DefaultWeightedEdge> tree;
	
	/**
	 * Map from vertices of the original graph G to leafs in the decomposition tree (used to get the leaf to a given vertex)
	 */
	private Map<V , TreeVertex<V>> leaf_map;
	
	/**
	 * The root vertex of the decomposition tree
	 */
	private TreeVertex<V> root;
	
	public DecompositionTree() {
		leaf_map = new HashMap<V , TreeVertex<V>>();
		tree = new SimpleDirectedGraph<TreeVertex<V> , DefaultWeightedEdge>(DefaultWeightedEdge.class);
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
		
		leaf_map.put(v, vertex);
		
		return vertex;
	}
	
	/**
	 * Returns the leaf vertex corresponding to a given vertex v of the original (now decomposed) graph G
	 * 
	 * @param v : The vertex of the original graph G
	 * @return : The leaf corresponding to the given vertex
	 */
	public TreeVertex<V> getLeaf(V v) {
		return leaf_map.get(v);
	}
	
	/**
	 * Returns corresponding vertices of all leaf vertices under a given {@link TreeVertex}
	 * 
	 * @param vertex : The starting {@link TreeVertex}
	 * @return : Set of vertices of the original graph under the given {@link TreeVertex}
	 */
	public Set<V> getAll(TreeVertex<V> vertex) {
		
		Set<V> set = new HashSet<V>();
		
		if (vertex.getType().equals(TreeVertexType.LEAF)) {
			set.add(vertex.getVertex());
		} else {
			for (DefaultWeightedEdge e : tree.outgoingEdgesOf(vertex)) {
				TreeVertex<V> next = tree.getEdgeTarget(e);
				set.addAll(getAll(next));
			}
		}
		
		return set;
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
	 * Returns the height of the decomposition tree.
	 * 
	 * @return Integer : The height of the decomposition tree.
	 */
	public Integer getHeight(Boolean collapseInfEdges) {
		return traverseHeight(root , 0 , collapseInfEdges);
	}
	
	/**
	 * Subroutine for {@link DecompositionTree.getHeight}; Computes the height of the tree (DFS)
	 * 
	 * @param v : The current vertex
	 * @param current_height : The current height
	 * @return Integer : The max-height below the given vertex v.  
	 */
	private Integer traverseHeight(TreeVertex<V> v , Integer current_height , Boolean collapseInfEdge) {
		
		if (v.getType() == TreeVertexType.LEAF) {
			return current_height;
		} else {
			Integer max_height = 0;
			for (DefaultWeightedEdge e : tree.outgoingEdgesOf(v)) {
				TreeVertex<V> target = tree.getEdgeTarget(e);
				Integer height;
				if (collapseInfEdge && tree.getEdgeWeight(e) == Double.POSITIVE_INFINITY) {
					 height = traverseHeight(target , current_height , collapseInfEdge);
				} else {
					height = traverseHeight(target , current_height+1 , collapseInfEdge);
				}
				
				if (height > max_height) {
					max_height = height;
				}
			}
			return max_height;
		}
		
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
	
	public DirectedGraph<TreeVertex<V>,DefaultWeightedEdge> getGraph() {
		return tree;
	}
}