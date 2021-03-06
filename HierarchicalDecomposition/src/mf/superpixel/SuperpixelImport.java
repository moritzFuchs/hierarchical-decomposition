package mf.superpixel;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import mf.gui.Pixel;
import javafx.scene.image.Image;

/**
 * Imports MATLAB-files containing the superpixel information. Each file contains a matrix of size equal to image-height * image-width.
 * Each cell of the matrix contains the superpixel-number that the corresponding pixel of the image belongs to.
 * The import automatically detects the boundaries of the superpixel by comparing the superpixel number of each pixel with its neighbors.
 * 
 * @author moritzfuchs
 * @date 09.09.2013
 *
 */
public class SuperpixelImport {

	/**
	 * Raw data generated by the MATLAB script
	 */
	private Integer[][] superpixel_data;
	
	/**
	 * The resulting superpixel map from superpixel-numbers ({@link Superpixel}) to the {@link Superpixel}
	 */
	private Map<Integer , Superpixel> superpixels;

	private HashMap<Pixel, Superpixel> pixelMap;
	
	/**
	 * Computes the superpixel map
	 * 
	 * @param path : Path to the superpixel-file
	 * @param img : The underlying image
	 */
	public SuperpixelImport(String path , Image img){
		
		Integer width = (int)img.getWidth();
		Integer height = (int)img.getHeight();
		
		superpixels = new HashMap<Integer , Superpixel>();
		pixelMap = new HashMap<Pixel , Superpixel>();

		try {
			
		superpixel_data = new Integer[height][width];
		
		BufferedReader br = new BufferedReader(new FileReader(path));
		String line = br.readLine();

	    Integer current_row = 0;
	    while(line != null) {
	    	String[] values = line.split(" +");
	    	for (int col=0;col<values.length-1;col++) {
	    		Integer superpixel_num = Double.valueOf(values[col+1]).intValue();
	    		superpixel_data[current_row][col] = superpixel_num;
	    	}
	    	line = br.readLine();
	    	current_row++;
	    }

	    br.close();
		
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		for (int row=0;row<height;row++) {
			for (int col=0;col<width;col++) {
				Integer superpixel_num = superpixel_data[row][col];
				Superpixel sp = getSuperpixel(superpixel_num , img);
				
				Pixel p = new Pixel(col,row);
				pixelMap.put(p, sp);
				
				addPixel(sp , row, col , img);
			}
		}
	}
	
	/**
	 * Returns the superpixel map from superpixel numbers ({@link Integer}) to {@link Superpixel}
	 * 
	 * @return {@code Map<Integer , Superpixel>}: The superpixel map.
	 */
	public Map<Integer , Superpixel> getSuperpixels() {
		return superpixels;
	}
	
	/**
	 * Returns a Map from {@link Pixel} to {@link Superpixel}.
	 * 
	 * @return : Map from {@link Pixel}to {@link Superpixel}
	 */
	public Map<Pixel , Superpixel> getPixelMap() {
		return pixelMap;
	}

	/**
	 * Adds a {@link Pixel} to a given {@link Superpixel}. Decides whether it is a boundary Pixel or not.
	 * 
	 * @param sp : The superpixel
	 * @param row : The row of the pixel
	 * @param col : The column of the pixel
	 */
	private void addPixel(Superpixel sp, int row, int col , Image img) {
		Pixel p = new Pixel(col,row);
		Set<Superpixel> neighbors = new HashSet<Superpixel>();
		Boolean on_boundary = false;
		if (col+1 < superpixel_data[row].length && !superpixel_data[row][col+1].equals(superpixel_data[row][col])) {
			on_boundary = true;
			Superpixel neighbor = getSuperpixel(superpixel_data[row][col+1] , img);
			neighbors.add(neighbor);
		}
		if (col-1 >= 0 && !superpixel_data[row][col-1].equals(superpixel_data[row][col])) {
			on_boundary = true;
			Superpixel neighbor = getSuperpixel(superpixel_data[row][col-1] , img);
			neighbors.add(neighbor);
		}
		if (row+1 < superpixel_data.length && !superpixel_data[row+1][col].equals(superpixel_data[row][col])) {
			on_boundary = true;
			Superpixel neighbor = getSuperpixel(superpixel_data[row+1][col] , img);
			neighbors.add(neighbor);
		}
		if (row-1 >= 0 && !superpixel_data[row-1][col].equals(superpixel_data[row][col])) {
			on_boundary = true;
			Superpixel neighbor = getSuperpixel(superpixel_data[row-1][col] , img);
			neighbors.add(neighbor);
		}
		
		if (on_boundary) {
			sp.addBoundaryPixel(p, neighbors);
		} else {
			sp.addPixel(p);
		}
		
	}

	/**
	 * Returns the {@link Superpixel} with the given superpixel number (as given by the MATLAB code)
	 * 
	 * @param num : The superpixel number
	 * @return The {@link Superpixel} corresponding to the given number
	 */
	private Superpixel getSuperpixel(Integer num , Image img) {
		Superpixel sp = superpixels.get(num);
		if (sp == null) {
			sp = new Superpixel(num , img);
			superpixels.put(num, sp);
		}
		return sp;
	}
	
}
