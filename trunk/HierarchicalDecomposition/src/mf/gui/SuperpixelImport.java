package mf.gui;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class SuperpixelImport {

	private Integer[][] superpixel_data;
	private Map<Integer , Superpixel> superpixels;
	
	public SuperpixelImport(String path , Image img){
		
		Integer width = (int)img.getWidth();
		Integer height = (int)img.getHeight();
		
		superpixels = new HashMap<Integer , Superpixel>();

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
		}
		
		
		for (int row=0;row<height;row++) {
			for (int col=0;col<width;col++) {
				Integer superpixel_num = superpixel_data[row][col];
				Superpixel sp = getSuperpixel(superpixel_num , img);
				
				addPixel(sp , row, col , img);
			}
		}
	}
	
	public Map<Integer , Superpixel> getSuperpixels() {
		return superpixels;
	}
	
	public void getSuperpixelGraph() {
		for (Superpixel p : superpixels.values()) {
			
		}
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
		try {
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
		}catch(Exception e){
			Integer a = 1;
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
