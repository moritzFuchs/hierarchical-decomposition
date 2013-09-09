package mf.gui.decomposition;

import mf.gui.Markable;

public abstract class Drawable {

	private String name;
	protected Markable m;
	
	public Drawable(String name , Markable m) {
		this.name = name;
		this.m = m;
	}
	
	public abstract void draw();

	public String toString() {
		return name;
	}
	
}
