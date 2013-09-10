package mf.gui;

import mf.gui.decomposition.Drawable;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

public class MarkableEventHandler implements EventHandler<MouseEvent>{

	private Markable markable;
	private Drawable marker;
	
	@Override
	public void handle(MouseEvent event) {
//		marker.handleMouseEvent(event , markable);
	}
	
}
