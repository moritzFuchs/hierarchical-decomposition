package mf.superpixel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import mf.gui.Pixel;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

/**
 * Represents a superpixel of an image
 * 
 * @author moritzfuchs
 * @date 09.09.2013
 *
 */
public class Superpixel {

	/******************** START CONSTANTS **********************/
	
	/**
	 * Constant for the edge weight computation: ||I_u - I_v|| might be 0, therefore we add EPSILON to it.
	 */
	private final static Double EPSILON = 0.000001;
	
	/**
	 * Factor by which the boundary length is factored in.
	 */
	private final static Double LAMBDA = 0.3;
	
	/******************* END CONSTANTS *************************/
	
	
	/**
	 * ID of the {@link Superpixel} as given by the MATLAB script
	 */
	private Integer id;
	
	/**
	 * The underlying image
	 */
	private Image img;
	
	/**
	 * The set of {@link Pixel} of the superpixel
	 */
	private Set<Pixel> pixel;	
	
	/**
	 * The set of {@link Pixel} that is on the boundary of the superpixel
	 */
	private Set<Pixel> boundary;
	
	/**
	 * Map from neighboring {@link Superpixel} to the set of boundary {@link Pixel}
	 */
	private Map<Superpixel , Set<Pixel>> boundary_to;
	
	public Superpixel(Integer id , Image img) {
		this.id = id;
		this.img = img;
		
		pixel = new HashSet<Pixel>();
		boundary = new HashSet<Pixel>();
		boundary_to = new HashMap<Superpixel , Set<Pixel>>();
	}
	
	/**
	 * Adds a simple {@link Pixel} to the {@link Superpixel}.
	 * 
	 * @param p : The new {@link Pixel}
	 */
	public void addPixel(Pixel p) {
		pixel.add(p);
	}
	
	/**
	 * Adds a {@link Pixel} to the {@link Superpixel} and remembers all neighboring {@link Superpixel}s.
	 *  
	 * @param pixel : The new {@link Pixel}
	 * @param neighbors : The set of {@link Superpixel} that this {@link Pixel} is adjacent to.
	 */
	public void addBoundaryPixel(Pixel pixel , Set<Superpixel> neighbors) {
		neighbors.addAll(neighbors);
		
		for (Superpixel neighbor : neighbors) {
			addNeighbor(pixel , neighbor);
		}
		
		boundary.add(pixel);
		this.pixel.add(pixel);
	}

	/**
	 * Adds a {@link Pixel} to the {@link Superpixel} and marks it as neighbor to the given {@link Superpixel}
	 * 
	 * @param pixel : The new {@link Pixel}
	 * @param neighbor : The {@link Superpixel} that the given {@link Pixel} is adjacent to
	 */
	private void addNeighbor(Pixel pixel, Superpixel neighbor) {
		Set<Pixel> on_boundary_pixels = boundary_to.get(neighbor); 
		if (on_boundary_pixels == null) {
			on_boundary_pixels = new HashSet<Pixel>();
			boundary_to.put(neighbor, on_boundary_pixels);
		}
		on_boundary_pixels.add(pixel);
	}

	/**
	 * The set of {@link Pixel} of this superpixel that is adjacent to other superpixels.
	 * 
	 * @return Set<Pixel> : The set of {@link Pixel} that are adjacent to another {@link Superpixel}
	 */
	public Set<Pixel> getBoundaryPixels() {
		Set<Pixel> pixels = new HashSet<Pixel>();
		for (Set<Pixel> p : boundary_to.values()) {
			pixels.addAll(p);
		}
		
		return pixels;
	}
	
	/**
	 * Get the set of {@link Pixel} that are adjacent to the given {@link Superpixel}.
	 * 
	 * @param p : The {@link Superpixel} neighbor
	 * @return Set<Pixel> : The set of {@link Pixel} of this {@link Superpixel} that are adjacent to the given {@link Superpixel} or null if they are not adjacent.
	 */
	public Set<Pixel> getBoundaryPixels(Superpixel p) {
		Set<Pixel> boundary = boundary_to.get(p);
		return boundary;
	}
	
	/**
	 * Returns the set of {@link Pixel} in this superpixel.
	 * 
	 * @return Set<Pixel> : The set of {@link Pixel} in this superpixel
	 */
	public Set<Pixel> getPixel() {
		return pixel;
	}
	
	/**
	 * Returns the (unique) ID of the {@link Superpixel}
	 * 
	 * @return
	 */
	public Integer getId() {
		return id;
	}
	
	/**
	 * Returns the mean RGB-values for this {@link Superpixel}.
	 * 
	 * @return Double[] : The mean RGB-values for this {@link Superpixel} 
	 */
	public Double[] getMeanRGB() {
		
		Double[] rgb = new Double[3];
		Arrays.fill(rgb, 0.0);
		
		PixelReader reader = img.getPixelReader();
		for (Pixel p : pixel) {
			Color c = reader.getColor(p.getX(), p.getY());
			rgb[0] += c.getRed();
			rgb[1] += c.getGreen();
			rgb[2] += c.getBlue();
		}
		rgb[0] = rgb[0] / pixel.size();
		rgb[1] = rgb[1] / pixel.size();
		rgb[2] = rgb[2] / pixel.size();
		
		return rgb;
	}
	
	/**
	 * Get weight of edge between this {@link Superpixel} and a given {@link Superpixel} 
	 * 
	 * @param neighbor : Another {@link Superpixel}
	 * @return Double : Weight of edge between the superpixels
	 */
	public Double getEdgeWeight(Superpixel neighbor) {
		if (!this.boundary_to.keySet().contains(neighbor)) {
			return 0.0;
		}
		
		Double weight = 0.0;
		
		Set<Pixel> boundary = this.getBoundaryPixels(neighbor);
		Double[] rgb_source = this.getMeanRGB();
		Double[] rgb_target = neighbor.getMeanRGB();
		
		Double rgb_distance = RGBDistance(rgb_source, rgb_target);
		
		weight += boundary.size() / (rgb_distance + EPSILON);
		weight += LAMBDA * boundary.size();
		
		//Make the weights integral
		weight = Math.ceil(weight);
		
		return weight;
	}
	
	
	/**
	 * Computes the l2-distance between two RGB-vectors.
	 * 
	 * @param rgb1 : First RGB-vector
	 * @param rgb2 : Second RGB-vector
	 * @return Double : The l2-distance of the RGB-vectors
	 */
	private Double RGBDistance(Double[] rgb1 , Double[] rgb2) {
		
		Double distance = 0.0;
		distance += Math.pow(rgb1[0] - rgb2[0], 2);
		distance += Math.pow(rgb1[1] - rgb2[1], 2);
		distance += Math.pow(rgb1[2] - rgb2[2], 2);
		
		distance = Math.sqrt(distance);
		
		return distance;
	}
	
	/**
	 * Returns the set of {@link Superpixel} neighbors
	 * 
	 * @return Set<Superpixel> : The set of {@link Superpixel} that are adjacent to this {@link Superpixel}. 
	 */
	public Set<Superpixel> getNeighbors() {
		return boundary_to.keySet();
		
	}
	
}
