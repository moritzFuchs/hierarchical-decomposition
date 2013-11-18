package mf.gui.multisegmentation;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class DoneButtonHandler implements EventHandler<ActionEvent>{

	private MultiSegmentationDrawable seg;
	
	public DoneButtonHandler(MultiSegmentationDrawable seg) {
		this.seg = seg;
	}
	
	@Override
	public void handle(ActionEvent arg0) {
		seg.draw();
	}

}
