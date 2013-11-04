package mf.gui.segmentation;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class ResetButtonHandler implements EventHandler<ActionEvent>{

	private InteractiveSegmentationDrawable seg;
	
	public ResetButtonHandler(InteractiveSegmentationDrawable seg) {
		this.seg = seg;
	}
	
	@Override
	public void handle(ActionEvent event) {
		seg.reset();
	}

}
