package mf.gui.decomposition.superpixel;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class HideSuperpixelHandler implements EventHandler<ActionEvent> {

	private SuperpixelDrawable dec;
	
	public HideSuperpixelHandler(SuperpixelDrawable dec) {
		this.dec = dec;
	}
	
	@Override
	public void handle(ActionEvent event) {
		dec.reset();
	}

}
