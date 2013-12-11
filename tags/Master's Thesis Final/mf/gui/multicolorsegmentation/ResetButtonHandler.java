package mf.gui.multicolorsegmentation;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class ResetButtonHandler implements EventHandler<ActionEvent>{

	private MultiColorSegmentationDrawable seg;
	
	public ResetButtonHandler(MultiColorSegmentationDrawable seg) {
		this.seg = seg;
	}
	
	@Override
	public void handle(ActionEvent event) {
		seg.reset();
	}

}
