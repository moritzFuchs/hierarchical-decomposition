package mf.gui.decomposition;

import mf.gui.Markable;

/**
 * List item which resets the canvas to show the unedited image below.
 * 
 * @author moritzfuchs
 * @date 09.09.2013
 *
 */
public class NoDecomposition extends Drawable {

	public NoDecomposition(Markable m) {
		super("None" , m);
	}
	
	/**
	 * Clears the canvas.
	 */
	@Override
	public void draw() {
		m.clear();
	}

	@Override
	public void onActivate() {
		draw();
	}

}
