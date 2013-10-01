package mf.gui.decomposition.superpixel;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class ShowSuperpixelHandler implements EventHandler<ActionEvent> {

	private SuperpixelDrawable dec;
	
	public ShowSuperpixelHandler(SuperpixelDrawable dec) {
		this.dec = dec;
	}
	
	@Override
	public void handle(ActionEvent event) {
		dec.draw();
	}

}
