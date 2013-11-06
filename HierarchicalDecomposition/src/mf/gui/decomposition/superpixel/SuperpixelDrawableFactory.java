package mf.gui.decomposition.superpixel;

import java.io.File;

import mf.gui.ButtonRow;
import mf.gui.Drawable;
import mf.gui.DrawableFactory;
import mf.gui.Markable;
import mf.superpixel.SuperpixelDecomposition;

public class SuperpixelDrawableFactory implements DrawableFactory{

	@Override
	public Drawable getInstance(String name, SuperpixelDecomposition dec,
			Markable markable, ButtonRow buttonRow, File file) {
		
		String num_str = file.getName().substring(10, file.getName().length()-4);		
		return new SuperpixelDrawable(dec, "Superpixel " + num_str, markable, buttonRow);

	}

	@Override
	public Boolean handles(String type) {
		return (type.compareTo("mat") == 0);
	}
}
