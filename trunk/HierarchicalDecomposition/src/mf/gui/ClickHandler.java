package mf.gui;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class ClickHandler<T extends MouseEvent> implements EventHandler<T> {

	@Override
	public void handle(T event) {
		System.out.println(event);
		
		ImageView img_view = (ImageView) event.getSource();
		CustomImage img = (CustomImage)img_view.getImage();
		
		
	}

}
