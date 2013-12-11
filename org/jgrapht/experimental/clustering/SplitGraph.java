package org.jgrapht.experimental.clustering;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

//TODO: Clean up methods, normalize names => refactor!

/**
 * Takes a Graph<V,E> G and generates an equal graph that has 1 additional vertex for each edge e in G. 
 * This additional vertex is called split-vertex of the edge.
 * 'Example': 
 * *----* in G turns into *--split-vertex--* in G'
 * 
 * @author moritzfuchs
 *
 * @param <V> : The type of vertices
 * @param <E> : The type of edges
 */
public class SplitGraph<V extends Comparable<V>,E> extends SimpleGraph<SplitVertex<V,E>, DefaultWeightedEdge> {

	private static final long serialVersionUID = -8891320512243160105L;
	
	/**
	 * The original graph G
	 */
	private Graph<V,E> g = null;
	
	/**
	 * A BiMap from edges in the original graph G to their corresponding SplitGraphVertices in G'
	 */
	private BiMap<E , SplitVertex<V,E>> edgeMap = null;
	
	/**
	 * A BiMap from vertices in the original graph G to their corresponding SplitGraphVertices in G'
	 */
	private BiMap<V , SplitVertex<V,E>> vertexMap = null;
	
	/**
	 * The current source vertex for the flow problem
	 */
	private SplitVertex<V,E> source = null;
	
	/**
	 * The current target vertex for the flow problem
	 */
	private SplitVertex<V,E> target = null;
	
	/**
	 * Original weights of the subdivision graph G' (weights of the edges in the original graph G)
	 */
	private Map<DefaultWeightedEdge , Double> original_weights;
	
	/**
	 * Initialize split graph: For each vertex in G inserts a vertex into G'. For each edge (u,v) in G inserts a vertex x_(u,v) into G' and connects it to x_u and x_v   
	 * 
	 * @param g : The original graph G
	 */
	public SplitGraph(Graph<V,E> g) {
		super(DefaultWeightedEdge.class);
		
		edgeMap = HashBiMap.create(g.edgeSet().size());
		vertexMap = HashBiMap.create(g.edgeSet().size());
		original_weights = new HashMap<DefaultWeightedEdge , Double>();
		
		this.g = g;
		
		for (V v:g.vertexSet()) {
			SplitVertex<V,E> splitVertex = new SplitVertex<V,E>();
			splitVertex.setVertex(v);
			
			this.addVertex(splitVertex);
			
			vertexMap.put(v, splitVertex);
		}
		
		for (E e:g.edgeSet()) {
			SplitVertex<V,E> splitVertex = new SplitVertex<V,E>();
			splitVertex.setEdge(e);

			this.addVertex(splitVertex);
			
			edgeMap.put(e , splitVertex);
		}
		
		for (E e:g.edgeSet()) {
			
			V source = g.getEdgeSource(e);
			V target = g.getEdgeTarget(e);
			
			Double weight = g.getEdgeWeight(e);
			
			DefaultWeightedEdge e1 = this.addEdge(vertexMap.get(source), edgeMap.get(e));
			DefaultWeightedEdge e2 = this.addEdge(edgeMap.get(e) , vertexMap.get(target));
			
			//Add 2 edges for each edge in the original graph => 1 vertex in the middle of each edge
			this.setEdgeWeight(e1 , weight);
			this.setEdgeWeight(e2 , weight);
			
			original_weights.put(e1, weight);
			original_weights.put(e2, weight);
		}
	}
	
	/**
	 * Adds a source s and a target t to the SplitGraph, s.t. every vertex in A_s is connected to the source with capacity 1, every vertex in A_t is connected to the target with capacity 0.5.
	 * All other edges receive capacity 2.
	 * 
	 * @param A_s : The source vertices
	 * @param A_t : The target vertices
	 */
	public void addSourceAndTarget(Set<SplitVertex<V,E>> A_s , Set<SplitVertex<V,E>> A_t ) {
		
		this.source = new SplitVertex<V,E>();
		this.target = new SplitVertex<V,E>();
		
		this.addVertex(this.source);
		this.addVertex(this.target);
		
		//Set all edge capacities to 
		for (DefaultWeightedEdge e : this.edgeSet()) {
			this.setEdgeWeight(e, original_weights.get(e));
		}
		
		//Connect all vertices in A_s to the source with capacity 1
		for (SplitVertex<V,E> v : A_s) {
			E e = getOriginalEdge(v);
			this.setEdgeWeight(this.addEdge(this.source, v) , g.getEdgeWeight(e));
		}
		
		//Connect all vertices in A_t to the target with capacity 0.5
		for (SplitVertex<V,E> v : A_t) {
			E e = getOriginalEdge(v);
			this.setEdgeWeight(this.addEdge(v, this.target) , g.getEdgeWeight(e) * 0.5);
		}
	}
	
