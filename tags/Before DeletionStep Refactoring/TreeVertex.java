package org.jgrapht.experimental.clustering;

import java.io.Serializable;

public class TreeVertex<V> implements Serializable{
	
	private static final long serialVersionUID = 558289795544820071L;

	/**
	 * The vertex in the original graph G that is represented by this vertex in the decomposition tree. (null if this vertex is an inner vertex, not null otherwise)
	 */
	private V vertex = null;
	
	/**
	 * The type of the vertex. LEAF if the vertex is a leaf (and therefore has a corresponding vertex in the original graph G) or TREE_VERTEX if the vertex is an tree vertex in the decomposition tree
	 */
	private TreeVertexType type;
	
	/**
	 * Creates a new tree vertex
	 */
	public TreeVertex() {
		type = TreeVertexType.TREE_VERTEX;
	}
	
	/**
	 * Creates a new leaf vertex
	 * 
	 * @param vertex : The corresponding vertex in the original graph G
	 */
	public TreeVertex(V vertex) {
		this.vertex = vertex;
		type = TreeVertexType.LEAF;
	}
	
	/**
	 * Get the corresponding vertex in the original graph G (or null if this is a tree vertex
	 * 
	 * @return : The corresponding vertex in the original graph G or null if this is a tree vertex
	 */
	public V getVertex() {
		return vertex;
	}
	
	/**
	 * Get the vertex type of the vertex: 
	 *  * LEAF if the vertex is a leaf in the decomposition tree. In this case the vertex has a corresponding vertex in the decomposed graph G
	 *  * TREE_VERTEX if the vertex is a tree vertex of the decomposition tree. In this case the vertex corresponds to a set of vertices of the decomposed graph G which are given by all reachable leafes below this vertex
	 * 
	 * @return : The type of the vertex
	 */
	public TreeVertexType getType() {
		return type;
	}
	
}
