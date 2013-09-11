package org.jgrapht.experimental.clustering;

/**
 * Container class for a SplitGraph (contains either a vertex v \in V of the original Graph g or an edge e \in E of the original graph G)
 * 
 * @author moritzfuchs
 *
 * @param <V> the vertex class of the original graph
 * @param <E> the edge class of the original graph
 */
public class SplitVertex<V extends Comparable<V>,E> implements Comparable<SplitVertex<V,E>>{

	/**
	 * The vertex in the original graph that is represented by this vertex
	 */
	private V v = null;
	
	/**
	 * The edge in the original graph that is represented by this vertex
	 */
	private E e = null;
	
	/**
	 * The type of this vertex:
	 *  * -1 : special(source or target)
	 *  *  1 : vertex
	 *  *  2 : edge
	 */
	private Integer type = -1;
	
	/**
	 * Adds the vertex v to the container and sets the type to 'vertex'( CAUTION: deletes e at the same time! )
	 * 
	 * @param v : The vertex to be saved
	 */
	public void setVertex(V v) {
		if (v == null ) {
			throw new NullPointerException("Vertex v is null!");
		}
		
		this.e = null;
		this.v = v;
		this.type = 1;
	}
	
	/**
	 * Adds the edge e to the container and sets the type to 'edge'( CAUTION: deletes v at the same time! )
	 * 
	 * @param e : The edge to be saved
	 */
	public void setEdge(E e) {
		if (e == null ) {
			throw new NullPointerException("Edge e is null!");
		}
		this.v = null;
		this.e = e;
		this.type = 0;
	}
	
	/**
	 * Returns vertex in container. WARNING: Might be null!
	 * 
	 * @return Returns the saved vertex or null if the vertex represents an edge
	 */
	public V getVertex() {
		return v;
	}
	
	/**
	 * Returns edge in container. WARNING: Might be null!
	 * 
	 * @return Returns the saved edge or null if the vertex represents an vertex
	 */
	public E getEdge() {
		return e;
	}
	
	/**
	 * Checks whether this container is an edge-container
	 * 
	 * @return True if the vertex represents an edge, false otherwise
	 */
	public Boolean isEdgeContainer() {
		return this.type == 0;
	}
	

	/**
	 * Checks whether this container a vertex-container
	 * 
	 * @return True if the vertex represents a vertex, false otherwise
	 */
	public Boolean isVertexContainer() {
		return this.type == 1;
	}
	
	/**
	 * Checks whether this container is empty. The container is empty if the vertex is special, e.g. a source or a target for the SplitGraph.
	 * 
	 * @return True if this is a source or target vertex, false otherwise
	 */
	public Boolean isSpecialVertex() {
		return type == -1;
	}
	
	/**
	 * @return String representation of the vertex
	 */
	public String toString() {
		if (isEdgeContainer())
			return this.e.toString();
		if (isVertexContainer()) {
			return this.v.toString();
		} else
			return "Source / Target";
	}

	@Override
	public int compareTo(SplitVertex<V, E> vertex) {
		if (this.isVertexContainer() && vertex.isVertexContainer()) {
			return this.getVertex().compareTo(vertex.getVertex());
		}
		
		if (this.isEdgeContainer() && vertex.isEdgeContainer()) {
			if (this.getEdge() == vertex.getEdge()) {
				return 0;
			}else {
				return -1;
			}
		}
		
		if (this.isSpecialVertex() && vertex.isSpecialVertex()) {
			if (this == vertex) {
				return 0;
			} else {
				return -1;
			}
		}
		
		return -1;
	}
}
