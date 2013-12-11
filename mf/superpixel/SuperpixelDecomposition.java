package mf.superpixel;

import java.util.Map;

import mf.gui.Pixel;

/**
 * Represents a Superpixel decomposition
 * 
 * @author moritzfuchs
 * @date 10.09.2013
 *
 */
public class SuperpixelDecomposition {

	/**
	 * Map from superpixel ID (Integer) to {@link Superpixel}
	 */
	private Map<Integer , Superpixel> superpixel;
	
	/**
	 * Map from {@link Pixel} to {@link Superpixel}
	 */
	private Map<Pixel , Superpixel> pixel_map;
	
	/**
	 * Creates the superpixel decomposition based on a map form superpixel ID (Integer) to {@link Superpixel} and a map from {@Pixel} to {@link Superpixel}
	 * CAUTION: The integrity of the maps is NOT checked!
	 * 
	 * @param superpixel : A map from superpixel ID (Integer) to {@link Superpixel}.
	 * @param pixelMap : A map from {@link Pixel} to {@link Superpixel}
	 */
	public SuperpixelDecomposition(Map<Integer, Superpixel> superpixel , Map<Pixel , Superpixel> pixelMap) {
		this.superpixel = superpixel;
		this.pixel_map = pixelMap;
	}
	
	/**
	 * Creates the superpixel decomposition based on the import-results of the {@link SuperpixelImport}
	 * 
	 * @param importer : A {@link SuperpixelImport}-Object (imports MATLAB results)
	 */
	public SuperpixelDecomposition(SuperpixelImport importer) {
		this.superpixel = importer.getSuperpixels();
		this.pixel_map = importer.getPixelMap();
	}
	
	/**
	 * Returns a {@link Superpixel} with the given ID or null if such a {@link Superpixel} does not exist.
	 * 
	 * @param id : The ID of the requested Superpixel
	 * @return : A {@link Superpixel} with the given ID or null if such a {@link Superpixel} does not exist.
	 */
	public Superpixel getSuperpixelById(Integer id) {
		return superpixel.get(id);
	}
	
	/**
	 * Returns the {@link Superpixel} in which the given {@link Pixel} is contained in or null if such a {@link Superpixel} does not exist.
	 * 
	 * @param p : The given {@link Pixel}.
	 * @return The {@link Superpixel} in which the given {@link Pixel} is contained in or null if such a {@link Superpixel} does not exist.
	 */
	public Superpixel getSuperpixelByPixel(Pixel p) {
		return pixel_map.get(p);
	}

	/**
	 * Returns the map from superpixel IDs (Integer) to {@link Superpixel}.
	 * 
	 * @return : The map from superpixel ID to {@link Superpixel}
	 */
	public Map<Integer, Superpixel> getSuperpixelMap() {
		return superpixel;
	}
	
	/**
	 * Returns the map from {@link Pixel} to {@link Superpixel}
	 * 
	 * @return
	 */
	public Map<Pixel, Superpixel> getPixelMap() {
		return pixel_map;
	}
}
