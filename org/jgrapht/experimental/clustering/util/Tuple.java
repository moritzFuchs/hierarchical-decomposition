package org.jgrapht.experimental.clustering.util;

/**
 * Implements a 3-Tuple <x,y,z>
 * 
 * @author moritzfuchs
 *
 * @param <X>
 * @param <Y>
 * @param <Z>
 */
public class Tuple<X,Y,Z> {
	public X x;
	public Y y;
	public Z z;
	
	public Tuple(X x, Y y, Z z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
}
