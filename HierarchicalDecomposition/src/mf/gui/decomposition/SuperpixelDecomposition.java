package mf.gui.decomposition;

import java.util.Map;

import mf.gui.Markable;
import mf.gui.Pixel;
import mf.gui.Superpixel;

public class SuperpixelDecomposition extends Drawable{

	Map<Integer , Superpixel> pixel;
	
	public SuperpixelDecomposition(Map<Integer , Superpixel> pixel, String name, Markable m) {
		super(name , m);
		this.pixel = pixel;
	}
	
	/**
	 * Paint the decomposition on a given {@link Markable}
	 * 
	 * @param m : The {@link Markable}
	 */
	public void draw() {
		m.startLoading();
		m.clear();
		for (Superpixel sp : pixel.values()) {
			for (Pixel p : sp.getBoundaryPixels()) {
				m.markPixel(p.getX(), p.getY());
			}
		}
		m.stopLoading();
	}
	
}
