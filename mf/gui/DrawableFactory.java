package mf.gui;

import java.io.File;

import mf.superpixel.SuperpixelDecomposition;

public interface DrawableFactory {
	public Boolean handles(String type);
	public Drawable getInstance(String name, SuperpixelDecomposition dec, Markable markable, ButtonRow buttonRow, File file);
}