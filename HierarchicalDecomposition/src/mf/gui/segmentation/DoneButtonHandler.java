package mf.gui.segmentation;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class DoneButtonHandler implements EventHandler<ActionEvent>{

	private InteractiveSegmentationDrawable seg;
	
	public DoneButtonHandler(InteractiveSegmentationDrawable seg) {
		this.seg = seg;
	}
	
	@Override
	public void handle(ActionEvent arg0) {
		seg.draw();
		
	}

}
