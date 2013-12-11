package mf.gui.decomposition.superpixel;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class MeanImageHandler implements EventHandler<ActionEvent> {

	SuperpixelDrawable drawable;
	
	public MeanImageHandler(SuperpixelDrawable drawable) {
		this.drawable = drawable;
	}
	
	@Override
	public void handle(ActionEvent event) {
		drawable.toggleMeanImage();

	}

}
