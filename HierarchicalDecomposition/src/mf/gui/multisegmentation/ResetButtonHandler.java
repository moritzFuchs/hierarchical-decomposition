package mf.gui.multisegmentation;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class ResetButtonHandler implements EventHandler<ActionEvent>{

	private MultiSegmentationDrawable seg;
	
	public ResetButtonHandler(MultiSegmentationDrawable seg) {
		this.seg = seg;
	}
	
	@Override
	public void handle(ActionEvent event) {
		seg.reset();
	}

}
