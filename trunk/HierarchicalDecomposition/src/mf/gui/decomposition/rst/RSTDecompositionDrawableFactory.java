package mf.gui.decomposition.rst;

import java.io.File;

import mf.gui.ButtonRow;
import mf.gui.Drawable;
import mf.gui.DrawableFactory;
import mf.gui.Markable;
import mf.superpixel.SuperpixelDecomposition;

public class RSTDecompositionDrawableFactory implements DrawableFactory{

	@Override
	public Boolean handles(String type) {
		return (type.compareTo("rst") == 0);
	}

	@Override
	public Drawable getInstance(String num_str, SuperpixelDecomposition dec,
			Markable markable, ButtonRow buttonRow, File file) {
		RSTDecompositionDrawable d = new RSTDecompositionDrawable(num_str, file.getPath(), dec, markable,buttonRow);
		return d;
	}

}
