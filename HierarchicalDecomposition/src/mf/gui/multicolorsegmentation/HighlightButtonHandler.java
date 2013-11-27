package mf.gui.multicolorsegmentation;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;

public class HighlightButtonHandler implements EventHandler<ActionEvent> {

	private MultiColorSegmentationDrawable seg;
	
	public HighlightButtonHandler(MultiColorSegmentationDrawable seg) {
		this.seg = seg;
	}
	
	@Override
	public void handle(ActionEvent arg0) {
		seg.setHighlight(!seg.isHighlighted());
		seg.updateButtons();
	}
}
