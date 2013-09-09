package mf.gui.decomposition;

import mf.gui.Markable;

public class NoDecomposition extends Drawable {

	public NoDecomposition(Markable m) {
		super("None" , m);
	}
	
	@Override
	public void draw() {
		m.clear();
	}

}
