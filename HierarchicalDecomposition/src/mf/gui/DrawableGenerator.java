package mf.gui;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.javafx.collections.MappingChange.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mf.gui.decomposition.Drawable;
import mf.gui.decomposition.DrawableFactory;
import mf.gui.decomposition.rst.RSTDecompositionDrawable;
import mf.gui.decomposition.rst.RSTDecompositionDrawableFactory;
import mf.gui.decomposition.superpixel.SuperpixelDrawable;
import mf.gui.segmentation.InteractiveSegmentationDrawable;
import mf.gui.segmentation.InteractiveSegmentationDrawableFactory;
import mf.superpixel.SuperpixelDecomposition;
import mf.superpixel.SuperpixelImport;

/**
 * Calls {@link DrawableFactory} s to generate as many {@link Drawable} s as possible
 * 
 * @author moritzfuchs
 * @date 04.11.2013
 *
 */
public class DrawableGenerator {

	/**
	 * Directory we are checking out
	 */
	private File dir;
	
	/**
	 * {@link ObservableList} containing all generated {@link Drawable}
	 */
	private ObservableList<Drawable> items;
	
	/**
	 * {@link HashMap} from Integer (number of superpixels) to {@link SuperpixelDecomposition}
	 */
	private HashMap<Integer, SuperpixelDecomposition> superpixel;
	
	/**
	 * {@link Markable} the {@link Drawable}s can use
	 */
	private Markable m;
	
	/**
	 * {@link ButtonRow} that {@link Drawable}s can use to add Buttons to the UI 
	 */
	private ButtonRow buttonRow;
	
	/**
	 * List of {@link DrawableFactory}
	 */
	private List<DrawableFactory> factories;
	
	public DrawableGenerator(File dir, ObservableList<Drawable> items, ButtonRow buttonRow, Markable m) {
		this.dir = dir;
		this.items = items;
		this.superpixel = new HashMap<Integer , SuperpixelDecomposition>();
		this.m = m;
		this.buttonRow = buttonRow;
		this.factories = new LinkedList<DrawableFactory>();
		
		this.factories.add(new RSTDecompositionDrawableFactory());
		this.factories.add(new InteractiveSegmentationDrawableFactory());
	}
	
	public void generate() {
		
		//First generate all superpixel decompositions
		for (File file : dir.listFiles()) {
	    	String name = file.getName();	

	    	if (name.toLowerCase().startsWith("superpixel") && name.toLowerCase().endsWith(".mat")) {
	    		String num_str = name.substring(10, name.length()-4);
	    		
	    		SuperpixelImport imp = new SuperpixelImport(file.getPath() , m.getImage());
	    		SuperpixelDecomposition dec = new SuperpixelDecomposition(imp.getSuperpixels(),imp.getPixelMap());
	    		SuperpixelDrawable super_drawable = new SuperpixelDrawable(dec, "Superpixel " + num_str, m, buttonRow);
	    		
	    		items.add(super_drawable);
	    		superpixel.put(Integer.valueOf(num_str), dec);
	    	}
	    }
		
		Pattern p = Pattern.compile("\\d+");
		
		for (File file : dir.listFiles()) {
			String name = file.getName();	
			
			String[] split = name.split("\\.");
			String ending = split[split.length-1];
			
			Matcher matcher = p.matcher(name);
			
			if (matcher.find()) {
				String num_str = matcher.group();
				Integer num = Integer.valueOf(num_str);
				
				for (DrawableFactory f : factories) {
					if (f.handles(ending)) {
						items.add(f.getInstance(num_str, superpixel.get(num), m, buttonRow, file));
					}
				}
			}
		}
		FXCollections.sort(items);
	}
}
