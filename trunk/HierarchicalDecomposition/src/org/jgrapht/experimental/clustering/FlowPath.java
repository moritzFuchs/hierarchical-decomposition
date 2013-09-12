package org.jgrapht.experimental.clustering;

import java.util.List;

//TODO: Document

public class FlowPath<V,E> {

	private Double weight;
	
	private List<V> path;
	
	
	public FlowPath(List<V> path , Double weight) {
		this.weight = weight;
		this.path = path;
	}	
	
	/**
	 * Get the path itself.
	 * 
	 * @return : An ordered list of {@link V}, representing the path. 
	 */
	public List<V> getPath() {
		return path;
	}
	
	/**
	 * Get the path's weight. 
	 * WARNING: Might not correspond to the flow it was computed from since {@link FlowPath.rescalePath} can change the weight of the path. 
	 * 
	 * @return : Weight of the flow path
	 */
	public Double getFlowPathWeight() {
		return weight;
	}
	
	/**
	 * Rescales the path by the given factor.
	 * 
	 * @param factor : The rescale factor for the path
	 */
	public void rescalePath(Double factor) {
		weight = weight * factor;
	}
	
	public String toString() {
		String ret = path.toString() + " with weight " + weight;

		return ret;
	}
	
}