	/**
	 * Adds a source and a target to G', connects s to x_e \in A_s with capacity cap(e) and t to x_h \in A_t with capacity cap(h)
	 * 
	 * @param A : The source vertices (which have to represent edges in the original graph G)
	 * @param B : The target vertices (which have to represent edges in the original graph G)
	 */
	public void addSourceAndTargetForLemma33(Set<SplitVertex<V,E>> A , Set<SplitVertex<V,E>> B ) {
		this.source = new SplitVertex<V,E>();
		this.target = new SplitVertex<V,E>();
		
		this.addVertex(this.source);
		this.addVertex(this.target);
		
		//Connect all vertices in A_s to the source with capacity of the associated edge
		for (SplitVertex<V,E> v : A) {
			this.setEdgeWeight(this.addEdge(this.source, v) , g.getEdgeWeight(v.getEdge()));
		}
		//Connect all vertices in A_t to the target with capacity of the associated edge
		for (SplitVertex<V,E> v : B) {
			this.setEdgeWeight(this.addEdge(v, this.target) , g.getEdgeWeight(v.getEdge()));
		}
		
		//return new Pair<SplitGraphVertex<V,E> , SplitGraphVertex<V,E>>(this.source,this.target);
	}
	
	/**
	 * Removes the source and the target from the graph and recovers all edge weights
	 */
	public void removeSourceAndTarget() {
		if (this.source != null) {
			this.removeVertex(this.source);
			this.source = null;
		}
			
		if (this.target != null) {
			this.removeVertex(this.target);
			this.target = null;
		}
		
		resetWeights();
	}
	
	/**
	 * Returns the set of split vertices that correspond to the given edge set. 
	 * 
	 * @param edges : A set of edges of the original graph G 
	 * @return The set of SplitVertices corresponding to the given edges
	 */
	public Set<SplitVertex<V,E>> getSplitVertices(Set<E> edges) {
		Set<SplitVertex<V,E>> ret = new HashSet<SplitVertex<V,E>>(); 
		for (E e: edges) {
			ret.add(edgeMap.get(e));
		}
		
		return ret;
	}
	
	/**
	 * Returns the split vertice for the given edge e \in E[G]
	 * 
	 * @param e : An edge in the original graph G
	 * @return the split graph vertex corresponding to e
	 */
	public SplitVertex<V,E> getSplitVertexFromEdge (E e) {
		return edgeMap.get(e);
	}
	
	/**
	 * Returns the split vertice for the given edge e \in E[G]
	 * 
	 * @param e : An edge in the original graph G
	 * @return the split graph vertex corresponding to v
	 */
	public SplitVertex<V,E> getSplitVertexFromVertex (V v) {
		return vertexMap.get(v);
	}
	
	/**
	 * Returns the set of all edge vertices of the SplitGraph (one for each edge in the original graph)
	 * 
	 * @return
	 */
	public Set<SplitVertex<V, E>> getEdgeVertices() {
		return edgeMap.values();
	}
	
	/**
	 * Returns a BiMap from edges of the original graph to vertices in the SplitGraph
	 * @return
	 */
	public BiMap<E , SplitVertex<V,E>> getEdgeBiMap() {
		return edgeMap;
	}
	
	/**
	 * Returns a BiMap from vertices of the original graph to vertices in the SplitGraph
	 * @return
	 */
	public BiMap<V , SplitVertex<V,E>> getVertexBiMap() {
		return vertexMap;
	}
	
	/**
	 * Returns the original graph on which this SplitGraph is based on. WARNING: The original graph might have changed after the creation of the SplitGraph!
	 * @return g:Graph<V,E>, the original graph.
	 */
	public Graph<V,E> getOriginalGraph (){
		return g;
	}
	
