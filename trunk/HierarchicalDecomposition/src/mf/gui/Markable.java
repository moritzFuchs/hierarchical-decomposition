package mf.gui;

import java.io.File;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;

public interface Markable {

	public void markPixel(Integer x , Integer y);
	public void markPixel(Integer x , Integer y, Color c);
	public void markArea(Integer[] x , Integer[] y);
	public void startLoading();
	public void stopLoading();
	public void clear();
	public void export(File file);
	public void registerMouseHandler(EventHandler<? super MouseEvent> drawable);
	public void registerScrollHandler(EventHandler<? super ScrollEvent> drawable);
}
