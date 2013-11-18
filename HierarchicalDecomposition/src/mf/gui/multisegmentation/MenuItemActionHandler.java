package mf.gui.multisegmentation;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.paint.Color;

public class MenuItemActionHandler implements EventHandler<ActionEvent> {

	private MultiSegmentationDrawable d;
	private Color c;
	
	public MenuItemActionHandler(MultiSegmentationDrawable d , Color c) {
		this.d = d;
		this.c = c;
	}
	
	@Override
	public void handle(ActionEvent event) {
		d.setMarkerColor(c);
	}

}
