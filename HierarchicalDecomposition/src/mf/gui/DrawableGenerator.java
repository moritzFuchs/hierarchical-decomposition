package mf.gui;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import mf.gui.decomposition.Drawable;
import mf.gui.decomposition.rst.RSTDecomposition;
import mf.gui.decomposition.superpixel.SuperpixelDrawable;
import mf.gui.segmentation.InteractiveSegmentationDrawable;
import mf.superpixel.SuperpixelDecomposition;
import mf.superpixel.SuperpixelImport;

public class DrawableGenerator {

	public static Set<Drawable> generate(String name, File file,ButtonRow buttonRow, DrawableImageView drawable) {
		
		Set<Drawable> d = new HashSet<Drawable>();
		
		//Superpixel decomposition
    	if (name.toLowerCase().startsWith("superpixel") && name.toLowerCase().endsWith(".mat")) {
    		String num_str = name.substring(10, name.length()-4);
    		
    		SuperpixelImport imp = new SuperpixelImport(file.getPath() , drawable.getImage());
    		SuperpixelDecomposition dec = new SuperpixelDecomposition(imp.getSuperpixels(),imp.getPixelMap());
    		SuperpixelDrawable super_drawable = new SuperpixelDrawable(dec, "Superpixel " + num_str, drawable, buttonRow);
    		
    		
    		d.add(super_drawable);
    	}
    	
    	//RST Decomposition
    	if (name.toLowerCase().startsWith("tree") && name.toLowerCase().endsWith(".rst")) {
    		
    		String num_str = name.substring(4, name.length()-4);
    		
    		SuperpixelImport imp = new SuperpixelImport(file.getParent() + "/superpixel"+num_str+".mat" , drawable.getImage());
    		SuperpixelDecomposition dec = new SuperpixelDecomposition(imp.getSuperpixels(),imp.getPixelMap());
    		
    		RSTDecomposition krv_dec = new RSTDecomposition(num_str, file.getPath(), dec, drawable,buttonRow);
    		
    		d.add(krv_dec);
    		
    		
    		InteractiveSegmentationDrawable seg = new InteractiveSegmentationDrawable("Interactive Segmentation " + num_str,drawable,buttonRow,krv_dec.getDecompositionTree(),dec);
    		d.add(seg);
    	}	
		
    	return d;
	}
	
}
