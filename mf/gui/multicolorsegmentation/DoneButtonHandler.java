package mf.gui.multicolorsegmentation;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class DoneButtonHandler implements EventHandler<ActionEvent>{

	private MultiColorSegmentationDrawable seg;
	
	public DoneButtonHandler(MultiColorSegmentationDrawable seg) {
		this.seg = seg;
	}
	
	@Override
	public void handle(ActionEvent arg0) {
		seg.draw();
	}

}
