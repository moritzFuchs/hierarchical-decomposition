package mf.gui.decomposition.superpixel;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class ToggleSuperpixelHandler implements EventHandler<ActionEvent> {

	private SuperpixelDrawable drawable;
	
	public ToggleSuperpixelHandler(SuperpixelDrawable dec) {
		this.drawable = dec;
	}
	
	@Override
	public void handle(ActionEvent event) {
		drawable.toggleSuperpixelDecomposition();
	}

}
