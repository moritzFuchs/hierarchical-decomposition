package mf.gui.decomposition;

import mf.gui.ButtonRow;
import mf.gui.Drawable;
import mf.gui.Markable;

/**
 * List item which resets the canvas to show the unedited image below.
 * 
 * @author moritzfuchs
 * @date 09.09.2013
 *
 */
public class NoDecomposition extends Drawable {

	public NoDecomposition(Markable m, ButtonRow buttonRow) {
		super("None" , m, buttonRow);
	}
	
	/**
	 * Clears the canvas.
	 */
	public void draw() {
		m.clear();
	}

	@Override
	public void onActivate() {
		draw();
	}

}
