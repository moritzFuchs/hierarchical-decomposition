package mf.gui.segmentation;

import java.io.File;

import mf.gui.ButtonRow;
import mf.gui.Markable;
import mf.gui.decomposition.Drawable;
import mf.gui.decomposition.DrawableFactory;
import mf.gui.segmentation.InteractiveSegmentationDrawable;
import mf.superpixel.SuperpixelDecomposition;

public class InteractiveSegmentationDrawableFactory implements DrawableFactory{

	@Override
	public Boolean handles(String type) {
		return (type.compareTo("rst") == 0);
	}

	@Override
	public Drawable getInstance(String num_str, SuperpixelDecomposition dec,
			Markable markable, ButtonRow buttonRow, File file) {
		InteractiveSegmentationDrawable d = new InteractiveSegmentationDrawable(num_str, markable, buttonRow, file.getPath(), dec);
		return d;
	}

}
