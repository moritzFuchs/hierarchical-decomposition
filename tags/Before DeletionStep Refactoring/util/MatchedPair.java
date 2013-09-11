package org.jgrapht.experimental.clustering.util;

/**
 * Container Class for a pair of matched vertices with specified weight
 * 
 * @author moritzfuchs
 *
 * @param <V>
 */
public class MatchedPair<V> {

	private V v;
	private V w;
	private Double weight;
	
	public MatchedPair (V v , V w , Double weight) {
		this.v = v;
		this.w = w;
		this.weight = weight;
	}
	
	public V getV() {
		return this.v;
	}
	
	public V getW() {
		return this.w;
	}
	
	public Double getWeight() {
		return weight;
	}
	
	public String toString() {
		return v.toString() + " -- " + w.toString() + " (" + weight + ")";
	}
	
}
