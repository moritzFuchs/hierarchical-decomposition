package mf.gui.decomposition;

import mf.gui.Markable;

/**
 * Interface for drawable 'stuff' that is put into the list. 
 * 
 * @author moritzfuchs
 * @date 09.09.2013
 *
 */
public abstract class Drawable {

	/**
	 * Label that will be displayed in the list.
	 */
	private String name;
	
	/**
	 * The {@link Markable} we want to print on.
	 */
	protected Markable m;
	
	public Drawable(String name , Markable m) {
		this.name = name;
		this.m = m;
	}
	
	/**
	 * Draws something on the {@link Markable} {@link Drawable.m}
	 */
	public abstract void draw();

	/**
	 * Returns the label of this item
	 */
	public String toString() {
		return name;
	}
	
}
