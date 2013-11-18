package mf.gui.multisegmentation;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;

public class HighlightButtonHandler implements EventHandler<ActionEvent> {

	private MultiSegmentationDrawable seg;
	private Button b;
	
	public HighlightButtonHandler(MultiSegmentationDrawable seg, Button b) {
		this.seg = seg;
		this.b = b; 
	}
	
	@Override
	public void handle(ActionEvent arg0) {
		seg.setHighlight(!seg.isHighlighted());
		seg.updateButtons();
	}
}
