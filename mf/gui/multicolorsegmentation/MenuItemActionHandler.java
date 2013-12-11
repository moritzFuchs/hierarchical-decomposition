package mf.gui.multicolorsegmentation;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.paint.Color;

public class MenuItemActionHandler implements EventHandler<ActionEvent> {

	private MultiColorSegmentationDrawable d;
	private Color c;
	
	public MenuItemActionHandler(MultiColorSegmentationDrawable d , Color c) {
		this.d = d;
		this.c = c;
	}
	
	@Override
	public void handle(ActionEvent event) {
		d.setMarkerColor(c);
	}

}
