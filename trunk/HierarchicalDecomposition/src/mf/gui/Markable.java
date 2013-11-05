package mf.gui;

import java.io.File;
import java.io.IOException;

import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;

public interface Markable {
	//Pixel-level methods
	public void markPixel(Integer x , Integer y);
	public void markPixel(Integer x , Integer y, Color c);
	public void markPixelComplementary(Integer x , Integer y);
	public Color getColor(Integer x , Integer y);
	
	//Loading methods
	public Boolean isLoading();
	public void startLoading();
	public void stopLoading();
	
	//Image methods
	public Image getImage();
	public int getImageWidth();
	public int getImageHeight();
	public void setImage(Image img);
	public void resetImage();
	
	//Reset methods
	public void clear();
	public void clearPixel(Integer x, Integer y); //could also be put to pixel methods..
	
	//Handler methods
	public void registerMouseHandler(EventHandler<? super MouseEvent> drawable);
	public void registerScrollHandler(EventHandler<? super ScrollEvent> drawable);
	public void registerKeyHandler(EventHandler<? super KeyEvent> drawable);
	
	//Export methods
	public void export(File file) throws IOException;
	
	//Other
	public void markArea(Integer[] x , Integer[] y);
	public void drawText(String string);
}