	/**
	 * Given a split vertex returns the edge in the original graph that is represented by this split vertex or null if the split vertex does not correspond to an edge in the original graph
	 * 
	 * @param v
	 * @return
	 */
	public E getOriginalEdge(SplitVertex<V,E> v) {
		return edgeMap.inverse().get(v);
	}
	
	/**
	 * Takes an edge e of E[G'] between two split graph vertices and returns the corresponding edge e' in G.
	 * 
	 * @param e : An edge of the split graph G'
	 * @return The corresponding edge e' in the original graph G
	 */
	public E getOriginalEdge( DefaultWeightedEdge e ) {
		SplitVertex<V,E> source = this.getEdgeSource(e);
		SplitVertex<V,E> target = this.getEdgeTarget(e);
		
		if (edgeMap.containsValue(source))
			return edgeMap.inverse().get(source);
		return edgeMap.inverse().get(target);
	}

	/**
	 * Takes a set of split vertices of G' and returns the corresponding edges in G (Split vertices for vertices in G are ignored)
	 * 
	 * @param splitGraphVertices : a set of split graph vertices 
	 * @return A set of edges in the original graph G that correspond to the given SplitGraphVertices
	 */
	public Set<E> getOriginalEdges(Set<SplitVertex<V,E>> splitGraphVertices) {
		Set<E> originalEdges = new HashSet<E>();
		for (SplitVertex<V,E> e:splitGraphVertices) {
			originalEdges.add(getOriginalEdge(e));
		}
		
		return originalEdges;
	}
	
	/**
	 * Sets flow source of the graph (used for defining flow problems).
	 * If a flow source was set previously, this will be ignored. 
	 * 
	 * @param s : The new flow source. 
	 */
	public void setFlowSource(SplitVertex<V,E> s) {
		this.source = s;
	}
	
	/**
	 * Sets flow target of the graph (used for defining flow problems).
	 * If a flow target was set previously, this will be ignored. 
	 * 
	 * @param t : The new flow target. 
	 */
	public void setFlowTarget(SplitVertex<V,E> t) {
		this.target = t;
	}
	
	/**
	 * Returns source for the flow-Graph G_s,t if existent
	 * 
	 * @return the source of the current flow problem on the graph
	 */
	public SplitVertex<V,E> getFlowSource() {
		return this.source;
	}
	
	/**
	 * Returns target for the flow-Graph G_s,t if existent
	 * 
	 * @return the target of the current flow problem on the graph
	 */
	public SplitVertex<V,E> getFlowTarget() {
		return this.target;
	}

	/**
	 * Takes a set of edges of G' and returns the induces edges in the original graph G.
	 * cut edge in G' x_v * ----- * --|-- * x_w => Edge  in G v * ----|---- * w
	 * cut edge in G' x_v * --|-- * ----- * x_w => Edge  in G v * ----|---- * w
	 * 
	 * @param cut 
	 */
	public Set<E> translateCut(Set<DefaultWeightedEdge> cut) {
		Set<E> translatedCut = new HashSet<E>();
		for (DefaultWeightedEdge e : cut) {
			translatedCut.add(getOriginalEdge(e));
		}
		
		return translatedCut;
	}

	/**
	 * Takes an edges in G' and returns the SplitGraphVertex in G' that represents an edge in G and is incident to the given edge
	 * 
	 * @param edge : An edge of the Graph
	 * @return : A SplitGraphVertex that is incident to the given edge and represents an edge in the original Graph G
	 */
	public SplitVertex<V,E> getIncidentSplitVertex(DefaultWeightedEdge edge) {
		
		//Check if the source of the edge is an EdgeContainer for G. If so return the source of the edge as SplitGraphVertex for the given edge
		if (this.getEdgeSource(edge).isEdgeContainer()) {
			return this.getEdgeSource(edge);
		}
		
		//If the source is not an edge container, the target must be. (even with source and target in place)
		return this.getEdgeTarget(edge);
	}

	/**
	 * Recovers weights as they were at creation time
	 */
	public void resetWeights() {
		for (DefaultWeightedEdge e : original_weights.keySet()) {
			this.setEdgeWeight(e, original_weights.get(e));
		}
	}
}
