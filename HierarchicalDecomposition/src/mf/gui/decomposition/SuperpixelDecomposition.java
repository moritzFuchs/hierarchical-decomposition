package mf.gui.decomposition;

import java.util.Map;

import mf.gui.Markable;
import mf.gui.Pixel;
import mf.superpixel.Superpixel;

public class SuperpixelDecomposition extends Drawable{

	/**
	 * The superpixel map from superpixel numbers ({@link Integer}) to {@link Superpixel}
	 */
	private Map<Integer , Superpixel> superpixel;
	
	public SuperpixelDecomposition(Map<Integer , Superpixel> pixel, String name, Markable m) {
		super(name , m);
		this.superpixel = pixel;
	}
	
	/**
	 * Paint the decomposition on a given {@link Markable}
	 * 
	 * @param m : The {@link Markable}
	 */
	public void draw() {
		m.startLoading();
		m.clear();
		for (Superpixel sp : superpixel.values()) {
			for (Pixel p : sp.getBoundaryPixels()) {
				m.markPixel(p.getX(), p.getY());
			}
		}
		m.stopLoading();
	}
	
}
