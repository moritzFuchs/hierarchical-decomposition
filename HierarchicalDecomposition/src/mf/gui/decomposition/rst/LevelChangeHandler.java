package mf.gui.decomposition.rst;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class LevelChangeHandler implements EventHandler<ActionEvent> {

	private Integer change;
	private RSTDecomposition dec;
	
	public LevelChangeHandler(Integer change , RSTDecomposition dec) {
		this.change = change;
		this.dec = dec;
	}
	
	@Override
	public void handle(ActionEvent event) {
		if (change < 0) {
			dec.composeNLevel(-change);
		} else {
			dec.decomposeNLevel(change);
		}
	}

}
