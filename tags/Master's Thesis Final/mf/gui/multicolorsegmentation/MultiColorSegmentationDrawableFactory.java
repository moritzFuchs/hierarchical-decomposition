package mf.gui.multicolorsegmentation;

import java.io.File;

import mf.gui.ButtonRow;
import mf.gui.Drawable;
import mf.gui.DrawableFactory;
import mf.gui.Markable;
import mf.gui.segmentation.InteractiveSegmentationDrawable;
import mf.superpixel.SuperpixelDecomposition;

public class MultiColorSegmentationDrawableFactory implements DrawableFactory{

	@Override
	public Boolean handles(String type) {
		return (type.compareTo("rst") == 0);
	}

	@Override
	public Drawable getInstance(String num_str, SuperpixelDecomposition dec,
			Markable markable, ButtonRow buttonRow, File file) {
		MultiColorSegmentationDrawable d = new MultiColorSegmentationDrawable(num_str, markable, buttonRow, file.getPath(), dec);
		return d;
	}

}
