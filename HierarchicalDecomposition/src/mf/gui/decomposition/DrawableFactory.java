package mf.gui.decomposition;

import java.io.File;

import mf.gui.ButtonRow;
import mf.gui.Markable;
import mf.superpixel.SuperpixelDecomposition;

public interface DrawableFactory {
	public Boolean handles(String type);
	public Drawable getInstance(String name, SuperpixelDecomposition dec, Markable markable, ButtonRow buttonRow, File file);
}