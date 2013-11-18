package mf.gui.multisegmentation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jgrapht.experimental.clustering.TreeVertex;
import org.jgrapht.graph.DefaultWeightedEdge;

import javafx.scene.paint.Color;

public class TreeVertexColor {

	private Map<Color, Double> costs;
	private Color min;
	private TreeVertex<Integer> t;
	private Map<Color, Set<DefaultWeightedEdge>> cutEdges;
	
	public TreeVertexColor(TreeVertex<Integer> t) {
		this.t = t;
		costs = new HashMap<Color,Double>();
		min = null;
		cutEdges = new HashMap<Color , Set<DefaultWeightedEdge>>();
	}
	
	public TreeVertex<Integer> getTreeVertex() {
		return t;
	}
	
	/**
	 * Returns the color with min. cost so far
	 * 
	 * @return
	 */
	public Color getMinColor() {
		return min;
	}
	
	/**
	 * Sets the min color manually. Useful for more convenient setup. 
	 * 
	 * @param c : The min-color
	 */
	public void setMinColor(Color c) {
		this.min = c;
	}
	
	/**
	 * Set costs for coloring the {@link TreeVertex} t with the given color c.
	 * 
	 * @param c : The color.
	 * @param d : The cost for the given color.
	 */
	public void setCost(Color c , Double d) {
		costs.put(c, d);
		if (min == null || costs.get(min) > d) {
			min = c;
		}
	}
	
	/**
	 * Get the cost for a given color (or null if no cost was set for the color)
	 * 
	 * @param c
	 * @return Double : The cost of the given color
	 */
	public Double getCost(Color c) {
		return costs.get(c);
	}
	
	/**
	 * Adds an edge that has to be cut when color c is chosen
	 * 
	 * @param c
	 * @param e
	 */
	public void addCutEdge(Color c, DefaultWeightedEdge e) {
		System.out.println("adding cut edge");
		
		
		Set<DefaultWeightedEdge> edges = cutEdges.get(c);
		if (edges == null) {
			edges = new HashSet<DefaultWeightedEdge>();
			cutEdges.put(c, edges);
		}
		edges.add(e);
	}
	
	/**
	 * Returns the cut edges if color c is chosen. (or null if none have to be cut) 
	 * 
	 * @param c
	 * @return : Set of edges that have to be cut when the color c is chosen.
	 */
	public Set<DefaultWeightedEdge> getCutEdges(Color c) {
		return cutEdges.get(c); 
	}
	
	/**
	 * Returns table of costs
	 * 
	 * @return : Map<Color,Double> - colors and their costs
	 */
	public Map<Color,Double> getCostMap() {
		return costs;
	}
}
